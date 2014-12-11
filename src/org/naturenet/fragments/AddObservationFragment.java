package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.fragments.ObservationFragment.NoteImage;
import org.naturenet.helper.DataSyncTask;
import org.naturenet.helper.LocationHelper;
import org.naturenet.model.Account;
import org.naturenet.model.Feedback;
import org.naturenet.model.Media;
import org.naturenet.model.Note;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import com.activeandroid.Model;
import com.google.common.collect.Lists;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class AddObservationFragment extends Fragment {
    /* UI elements */
    private View	       rootView;
    private EditText	   etDescription;
    private Button	     submitBtn;
    private Button	     cancelBtn;
    private ImageView	  imgPreview;
    private Spinner	    categorySpinner;
    private Spinner	    locationSpinner;
    private Spinner	    activitySpinner;
    private RelativeLayout    loadingLayout;

    /* keys used in arguments */
    final public static String IMAGE_CAPTURE_PATH = "mPath";
    final public static String LANDMARKNAME       = "landmarkName";
    final public static String ACTIVITYNAME = "context.activity.id";
    final public static String NOTE_ID	   = "note.id";
    final public static String TAG		= AddObservationFragment.class.getName();

    /* note (observation) information */
    private String	     	mCurrentPhotoPath;
    private LocationManager    	locationMngr;
    private Location	   	geoInfo;
    private NoteImage 		noteImage;

    /* observation note attributes */
    private Account	     	mAccount;
    private Site		mSite;
    private List<org.naturenet.model.Context> landmarks;
    private List<org.naturenet.model.Context> activities; 
    private String currGPSLandmarkName;
    private long		context_activity_id;
    private long		context_landmark_id;
    org.naturenet.model.Context landmarkContext;
    private Note		mNote;
    private Media	        mMedia;

    private int	landmarkSpinnerPosition = 0;
    private int	activitySpinnerPosition;
    private onPassNewObservationListener dataPasser;

    public static AddObservationFragment newInstance() {
	AddObservationFragment f = new AddObservationFragment();
	return f;
    }

    /* get the account and site from current session */
    public void initModel() {
	mAccount = Session.getAccount();
	checkNotNull(mAccount);
	mSite = Session.getSite();
	checkNotNull(mSite);
	landmarks = mSite.getLandmarks();
	checkNotNull(landmarks);
	activities = mSite.getActivities();
	// set up the default acitvity selection to be "Free Observation"
	int index = 0;
	for (org.naturenet.model.Context activity : activities) {
	    if (activity.getTitle().equals("Free Observation")) {
		activitySpinnerPosition = index;
		break;
	    }
	    index++;
	}
	checkNotNull(activities);
    }
    
    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (MainActivity) a;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
	rootView = inflater.inflate(R.layout.fragment_edit_obs, container, false);
	imgPreview = (ImageView) rootView.findViewById(R.id.preview_observation);
	// categorySpinner = (Spinner) rootView.findViewById(R.id.spinner_obs_category);
	locationSpinner = (Spinner) rootView.findViewById(R.id.spinner_obs_location);
	activitySpinner = (Spinner) rootView.findViewById(R.id.spinner_obs_activity);
	etDescription = (EditText) rootView.findViewById(R.id.et_edit_description);
	// setup loading overlay
	loadingLayout = (RelativeLayout) getActivity().findViewById(R.id.llayout_initapp_loading);
	loadingLayout.setAlpha(90);
	loadingLayout.setVisibility(View.VISIBLE);
	submitBtn = (Button) rootView.findViewById(R.id.btn_addObs_submit);
	cancelBtn = (Button) rootView.findViewById(R.id.btn_addObs_cancel);
	locationMngr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
	submitBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		dispathSubmit();
	    }
	});
	
	cancelBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		cancelEdit();
	    }
	});

	RelativeLayout rlayout = (RelativeLayout) rootView.findViewById(R.id.rlayout_add_obs);
	rlayout.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
			Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
	    }
	});
	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	setHasOptionsMenu(true);
	initModel();
	// handle data from arguments
	if (getArguments() != null) {
	    if (getArguments().getLong(NOTE_ID) != 0) {
		Long note_id = getArguments().getLong(NOTE_ID);
		mNote = Model.load(Note.class, note_id);
		checkNotNull(mNote);
		// Log.d("debug", "passed noteid is " + note_id);
		mCurrentPhotoPath = mNote.getMediaSingle().getPath();
		if (mNote.getContent().length() > 0)
		    etDescription.setText(mNote.getContent());
	    }

	    if (getArguments().containsKey(IMAGE_CAPTURE_PATH)) {
		mCurrentPhotoPath = getArguments().getString(IMAGE_CAPTURE_PATH);
	    }
	
	    // if tour fragment passes location info.
	    if (getArguments().containsKey(LANDMARKNAME)) {
		// Log.d("debug", "user selected in AddFrag: " + getArguments().getString(LANDMARKNAME));
		String markerId = getArguments().getString(LANDMARKNAME).substring(1);
		landmarkSpinnerPosition = Integer.valueOf(markerId);
		//landmarkSpinnerPosition = getPositionByName(getArguments().getString(LANDMARKNAME),
		//	mSite.getLandmarks());
	    }
	    
	    // if tour fragment passes location info.
	    if (getArguments().containsKey(ACTIVITYNAME)) {
		//Log.d("debug", "user selected acitivty from ActFrag in AddFrag: " 
		//		+ getArguments().getString(ACTIVITYNAME));
		activitySpinnerPosition = getPositionByName(getArguments().getString(ACTIVITYNAME),
			mSite.getActivities());
	    }
		
	    previewCapturedImage();
	} else {
	     mNote = new Note();
	    // Log.d(TAG, "error, no arugments passed to here!");
	}

	// set spinners after mNote is initialized 
	setSpinner();
	return rootView;
    }
    
    /* onResume, get last current location */
    @Override
    public void onResume() {
	super.onResume();
	LocationHelper locationHelper = new LocationHelper(locationMngr, new Handler(),
		this.getActivity());
	if (!locationMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	    locationHelper.showSettingsAlert();
	}  else {
	    geoInfo = locationHelper.getCurrentLocation(50);
	    checkNotNull(landmarks);
	    try {
		checkNotNull(geoInfo);
		currGPSLandmarkName = getAccurateLocationName(geoInfo.getLatitude(),
			    geoInfo.getLongitude());
	    } catch (Exception e) {
		 Toast.makeText(this.getActivity(), "waiting for location", Toast.LENGTH_SHORT).show();
	    }
	    landmarkSpinnerPosition = getPositionByName(currGPSLandmarkName, landmarks);
	}
	if (mNote == null) {
	    locationSpinner.setSelection(landmarkSpinnerPosition);
	}
    }

    @Override
    public void onStop() {
	super.onStop();
	if (mNote != null) {
	    DataSyncTask mSyncTask = null;
	    if (noteImage == null) { 
		mSyncTask = new DataSyncTask(mAccount, mNote, DataSyncTask.SYNC_NOTE);
	    } else {
		mSyncTask = new DataSyncTask(mAccount, mNote, noteImage, 
					dataPasser, DataSyncTask.UPDATE_NOTE);
	    }

	    mSyncTask.execute();
	}
	// dismiss loading overlay
	loadingLayout.setVisibility(View.GONE);
	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
		Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		cancelEdit();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }

    /* Display image from a path to ImageView */
    private void previewCapturedImage() {
	RequestCreator picRequstor = Picasso.with(getActivity()).load(mCurrentPhotoPath)
					//.fit();
					.resize(800, 600).centerCrop();
	picRequstor.placeholder(R.drawable.loading).into(imgPreview,  new Callback() {
            @Override
            public void onSuccess() {
        	imgPreview.setVisibility(View.VISIBLE);
        	loadingLayout.setVisibility(View.GONE);
            }
            
            @Override
            public void onError() {
        	loadingLayout.setVisibility(View.GONE);
        	Log.d("debug", "something wrong in picasso");
            }
	});
    }

    /* set up spinners for category and location */
    private void setSpinner() {
	setLandmarkSpinner();
	setActivitySpinner();
	// setCategorySpinner();
    }

    /* create a new observation */
    private void createObservation() {
	Double longitude = null;
	Double latitude = null;
	String description = etDescription.getText().toString();
	if (geoInfo == null || description == null || mAccount == null) {
	    return;
	}
	// if there is no note passed by other fragment, create a new one
	if (mNote == null) {
	    mNote = new Note();
	}
	Account account = Model.load(Account.class, mAccount.getId());
	mNote.setAccount(account);
	mNote.setKind("FieldNote");
	longitude = geoInfo.getLongitude();
	latitude = geoInfo.getLatitude();
	mNote.setLongitude(longitude);
	mNote.setLatitude(latitude);
	mNote.setContent(description);
	// Log.d("debug", "user's input is: " + description);
	// mNote.commit();
	
	// mNote is null, no matter what user selected from the landmark spinner
	// the real selection is based on current location
	if (mNote == null) {
	    currGPSLandmarkName = getAccurateLocationName(geoInfo.getLatitude(), geoInfo.getLongitude());
	    landmarkSpinnerPosition = getPositionByName(currGPSLandmarkName, landmarks);
	    context_landmark_id = landmarks.get(landmarkSpinnerPosition).getId();
	}

	org.naturenet.model.Context activityContext = Model.load(org.naturenet.model.Context.class,
		context_activity_id);
	mNote.setContext(activityContext);
	mNote.commit(); // mNote must commit to be a note in database, then
			// setLandmarkFeedback can work
	mMedia = new Media();
	mMedia.setNote(mNote);
	mMedia.setLocal(mCurrentPhotoPath);
	mMedia.commit();
	org.naturenet.model.Context landmarkContext = Model.load(org.naturenet.model.Context.class,
		context_landmark_id);
	mNote.setLandmarkFeedback(landmarkContext); // setLandmarkFeedback.requires
						    // a note id!!!!!!
	mNote.commit();
	
	noteImage = new NoteImage(mCurrentPhotoPath, mMedia.getTimeCreated(), mNote.getId());
	noteImage.setNoteState(mNote.getSyncState());
	dataPasser.onPassNewObservation(noteImage);
    }
    
    /* process data after press submit */
    private void dispathSubmit() {
	createObservation();
	Toast.makeText(getActivity(), "Thank you very much for your contribution!", Toast.LENGTH_SHORT).show();
	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
		Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
	FragmentManager fm = getActivity().getSupportFragmentManager();
	if (fm.findFragmentByTag(ObservationFragment.TAG) != null) {
	    fm.popBackStack(ObservationFragment.TAG, 0);
	} else {
	    ObservationFragment newFragment = ObservationFragment.newInstance();
	    ((MainActivity) getActivity()).replaceFragment(newFragment,
			 R.id.main_container, ObservationFragment.TAG);
	}
    }
    
    /* cancel button */
    private void cancelEdit() {
	FragmentManager fm = getActivity().getSupportFragmentManager();
	fm.popBackStack();
    }

    /* set up the spinner of landmarks */
    private void setLandmarkSpinner() {
	ArrayAdapter<String> locationSpinAdpater = new ArrayAdapter<String>(getActivity(),
		android.R.layout.simple_spinner_item, getContextNames(landmarks));
	locationSpinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	locationSpinner.setAdapter(locationSpinAdpater);
	int position = 0;
	// if the landmark is selected from activity or updated by gps, set the location to gps one 
	if (landmarkSpinnerPosition != 0) {
	    position = landmarkSpinnerPosition;
	    // Log.d("debug", "user selected: " + landmarkSpinnerPosition);
	}
	
	// if there is note, it is in "edit" mode, the position should be the landmark data from database
	if (mNote != null) {
	    Feedback landmarkFeedback = mNote.getLandmarkFeedback();
	    if (landmarkFeedback == null) {
		position = 0;
	    } else {
		position = getPositionByName(landmarkFeedback.getContent(),
			mSite.getLandmarks());
	    }
	}
	locationSpinner.setSelection(position);
	locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
		    int position, long id) {
		checkNotNull(landmarks.get(position));
		context_landmark_id = landmarks.get(position).getId();
		landmarkContext = landmarks.get(position);
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
		checkNotNull(landmarks.get(0));
		Log.d("debug", "context id: " + landmarks.get(0).getTitle());
		context_landmark_id = landmarks.get(0).getId();
	    }
	});
    }
    
    /* set up the spinner of activity */
    private void setActivitySpinner() {
	ArrayAdapter<String> activitySpinAdpater = new ArrayAdapter<String>(getActivity(),
		android.R.layout.simple_spinner_item, getContextNames(activities));
	activitySpinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	activitySpinner.setAdapter(activitySpinAdpater);
	if (mNote != null) {
	    int position = getPositionByName(mNote.getContext().getName(),
		    mSite.getActivities());
	    activitySpinner.setSelection(position);
	} else {
	    activitySpinner.setSelection(activitySpinnerPosition);
	    // Log.d("debug", "user selected acitivity: " + activitySpinnerPosition);
	}
	activitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
		    int position, long id) {
		checkNotNull(activities.get(position));
		// Log.d("debug", "context id: " + activities.get(position).getTitle());
		context_activity_id = activities.get(position).getId();
		// Log.d("debug", "context id: " + context_activity_id);
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
		checkNotNull(activities.get(0));
		context_activity_id = activities.get(0).getId();
		Log.d("debug", "context id: " + activities.get(0).getTitle());
		// mNote.setContext(contexts.get(0));
	    }
	});
	// setSpinnerListener(activitySpinner, activities);
    }
    
    /* setup spinner of category */
    private void setCategorySpinner() {
	ArrayAdapter<String> categorySpinAdpater = new ArrayAdapter<String>(getActivity(),
		android.R.layout.simple_spinner_item, getResources().getStringArray(
			R.array.category));
	categorySpinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	categorySpinner.setAdapter(categorySpinAdpater);
    }

    /* give a list of contexts, get the name (titles) of the contexts */
    private String[] getContextNames(List<org.naturenet.model.Context> contexts) {
	List<String> contextNames = new ArrayList<String>();
	for (org.naturenet.model.Context c : contexts) {
	    contextNames.add(c.getTitle());
	}
	String[] simpleArray = new String[contextNames.size()];
	return contextNames.toArray(simpleArray);
    }

    /* get the id? position? of the selected name in the activities */
    public int getPositionByName(String name, List<org.naturenet.model.Context> activities) {
	List<String> context_names = Lists.newArrayList();
	for (org.naturenet.model.Context c : activities) {
	    context_names.add(c.getName());
	}
	return context_names.indexOf(name);
    }

    private String getExifTag(ExifInterface exif, String tag) {
	String attribute = exif.getAttribute(tag);
	return (null != attribute ? attribute : "");
    }

    /* determine gps location by photo not used */
    public void getPhotoInfo(String mCurrentPhotoPath) {
	ExifInterface exif = null;
	try {
	    exif = new ExifInterface(mCurrentPhotoPath);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	StringBuilder builder = new StringBuilder();
	builder.append("Date & Time: " + getExifTag(exif, ExifInterface.TAG_DATETIME) + "\n\n");
	builder.append("Flash: " + getExifTag(exif, ExifInterface.TAG_FLASH) + "\n");
	builder.append("Focal Length: " + getExifTag(exif, ExifInterface.TAG_FOCAL_LENGTH) + "\n\n");
	builder.append("GPS Datestamp: " + getExifTag(exif, ExifInterface.TAG_FLASH) + "\n");
	builder.append("GPS Latitude: " + getExifTag(exif, ExifInterface.TAG_GPS_LATITUDE) + "\n");
	builder.append("GPS Latitude Ref: " + getExifTag(exif, ExifInterface.TAG_GPS_LATITUDE_REF)
		+ "\n");
	builder.append("GPS Longitude: " + getExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE) + "\n");
	builder.append("GPS Longitude Ref: "
		+ getExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE_REF) + "\n");
	builder.append("GPS Processing Method: "
		+ getExifTag(exif, ExifInterface.TAG_GPS_PROCESSING_METHOD) + "\n");
	builder.append("GPS Timestamp: " + getExifTag(exif, ExifInterface.TAG_GPS_TIMESTAMP)
		+ "\n\n");
	builder.append("Image Length: " + getExifTag(exif, ExifInterface.TAG_IMAGE_LENGTH) + "\n");
	builder.append("Image Width: " + getExifTag(exif, ExifInterface.TAG_IMAGE_WIDTH) + "\n\n");
	builder.append("Camera Make: " + getExifTag(exif, ExifInterface.TAG_MAKE) + "\n");
	builder.append("Camera Model: " + getExifTag(exif, ExifInterface.TAG_MODEL) + "\n");
	builder.append("Camera Orientation: " + getExifTag(exif, ExifInterface.TAG_ORIENTATION)
		+ "\n");
	builder.append("Camera White Balance: " + getExifTag(exif, ExifInterface.TAG_WHITE_BALANCE)
		+ "\n");

	Log.d("photo", builder.toString());
    }
    
    /* get location name based on current location */
    private String getAccurateLocationName(Double latitude, Double longitude) {
	Double latError = 0.0001;
	Double lonError = 0.0001;
	String name = null;
	// Log.d("mylocation", "Your current location lat is: "+ latitude);
	int index = 0;
	for (org.naturenet.model.Context loc : landmarks) {
	    // doing this because location "other" has no geo info
	    if (index == (landmarks.size() - 1)) {
		name = loc.getName();
		break;
	    }
	    Double landmarkLat = (Double) loc.getExtras().get("latitude");
	    Double landmarkLon = (Double) loc.getExtras().get("longitude");
	    // Log.d("mylocation", loc.getName() + " 's " + " lat is: " + landmarkLat + " lon is: " + landmarkLon);
	    if (Math.abs(landmarkLat - latitude) < latError 
		    && Math.abs(landmarkLon - longitude) < lonError) {
			name = loc.getName();
			// Log.d("mylocation","You're at: " + loc.getName());
			break;
	    }
	    index++;
	}
    
	return name;
    }
    
    /* receive data from MainActivity, passed from ObservationFragment */
    public void setNoteFromObservation(long id) {
	mNote = Model.load(Note.class, id);
    }
    
    /* interface for new observation listener */
    public interface onPassNewObservationListener {
	public void onPassNewObservation(NoteImage image);
	public void onObservationStateChange(NoteImage image);
    }
}


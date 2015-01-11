package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.fragments.ObservationFragment.NoteImage;
import org.naturenet.helper.DataSyncTask;
import org.naturenet.helper.LocationHelper;
import org.naturenet.helper.LocationHelper.ILocationHelper;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
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

public class AddObservationFragment extends Fragment implements ILocationHelper{
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
   

    private int	landmarkSpinnerDefaultPosition = 0;
    private int	activitySpinnerDefaultPosition;
    private onPassNewObservationListener newObservationPasser;
    private LocationHelper locationHelper;

    public static AddObservationFragment newInstance() {
	AddObservationFragment f = new AddObservationFragment();
	return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);	
    }

    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	newObservationPasser = (MainActivity) a;
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
		mCurrentPhotoPath = mNote.getMediaSingle().getPath();
		if (mNote.getContent().length() > 0) {
		    etDescription.setText(mNote.getContent());
		}
	    }

	    if (getArguments().containsKey(IMAGE_CAPTURE_PATH)) {
		mCurrentPhotoPath = getArguments().getString(IMAGE_CAPTURE_PATH);
	    }
		    
	    // if ActivityFragment passes activity info.
	    if (getArguments().containsKey(ACTIVITYNAME)) {
		activitySpinnerDefaultPosition = getPositionByName(getArguments().getString(ACTIVITYNAME),
			mSite.getActivities());
	    }
	    previewCapturedImage();
	} 

	// set spinners after mNote is initialized 
	setSpinner();
	return rootView;
    }
    
    /* onResume, request last current location */
    @Override
    public void onResume() {
	super.onResume();
	// detect location only if it is a new note
	if (mNote == null) {
	    locationHelper = new LocationHelper(locationMngr, new Handler(), this.getActivity(),
		    this);
	    if (!locationMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		locationHelper.showSettingsAlert();
	    } else {
		locationHelper.requestCurrentLocation();
	    }
	}
    }

    @Override
    public void onStop() {
	super.onStop();
	if (mNote != null) {
//	    DataSyncTask mSyncTask = null;
//	    if (noteImage == null) { 
//		Log.d("debug", "noteImage is null::");
//		mSyncTask = new DataSyncTask(mAccount, mNote, DataSyncTask.SYNC_NOTE);
//	    } else {
//		Log.d("debug", "noteImage is not null::");
//		mSyncTask = new DataSyncTask(mAccount, mNote, noteImage, 
//					newObservationPasser, DataSyncTask.UPDATE_NOTE);
//	    }
	    DataSyncTask mSyncTask = new DataSyncTask(mAccount, mNote, noteImage, 
			newObservationPasser, DataSyncTask.UPDATE_NOTE);
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

    /* get the account and site from current session */
    private void initModel() {
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
        	activitySpinnerDefaultPosition = index;
        	break;
            }
            index++;
        }
        checkNotNull(activities);
    }

    /* Display image from a path to ImageView */
    private void previewCapturedImage() {
	RequestCreator picRequstor = Picasso.with(getActivity()).load(mCurrentPhotoPath)
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
	// if there is no note passed by other fragment, create a new one
	// a new GEO info attached to this note,
	// if mNote != null, mNote is from an existing note, no need for new geo info.
	if (mNote == null) {
	    mNote = new Note();
	    longitude = geoInfo.getLongitude();
	    latitude = geoInfo.getLatitude();
	}
	
	Account account = Model.load(Account.class, mAccount.getId());
	mNote.setAccount(account);
	mNote.setKind("FieldNote");
	String description = etDescription.getText().toString();
	mNote.setLongitude(longitude);
	mNote.setLatitude(latitude);
	mNote.setContent(description);
	org.naturenet.model.Context activityContext = Model.load(org.naturenet.model.Context.class,
		context_activity_id);
	mNote.setContext(activityContext);
	// mNote must commit to be a note in database, then setLandmarkFeedback can work
	mNote.commit(); 
	
	mMedia = new Media();
	mMedia.setNote(mNote);
	mMedia.setLocal(mCurrentPhotoPath);
	mMedia.commit();
	org.naturenet.model.Context landmarkContext = Model.load(org.naturenet.model.Context.class,
		context_landmark_id);
	// setLandmarkFeedback.requires a note id!!!!!!
	mNote.setLandmarkFeedback(landmarkContext); 
	mNote.commit();
	
	// ready to pass the note image to ObservationsFragment GridView's Adapter
	noteImage = new NoteImage(mCurrentPhotoPath, mMedia.getTimeCreated(), mNote.getId());
	noteImage.setNoteState(mNote.getSyncState());
	newObservationPasser.onPassNewObservation(noteImage);
    }
    
    /* process data after press submit */
    private void dispathSubmit() {
	if (geoInfo == null && mNote == null) {
	    // only show alert when this is a new note(!=null) and missing a geo info
	    showNoLocationFoundAlert();
	} else {
	    createObservation();
	    Toast.makeText(getActivity(), "Thank you very much for your contribution!",
		    Toast.LENGTH_SHORT).show();
	    FragmentManager fm = getActivity().getSupportFragmentManager();
	    if (fm.findFragmentByTag(ObservationFragment.TAG) != null) {
		fm.popBackStack(ObservationFragment.TAG, 0);
	    } else {
		ObservationFragment newFragment = ObservationFragment.newInstance();
		((MainActivity) getActivity()).replaceFragment(newFragment, R.id.main_container,
			ObservationFragment.TAG);
	    }
	}
	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
		Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
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
	if (landmarkSpinnerDefaultPosition != 0) {
	    position = landmarkSpinnerDefaultPosition;
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
	    activitySpinner.setSelection(activitySpinnerDefaultPosition);
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
		// Log.d("debug", "context id: " + activities.get(0).getTitle());
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

    /* get location name based on current location */
    private String getAccurateLocationName(Double latitude, Double longitude) {
	Double latError = 0.0001;
	Double lonError = 0.0001;
	String name = null;
	int index = 0;
	for (org.naturenet.model.Context loc : landmarks) {
	    // location "other" has no geo info
	    if (index == (landmarks.size() - 1)) {
		name = loc.getName();
		break;
	    }
	    Double landmarkLat = (Double) loc.getExtras().get("latitude");
	    Double landmarkLon = (Double) loc.getExtras().get("longitude");
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
    
    /* alert dialog if no location was found */
    public void showNoLocationFoundAlert() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
	    builder.setTitle("Location Not Found").setMessage("Would like to wait for location?")
		    .setCancelable(true)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				locationHelper.requestCurrentLocation();
			}
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    cancelEdit();
			}
		    });
	    AlertDialog alert = builder.create();
	    alert.show();
    }
    
    /* interface for new observation listener */
    public interface onPassNewObservationListener {
	public void onPassNewObservation(NoteImage image);
	public void onObservationStateChange(NoteImage image);
    }

    /* location was found from LocationHelper, then set geoInfo */
    @Override
    public void foundLocation(Location loc) {
	Log.d("mylocation", "location found" + loc.toString());
	this.geoInfo = loc;
	currGPSLandmarkName = getAccurateLocationName(geoInfo.getLatitude(), geoInfo.getLongitude());
	landmarkSpinnerDefaultPosition = getPositionByName(currGPSLandmarkName, landmarks);
	// if the note is a new note, set the default selection to be based on current location
	if (mNote == null) {
	    locationSpinner.setSelection(landmarkSpinnerDefaultPosition);
	}
    }

    @Override
    public void locationNotFound() {
	Log.d("mylocation", "location not found");
	// Toast.makeText(getActivity(), "Location Not Found!", Toast.LENGTH_SHORT).show();
	// locationHelper.requestCurrentLocation();
    }
}


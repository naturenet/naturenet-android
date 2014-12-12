package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.adapters.ObservationGridAdapter;
import org.naturenet.model.Account;
import org.naturenet.model.NNModel;
import org.naturenet.model.Note;
import org.naturenet.model.Media;
import org.naturenet.model.Session;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ObservationFragment extends Fragment {
    public static HomeFragmentListener homeFragmentListener;
    private View		rootView;
    private GridView	    previewGridView;
    private TextView tv_warning; 
    
    /* keys in onResult */
    public final static int REQ_CODE = 1001;
    public final static String RESULT_KEY_OBSERVATION = "result";
    public final static String TAG = ObservationFragment.class.getName();
    
    /* keys in getArguments() */
    public final static String IMAGEPATH = "imagepath";
    public final static String IMAGETIME = "imagetime";
    public final static String IMAGENOTEID = "imagenoteid";
    
    /* data */
    public long account_id;
    public List<Note> notes;
    private List<NoteImage> images;
    private OnDataPassListener dataPasser;
    private ObservationGridAdapter oAdapter;
    private NoteImage newImage;

    public static ObservationFragment newInstance() {
	ObservationFragment f = new ObservationFragment();
	return f;
    }

    public static ObservationFragment newInstance(HomeFragmentListener listener) {
	homeFragmentListener = listener;
	return new ObservationFragment();
    }

    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (OnDataPassListener) a;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	this.notes = getNotes();
	this.images = getImages();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	rootView = inflater.inflate(R.layout.fragment_observation, container, false);
	ImageView iButton = (ImageView) rootView.findViewById(R.id.new_observation);
	previewGridView = (GridView) rootView.findViewById(R.id.observations_preview_grid);
	tv_warning = (TextView) rootView.findViewById(R.id.observations_preview_warning);
	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	iButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		((MainActivity) getActivity()).dispatchTakePictureIntent();
	    }
	});

	if (notes == null || notes.isEmpty()) {
	    previewGridView.setVisibility(View.GONE);
	    tv_warning.setVisibility(View.VISIBLE);
	} else {
	    previewGridView.setVisibility(View.VISIBLE);
	    tv_warning.setVisibility(View.GONE);
	    oAdapter = new ObservationGridAdapter(this.getActivity(), images);	
	    previewGridView.setAdapter(oAdapter);
	}
	
	previewGridView.setOnItemClickListener(new OnClickToLaunchEditNote());
	return rootView;
    }
    
    /* get account id from MainActivity, not used */
    public void setAccoutId(long id) {
	account_id = id;
    }
 
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_observations, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	case R.id.observations_reload:
	    if (!notes.isEmpty()) {
		refreshImageStatus();
	    }
	    break;
	case android.R.id.home:
	    FragmentManager fm = getActivity().getSupportFragmentManager();
	    fm.popBackStack(HomeFragment.TAG, 0);
	    break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_observations);
        if (this.newImage != null) {
            Collections.sort(images);
            // if it is a first note's image, setup the adapter
            if (notes == null || notes.isEmpty()) {
        	previewGridView.setVisibility(View.VISIBLE);
        	tv_warning.setVisibility(View.GONE);
        	oAdapter = new ObservationGridAdapter(this.getActivity(), images);	
    	    	previewGridView.setAdapter(oAdapter);
            } else {
        	oAdapter.notifyDataSetChanged();
            }
            this.newImage = null;
        }   
    }
    
    /* receiving a new image from MainActivity, originally from AddObservationFragment */
    public void updataNewImage(NoteImage newImage) {
	boolean imgExisted = false;
	for (NoteImage image : images) {
	    if (image.getNoteId() == newImage.getNoteId()) {
		imgExisted = true;
		if (newImage.getNoteState().equals("sent")) {
		    image.setNoteState(4);
		} 
		if (newImage.getNoteState().equals("ready to send")) {
		    image.setNoteState(2);
		} 
		break;
	    }
	}
	if (!imgExisted) {
	    images.add(newImage);
	    this.newImage = newImage;
	} else {
	    if (oAdapter != null) {
		oAdapter.notifyDataSetChanged();
	    }
	}
    }
    
    /* receiving a new state of an image from MainActivity, originally from AddObservationFragment */
    public void updateImageState (NoteImage newImage) { 
	for (NoteImage image : images) {
	    if (image.getNoteId() == newImage.getNoteId()) {
		if (newImage.getNoteState().equals("sent")) {
		    image.setNoteState(4);
		} 
		if (newImage.getNoteState().equals("ready to send")) {
		    image.setNoteState(2);
		} 
		break;
	    }
	}
	if (oAdapter != null) {
		oAdapter.notifyDataSetChanged();
	}
    }
    
    /* get notes from a user */
    public List<Note> getNotes() {
	Account account = Session.getAccount();
	checkNotNull(account);
	List<Note> notes = new ArrayList<Note> ();
	notes = account.getNotes();
	checkNotNull(notes);
	return notes;
    }

    /* get the photo's paths of observations */
    public List<NoteImage> getImages() {
	List<NoteImage> images = new ArrayList<NoteImage>();
	checkNotNull(notes);
	for (Note note : notes) {
	    // Log.d("debug", "note info is: " + note.toString());
	    Media media = note.getMediaSingle();
	    if (media != null) {
		String path = media.getPath();
		// check the file still exists in the phone
		if (checkFileExist(path)) {
		    long time = media.getTimeCreated();
		    long note_id = note.getId();
		    int note_state = note.getSyncState();
		    NoteImage image = new NoteImage(path, time, note_id);
		    image.setNoteState(note_state);
		    images.add(image);
		}
	    }
	}

	if (!images.isEmpty()) {
	    Collections.sort(images);
	}
	return images;
    }
    
    /* check the file exists in the phone*/
    private boolean checkFileExist(String path) {
	boolean exist = false;
	char firstChar = path.charAt(0);
	if (firstChar == 'f') {
	    String rPath = path.substring(5);
	    File file = new File(rPath);
	    if (file.exists()) {
		exist = true;
	    }
		
	} else if (firstChar == 'h') {
	    exist = true;
	}
	
	return exist;
    }
    
    /* refresh the status of each observation */
    private void refreshImageStatus() {
	for (Note note : notes) {
	    for (NoteImage nImage : images) {
		if (note.getId() == nImage.getNoteId()) {
		    nImage.setNoteState(note.getSyncState());
		}
	    }
	} 
	if (oAdapter != null) {
	    oAdapter.notifyDataSetChanged();
	}
    }
    
    /* grid view item onclick listener class*/
    private class OnClickToLaunchEditNote implements OnItemClickListener{
	@Override
	public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
		long note_id = images.get(position).getNoteId();
		Log.d("debug", "touched image's note id is: " + note_id);
		dataPasser.onNoteIdPass(note_id);
	}
    }
    
    /* a class for save image information for gridview adapter */
    public static class NoteImage implements Comparable<Object> {
	private String path;
	private long   time;
	private long noteId;
	private String noteState;

	public NoteImage(String path, long time, long noteId) {
	    super();
	    this.path = path;
	    this.time = time;
	    this.noteId = noteId;
	}

	public void setNoteState(int state) {
	    switch (state) {
	    	case 2:  // SAVED
	    	    this.noteState = "ready to send";
	    	    break;
	    	case 4: // SYNCED
	    	    this.noteState = "sent";
	    	    break;
	    	default:
	    	    this.noteState = "ready to send";
	    }
	}
	
	public String getNoteState() {
	    return this.noteState;
	}
	
	@Override
	public int compareTo(Object arg) {
	    long time2 = ((NoteImage) arg).getTime();
	    if (this.time > time2)
		return -1;
	    if (this.time < time2)
		return 1;
	    return 0;
	};

	public String getPath() {
	    return this.path;
	}

	public long getTime() {
	    return this.time;
	}
	
	public long getNoteId() {
	    return this.noteId;
	}

	@Override
	public String toString() {
	    return "ImageInfo [path=" + path + ", time=" + time + ", noteId=" + noteId + "]";
	}
    }
    
    /* interface for passing data to mainActivity and other fragment */
    public interface OnDataPassListener {
	public void onNoteIdPass(long note_id);
    }
}

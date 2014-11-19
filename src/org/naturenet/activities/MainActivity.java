package org.naturenet.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.naturenet.fragments.ActivityFragment;
import org.naturenet.fragments.AddObservationFragment;
import org.naturenet.fragments.HomeFragment;
import org.naturenet.fragments.ActivitiesFragment;
import org.naturenet.fragments.ObservationFragment.NoteImage;
import org.naturenet.fragments.TourFragment;
import org.naturenet.fragments.ObservationFragment;
import org.naturenet.helper.InitialDownloadTask;
import org.naturenet.model.Note;
import org.naturenet.model.Session;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements HomeFragment.OnPassAccount,
	ObservationFragment.OnDataPassListener, TourFragment.OnDataPassListener,
	ActivitiesFragment.onActivityPassListener, ActivityFragment.OnActivityIdPassListener,
	AddObservationFragment.onPassNewObservationListener,
	FragmentManager.OnBackStackChangedListener {
    private ActionBar actionBar;
    private Menu abMenu;
    
    public final String APP_TAG = "NatureNet";
    private String mCurrentPhotoPath;
    private Uri       fileUri;		  // file url for image file path
    private String selectLocation;   //location name from tour fragment
    private String selectActivity;   //activity name from activity fragment
    private FragmentManager fragmentManager;

    public final static int REQUEST_IMAGE_CAPTURE = 0;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static String SIGNIN = "Sign In";
    public final static String SIGNOUT = "Sign Out";

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	actionBar = getActionBar();
	fragmentManager = getSupportFragmentManager();
	fragmentManager.addOnBackStackChangedListener(this);
	if (savedInstanceState == null) {
	    /* initialize site data */
	    InitialDownloadTask task = new InitialDownloadTask(this);
	    task.execute((Void) null);
	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	this.abMenu = menu;
	if (Session.isSignedIn()) {
	    menu.findItem(R.id.action_settings).setTitle(SIGNOUT);
	}
	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	switch (item.getItemId()) {
	    case R.id.action_settings:
		if (item.getTitle().equals(SIGNOUT)) {
		    showLogOutAlert();
		    item.setTitle(SIGNIN);
		} else {
		    HomeFragment hFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.TAG);
		    hFragment.onClickSingIn();
		}
		return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
    
    /* change settings title */
    public void setOptionTitle(int id, String title) {
        MenuItem item = abMenu.findItem(id);
        item.setTitle(title);
    }

    @Override
    public void onBackPressed() {	
	HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.TAG);
	ObservationFragment obsFragment = (ObservationFragment) fragmentManager.findFragmentByTag(ObservationFragment.TAG);
	if (homeFragment != null && homeFragment.isVisible()) {
	    if (Session.isSignedIn()) {
		showLogOutAlert();
	    } else {
		this.finish();
	    }
	} else if (obsFragment != null && obsFragment.isVisible()) {
	    fragmentManager.popBackStack(HomeFragment.TAG, 0);
	} else {
	    fragmentManager.popBackStack();
	}
    }
    
    @Override
    public void onBackStackChanged() {

    }
    
    /* sign out alert */
    public void showLogOutAlert() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog.setTitle("Confirm Sign Out...");
	alertDialog.setMessage("Are you sure you want to Sign Out?");
	alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		Session.signOut();
		HomeFragment homeFragment = (HomeFragment) fragmentManager
			.findFragmentByTag(HomeFragment.TAG);
		if (homeFragment != null) {
		    if (homeFragment.isVisible()) {
			MainActivity.this.finish();
		    } else {
			fragmentManager.popBackStack(HomeFragment.TAG, 0);
		    }
		}
		homeFragment.showSignInBtn();
	    }
	});
	alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	    }
	});
	alertDialog.show();
    }
    
    /* Replace this fragment with next fragment*/
    public void replaceFragment(Fragment fragment, int fragmentid, String tag) {
	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	transaction.replace(fragmentid, fragment, tag);
	transaction.addToBackStack(tag);
	transaction.commit();
    }

    @Override
    public void onPassAccountIdListener(long id) {
	ObservationFragment obsFragment =  
		(ObservationFragment) getSupportFragmentManager().findFragmentByTag(ObservationFragment.TAG);
	obsFragment.setAccoutId(id);
    }
    
    /* Receiving data from ObservationFragment */
    @Override
    public void onDataPass(long note_id) {
	AddObservationFragment nFragment = AddObservationFragment.newInstance();
	Bundle b = new Bundle();
	b.putLong(AddObservationFragment.NOTE_ID, note_id);
	nFragment.setArguments(b);
	replaceFragment(nFragment, R.id.main_container, AddObservationFragment.TAG); 
    }

    /* Receiving data from TourFragment */
    @Override
    public void onPassLocation(String markerId) {
//	AddObservationFragment nFragment = AddObservationFragment.newInstance();
//	Bundle b = new Bundle();
//	b.putString(AddObservationFragment.LANDMARKNAME, name);
//	nFragment.setArguments(b);
//	replaceFragment(nFragment, R.id.main_container, AddObservationFragment.TAG); 
	this.selectLocation = markerId;
    }

    /* Receiving data from ActivitiesFragment */
    @Override
    public void onActivityPass(org.naturenet.model.Context context) {
	ActivityFragment nFragment = ActivityFragment.newInstance();
	Bundle b = new Bundle();
	b.putString(ActivityFragment.ACTIVITYDESC, context.getDescription());
	b.putLong(ActivityFragment.ACTIVITYID, context.getId());
	b.putString(ActivityFragment.ACTIVITYNAME, context.getName());
	b.putString(ActivityFragment.ACTIVITYTITLE, context.getTitle());
	nFragment.setArguments(b);
	replaceFragment(nFragment, R.id.main_container, ActivityFragment.TAG); 
    }

    @Override
    public void onPassActivityName(String name) {
	Log.d("debug", "acitivty Id selected in MainActivity: " + name);
	this.selectActivity = name;
    }

    /* Receiving data from AddObservationFragment, going to add one new image in observation fragment */
    @Override
    public void onPassNewObservation(NoteImage image) {
	Bundle b = new Bundle();
	b.putString(ObservationFragment.IMAGEPATH, image.getPath());
	b.putLong(ObservationFragment.IMAGETIME, image.getTime());
	b.putLong(ObservationFragment.IMAGENOTEID, image.getNoteId());
	// nFragment.setArguments(b);
	ObservationFragment obsFragment = (ObservationFragment) getSupportFragmentManager()
				.findFragmentByTag(ObservationFragment.TAG);
	if (obsFragment != null) {
	    obsFragment.updataNewImage(image);
	}
	// replaceFragment(nFragment, R.id.main_container, ObservationFragment.TAG); 
    }
    
    /* Receiving data from AddObservationFragment, 
     * going to update the state of one image in observation fragment */
    @Override
    public void onObservationStateChange(NoteImage image) {
	ObservationFragment obsFragment = (ObservationFragment) getSupportFragmentManager()
		.findFragmentByTag(ObservationFragment.TAG);
	if (obsFragment != null) {
	    obsFragment.updateImageState(image);
	}
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == REQUEST_IMAGE_CAPTURE) {
	    if (resultCode == RESULT_OK) {	
		AddObservationFragment nFragment = AddObservationFragment.newInstance();
		Bundle bundle = new Bundle();		
		bundle.putString(AddObservationFragment.IMAGE_CAPTURE_PATH, mCurrentPhotoPath);
		if (this.selectLocation != null) {
		    Log.d("debug", "user selected in MainActivity: " + this.selectLocation);
		    bundle.putString(AddObservationFragment.LANDMARKNAME, selectLocation);
		}
		if (this.selectActivity != null) {
		    Log.d("debug", "user selected in MainActivity: " + this.selectActivity);
		    bundle.putString(AddObservationFragment.ACTIVITYNAME, this.selectActivity);
		}
		nFragment.setArguments(bundle);
		replaceFragment(nFragment,
			 R.id.main_container, AddObservationFragment.TAG);
	    } else if (resultCode == RESULT_CANCELED) {
		// delete the created the empty file if it is existing
		if (fileUri != null) {
		    File file = new File(fileUri.getPath());
		    file.delete();
		}
	    }
	}
    }
    
    @Override
    public void onStop() {
	super.onStop();
    }
    
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                fileUri = Uri.fromFile(photoFile);
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE );
            }
        }
    }  

    private File createImageFile() throws IOException {
	// Create an image file name
	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	String imageFileName = "JPEG_" + timeStamp + "_";
	File storageDir = Environment
		.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	File imageFile = File.createTempFile(imageFileName, /* prefix */
		".jpg", /* suffix */
		storageDir /* directory */
	);
	// Save a file: path for use with ACTION_VIEW intents
	mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
	return imageFile;
    }
    
    /* add photo to phone's gallery */
    public void galleryAddPic() {
	Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	File f = new File(mCurrentPhotoPath);
	Uri contentUri = Uri.fromFile(f);
	mediaScanIntent.setData(contentUri);
	sendBroadcast(mediaScanIntent);
    }
}

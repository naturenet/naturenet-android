package org.naturenet.helper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.naturenet.activities.R;
import org.naturenet.adapters.EndlessScrollListener;
import org.naturenet.adapters.ObservationGridAdapter;
import org.naturenet.model.Account;
import org.naturenet.model.Media;
import org.naturenet.model.Note;
import org.naturenet.model.Session;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadImgAsync extends AsyncTask<Void, Void, Void> {
    private ProgressDialog mProgressDialog;
    private Context	context;
    private Activity       activity;
    private GridView       gridview;
    private TextView tv_warning;
    private ObservationGridAdapter oAdapter;

    public LoadImgAsync(Activity activity, GridView previewGridView, TextView tv_warning) {
   	super();
	this.activity = activity;
   	this.context = activity.getBaseContext();
   	this.gridview = previewGridView;
   	this.tv_warning = tv_warning;
    }

    @Override
    protected void onPreExecute() {
	super.onPreExecute();
	this.mProgressDialog = ProgressDialog.show(activity, "", 
                "Loading. Please wait...", true);
	mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void...arg0) {
	Account account = Session.getAccount();
	checkNotNull(account);
	List<Note> notes = new ArrayList<Note> ();
	notes = account.getNotes();
	checkNotNull(notes);
//	ACES site id is 2 or 1 ???
//	List<Note>notes = account.getNotesOrderedByRecencyAtSite(1);
	if (!notes.isEmpty()) {
//		oAdapter = new ObservationGridAdapter(activity, notes);
	}
	return null;
    }

    @Override
    protected void onPostExecute(Void params) {
	if (oAdapter != null) {
	    gridview.setVisibility(View.VISIBLE);
	    tv_warning.setVisibility(View.GONE);
	    gridview.setAdapter(oAdapter);

	} else {
	    gridview.setVisibility(View.GONE);
	    tv_warning.setVisibility(View.VISIBLE);
	}

	mProgressDialog.dismiss();
    }
    
    private List<String> RetriveCapturedImagePath() {
	File storageDir = Environment
		      .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	List<String> tFileList = new ArrayList<String>();
	File f = new File(storageDir.getAbsolutePath());
	if (f.exists()) {
	    File[] files = f.listFiles();
	    Arrays.sort(files);
	    for (int i = 0; i < files.length; i++) {
		File file = files[i];
		if (file.isDirectory())
		    continue;
		tFileList.add(file.getPath());
//		Log.d("filepath", file.getPath());
	    }
	}
	return tFileList;
    }

   
}

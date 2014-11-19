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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadObsPicAsync extends AsyncTask<Void, Void, Void> {

    private Activity       activity;
    private String 	path;
    private RequestCreator picRequstor;
    private ImageView	imageView;
    private RelativeLayout rLoadPanel;
    private TextView tvStatus;
    
    public LoadObsPicAsync(Activity activity, String path, ImageView imageView, RelativeLayout pb, TextView tv) {
	super();
	this.activity = activity;
	this.path = path;
	this.imageView = imageView;
	this.rLoadPanel = pb;
	this.tvStatus = tv;
    }

    @Override
    protected void onPreExecute() {
	super.onPreExecute();
//	this.mProgressDialog = ProgressDialog.show(activity, "", 
//                "Loading. Please wait...", true);
//	mProgressDialog.show();
//	pb.setVisibility(View.VISIBLE);
//	activity.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
	imageView.setVisibility(View.GONE);
	rLoadPanel.setVisibility(View.VISIBLE); // no effect!!!!!
    }

    @Override
    protected Void doInBackground(Void...arg0) {
//	Log.d("debug", "thumbnail link or path: " + path);

//	if (path.charAt(0) == 'f')
//	    picRequstor = Picasso.with(this.activity.getBaseContext()).load(path).resize(300, 250).centerCrop();
//	else
//	    picRequstor = Picasso.with(this.activity.getBaseContext()).load(path);
	
	picRequstor = Picasso.with(this.activity.getBaseContext()).load(path).resize(250, 250).centerCrop();
	return null;
    }

    @Override
    protected void onPostExecute(Void params) {
	picRequstor.into(imageView,  new ImageLoadedCallback(rLoadPanel) {
            @Override
            public void onSuccess() {
                if (this.loadPanel != null) {
                    this.loadPanel.setVisibility(View.GONE);
                 }
                imageView.setVisibility(View.VISIBLE);
        	tvStatus.setText("sent");
            }
            
            @Override
            public void onError() {
        	tvStatus.setText("not sent");
        	Log.d("debug", "something wrong in picasso");
            }
	});	 //???
//	imageView.setVisibility(View.VISIBLE);
//	tvStatus.setText("done");
//	rLoadPanel.setVisibility(View.GONE);
//	mProgressDialog.dismiss();
    }

    // callback when image loads sucefully
    private class ImageLoadedCallback implements Callback {
	RelativeLayout loadPanel;

	public ImageLoadedCallback(RelativeLayout progBar) {
	    loadPanel = progBar;
	}

	@Override
	public void onSuccess() {

	}

	@Override
	public void onError() {

	}
    }

}

/*
 * Adapter for grid view in home fragment
 */
package org.naturenet.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.naturenet.activities.R;
import org.naturenet.fragments.ObservationFragment.NoteImage;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ObservationGridAdapter extends BaseAdapter {
    private Context      context;
    private Activity activity;
    private List<NoteImage> imageList;

    public ObservationGridAdapter(Activity a, List<NoteImage> images) {
  	this.activity = a;
  	this.context = a.getBaseContext();
  	this.imageList = images;
      }

    @Override
    public int getCount() {
	if (imageList != null) {
	    return imageList.size();
	}
	return 0;
    }

    @Override
    public Object getItem(int position) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public long getItemId(int position) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View gridView = convertView;
	if (gridView == null) {
	    gridView = inflater.inflate(R.layout.obs_gridview_item, viewGroup, false);
	    gridView.setTag(R.id.obs_squre_thumbnail,
		    gridView.findViewById(R.id.obs_squre_thumbnail));
	    gridView.setTag(R.id.loadingPanel, gridView.findViewById(R.id.loadingPanel));
	    gridView.setTag(R.id.obs_status, gridView.findViewById(R.id.obs_status));
	}
	
	final ImageView imageView = (ImageView) gridView.getTag(R.id.obs_squre_thumbnail);
	final TextView statusTextView = (TextView) gridView.getTag(R.id.obs_status);	
	RelativeLayout rLoadPanel = (RelativeLayout) gridView.getTag(R.id.loadingPanel);
	imageView.setId(position);
	final NoteImage nImage = imageList.get(position);
	String path = nImage.getPath();
	// if the path comes from http, get the thumbnail
	if (path.charAt(0) == 'h') {
	    path = getThumbNailLink (path);
	}
	imageView.setVisibility(View.GONE);
	rLoadPanel.setVisibility(View.VISIBLE);
	RequestCreator picRequstor = Picasso.with(context).load(path).resize(400, 300).centerCrop();
	picRequstor.into(imageView,  new ImageLoadedCallback(rLoadPanel) {
            @Override
            public void onSuccess() {
                if (this.loadPanel != null) {
                    this.loadPanel.setVisibility(View.GONE);
                 }
                imageView.setVisibility(View.VISIBLE);
        	statusTextView.setText(nImage.getNoteState());
            }
            
            @Override
            public void onError() {
        	statusTextView.setText("not sent");
        	Log.d("debug", "something wrong in picasso");
            }
	});
	return gridView;
    }
    
    public void updateImageList(List<NoteImage> newlist) {
	imageList.clear();
	imageList.addAll(newlist);
	Collections.sort(imageList);
	this.notifyDataSetChanged();
    }

    /* given the url of the note's image, return a url for thumb nail */
    private String getThumbNailLink (String url) {
	StringBuilder sb = new StringBuilder();
	String[] splits = url.split("/");
	int numbers = 0;
	for (String s : splits) {
	    numbers++;
	    if (numbers == splits.length) {
		sb.append(s+".jpg");
	    } else {
		sb.append(s + "/");
	    }
	    
	    if (s.equals("upload")) {
		sb.append("c_fit,w_400,h_300/");
	    } 
	}
	
	String name = splits[splits.length - 1];
	Map<String, String> config = new HashMap<String, String> ();
	config.put("cloud_name", "university-of-colorado");
	config.put("api_key", "893246586645466");
	config.put("api_secret", "8Liy-YcDCvHZpokYZ8z3cUxCtyk");
	Cloudinary cloudinary = new Cloudinary(config);
	String linkurl = cloudinary.url()
	  .transformation(
	    new Transformation().width(300).height(300).crop("thumb")
	  ).generate(name);	
	return linkurl;
    }

    // callback when image loads sucefully
    private class ImageLoadedCallback implements Callback {
	RelativeLayout loadPanel;

	public ImageLoadedCallback(RelativeLayout layout) {
	    loadPanel = layout;
	}

	@Override
	public void onSuccess() {

	}

	@Override
	public void onError() {

	}
    }
}
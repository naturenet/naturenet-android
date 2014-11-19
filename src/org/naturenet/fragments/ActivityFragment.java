package org.naturenet.fragments;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityFragment extends Fragment {
    public final static String TAG = ActivityFragment.class.getName();

    public final static String ACTIVITYDESC = "activity_description";
    public final static String ACTIVITYID = "activity_id";
    public final static String ACTIVITYNAME = "activity_name";
    public final static String ACTIVITYTITLE = "activity_title";


    public String description;
    public long activity_id;
    public String name;
    public String title;
    public OnActivityIdPassListener dataPasser;

    public static ActivityFragment newInstance() {
	ActivityFragment f = new ActivityFragment();
	return f;
    }

    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (OnActivityIdPassListener) a;
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View rootView = inflater.inflate(R.layout.fragment_in_activity, container, false);
	if (getArguments() != null) {
	    if (getArguments().containsKey(ACTIVITYDESC)) {
	    	description = getArguments().getString(ACTIVITYDESC);
	    } 
	    if (getArguments().containsKey(ACTIVITYID)) {
		activity_id = getArguments().getLong(ACTIVITYID);
	    }
	    if (getArguments().containsKey(ACTIVITYNAME)) {
		name = getArguments().getString(ACTIVITYNAME);
	    }
	    if (getArguments().containsKey(ACTIVITYTITLE)) {
		title = getArguments().getString(ACTIVITYTITLE);
	    }
	}
	
	TextView tv_description = (TextView) rootView.findViewById(R.id.observation_about);
	tv_description.setText(description);
	ImageView ivTakeObs = (ImageView) rootView.findViewById(R.id.cam_in_activity);

	ivTakeObs.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		/* pass maker's id now not name*/
		dataPasser.onPassActivityName(name);
		((MainActivity) getActivity()).dispatchTakePictureIntent();
	    }
	});

	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	getActivity().getActionBar().setTitle(title);
	setHasOptionsMenu(true);
	return rootView;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		Log.d("debug", "home up pressed in activities fragment!");
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStack();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
    
    /* public interface for pass data to MainActivity */
    public interface OnActivityIdPassListener{
	public void onPassActivityName(String name);
    }

}

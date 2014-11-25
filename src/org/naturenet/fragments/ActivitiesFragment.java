package org.naturenet.fragments;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.naturenet.activities.R;
import org.naturenet.adapters.ActivitiesListViewAdapter;
import org.naturenet.model.Account;
import org.naturenet.model.Context;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import com.google.android.gms.maps.SupportMapFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ActivitiesFragment extends Fragment {
    private ListView lvActivities;
    private int[] counts = new int[] { 200, 150, 66, 20};
    public final static String TAG = ActivitiesFragment.class.getName();

    private Account	     	mAccount;
    private Site		mSite;
    private List<Context> activityList;
    private onActivityPassListener dataPasser;
    
    public void initModel() {
	mAccount = Session.getAccount();
	checkNotNull(mAccount);
	mSite = Session.getSite();
	checkNotNull(mSite);
	activityList = mSite.getActivities();
	checkNotNull(activityList);
    }
    
    public static ActivitiesFragment newInstance() {
	ActivitiesFragment f = new ActivitiesFragment();
	return f;
    }

    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (onActivityPassListener) a;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	initModel();
        View rootView = inflater.inflate(R.layout.fragment_activities, container, false);
        lvActivities = (ListView) rootView.findViewById(R.id.listView_activities);
        ActivitiesListViewAdapter adapter = new ActivitiesListViewAdapter(getActivity(), activityList, counts);
        lvActivities.setAdapter(adapter);
        lvActivities.setOnItemClickListener(new OnClickToLaunchActivity());
        
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	getActivity().getActionBar().setTitle(R.string.title_fragment_activity);
	setHasOptionsMenu(true);

        return rootView;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
	// Log.d("debug", "activities fragment now in onViewCreated!");
    }

    @Override
    public void onDestroyView() {
	// Log.d("debug", "acitivites fragment now in onDestroyView!");
	super.onDestroyView();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_activity);
	setHasOptionsMenu(true);
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
    
    private class OnClickToLaunchActivity implements OnItemClickListener{
	@Override
	public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
		dataPasser.onActivityPass(activityList.get(position));
	}
    }
    
    /* pass activity selection to MainActivity */
    public interface onActivityPassListener {
	public void onActivityPass(org.naturenet.model.Context activity);
    }
}
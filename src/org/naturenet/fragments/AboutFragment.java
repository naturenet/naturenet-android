package org.naturenet.fragments;

import org.naturenet.activities.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {
    public final static String TAG = AboutFragment.class.getName();
    
    public static AboutFragment newInstance() {
	AboutFragment f = new AboutFragment();
	return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	View rootView = inflater.inflate(R.layout.fragment_about, container, false);

	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	setHasOptionsMenu(true);
	return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_about);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStack();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
}

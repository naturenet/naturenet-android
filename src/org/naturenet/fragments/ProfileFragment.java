package org.naturenet.fragments;

import org.naturenet.activities.R;
import org.naturenet.adapters.ObservationGridAdapter;
import org.naturenet.model.Account;
import org.naturenet.model.Session;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    public final static String TAG = ProfileFragment.class.getName();
    
    public static ProfileFragment newInstance() {
	ProfileFragment f = new ProfileFragment();
	return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
	Account account = Session.getAccount();
	
	TextView tv_name = (TextView) rootView.findViewById(R.id.textView_profile_name);
	tv_name.setText(account.getName());	
	TextView tv_email = (TextView) rootView.findViewById(R.id.textView_profile_email);	
	tv_email.setText(account.getEmail());
	TextView tv_id = (TextView) rootView.findViewById(R.id.textView_userid);	
	tv_id.setText("Hi, " + account.getUsername());
	TextView tv_num_obs = (TextView) rootView.findViewById(R.id.textView_profile_num_obs);	
	tv_num_obs.setText(""+getNotesNumber(account));
	TextView tv_num_ideas = (TextView) rootView.findViewById(R.id.textView_profile_num_ideas);	
	tv_num_ideas.setText(""+getIdeasNumber(account));
	Button btn_signout = (Button) rootView.findViewById(R.id.btn_signout_in_profile);
	btn_signout.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onSignOut();
	    }
	});
	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	setHasOptionsMenu(true);
	return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_profile);
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
    
    private int getNotesNumber(Account account) {
	return account.getFeedbacks().size();
    }
    
    private int getIdeasNumber(Account account) {
	return account.getNotes().size() - account.getFeedbacks().size();
    }
    
    private void onSignOut() {
	Session.signOut();
//	getActivity().deleteDatabase("RestClient.db");
	FragmentManager fm = getActivity().getSupportFragmentManager();
	fm.popBackStack();
    }
}

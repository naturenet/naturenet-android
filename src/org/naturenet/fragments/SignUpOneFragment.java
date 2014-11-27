package org.naturenet.fragments;

import org.naturenet.activities.R;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.naturenet.activities.LoginActivity;

public class SignUpOneFragment extends Fragment {
    public static final String TAG = SignUpOneFragment.class.getName();

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	ActionBar actionBar = getActivity().getActionBar();
	actionBar.setTitle("Sign Up Step:About the project");
	actionBar.setDisplayHomeAsUpEnabled(true);
	setHasOptionsMenu(true);
	
	View rootView = inflater.inflate(R.layout.fragment_signup_1, container,
		false);
	rootView.findViewById(R.id.step1_next_button).setOnClickListener(
		new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			SignUpTwoFragment newFragment = new SignUpTwoFragment();
			((LoginActivity) getActivity()).replaceFragment(
				newFragment, R.id.fragment_container_login, SignUpTwoFragment.TAG);
		    }
		});
	return rootView;
    }

    // trying to dismiss the soft keyboard if it is displaying. Not working now
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
	super.onActivityCreated(savedInstanceState);
	final InputMethodManager imm = (InputMethodManager) getActivity()
		.getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    getActivity().getSupportFragmentManager().popBackStack();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }
}

package org.naturenet.fragments;

import org.naturenet.activities.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.naturenet.activities.LoginActivity;

public class SignUpOneFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	((LoginActivity) getActivity()).setActionBarTitle("Sign Up Step 1");
	View rootView = inflater.inflate(R.layout.fragment_signup_1, container,
		false);
	rootView.findViewById(R.id.step1_next_button).setOnClickListener(
		new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			SignUpTwoFragment newFragment = new SignUpTwoFragment();
			((LoginActivity) getActivity()).replaceFragment(
				newFragment, R.id.fragment_container_login);
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

}

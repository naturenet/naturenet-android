package org.naturenet.fragments;

import org.naturenet.activities.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.naturenet.activities.LoginActivity;

import com.google.common.collect.Lists;

public class SignUpTwoFragment extends Fragment {
    private View rootView;
    private CheckBox chkOne;
    private CheckBox chkTwo;
    private CheckBox chkThree;
    private CheckBox chkFour;
    private Button nextBtn;
    private OnDataPassListener dataPasser;
    public final static String CONSENT = "consentText";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	((LoginActivity) getActivity()).setActionBarTitle("Sign Up Step 2");
	rootView = inflater.inflate(R.layout.fragment_signup_2, container,
		false);
	LinearLayout llayout = (LinearLayout) rootView.findViewById(R.id.llayout_LoginFormContainer);
	llayout.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
			Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	    }
	});

	initFragment();
	addListnersOnChks();
	addNexBtnOnClick();
	return rootView;
    }

    /**
     * initialize the checkboxes in this fragment
     * */
    public void initFragment() {
	this.chkOne = (CheckBox) rootView.findViewById(R.id.checkBox1);
	this.chkTwo = (CheckBox) rootView.findViewById(R.id.checkBox2);
	this.chkThree = (CheckBox) rootView.findViewById(R.id.checkBox3);
	this.chkFour = (CheckBox) rootView.findViewById(R.id.checkBox4);
	this.nextBtn = (Button) rootView.findViewById(R.id.step2_next_button);
    }

    /**
     * add click listeners to the checkboxes
     * */
    private void addListnersOnChks() {
	addListenerOnCheckOne();
	addListenerOnCheckTwo();
    }

    private void addListenerOnCheckOne() {
	chkOne.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (isRequiredChecked())
		    nextBtn.setEnabled(true);
		else
		    nextBtn.setEnabled(false);
	    }
	});
    }

    private void addListenerOnCheckTwo() {
	chkTwo.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (isRequiredChecked())
		    nextBtn.setEnabled(true);
		else
		    nextBtn.setEnabled(false);
	    }
	});
    }

    private void addNexBtnOnClick() {
	nextBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
//		boolean required = chkOne.isChecked() && chkTwo.isChecked();
//		nextBtn.setEnabled(required);	
		String consentText = "";
		for (CheckBox ch : Lists.newArrayList(chkOne,chkTwo,chkThree,chkFour)){
			if (ch.isChecked()){
				consentText += ch.getText().toString();
			}
		}
		
		dataPasser.onConsentDataPass(consentText);
	    }

	});
    }

    /**
     * if first and second check box are checked, return true
     * */
    private boolean isRequiredChecked() {
	if (chkOne != null & chkTwo != null) {
	    if (chkOne.isChecked() && chkTwo.isChecked()) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (OnDataPassListener) a;
    }
    
    /* a public interface for sending consent text to final stage fragment */
    public interface OnDataPassListener {
	public void onConsentDataPass(String consent);
    } 
}

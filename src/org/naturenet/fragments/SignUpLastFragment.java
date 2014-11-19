package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import org.naturenet.activities.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;

import org.naturenet.activities.LoginActivity;
import org.naturenet.model.Account;
import org.naturenet.rest.NatureNetAPI;
import org.naturenet.rest.NatureNetAPI.Result;
import org.naturenet.rest.NatureNetRestAdapter;

import retrofit.RetrofitError;

public class SignUpLastFragment extends Fragment {

    // Values for email and password at the time of the login attempt.
    private String	 mUsername;
    private String	 mName;
    private String	 mEmail;
    private String	 mPassword;

    // UI references.
    private View	   rootView;
    private Button	 signUpBtn;
    private EditText       mUsernameView;
    private EditText       mNameView;
    private EditText       mEmailView;
    private EditText       mPasswordView;
    private View	   mSignUpFormView;
    private View	   mSignUpStatusView;
    private TextView       mLoginStatusMessageView;

    private String	 mConsentText;
    private Account	mAccount;
    private OnAcccountPassListener accountPasser;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserSignUpTask mAuthTask = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	((LoginActivity) getActivity()).setActionBarTitle("Sign Up Step 3");
	rootView = inflater.inflate(R.layout.fragment_signup_3, container, false);
	if (getArguments() != null) {
	    String consent = getArguments().getString(SignUpTwoFragment.CONSENT);
//	    Log.d("debug", "got consent text: " + consent);
	    mConsentText = consent;
	}

	// Set up the login form.
	mUsernameView = (EditText) rootView.findViewById(R.id.signup_username);
	mUsernameView.setText(mUsername);

	mNameView = (EditText) rootView.findViewById(R.id.signup_name);
	mNameView.setText(mName);

	// mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
	 mEmailView = (EditText) rootView.findViewById(R.id.signup_email);
	 mEmailView.setText(mEmail);


	mPasswordView = (EditText) rootView.findViewById(R.id.signup_password);
	mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
		if (id == R.id.login || id == EditorInfo.IME_NULL) {
		    attemptSignUp();
		    return true;
		}
		return false;
	    }
	});

	mSignUpFormView = rootView.findViewById(R.id.signup_form);
	mSignUpStatusView = rootView.findViewById(R.id.signup_status);
	mLoginStatusMessageView = (TextView) rootView.findViewById(R.id.signup_status_message);
	
	signUpBtn = (Button) rootView.findViewById(R.id.sign_up_button);
	signUpBtn.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		attemptSignUp();
	    }
	});

	return rootView;

    }
    
    public void onAttach(Activity a) {
	super.onAttach(a);
	accountPasser = (OnAcccountPassListener) a;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptSignUp() {
	if (mAuthTask != null) {
	    return;
	}

	// Reset errors.
	mUsernameView.setError(null);
	mNameView.setError(null);
	mEmailView.setError(null);
	mPasswordView.setError(null);

	// Store values at the time of the login attempt.
	mUsername = mUsernameView.getText().toString().trim();
	mName = mNameView.getText().toString();
	mEmail = mEmailView.getText().toString();
	mPassword = mPasswordView.getText().toString();

	boolean cancel = false;
	View focusView = null;

	// Check for a valid username
	if (TextUtils.isEmpty(mUsername)) {
	    mUsernameView.setError(getString(R.string.error_field_required));
	    focusView = mUsernameView;
	    cancel = true;
	} else if (TextUtils.split(mUsername, " ").length > 1) {
	    mUsernameView.setError("Username can not have spaces");
	}

	// Check for a valid name
	if (TextUtils.isEmpty(mName)) {
	    mNameView.setError(getString(R.string.error_field_required));
	    focusView = mNameView;
	    cancel = true;
	}

	// Check for a valid password.
	if (TextUtils.isEmpty(mPassword)) {
	    mPasswordView.setError(getString(R.string.error_field_required));
	    focusView = mPasswordView;
	    cancel = true;
	} else if (mPassword.length() != 4 || !TextUtils.isDigitsOnly(mPassword)) {
	    mPasswordView.setError(getString(R.string.error_invalid_password));
	    focusView = mPasswordView;
	    cancel = true;
	}

	// Check for a valid email address.
	if (TextUtils.isEmpty(mEmail)) {
	    mEmailView.setError(getString(R.string.error_field_required));
	    focusView = mEmailView;
	    cancel = true;
	} else if (!mEmail.contains("@")) {
	    mEmailView.setError(getString(R.string.error_invalid_email));
	    focusView = mEmailView;
	    cancel = true;
	}

	if (cancel) {
	    // There was an error; don't attempt login and focus the first
	    // form field with an error.
	    focusView.requestFocus();
	} else {
	    // Show a progress spinner, and kick off a background task to
	    // perform the user login attempt.
	    mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
	    showProgress(true);
	    mAuthTask = new UserSignUpTask();
	    mAuthTask.execute((Void) null);
	}
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
	// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
	// for very easy animations. If available, use these APIs to fade-in
	// the progress spinner.
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
	    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

	    mSignUpStatusView.setVisibility(View.VISIBLE);
	    mSignUpStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
		    .setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
			    mSignUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		    });

	    mSignUpFormView.setVisibility(View.VISIBLE);
	    mSignUpFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
		    .setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
			    mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		    });
	} else {
	    // The ViewPropertyAnimator APIs are not available, so simply show
	    // and hide the relevant UI components.
	    mSignUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
	    mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

	private NatureNetAPI api;
	private String       errorMessage;

	@Override
	protected Boolean doInBackground(Void... params) {
	    api = NatureNetRestAdapter.get();
	    try {
		Result<Account> r = api.createAccount(mUsername, mName, mPassword, mEmail,
			mConsentText);
		// register the new account here.
		mAccount = new Account();
		mAccount.setUsername(mUsername);
		mAccount.setName(mName);
		// added by Jinyue
		mAccount.setPassword(mPassword);
		mAccount.setEmail(mEmail);
		
		mAccount.setUId(r.data.getUId());
		
//		mAccount.save();
		
		// changed by Jinyue
		mAccount.commit();
		Log.d("debug", mAccount.toString());
		return true;

	    } catch (RetrofitError e) {
		if (e.getResponse() != null && e.getResponse().getStatus() == 400) {
		    errorMessage = "This username is already taken";
		} else {
		    errorMessage = "Error communicating with the server.";
		}
		return false;
	    }
	}

	@Override
	protected void onPostExecute(final Boolean success) {
	    mAuthTask = null;
	    showProgress(false);

	    if (success) {
		checkNotNull(mAccount);
		accountPasser.onAccountPass(mAccount.getId());
//		Intent i = new Intent();
//		i.putExtra(RESULT_KEY_LOGIN, mAccount.getId());
//		getActivity().setResult(Activity.RESULT_OK, i);
		getActivity().finish();
	    } else {
		mUsernameView.setError(errorMessage);
		mUsernameView.requestFocus();
	    }
	}

	@Override
	protected void onCancelled() {
	    mAuthTask = null;
	    showProgress(false);
	}
    }
    
    /* an interface for passing account info to LoginActivity */
    public interface OnAcccountPassListener {
	public void onAccountPass(Long id);
    }
}

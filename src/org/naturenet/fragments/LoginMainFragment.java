package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import org.naturenet.activities.R;
import org.naturenet.activities.LoginActivity;
import org.naturenet.model.Account;
import org.naturenet.model.NNModel;
import org.naturenet.model.Site;

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
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class LoginMainFragment extends Fragment {
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[] { "foo@example.com:hello",
	    "bar@example.com:world"		};

    /**
     * The default email to populate the email field with.
     */
    public static final String    EXTRA_EMAIL       = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask	 mAuthTask	 = null;

    // Values for email and password at the time of the login attempt.
    private String		mUsername;
    private String		mPassword;

    // UI references.
    private EditText	      mEmailView;
    private EditText	      mPasswordView;
    private View		  mLoginFormView;
    private View		  mLoginStatusView;
    private TextView	      mLoginStatusMessageView;
    private PassAccount	   passComm;

    public static LoginMainFragment newInstance() {
	LoginMainFragment f = new LoginMainFragment();
	return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View rootView = inflater.inflate(R.layout.fragment_login_main, container, false);
	((LoginActivity) getActivity()).setActionBarTitle("Sign in");

	// Set up the login form.
	mUsername = getActivity().getIntent().getStringExtra(EXTRA_EMAIL);
	mEmailView = (EditText) rootView.findViewById(R.id.email);
	mEmailView.setText(mUsername);

	mPasswordView = (EditText) rootView.findViewById(R.id.password);
	mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
		if (id == R.id.login || id == EditorInfo.IME_NULL) {
		    attemptLogin();
		    return true;
		}
		return false;
	    }
	});

	mLoginFormView = rootView.findViewById(R.id.login_form);
	mLoginStatusView = rootView.findViewById(R.id.login_status);
	mLoginStatusMessageView = (TextView) rootView.findViewById(R.id.login_status_message);

	rootView.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
			Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		attemptLogin();
	    }
	});

	rootView.findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		SignUpOneFragment newFragment = new SignUpOneFragment();
		((LoginActivity) getActivity()).replaceFragment(newFragment,
			R.id.fragment_container_login);
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
			Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	    }
	});
	return rootView;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
	if (mAuthTask != null) {
	    return;
	}

	// Reset errors.
	mEmailView.setError(null);
	mPasswordView.setError(null);

	// Store values at the time of the login attempt.
	mUsername = mEmailView.getText().toString();
	mPassword = mPasswordView.getText().toString();

	boolean cancel = false;
	View focusView = null;

	// Check for a valid password.
	if (TextUtils.isEmpty(mPassword)) {
	    mPasswordView.setError(getString(R.string.error_field_required));
	    focusView = mPasswordView;
	    cancel = true;
	} else if (mPassword.length() < 4) {
	    mPasswordView.setError(getString(R.string.error_invalid_password));
	    focusView = mPasswordView;
	    cancel = true;
	}

	// Check for a valid email address.
	if (TextUtils.isEmpty(mUsername)) {
	    mEmailView.setError(getString(R.string.error_field_required));
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
	    mAuthTask = new UserLoginTask();
	    try {
		mAuthTask.execute((Void) null);
	    } catch (Exception e) {
		    Log.d("debug", "can't log in");
	    }
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

	    mLoginStatusView.setVisibility(View.VISIBLE);
	    mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
		    .setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
			    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		    });

	    mLoginFormView.setVisibility(View.VISIBLE);
	    mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
		    .setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
			    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		    });
	} else {
	    // The ViewPropertyAnimator APIs are not available, so simply show
	    // and hide the relevant UI components.
	    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
	    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
	private Account mAccount;
	private String  errorMessage = "Login error";

	@Override
	protected Boolean doInBackground(Void... params) {
	    boolean success = false;
	    try {
		// pull site table first
		if (NNModel.countLocal(Site.class) == 0) {
		    NNModel.resolveByName(Site.class, "aces");
		}
		// pull account table
		mAccount = NNModel.resolveByName(Account.class, mUsername);
		// if the account does not exist
		if (mAccount == null) {
		    errorMessage = "Unable to find " + mUsername;
		    success = false;
		} else {
		    checkNotNull(mAccount);
		    // Log.d("debug", mAccount.toString());
		    if (mAccount.getPassword().equalsIgnoreCase(mPassword)) {
			// pull notes executed here... why??
			mAccount.pullNotes();
			success = true;
		    } else {
			errorMessage = "Username and PIN are incorrect";
			success = false;
		    }

		}
	    } catch (Exception e) {
		errorMessage = "No Internet connction";
		Log.d("debug", "can't log in");
	    }
	    return success;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
	    mAuthTask = null;
	    showProgress(false);
	    if (success) {
		checkNotNull(mAccount);
		passComm.onAccountPass(mAccount.getId());
		getActivity().finish();
	    } else {
		mEmailView.setError(errorMessage);
		mEmailView.requestFocus();
	    }
	}

	@Override
	protected void onCancelled() {
	    mAuthTask = null;
	    showProgress(false);
	}
    }

    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	passComm = (PassAccount) a;
    }

    public interface PassAccount {
	public void onAccountPass(Long id);
    }
}

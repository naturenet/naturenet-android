package org.naturenet.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import org.naturenet.fragments.LoginMainFragment;
import org.naturenet.fragments.SignUpLastFragment;
import org.naturenet.fragments.SignUpTwoFragment;
import org.naturenet.model.Account;

import com.activeandroid.Model;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity implements LoginMainFragment.PassAccount, 
			SignUpTwoFragment.OnDataPassListener, SignUpLastFragment.OnAcccountPassListener {
    private ActionBar actionBar;
    private Account account;
    public static String RESULT_KEY_LOGIN = "userId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_login);
	actionBar = getActionBar();
	actionBar.setHomeButtonEnabled(true);
	// Check that the activity is using the layout version with
	// the fragment_container FrameLayout
	if (findViewById(R.id.fragment_container_login) != null) {

	    // However, if we're being restored from a previous state,
	    // then we don't need to do anything and should return or else
	    // we could end up with overlapping fragments.
	    if (savedInstanceState != null) {
		return;
	    }

	    // Create a new Fragment to be placed in the activity layout
	    LoginMainFragment firstFragment = new LoginMainFragment();

	    // In case this activity was started with special instructions from
	    // an Intent, pass the Intent's extras to the fragment as arguments
	    firstFragment.setArguments(getIntent().getExtras());

	    // Add the fragment to the 'fragment_container' FrameLayout
	    getSupportFragmentManager().beginTransaction()
		    .add(R.id.fragment_container_login, firstFragment).commit();
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	// getMenuInflater().inflate(R.menu.login, menu);
	return true;
    }

    /* click app logo, go home */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    // app icon in action bar clicked; goto parent activity.
	    this.finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }
    
    @Override
    public void onAccountPass(Long account_id) {
	Intent i = new Intent();
	i.putExtra(RESULT_KEY_LOGIN, account_id);
	setResult(Activity.RESULT_OK, i);
    }
    
    /**
     * ActionBar's title changes based on the fragment
     */
    public void setActionBarTitle(String title) {
	// Log.d("debug", "title is changed to: " + title);
	getActionBar().setTitle(title);
	// Log.d("debug", "get title is" + getActionBar().getTitle());
    }

    /**
     * Replace this fragment with next fragment
     */
    public void replaceFragment(Fragment fragment, int fragmentid) {
	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	transaction.replace(fragmentid, fragment);
	transaction.addToBackStack(null);
	// Commit the transaction
	transaction.commit();
    }


    @Override
    public void onDataPass(String consentText) {
	SignUpLastFragment lastFragment = new SignUpLastFragment();
	Bundle b = new Bundle();
	b.putString(SignUpTwoFragment.CONSENT, consentText);
	lastFragment.setArguments(b);
	replaceFragment(lastFragment, R.id.fragment_container_login);
    }
}

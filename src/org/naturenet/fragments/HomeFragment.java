package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.activities.LoginActivity;
import org.naturenet.adapters.HomeGridAdapter;
import org.naturenet.adapters.ObservationGridAdapter;
import org.naturenet.helper.InitialDownloadTask;
import org.naturenet.model.Account;
import org.naturenet.model.NNModel;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import com.activeandroid.Model;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
    private String[] items  = { "Observations", "Activities", "Design Ideas", "ACES Tour",
	    "Profile", "About"};
    private int[]    imgIds = { R.drawable.camera, R.drawable.activity, R.drawable.bulb,
	    R.drawable.map, R.drawable.profile, R.drawable.about };
    private View     rootView;
    private GridView gridView;
    private Button   btnSign;
    private TextView tvWelcome;
    private ActionBar actionBar;
    
    private Account account;
    public OnPassAccount passer;    
    
    public static final int REQUEST_SELECT_ACCOUNT = 6;
    final public static String TAG = HomeFragment.class.getName();

    public static HomeFragment newInstance() {
	HomeFragment f = new HomeFragment();
	return f;
    }

    public static HomeFragment newInstance(HomeFragmentListener listener) {
	return new HomeFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	account = Session.getAccount();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	actionBar = getActivity().getActionBar();
	setHasOptionsMenu(true);
	actionBar.setTitle(R.string.title_fragment_home);
	actionBar.setDisplayHomeAsUpEnabled(false);
	rootView = inflater.inflate(R.layout.fragment_home, container, false);
	HomeGridAdapter adapter = new HomeGridAdapter(rootView.getContext(), items, imgIds);
	gridView = (GridView) rootView.findViewById(R.id.home_grid);
	gridView.setAdapter(adapter);
	tvWelcome = (TextView) rootView.findViewById(R.id.textView_signin);
	btnSign = (Button) rootView.findViewById(R.id.btn_sign_in);
	
	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Fragment newFragment = null;
		String tag = null;
		if (account == null || !Session.isSignedIn()) {
		    showLogInAlert();
		    return;
		}
		switch (position) {
		case 0:
		    newFragment = ObservationFragment.newInstance(); 
		    tag = ObservationFragment.TAG;
		    break;
		case 1:
		    newFragment = ActivitiesFragment.newInstance();
		    tag = ActivitiesFragment.TAG;
		    break;
		case 2:
		    newFragment = DesignIdeasFragment.newInstance();
		    tag = DesignIdeasFragment.TAG;
		    break;
		case 3:
		    newFragment = TourFragment.newInstance();
		    tag = TourFragment.TAG;
		    break;
		case 4:
		    newFragment = ProfileFragment.newInstance();
		    tag = ProfileFragment.TAG;
		    break;
		case 5:
		    newFragment = AboutFragment.newInstance();
		    tag = AboutFragment.TAG;
		    break;

		}
		((MainActivity) getActivity()).replaceFragment(newFragment,
			 R.id.main_container, tag);
	    }
	});

	if (account == null || !Session.isSignedIn()){ 
	    showSignInBtn();
	} else {  
	    showWelcome();
	}
	setSigninBtn();
	return rootView;
    }
    
    @Override
    public void onResume() {
	super.onResume();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	passer = (OnPassAccount) a;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		actionBar.setDisplayHomeAsUpEnabled(false);
	        return true;
	    case R.id.action_settings:
		return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
    
    /* set the sign in button in this fragment */
    public void setSigninBtn() {
	btnSign.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		 onClickSingIn();
	    }
	});
    }
    
    /* onclick listener for sign in */
    public void onClickSingIn() {
	ConnectivityManager conMan = (ConnectivityManager) getActivity().getSystemService(
		Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = conMan.getActiveNetworkInfo();
	if (netInfo == null || !netInfo.isConnected()) {
	    showNoInternetAlert();
	    return;
	}
	Intent i = new Intent(getActivity(), LoginActivity.class);
	startActivityForResult(i, REQUEST_SELECT_ACCOUNT);
    }
    
    /* after login activity sends account_id back */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == REQUEST_SELECT_ACCOUNT) {
	    if (resultCode == Activity.RESULT_OK && data.getExtras().containsKey(LoginActivity.RESULT_KEY_LOGIN)) {
		Long account_id = data.getExtras().getLong(LoginActivity.RESULT_KEY_LOGIN);
		account = Model.load(Account.class,  account_id);
		checkNotNull(account);
		showWelcome();
		onSignedIn();
	    } else {
		// error has happened !!
	    }
	}
    }
    
    /* show welcome message after a user signs in */
    private void showWelcome() {
	btnSign.setVisibility(View.GONE);
	tvWelcome.setVisibility(View.VISIBLE);
	String userName = account.getUsername();
//	String welcome = "If you're not "+ userName + ", please sign in!";
//	SpannableString content = new SpannableString(welcome);
//	content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
	String welcome = "Welcome, " + userName;
	tvWelcome.setText(welcome);
	((MainActivity) getActivity()).setOptionTitle(R.id.action_settings, MainActivity.SIGNOUT);
    }
    
    /* show signin button after a user logs out */
    public void showSignInBtn() {
	tvWelcome.setVisibility(View.GONE);
	btnSign.setVisibility(View.VISIBLE);
    }
    
    private void onSignedIn(){
	Site site = NNModel.resolveByName(Site.class,  "aces");
	Session.signIn(account, site);
    }
    
    /* is wifi connected */
    public boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
        	.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
        return mWifi.isConnected();
   }
    
    public interface OnPassAccount {
	public void onPassAccountIdListener(long account_id);
    }
    
    private void showNoInternetAlert() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());
	alertDialog.setTitle("Warning");
	alertDialog.setMessage("No Internet Connection, Please Check!");
	alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	    }
	});
	alertDialog.show();
    }
    
    private void showLogInAlert() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());
	// alertDialog.setTitle("Warning");
	alertDialog.setMessage("Please Sign In First!");
	alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	    }
	});
	alertDialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		onClickSingIn();
	    }
	});
	alertDialog.show();
    }
}
package org.naturenet.helper;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.fragments.HomeFragment;
import org.naturenet.model.Account;
import org.naturenet.model.NNModel;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import retrofit.RetrofitError;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class InitialDownloadTask extends AsyncTask<Void, Integer, Boolean> {
    private Activity activity;
    private boolean ready = false;    
    public final String TAG = InitialDownloadTask.class.getName();

    public InitialDownloadTask(Activity activity) {
	this.activity = activity;
    }


    @Override
    protected Boolean doInBackground(Void... params) {
	 Log.d(TAG, "downloading initial data");
	try {
	    if (NNModel.countLocal(Site.class) == 0) {
		NNModel.resolveByName(Site.class, "aces");
//		NNModel.resolveByName(Site.class, "cu");
//		NNModel.resolveByName(Site.class, "umd");
//		NNModel.resolveByName(Site.class, "uncc");
//		account = Session.getAccount();
	    }
	    return isInitialSyncCompleted();
	} catch (RetrofitError e) {
	    return false;
	}
    }

    @Override
    protected void onPreExecute() {
	// activity.setContentView(R.layout.activity_loading);
    }

    @Override
    protected void onPostExecute(Boolean result) {	
	if (result) {
	    RelativeLayout ll = (RelativeLayout) activity.findViewById(R.id.llayout_initapp_loading);
	    ll.setVisibility(View.GONE);
	    ((MainActivity) activity).getSupportFragmentManager().beginTransaction()
		.add(R.id.main_container, HomeFragment.newInstance(), HomeFragment.TAG)
		.addToBackStack(HomeFragment.TAG)
		.commit();
	}
    }

    boolean isInitialSyncCompleted() {
	return NNModel.countLocal(Site.class) == 1;
    }
    
    public boolean isReady() {
	return this.ready;
    }

}

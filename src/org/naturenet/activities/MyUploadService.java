package org.naturenet.activities;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyUploadService extends IntentService {

    public MyUploadService(String name) {
	super("UploadService");
    }
    
    public MyUploadService(){
	super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	// have all the code required to upload the file.
	Log.d("demo", intent.getStringExtra("location"));
	Toast.makeText(this, "Inside Service", Toast.LENGTH_LONG).show();
	
	
	
    }

}

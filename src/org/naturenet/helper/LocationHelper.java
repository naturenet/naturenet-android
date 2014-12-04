package org.naturenet.helper;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class LocationHelper {
    private static final int MAX_LAST_LOCATION_AGE = 30000;
    private static final int LOCATION_FOUND	= 1;
    private static final int LOCATION_NOT_FOUND    = 2;
    private static final int PROVIDER_NOT_ENABLED  = 3;
    private LocationManager  locationManager;
    private Handler	  handler;
    private Runnable	 doAfterTimeExpires;
    private Context	  context;

    public LocationHelper(LocationManager locationManager, Handler handler, Context context) {
	this.locationManager = locationManager;
	this.handler = handler;
	this.context = context;
	this.doAfterTimeExpires = new Runnable() {
	    @Override
	    public void run() {
		endLocationListen(null, LOCATION_NOT_FOUND);
	    }
	};
    }

    public Location getCurrentLocation(int maxWaitSeconds) {
	// getting GPS status
	boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location lastLoc = getLastLocation(isGPSEnabled, isNetworkEnabled);
        
	// Define the criteria how to select the locatioin provider -> use
	// default
        //	Criteria criteria = new Criteria();
        //	String provider = locationManager.getBestProvider(criteria, false);
        //	Location lastLoc = locationManager.getLastKnownLocation(provider);
        
	if (lastLoc != null && lastLoc.getTime() >= (System.currentTimeMillis() - MAX_LAST_LOCATION_AGE)) {
	    endLocationListen(lastLoc, LOCATION_FOUND);
	} else {
	    if (isGPSEnabled) {
		locationManager
		    .requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListner);
	    }
		
	    if (isNetworkEnabled) {
		locationManager
			.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListner);
	    }

	    handler.postDelayed(doAfterTimeExpires, maxWaitSeconds * 1000);
	}
	return lastLoc;
    }

    /* get last known location by GPS or network */
    public Location getLastLocation(boolean isGPSEanabled, boolean isNetworkEnabled) {
	Location gpsLastLocation = null;
	Location networkLastLocation = null;

	if (isGPSEanabled) {
	    gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	if (isNetworkEnabled) {
	    networkLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	if (networkLastLocation != null && gpsLastLocation != null) {
	    if (networkLastLocation.getTime() < gpsLastLocation.getTime()) {
		return gpsLastLocation;
	    } else {
		return networkLastLocation;
	    }
	}
	
	if (gpsLastLocation != null) {
	    return gpsLastLocation;
	}
	
	if (networkLastLocation != null) {
	    return networkLastLocation;
	}
	
	return null;
    }
    
    private LocationListener locationListner = new LocationListener() {
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    if (status == LocationProvider.OUT_OF_SERVICE) {
		endLocationListen(null,	PROVIDER_NOT_ENABLED);
	    }
	}

	public void onProviderEnabled(String provider) {
	    Log.d("mylocation", provider);
	}

	public void onProviderDisabled(String provider) {
	    endLocationListen(null, PROVIDER_NOT_ENABLED);
	}

	public void onLocationChanged(Location location) {
	    if (location != null) {
		endLocationListen(location, LOCATION_FOUND);
	    }
	}
    };
    
    private void endLocationListen(Location location, int errorCode) {
	handler.removeCallbacks(doAfterTimeExpires);
	locationManager.removeUpdates(locationListner);
	switch (errorCode) {
	case PROVIDER_NOT_ENABLED:
	    Toast.makeText(context, "GPS not enabled", Toast.LENGTH_SHORT).show();
	    break;
	case LOCATION_NOT_FOUND:
	    Toast.makeText(context, "Unable to find location", Toast.LENGTH_SHORT).show();
	    break;
	case LOCATION_FOUND:
	    // Toast.makeText(context, location.getLatitude() + "," +
	    // location.getLongitude(),
	    // Toast.LENGTH_SHORT).show();
	    Log.d("mylocation", "location found! " + "lat: " + location.getLatitude() + " long: "
		    + location.getLongitude());
	    break;
	}
    }
    
    /* Function to show settings alert dialog */
    public void showSettingsAlert() {
	Log.d("debug", "in show gps alert");
	  AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setTitle("GPS not enabled").setMessage("Would like to enable the GPS settings")
		    .setCancelable(true)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			    context.startActivity(i);
			}
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    dialog.cancel();
			}
		    });
	    AlertDialog alert = builder.create();
	    alert.show();
    }
}

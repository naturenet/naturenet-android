package org.naturenet.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

public class LocationHelper {
    private static final int MAX_LAST_LOCATION_AGE = 30000;
    private static final int MAX_WAIT_TIME = 20;
    private static final int LOCATION_FOUND	= 1;
    private static final int LOCATION_NOT_FOUND    = 2;
    private static final int PROVIDER_NOT_ENABLED  = 3;
    private LocationManager  locationManager;
    private Handler	  handler;
    private Runnable	 notifyNoLocationFound;
    private Context	  context;
    private ILocationHelper mListener;

    public LocationHelper(LocationManager locationManager, Handler handler, Context context, ILocationHelper mListner) {
	this.locationManager = locationManager;
	this.handler = handler;
	this.context = context;
	this.mListener = mListner;
	this.notifyNoLocationFound = new Runnable() {
	    @Override
	    public void run() {
		mListener.locationNotFound();
	    }
	};
    }

    public void requestCurrentLocation() {
	// getting GPS status
	boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location lastLoc = getLastLocation(isGPSEnabled, isNetworkEnabled);
	if (lastLoc != null && lastLoc.getTime() >= (System.currentTimeMillis() - MAX_LAST_LOCATION_AGE)) {
	    mListener.foundLocation(lastLoc);
	    endLocationListener(lastLoc, LOCATION_FOUND);
	} else {
	    // Toast.makeText(context, "waiting for location", Toast.LENGTH_SHORT).show();
	    if (isGPSEnabled) {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListner);
	    }
	    if (isNetworkEnabled) {
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListner);
	    }
	    // notify no location found after MAX_WAIT_TIME
	    handler.postDelayed(notifyNoLocationFound, MAX_WAIT_TIME * 3000);
	}
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
		endLocationListener(null, PROVIDER_NOT_ENABLED);
	    }
	}

	public void onProviderEnabled(String provider) {
	}

	public void onProviderDisabled(String provider) {
	    endLocationListener(null, PROVIDER_NOT_ENABLED);
	}

	public void onLocationChanged(Location location) {
	    if (location != null) {
		endLocationListener(location, LOCATION_FOUND);
	    }
	}
    };
    
    /* end location listener */
    private void endLocationListener(Location location, int errorCode) {
	handler.removeCallbacks(notifyNoLocationFound);
	locationManager.removeUpdates(locationListner);
	switch (errorCode) {
	case PROVIDER_NOT_ENABLED:
	    Toast.makeText(context, "GPS not enabled", Toast.LENGTH_SHORT).show();
	    break;
	case LOCATION_NOT_FOUND:
	    Toast.makeText(context, "Unable to find location", Toast.LENGTH_SHORT).show();
	    break;
	case LOCATION_FOUND:
	    mListener.foundLocation(location);
	    break;
	}
    }
    
    /* Function to show settings alert dialog */
    public void showSettingsAlert() {
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
    
    /* interface for AddObservationFragment to get currentLocation */
    public interface ILocationHelper{
	public void foundLocation(Location loc);
	public void locationNotFound();
    }
    
}

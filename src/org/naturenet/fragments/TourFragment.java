package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.naturenet.activities.MainActivity;
import org.naturenet.activities.R;
import org.naturenet.adapters.LocationInfoWindowAdapter;
import org.naturenet.model.Location;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class TourFragment extends Fragment {

    private GoogleMap       mMap;
    private LocationManager locationMngr;
    private List<Location>  locations;
    private TextView	tvDescription;
    private ImageView 	ivTakeObs;
    private String selectedMarkerTitle;

    private String selectedMarkerId;
    private OnDataPassListener dataPasser;
    public final static String TAG = TourFragment.class.getName();


    public static TourFragment newInstance() {
	TourFragment f = new TourFragment();
	return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	Log.d("debug", "tour fragment now in onCreateView!");
	View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_tour, container, false);
	ivTakeObs = (ImageView) rootView.findViewById(R.id.cam_in_tour);
	locationMngr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//	tvDescription = (TextView) rootView.findViewById(R.id.locdescription);
//	tvDescription.setMovementMethod(new ScrollingMovementMethod());
	initMapView();
	setUpMapIfNeeded();
	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	getActivity().getActionBar()
	            .setTitle(R.string.title_fragment_tour);
	setHasOptionsMenu(true);
	ivTakeObs.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		// pass maker's id now not name
		dataPasser.onPassLocation(selectedMarkerId);
		((MainActivity) getActivity()).dispatchTakePictureIntent();
	    }
	    
	});
	return rootView;
    }
    
    @Override
    public void onAttach(Activity a) {
	super.onAttach(a);
	dataPasser = (OnDataPassListener) a;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
	Log.d("debug", "tour fragment now in onViewCreated!");
	if (mMap != null) {
	    setUpMap();
	}

	if (mMap == null) {
	    // Try to obtain the map from the SupportMapFragment.
	    mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
		    .findFragmentById(R.id.map)).getMap();
	    // Check if we were successful in obtaining the map.
	    if (mMap != null)
		setUpMap();
	}
    }

    @Override
    public void onDestroyView() {
	Log.d("debug", "tour fragment now in onDestroyView!");
	Fragment mFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
	if (mFragment.isResumed()) {
	    getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
	    mMap = null;
	}

	if (mMap != null) {
	    getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
	    mMap = null;
	}
	super.onDestroyView();
    }

    @Override
    public void onDestroy() {
	Log.d("debug", "tour fragment now in onDestroy of fragment!");
	Fragment mFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
	FragmentTransaction transaction = getActivity().getSupportFragmentManager()
		.beginTransaction();
	if (mMap != null) {
	    transaction.remove(mFragment);
	    transaction.addToBackStack("removeTour");
	    transaction.commit();
	    mMap = null;
	}
	super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_tour);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStack();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
    
    /* Sets up the map if it is possible to do so */
    public void setUpMapIfNeeded() {
	// Do a null check to confirm that we have not already instantiated the
	// map.
	if (mMap == null) {
	    // Try to obtain the map from the SupportMapFragment.
	    mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
		    .findFragmentById(R.id.map)).getMap();
	    // Check if we were successful in obtaining the map.
	    if (mMap != null)
		setUpMap();
	}
    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
    private void setUpMap() {
	mMap.setMyLocationEnabled(true);
	mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	List<MarkerOptions> markerOptions = setMarkerOptionsList();
	// use customized map info windwo layout
	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
		Context.LAYOUT_INFLATER_SERVICE);
	View mymarkerview = inflater.inflate(R.layout.map_snip_infowindow, null);
	LocationInfoWindowAdapter lAadpter = new LocationInfoWindowAdapter(mymarkerview);
	mymarkerview.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		
	    }
	    
	});
	
	mMap.setInfoWindowAdapter(lAadpter);
	
	for (MarkerOptions mo : markerOptions) {
	    mMap.addMarker(mo);
	}

//	LatLngBounds Aces = new LatLngBounds(new LatLng(-44, 113), new LatLng(-10, 154));
//	mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(Aces, 0));
	// For zooming automatically to the Dropped PIN Location
	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.195998, -106.821823),
		17.5f));
	mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
	    @Override
	    public boolean onMarkerClick(Marker arg0) {
		arg0.showInfoWindow();
		selectedMarkerTitle = arg0.getTitle();
		selectedMarkerId = arg0.getId();
		Log.d("debug" , "marker id is : " + selectedMarkerId);
//		setMakerOnClick(arg0);
		return true;
	    }

	});

    }

    /**
     * set up a list of location makers need to be diplayed
     */
    public List<MarkerOptions> setMarkerOptionsList() {
	Site mSite = Session.getSite();
	checkNotNull(mSite);
	final List<org.naturenet.model.Context> landmarks = mSite.getLandmarks();
	String[] locTitles = getContextNames(landmarks);
//	String[] locTitles = getResources().getStringArray(R.array.locations);
	String[] locDescriptions = getResources().getStringArray(R.array.desciptions);
	String[] locLats = getResources().getStringArray(R.array.latitudes);
	String[] locLons = getResources().getStringArray(R.array.longitudes);
	int[] icons = {R.drawable.number1, R.drawable.number2, R.drawable.number3, R.drawable.number4,
		R.drawable.number5, R.drawable.number6, R.drawable.number7, R.drawable.number8, 
		R.drawable.number9, R.drawable.number10, R.drawable.number11,};
	int indexOfLandmark = 0;
	this.locations = new ArrayList<Location>();
	List<MarkerOptions> options = new ArrayList<MarkerOptions>();
	for (String title : locTitles) {
	    if (indexOfLandmark == 11) break;
	    String desciption = locDescriptions[indexOfLandmark];
	    String lat = locLats[indexOfLandmark];
	    String lon = locLons[indexOfLandmark];
	    int icon = icons[indexOfLandmark];
	    Location location = new Location(lat, lon, title, desciption, icon);
	    locations.add(location);
	    options.add(location.toMarkerPosition());
	    indexOfLandmark++;
	}

	return options;
    }

    /**
     * given a location's title, get the description detail
     */
    public String getMarkerDesc(char bc) {
	for (Location loc : this.locations) {
	    char beginChar = loc.getTitle().charAt(0);
	    if (beginChar == bc) {
		return loc.getDescription();
	    }
	}

	return null;
    }

    /**
     * set textview to display description of each location
     */
    public void setMakerOnClick(Marker marker) {
	String title = marker.getTitle();
	char beginChar = title.charAt(0);
	String description = null;
	description = getMarkerDesc(beginChar);
	tvDescription.setText(description);
    }

    public void initMapView() {
	if (!locationMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
	    builder.setTitle("GPS not enabled").setMessage("Would like to enable the GPS settings")
		    .setCancelable(true)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			    startActivity(i);
			}
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    dialog.cancel();
			    getActivity().finish();
			}
		    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
    }
    
    // give a list of contexts, get the name (titles) of the contexts
    private String[] getContextNames(List<org.naturenet.model.Context> contexts) {
	List<String> contextNames = new ArrayList<String>();
	for (org.naturenet.model.Context c : contexts) {
	    contextNames.add(c.getTitle());
	}
	String[] simpleArray = new String[contextNames.size()];
	return contextNames.toArray(simpleArray);
    }

    
    /* public interface for pass data to MainActivity */
    public interface OnDataPassListener{
	public void onPassLocation(String markerId);
    }

}

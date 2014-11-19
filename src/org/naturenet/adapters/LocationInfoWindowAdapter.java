package org.naturenet.adapters;

import org.naturenet.activities.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class LocationInfoWindowAdapter implements InfoWindowAdapter {
    private View    mymarkerview;
    private Context context;
    private String  description;

    public LocationInfoWindowAdapter(View mymarkerview, String description) {
	super();
	this.mymarkerview = mymarkerview;
	this.description = description;
    }

    public LocationInfoWindowAdapter(View mymarkerview) {
   	super();
   	this.mymarkerview = mymarkerview;
    }

    public LocationInfoWindowAdapter() {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	mymarkerview = inflater.inflate(R.layout.map_snip_infowindow, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
	render(marker, mymarkerview);
	return mymarkerview;
    }

    @Override
    public View getInfoWindow(Marker marker) {
	// TODO Auto-generated method stub
	return null;
    }

    private void render(Marker marker, View view) {
	// Add the code to set the required values
	// for each element in your custominfowindow layout file
	TextView info = (TextView) view.findViewById(R.id.map_snip);
	TextView title = (TextView) view.findViewById(R.id.loc_snip_title);
	title.setText(marker.getTitle());
	info.setText(marker.getSnippet());
//	ImageView close = (ImageView) view.findViewById(R.id.snip_close);
//	close.setOnClickListener(new OnClickListener() {
//	    @Override
//	    public void onClick(View arg0) {
//		marker.hideInfoWindow();
//	    }
//	    
//	});
    }

}

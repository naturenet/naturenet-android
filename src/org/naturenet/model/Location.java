package org.naturenet.model;

import org.naturenet.activities.R;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Location {
	private Double latitude;
	private Double longitude;
	private String title;
	private String description;
	private int icon;

	public Location(String lat, String lon, String title, String description, int icon) {
		this.latitude = Double.valueOf(lat);
		this.longitude = Double.valueOf(lon);
		this.title = title;
		this.description = description;
		this.icon = icon;
	}

	public MarkerOptions toMarkerPosition() {
		LatLng latlng = new LatLng(this.latitude, this.longitude);
		MarkerOptions makerOptions = new MarkerOptions()
					.position(latlng)
					.title(this.title)
					.snippet(this.description)
					.icon(BitmapDescriptorFactory.fromResource(this.icon));
		return makerOptions;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return "Location [latitude=" + latitude + ", longitude=" + longitude
				+ ", title=" + title + "]";
	}
	
	
}
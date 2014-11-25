package org.naturenet.adapters;

import java.util.List;

import org.naturenet.activities.R;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * customized adapter for listview in observation edit page
 * 
 * */
public class ActivitiesListViewAdapter extends BaseAdapter {
    private final Context	      context;
    private List<org.naturenet.model.Context> activities;
    private String[]	     titles;
    private int[] counts;
    
    
//    public ActivitiesListViewAdapter(Context context, String[] values,
//	    int[] likes) {
////	super(context, R.layout.listview_activities, values);
//	this.context = context;
//	this.titles = values;
//	this.counts = likes;
//    }
    
    public ActivitiesListViewAdapter(Activity activity, List<org.naturenet.model.Context> activities, int[] counts) {
	this.context = activity.getBaseContext();
	this.activities = activities;
	this.counts = counts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View rowView = inflater.inflate(R.layout.listview_activities, parent, false);
//	TextView tvCounts = (TextView) rowView.findViewById(R.id.textview_activities_count);
//	tvCounts.setText(String.valueOf(counts[position]));
	TextView tvTitle = (TextView) rowView.findViewById(R.id.textview_activity_title);
	String title = activities.get(position).getTitle();
	tvTitle.setText(title);
	String imageLink =  activities.get(position).getLinkExtras();
	ImageView ivActivity = (ImageView) rowView.findViewById(R.id.activityImg_in_listview);
	Picasso.with(context).load(imageLink).resize(150, 150).centerCrop().into(ivActivity);
//	Picasso.with(context).load(imageLink).fit().into(ivActivity);
	return rowView;
    }

    @Override
    public int getCount() {
	// TODO Auto-generated method stub
	return activities.size();
    }

    @Override
    public Object getItem(int arg0) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public long getItemId(int arg0) {
	// TODO Auto-generated method stub
	return 0;
    }
}

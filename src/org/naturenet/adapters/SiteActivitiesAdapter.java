package org.naturenet.adapters;

import java.util.ArrayList;
import java.util.List;

import org.naturenet.model.Context;
import org.naturenet.model.Site;

import com.google.common.collect.Lists;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SiteActivitiesAdapter extends ArrayAdapter<Context> {
    private List<Context> activities;


    public SiteActivitiesAdapter(android.content.Context context, List<Context> activities) {
//	super(context, android.R.layout.simple_list_item_2);
	super(context, android.R.layout.simple_list_item_2, activities);
	this.activities = activities;
	addAll(activities);
    }

    @Override
    // don't override if you don't want the default spinner to be a two line
    // view
    public View getView(int position, View convertView, ViewGroup parent) {
	return initView(position, convertView);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
	return initView(position, convertView);
    }

    private View initView(int position, View convertView) {
	List<String> activityNames = new ArrayList<String> ();
	for (Context c : activities) {
	    activityNames.add(c.getName());
	}
	
	if (convertView == null)
	    convertView = View.inflate(getContext(), android.R.layout.simple_spinner_item, null);

	return convertView;
    }
    
    public int getPositionByName(String name) {
	final List<String> context_names = Lists.newArrayList();
	for (Context c : activities) {
	    context_names.add(c.getName());
	}
	return context_names.indexOf(name);
    }

}

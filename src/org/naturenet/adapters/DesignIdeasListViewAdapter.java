package org.naturenet.adapters;

import java.util.List;

import org.naturenet.activities.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * customized adapter for listview in observation edit page
 * 
 * */
public class DesignIdeasListViewAdapter extends ArrayAdapter<String> {
    private final Context	      context;
    private String[]	     ideas;
    private int[] likes;
    
    
    public DesignIdeasListViewAdapter(Context context, String[] values,
	    int[] likes) {
	super(context, R.layout.listview_design_ideas, values);
	this.context = context;
	this.ideas = values;
	this.likes = likes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View rowView = inflater.inflate(R.layout.listview_design_ideas, parent, false);
	TextView tvLikes = (TextView) rowView.findViewById(R.id.textview_likes);
	tvLikes.setText(String.valueOf(likes[position]));
	TextView tvIdea = (TextView) rowView.findViewById(R.id.textview_design_idea);
	tvIdea.setText(ideas[position]);
	return rowView;
    }
}

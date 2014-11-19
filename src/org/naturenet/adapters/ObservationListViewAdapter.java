package org.naturenet.adapters;

import java.util.List;

import org.naturenet.activities.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * customized adapter for listview in observation edit page
 * 
 * */
public class ObservationListViewAdapter extends ArrayAdapter<String>{
	private final Context context;
	private final String[] values;
	private List<ArrayAdapter<String>> spinAdapters;

	public ObservationListViewAdapter(Context context, String[] values, List<ArrayAdapter<String>> spinAdapters) {
	    super(context, R.layout.listview_edit_obs, values);
	    this.context = context;
	    this.values = values;
	    this.spinAdapters = spinAdapters;
	}

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.listview_edit_obs, parent, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.textView_obs_listview);
	    Spinner spinner = (Spinner) rowView.findViewById(R.id.spinner_obs_listview);
	    textView.setText(values[position]);
	    ArrayAdapter<String> adapter = spinAdapters.get(position);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);	
	    return rowView;
	  }
}

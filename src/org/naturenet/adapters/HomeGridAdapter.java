/*
 * Adapter for grid view in home fragment
 */
package org.naturenet.adapters;

import org.naturenet.activities.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeGridAdapter extends BaseAdapter {
    private Context	mContext;
    private final String[] items;
    private final int[]    imageIds;

    public HomeGridAdapter(Context c, String[] items, int[] Imageid) {
	mContext = c;
	this.imageIds = Imageid;
	this.items = items;
    }

    @Override
    public int getCount() {
	// TODO Auto-generated method stub
	return items.length;
    }

    @Override
    public Object getItem(int position) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public long getItemId(int position) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View grid;
	LayoutInflater inflater = (LayoutInflater) mContext
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	if (convertView == null) {
	    grid = new View(mContext);
	    grid = inflater.inflate(R.layout.home_grid_img, null);
	    TextView textView = (TextView) grid.findViewById(R.id.grid_text);
	    ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
	    textView.setText(items[position]);
	    imageView.setImageResource(imageIds[position]);
	} else {
	    grid = (View) convertView;
	}
	return grid;
    }

}
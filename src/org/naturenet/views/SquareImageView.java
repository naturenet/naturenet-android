package org.naturenet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A subclass view for display observation pictures in square view
 * originially this code is from 
 * "https://github.com/rogcg/gridview-autoresized-images-sample"
 * */
public class SquareImageView extends ImageView {

	public SquareImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// Snap to width
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); 
	}

}

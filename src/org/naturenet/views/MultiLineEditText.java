package org.naturenet.views;

/**
 * subclass of EditText allowing display DONE while typing in multiple line edit text view
 * */
import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class MultiLineEditText extends EditText{

	public MultiLineEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public MultiLineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiLineEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	    InputConnection connection = super.onCreateInputConnection(outAttrs);
	    int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
	    if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
	        // clear the existing action
	        outAttrs.imeOptions ^= imeActions;
	        // set the DONE action
	        outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
	    }
	    if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
	        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
	    }
	    return connection;
	}
}

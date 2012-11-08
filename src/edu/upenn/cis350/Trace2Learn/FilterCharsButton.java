package edu.upenn.cis350.Trace2Learn;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;

/**
 * Button to filter Characters in an activity. The activity must implement filterable.
 * @author cmilner (Charles Milner)
 */
public class FilterCharsButton extends Button {
	final Activity act;
	final Filterable filterAct;
	final DbAdapter dba;
	boolean filtered;

	public FilterCharsButton(Activity activity, DbAdapter dbAdapter) {
		super(activity.getApplicationContext());
		act = activity;
		if (act instanceof Filterable) {
			filterAct = (Filterable) act;
		} else {
			throw new IllegalArgumentException("Activity must implement Filterable!");
		}
		this.dba = dbAdapter;
		setText(R.string.filter);
		filtered = false;
        setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (filtered) {
					filtered = false;
					filterAct.setCharList(dba.getAllCharIdsByOrder());
					setText(act.getString(R.string.filter));
					filterAct.showToast(act.getString(R.string.filter_toast));
				} else {
					filtered = true;
					initiateFilterPopup();
					setText(act.getString(R.string.filter_clear));
				}
			}
        	
        });
	}
	
	/* Create a popup for the user to enter a filter tag */
	private void initiateFilterPopup() {
		Display display = act.getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();
		int height = display.getHeight();  // deprecated
        //We need to get the instance of the LayoutInflater, use the context of this activity
        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Inflate the view from a predefined XML layout
        final View filter_layout = inflater.inflate(R.layout.filter_popup,(ViewGroup) findViewById(R.id.filter_layout));
        filter_layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Button confirmButton = (Button) filter_layout.findViewById(R.id.filter_confirm_button);
        final PopupWindow filterWindow = new PopupWindow(filter_layout, (int)(width * 0.8), 200, true);
        // display the popup in the center
        filterWindow.showAtLocation(act.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String filter = ((TextView) filter_layout.findViewById(R.id.filter_text)).getText().toString().trim();
				if (filter.length() > 0) {
				    List<Long> charIds = dba.getCharsByTag(filter);
				    filterAct.setCharList(charIds);
				}
				filterWindow.dismiss();
			}	
        });
	}
	

}

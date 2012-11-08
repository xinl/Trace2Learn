package edu.upenn.cis350.Trace2Learn;

import java.util.List;

/**
 * Required interface for activity that wants to feature a FilterCharsButton.
 * @author cmilner (Charles Milner)
 *
 */
public interface Filterable {

	/**
	 * Display a list of Characters.
	 * @param charIds the ids for the Characters to be displayed.
	 */
	void setCharList(List<Long> charIds);
	
	/**
	 * Display a toast message in the activity.
	 * @param msg the message to be displayed.
	 */
	void showToast(String msg);
}

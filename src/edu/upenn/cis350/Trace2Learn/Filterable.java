package edu.upenn.cis350.Trace2Learn;


/**
 * Required interface for activity that wants to feature a FilterCharsButton.
 * @author cmilner (Charles Milner)
 *
 */
public interface Filterable {

	/**
	 * Filter the view to show only items matching attr.
	 * @param attr the attribute to be matched..
	 */
	void filterView(String attr);
	
	/**
	 * Display a toast message in the activity.
	 * @param msg the message to be displayed.
	 */
	void showToast(String msg);
}

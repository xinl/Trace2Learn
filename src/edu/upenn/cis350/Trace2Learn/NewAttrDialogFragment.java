package edu.upenn.cis350.Trace2Learn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class NewAttrDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.new_attr_dialog, null);

		AutoCompleteTextView attrKeyTextEdit = (AutoCompleteTextView) (view.findViewById(R.id.attr_key));
		attrKeyTextEdit.setAdapter(((EditTagsActivity) getActivity()).getAttrKeyAutoCompleteAdapter());

		builder.setView(view).setTitle(R.string.new_attr)
		// Add action buttons
				.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mListener.onNewAttrDialogPositiveClick(NewAttrDialogFragment.this);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						NewAttrDialogFragment.this.getDialog().cancel();
					}
				});
		return builder.create();
	}

	/*
	 * The activity that creates an instance of this dialog fragment must 
	 * implement this interface in order to receive event callbacks. 
	 * Each method passes the DialogFragment in case the host needs to query it.
	 */
	public interface NewAttrDialogListener {
		public void onNewAttrDialogPositiveClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	NewAttrDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the NewAttrDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NewAttrDialogListener so we can send events to the host
			mListener = (NewAttrDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement NewAttrDialogListener");
		}
	}
}
package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CollectionListAdapter extends ArrayAdapter<Collection> {

	private ArrayList<Collection> _items;
	
	private LayoutInflater _vi;
	
	public CollectionListAdapter( Context context, List<Collection> objects, LayoutInflater vi) 
	{
		super(context, 0, objects);
		_items = new ArrayList<Collection>(objects);
		_vi = vi;
	}
	
	/**
	 * Configures the view for the given item in the list
	 * @param position - the index of the item in the list
	 * @param convertView - the constructed view that should be modified
	 * @param parent - The contained of the list
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		View v = convertView;
		if (v == null) {
			v = _vi.inflate(R.layout.main_menu, null);
		}
		Collection item = _items.get(position);
		TextView text = (TextView)v.findViewById(R.id.main_text);
		text.setText(item.getName() + " (" + item.size() + " words)");
		
		return v;
	}
	
}

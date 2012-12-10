package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem;

public class TraceableListAdapter extends ArrayAdapter<TraceableItem> {

	private List<TraceableItem> items;
	
	private LayoutInflater vi;
	
	public TraceableListAdapter(Context context,
			List<TraceableItem> objects,LayoutInflater vi) {
		super(context, 0, objects);
		this.items = new ArrayList<TraceableItem>(objects);
		this.vi = vi;
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
			v = vi.inflate(R.layout.traceable_list_item, null);
		}
		TraceableItem item = items.get(position);
		ImageView image = (ImageView)v.findViewById(R.id.li_image);
		TextView text = (TextView)v.findViewById(R.id.li_description);
		TextView text2 = (TextView)v.findViewById(R.id.li_description2);
		Bitmap bitmap = BitmapFactory.buildBitmap(item, 64);
		image.setImageBitmap(bitmap);
		
		StringBuilder sb = new StringBuilder();
		Map<String, Set<String>> attributes = item.getAttributes();
		for (String key: attributes.keySet()) {
			sb.append(key + ":");
			Set<String> values = attributes.get(key);
			for(String value: values) {
				sb.append(" " + value + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("   ");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		text.setText(sb.toString());
		
		Set<String> tags = item.getTags();
		sb = new StringBuilder();
		for(String tag : tags){
			Log.e("Tag","Found");
			sb.append(", "+ tag);
		}
		String s = "";
		if(sb.length()>0){
			s = sb.substring(2);
			Log.e("Printing Tags",s);
		}
		text2.setText(s);
		return v;
	}
	
}
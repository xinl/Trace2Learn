package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class BrowseCollectionsActivity extends ListActivity {

	private DbAdapter dba; 
	private ArrayList<Collection> items;
	
	final Context c = this;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_collections);
        dba = new DbAdapter(this);
        dba.open(); //opening the connection to database        
        
        items = new ArrayList<Collection>(dba.getAllCollections()); //items to show in ListView to choose from 
        
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CollectionListAdapter la = new CollectionListAdapter(this,items,vi);
        setListAdapter(la);
        registerForContextMenu(getListView());
        setTitle(getTitle() + " » All Collections");
    }

	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {  
	  super.onListItemClick(l, v, position, id);  

	  clickOnItem(position);
	} 

	//when character is clicked, it starts the display mode for that char
	public void clickOnItem(int position){
		Collection le = ((Collection)items.get(position));
		Intent i = new Intent(this, BrowseWordsActivity.class);
		i.putExtra("ID", le.getId());
//		i.putExtra("lessonIndex", position + 1);
//		i.putExtra("lessonTotal", items.size());
		i.putExtra("fromCollection", true);
		startActivity(i);
		finish();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    menu.setHeaderTitle("Options");
	    String[] menuItems = {"Delete", "Export"};
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  Collection collection = (Collection)items.get(info.position);
	  // Log.e("MenuIndex",Integer.toString(menuItemIndex));
	  // Log.e("ListIndex",Integer.toString(info.position));

	  //delete lesson
	  if(menuItemIndex == 0){
		  boolean result = dba.deleteCollection(collection);
		  Log.d("Result", "" + result);
		  if(result == false){
			  showToast("Could not delete the lesson");
			  return false;
		  }
		  else{
			  showToast("Successfully deleted");
			  startActivity(getIntent()); 
			  finish();
			  return true;
		  }
	  } else if(menuItemIndex == 1){
			Intent i = new Intent(this, ExportActivity.class);
			i.putExtra("ID", collection.getId());
			startActivity(i);
	  }
	  return false;
	}
	
	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}

	public void showToast(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

}
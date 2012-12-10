package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem;

public class BrowseCharactersActivity extends ListActivity implements Filterable {
	private DbAdapter dba;
	private ArrayList<TraceableItem> items;
	
	//initialized list of all characters
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_chars);
        
        dba = new DbAdapter(this);
        dba.open();
        
        setCharList(dba.getAllCharacters());
        registerForContextMenu(getListView());
        
        setTitle(getTitle() + " » All Characters");
        
        Button b = new FilterButton(this);
        LinearLayout layout = (LinearLayout) findViewById(R.id.button_panel);
        layout.addView(b);
	}
	
	public void setCharList(List<Character> characters) {
		items = new ArrayList<TraceableItem>();
		for(Character c: characters) {
			items.add(c);
		}
		resetListView();
	}
	
	public void resetListView() {
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setListAdapter(new TraceableListAdapter(this, items, vi));
	}
	
	@Override
	public void filterView(String attr) {
		List<Character> filteredList = dba.getCharactersByAttribute(attr);
		setCharList(filteredList);
	}
	
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {  
	  super.onListItemClick(l, v, position, id);  
	  clickOnItem(items.get(position));
	}  

	//when character is clicked, it starts the display mode for that char
	public void clickOnItem(TraceableItem li){
		Intent intent = new Intent();
		Bundle bun = new Bundle();

		bun.putString("mode", "display");
		bun.putLong("charId", li.getId());

		intent.setClass(this, ViewCharacterActivity.class);
		intent.putExtras(bun);
		intent.putExtra("ISCREATE", false);
		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    menu.setHeaderTitle("Options");
	    String[] menuItems = {"Edit Tags","Move Up","Move Down", "Delete"};
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		Character lc = (Character)items.get(info.position);
		Log.e("MenuIndex",Integer.toString(menuItemIndex));
		Log.e("ListIndex",Integer.toString(info.position));

		//add tags
		if(menuItemIndex==0){
			Intent i = new Intent(this, EditTagsActivity.class);
			i.putExtra("ID", lc.getId());
			i.putExtra("TYPE", "CHARACTER");
			i.putExtra("FROM", true);//Qin
			startActivity(i);
			finish();
			return true;
		}
		if(menuItemIndex==1){
			if(info.position == 0) {
				showToast("Character can't be moved up");
				return false;
			}
			else {
				Character above = (Character)items.get(info.position - 1);
				swapPositions(lc, above, info.position, info.position - 1);

				showToast("Successfully moved up");
				return true;
			}
		}
		if(menuItemIndex==2){
			if(info.position == items.size() - 1){
				showToast("Character can't be moved down");
				return false;
			}
			else {
				Character above = (Character)items.get(info.position + 1);
				swapPositions(lc, above, info.position, info.position + 1);
				showToast("Successfully moved down");
				return true;
			}

		}
	  //delete
	  else if(menuItemIndex==3) {
		  if (dba.deleteCharacter(lc)) {
			  showToast("Successfully deleted");
			  items.remove(info.position);
			  resetListView();
			  return true;
		  } else {
			  showToast("Cannot delete character that is used in a word.");
			  return false;
		  }
	  }
	  return false;
	}
	
	private void swapPositions(Character c1, Character c2, int position1, int position2) {
		  long temp = c2.getOrder();
	      c2.setOrder(c1.getOrder());
	      c1.setOrder(temp);
	      dba.updateCharacter(c1);
	      dba.updateCharacter(c2);
	      
	      items.set(position1, c2);
	      items.set(position2, c1);
	      
	      resetListView();
	}
	
    @Override
	public void showToast(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
	
}

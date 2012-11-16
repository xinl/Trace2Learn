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
        
        setCharList(dba.getAllCharIdsByOrder());
        registerForContextMenu(getListView());
        
        Button b = new FilterCharsButton(this, dba);
        LinearLayout layout = (LinearLayout) findViewById(R.id.button_panel);
        layout.addView(b);
	}
	
	@Override
	public void setCharList(List<Long> charIds) {
		items = new ArrayList<TraceableItem>();
        for(long id : charIds){
        	TraceableItem character = dba.getCharacter(id);
        	character.setTags(dba.getCharacterTags(id));
        	items.add(character);
        }
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setListAdapter(new TraceableListAdapter(this, items, vi));
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

		intent.setClass(this, CharacterCreationActivity.class);
		intent.putExtras(bun);
		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    menu.setHeaderTitle("Options");
	    String[] menuItems = {"Edit Tags","Move Up","Move Down", "Delete"};//Qin
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
		  Intent i = new Intent(this, TagActivity.class);
		  i.putExtra("ID", lc.getId());
		  i.putExtra("TYPE", "CHARACTER");
		  startActivity(i);
		  finish();
		  return true;
	  }
	//Qin
	  if(menuItemIndex==1){
	      long id = lc.getId();
	      long result = dba.moveupCharacter(id);//swap two rows' order
	      Log.e("Result" , Long.toString(result));
	      if(result<0){
		  showToast("Character can't be moved up");
		  return false;
	      }
	      else{
		  showToast("Successfully moved up");
		  startActivity(getIntent());
		  finish();
		  return true;
	      }
	  }
	  if(menuItemIndex==2){
	      long id = lc.getId();
	      long result = dba.movedownCharacter(id);//swap two rows' order
	      Log.e("Result" , Long.toString(result));
	      if(result<0){
		  showToast("Character can't be moved down");
		  return false;
	      }
	      else{
		  showToast("Successfully moved down");
		  startActivity(getIntent());
		  finish();
		  return true;
	      }
	  }
	  //delete
	  else if(menuItemIndex==3){
		  long id = lc.getId();
		  long result = dba.deleteCharacter(id);
		  Log.e("Result",Long.toString(result));
		  if(result<0){
			  showToast("Character is used by a phrase: cannot delete");
			  return false;
		  }
		  else{
			  showToast("Successfully deleted");
			  startActivity(getIntent()); 
			  finish();
			  return true;
		  }
	  }
	  return false;
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

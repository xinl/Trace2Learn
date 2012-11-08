package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class BrowseCharactersActivity extends ListActivity {
	private DbAdapter dba;
	private ArrayList<LessonItem> items;
	private boolean filtered;
	
	//initialized list of all characters
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_chars);
        dba = new DbAdapter(this);
        dba.open();
        
        setCharList(dba.getAllCharIdsByOrder());
        registerForContextMenu(getListView());
        
        filtered = false;
	}
	
	private void setCharList(List<Long> charIds) {
		items = new ArrayList<LessonItem>();
        for(long id : charIds){
        	LessonItem character = dba.getCharacterById(id);
        	character.setTagList(dba.getCharacterTags(id));
        	items.add(character);
        }
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setListAdapter(new LessonItemListAdapter(this, items, vi));
	}
	
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {  
	  super.onListItemClick(l, v, position, id);  
	  clickOnItem(items.get(position));
	}  

	//when character is clicked, it starts the display mode for that char
	public void clickOnItem(LessonItem li){
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
	  LessonCharacter lc = (LessonCharacter)items.get(info.position);
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
	
	    //filters the chars based on user input
		public void onFilterCharsClick(View view) {
			Button filterButton = (Button) findViewById(R.id.filter_button);
			if (filtered) {
				filtered = false;
				setCharList(dba.getAllCharIdsByOrder());
				filterButton.setText(getString(R.string.filter));
				showToast(getString(R.string.filter_toast));
			} else {
				filtered = true;
				initiateFilterPopup();
				filterButton.setText(getString(R.string.filter_clear));
			}
		}
		
		private void initiateFilterPopup() {
			Display display = getWindowManager().getDefaultDisplay(); 
			int width = display.getWidth();
			int height = display.getHeight();  // deprecated
	        //We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        final View filter_layout = inflater.inflate(R.layout.filter_popup,(ViewGroup) findViewById(R.id.filter_layout));
	        filter_layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	        Button confirmButton = (Button) filter_layout.findViewById(R.id.filter_confirm_button);
	        final PopupWindow filterWindow = new PopupWindow(filter_layout, (int)(width * 0.8), (int)(height * 0.2), true);
	        // display the popup in the center
	        filterWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
	        confirmButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String filter = ((TextView) filter_layout.findViewById(R.id.filter_text)).getText().toString().trim();
					if (filter.length() > 0) {
					    List<Long> charIds = dba.getCharsByTag(filter);
					    setCharList(charIds);
					}
					filterWindow.dismiss();
				}	
	        });
		}
	
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

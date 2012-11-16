package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Lesson;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
import edu.upenn.cis350.Trace2Learn.Database.LessonWord;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseWordsActivity extends ListActivity {
	private DbAdapter dba; 
	private ListView lessonList; //list of words to display in listview
	private ArrayList<LessonItem> items;
	private View layout;
	private PopupWindow window;
	private LessonWord lw;
	private long id;
	private boolean fromCollection; 
	private String collectionName = "";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_lesson);
        dba = new DbAdapter(this);
        dba.open();
        
        //Set up the ListView
        items = new ArrayList<LessonItem>(); //items to show in ListView to choose from 
        id = this.getIntent().getLongExtra("ID", -1);
        fromCollection = this.getIntent().getBooleanExtra("fromCollection", false);
        
        //id=1;
        if(id==-1){
        
	        List<Long> ids = dba.getAllWordIdsByOrder();//Qin
	        for(long id : ids){
	        	LessonItem word = dba.getWordById(id);
	        	word.setTagList(dba.getWordTags(id));
	        	items.add(word);
	        }
        }
        else{
        	Lesson les = dba.getLessonById(id);
            collectionName = les.getLessonName();
    		
    		items = new ArrayList<LessonItem>();
    		List<Long> ids = dba.getWordsFromLessonId(id);
			for (long id : ids){
				LessonItem word = dba.getWordById(id);
			    word.setTagList(dba.getWordTags(id));
			    items.add(word);
			}

            TextView title = (TextView)findViewById(R.id.instructions);
    		title.setText("Browsing " + collectionName + " (" + ids.size() + " words)");
        }
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setListAdapter(new LessonItemListAdapter(this, items, vi));

        registerForContextMenu(getListView());
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
		bun.putLong("wordId", li.getId());
		if (id != -1) {
			bun.putLong("collectionId", id);
			bun.putString("collectionName", collectionName);
		}

		intent.setClass(this, PhrasePracticeActivity.class);
		intent.putExtras(bun);
		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		if(fromCollection) return;
	    menu.setHeaderTitle("Options");
	    String[] menuItems = {"Add to Collection","Edit Tags","Move Up", "Move Down", "Delete"};
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  lw = (LessonWord)items.get(info.position);
	  Log.e("MenuIndex",Integer.toString(menuItemIndex));
	  Log.e("ListIndex",Integer.toString(info.position));
	  
	  //add to collection
	  if(menuItemIndex==0){
		  initiatePopupWindow();
		  return true;
	  }
	  
	  else if(menuItemIndex==1){
		  Intent i = new Intent(this, TagActivity.class);
		  i.putExtra("ID", lw.getId());
		  i.putExtra("TYPE", "WORD");
		  startActivity(i);
		  finish(); 
		  return true;
	  }
	  //Qin move up
	  else if(menuItemIndex==2){
	      long id = lw.getId();
	      long result = dba.moveupWord(id);//swap two rows' order
	      Log.e("Result" , Long.toString(result));
	      if(result<0){
		  showToast("Word can't be moved up");
		  return false;
	      }
	      else{
		  showToast("Successfully moved up");
		  startActivity(getIntent());
		  finish();
		  return true;
	      }
	  }
	  //Qin move down
	  else if(menuItemIndex==3){
	      long id = lw.getId();
	      long result = dba.movedownWord(id);//swap two rows' order
	      Log.e("Result" , Long.toString(result));
	      if(result<0){
		  showToast("Word can't be moved down");
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
	  else if(menuItemIndex==4){
		  long id = lw.getId();
		  long result = dba.deleteWord(id);
		  Log.e("Result",Long.toString(result));
		  if(result<0){
			  showToast("Could not delete the word");
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
	
	public void showToast(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	private void initiatePopupWindow(){
		try {
			Display display = getWindowManager().getDefaultDisplay(); 
			int width = display.getWidth();
			int height = display.getHeight();  // deprecated
	        //We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        layout = inflater.inflate(R.layout.add_to_collection_popup,(ViewGroup) findViewById(R.id.popup_layout));
	        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	        // create a 300px width and 470px height PopupWindow
	        List<String> allLessons = dba.getAllLessonNames();
	        Log.e("numLessons",Integer.toString(allLessons.size()));
	        lessonList = (ListView)layout.findViewById(R.id.collectionlist);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,allLessons); 
	        lessonList.setAdapter(adapter);
	        window = new PopupWindow(layout, (int)(width * 0.8), (int)(height * 0.8), true);
	        
	        // display the popup in the center
	        window.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
	
	        lessonList.setOnItemClickListener(new OnItemClickListener() {
	            
	            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {     
	               String name = ((String)lessonList.getItemAtPosition(position));
	               Log.e("name",name);
	               long success = dba.addWordToLesson(name, lw.getId());
	               Log.e("adding word",Long.toString(success));
	               showToast("Successfully Added");
	               window.dismiss();
	            }
	        });
	        
	 
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void onSkipButtonClick(View view){
		window.dismiss();
	}
	
	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
	
	public void onNewCollectionButtonClick(View view){
		EditText editText = (EditText)layout.findViewById(R.id.newcollection);
		Editable edit = editText.getText();
		String name = edit.toString();
		if(name.equals("")){
			showToast("You must name the lesson!");
			return;
		}
		Lesson lesson = new Lesson();
		lesson.setPrivateTag(name);
		lesson.addWord(lw.getId());
		dba.addLesson(lesson);
		showToast("Successfully Created");
		window.dismiss();
	}
}

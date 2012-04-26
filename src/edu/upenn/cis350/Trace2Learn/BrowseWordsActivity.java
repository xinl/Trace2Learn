package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Lesson;
import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
import edu.upenn.cis350.Trace2Learn.Database.LessonWord;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseWordsActivity extends ListActivity {
	private DbAdapter dba; 
	private ListView list, lessonList; //list of words to display in listview
	private Gallery gallery; 
	private ImageAdapter imgAdapter;
	private Lesson newLesson; 
	private ArrayList<Bitmap> currentWords;
	private int numWords;
	private ArrayList<LessonItem> items;
	private View layout;
	private PopupWindow window;
	private LessonWord lw;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numWords = 0;
        currentWords = new ArrayList<Bitmap>();
        setContentView(R.layout.create_lesson);
        dba = new DbAdapter(this);
        dba.open();
        
        //imgAdapter = new ImageAdapter(this,currentWords);
        //gallery = (Gallery)findViewById(R.id.gallery);

        //gallery.setSpacing(0);
        
        //gallery.setAdapter(imgAdapter);
    	
        //list = (ListView)findViewById(R.id.wordlist);
     
        newLesson = new Lesson();
        
        //Set up the ListView
        items = new ArrayList<LessonItem>(); //items to show in ListView to choose from 
        List<Long> ids = dba.getAllWordIds();
        for(long id : ids){
        	LessonItem word = dba.getWordById(id);
        	word.setTagList(dba.getTags(id));
        	items.add(word);
        }
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setListAdapter(new LessonItemListAdapter(this, items, vi));

        /*list.setOnItemClickListener(new OnItemClickListener() {    
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {     
            	numWords++;
                Log.e("Position",Long.toString(position));
                Log.e("Type",list.getItemAtPosition(position).getClass().getName());
                long wordId = ((LessonWord)list.getItemAtPosition(position)).getId();
                Log.e("Id",Long.toString(wordId));
                newLesson.addWord(wordId);
                LessonItem item = (LessonWord)list.getItemAtPosition(position);
                Bitmap bitmap = BitmapFactory.buildBitmap(item, 64, 64);
                currentWords.add(bitmap);
                imgAdapter.update(currentWords);
                imgAdapter.notifyDataSetChanged();

                gallery.setSelection(numWords/2);
            }
        });*/
        registerForContextMenu(getListView());
    }
	
	/*public void onSaveLessonButtonClick(View view){
		if(dba.addLesson(newLesson)){
			TextView word = (TextView)findViewById(R.id.words);
			word.setText("Successfully added!");
		}
	}
	
	public void onAddTagButtonClick(View view){
		Intent i = new Intent(this, TagActivity.class);
		i.putExtra("ID", newLesson.getId());
		i.putExtra("TYPE", newLesson.getItemType().toString());
		startActivity(i);
	}*/
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    menu.setHeaderTitle("Options");
	    String[] menuItems = {"Add to Collection","Delete"};
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
	  
	  //delete
	  else if(menuItemIndex==1){
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
			int width = display.getWidth();  // deprecated
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
	        window = new PopupWindow(layout, layout.getMeasuredWidth(), (int)(height*.8), true);
	        // display the popup in the center
	        window.showAtLocation(layout, Gravity.CENTER, 0, 0);
	
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
	        
	        
	        /*mResultText = (TextView) layout.findViewById(R.id.server_status_text);
	        Button cancelButton = (Button) layout.findViewById(R.id.end_data_send_button);
	        makeBlack(cancelButton);
	        cancelButton.setOnClickListener(cancel_button_click_listener);*/
	 
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void onSkipButtonClick(View view){
		window.dismiss();
	}
	
	public void onNewCollectionButtonClick(View view){
		EditText editText = (EditText)layout.findViewById(R.id.newcollection);
		Editable edit = editText.getText();
		String name = edit.toString();
		if(name.equals("")){
			showToast("You must name the collection!");
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

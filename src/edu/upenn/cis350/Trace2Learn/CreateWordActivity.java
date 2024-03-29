package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class CreateWordActivity extends Activity implements Filterable {
	
	private DbAdapter dba;
	private Word newWord;
	private ListView list, collectionList;
	private ArrayList<Bitmap> currentChars;
	private Gallery gallery;
	private ThumbnailAdapter imgAdapter;
	private int numChars;
	private PopupWindow window;
	private View layout;
	private boolean saved;
	
	//initializes the list of all characters in the database
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved = false;
        numChars = 0;
        newWord = new Word();
        currentChars = new ArrayList<Bitmap>();
        setContentView(R.layout.create_word);
        dba = new DbAdapter(this);
        dba.open();
        
        imgAdapter = new ThumbnailAdapter(this,currentChars);
        gallery = (Gallery)findViewById(R.id.gallery);
        gallery.setSpacing(0);
        
        gallery.setAdapter(imgAdapter);
        
        list = (ListView)findViewById(R.id.charslist);
        setCharList(dba.getAllCharacters());

        //when a char is clicked, it is added to the new word and added to the gallery
        list.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {     
            if(!saved){	
        	numChars++;
                Character c = (Character)list.getItemAtPosition(position);
                newWord.addCharacter(c);
                Bitmap bitmap = BitmapFactory.buildBitmap(c, 64, 64);
                currentChars.add(bitmap);
                imgAdapter.update(currentChars);
                imgAdapter.notifyDataSetChanged();
                gallery.setSelection(numChars/2);
            }
            else{
        	showToast("Please press Clear to create a new word");
            }
            }
        });
        
        Button b = new FilterButton(this);
        LinearLayout layout = (LinearLayout) findViewById(R.id.button_panel);
        layout.addView(b);
        
        setTitle(getTitle() + " » Create Word");
        
    }

	public void setCharList(List<Character> characters) {
		ArrayList<TraceableItem> items = new ArrayList<TraceableItem>();
		for(Character c: characters) {
			items.add(c);
		}
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        list.setAdapter(new TraceableListAdapter(this, items, vi));
	}
	
	@Override
	public void filterView(String attr) {
		setCharList(dba.getCharactersByAttribute(attr));
	}
	
	private void initiateCollectionPopupWindow(){
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
	        List<Collection> allCollections = dba.getAllCollections();
	        
	        collectionList = (ListView)layout.findViewById(R.id.collectionlist);
	        CollectionListAdapter adapter = new CollectionListAdapter( 
	        		this, allCollections, inflater); 
	        collectionList.setAdapter(adapter);
	        window = new PopupWindow(layout, (int)(width * 0.8), (int)(height*.8), true);
	        // display the popup in the center
	        window.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
	
	        collectionList.setOnItemClickListener(new OnItemClickListener() {
	            
	            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {     
	               Collection coll = (Collection)collectionList.getItemAtPosition(position);
	               coll.addWord(newWord);
	               dba.updateCollection(coll);
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
	
	public void onNewCollectionButtonClick(View view){
		EditText editText = (EditText)layout.findViewById(R.id.newcollection);
		Editable edit = editText.getText();
		String name = edit.toString();
		if(name.equals("")){
			showToast("You must name the collection!");
			return;
		}
		List<Collection> allCollections = dba.getAllCollections();
		for(Collection existedcoll: allCollections){
		    if(name.equals(existedcoll.getName())){
			showToast("A collection with that name already exists.");
			return;
		    }
		}
		Collection coll = new Collection();
		coll.setName(name);
		coll.addWord(newWord);
		dba.addCollection(coll);
		window.dismiss();
	}
	
	//adds the new word to the database
	public void onSaveWordButtonClick(View view){
	    if(!saved){
	    	if(newWord.size() > 0 && dba.addWord(newWord)){
	    		saved = true;
	    		//TextView word = (TextView)findViewById(R.id.characters);
	    		showToast("Successfully saved!");
	    		initiateCollectionPopupWindow();
	    		return;
	    	}
	    	showToast("Word is empty");
	    	//return to home screen
	    }
	    else{
	    	showToast("Word already saved.");
	    }
	}
	
	//brings the user to the tag screen
	public void onEditTagButtonClick(View view){
		if(!saved){
			showToast("Please save the word before adding tags");
			return;
		}
		Intent i = new Intent(this, EditTagsActivity.class);
		i.putExtra("ID", newWord.getId());
		i.putExtra("TYPE", TraceableItem.ItemType.WORD.toString());
		i.putExtra("FROM", false);
		startActivity(i);
	}
	
	public void onClearWordButtonClick(View view){
	        saved = false;
	        numChars = 0;
	        newWord = new Word();
	        currentChars = new ArrayList<Bitmap>();
	        
	        imgAdapter = new ThumbnailAdapter(this,currentChars);
	        gallery = (Gallery)findViewById(R.id.gallery);
	        gallery.setSpacing(0);	        
	        gallery.setAdapter(imgAdapter);
	}
	
	
	//for testing purposes
	public Word getWord(){
		return newWord;
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
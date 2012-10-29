package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
import edu.upenn.cis350.Trace2Learn.Database.LessonWord;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class SearchActivity extends ListActivity {

	private DbAdapter mDbHelper;
	private boolean showingChars;
	ArrayList<LessonItem> items;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_tag_db);
        //setContentView(R.layout.tag); //Isabel
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
    }
	
	@Override
	public void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}
	
	/*public void onSubmitButtonClick(View view){
		EditText charEt = (EditText)findViewById(R.id.character);
		String charText = charEt.getText().toString();
		long charId = Long.valueOf(charText);
		
		EditText tagEt = (EditText)findViewById(R.id.tag);
		String tagText = tagEt.getText().toString();
		
		mDbHelper.createTags(charId,tagText);
	}*/
	
	private void setCharList(List<Long> ids)
	{
		items = new ArrayList<LessonItem>();
		for(long id : ids)
		{
			Log.i("Found", "id:"+id);
			// TODO add in code for loading LessonWord
			LessonItem character;
			try
			{
				character = mDbHelper.getCharacterById(id);
			}
			catch(Exception e)
			{
				character = new LessonCharacter(id);
				Log.d("SEARCH", "Character " + id + " not found in db");
			}
			character.setTagList(mDbHelper.getCharacterTags(id));
			items.add(character);
		}
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new LessonItemListAdapter(this, items, vi));
	}
	
	private void setWordList(List<Long> ids)
	{
		items = new ArrayList<LessonItem>();
		for(long id : ids)
		{
			Log.i("Found", "Word id: "+id);
			// TODO add in code for loading LessonWord
			LessonWord word = this.mDbHelper.getWordById(id);
			items.add(word);
		}
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new LessonItemListAdapter(this, items, vi));
	}
	
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {  
	  super.onListItemClick(l, v, position, id);  
	  if(this.showingChars)
		  clickOnChar(items.get(position));
	  else
		  clickOnWord(items.get(position));
	}  
	
	public void clickOnChar(LessonItem li){
		Intent intent = new Intent();
		Bundle bun = new Bundle();

		bun.putString("mode", "display");
		bun.putLong("charId", li.getId());

		intent.setClass(this, CharacterCreationActivity.class);
		intent.putExtras(bun);
		startActivity(intent);
	}

	public void clickOnWord(LessonItem li){
		//TODO: implement 
	}

	/**
	 * Displays a list of chars associated with the tag entered by user.
	 */
	public void onCharSearchButtonClick(View view){
		EditText charEt = (EditText)findViewById(R.id.search_char);
		String searchText = charEt.getText().toString().trim();
		
		List<Long> ids = mDbHelper.getCharsByTag(searchText);
		if (ids.size() == 0) {
			Log.d(ACTIVITY_SERVICE, "zeroRows");
		}
		
		setCharList(ids);
		showingChars=true;
	}
	
	/*public void onWordSearchButtonClick(View view){
		EditText tagEt = (EditText)findViewById(R.id.search_tag);
		String tagText = tagEt.getText().toString();
		
		Cursor c = mDbHelper.getWords(tagText);
		List<Long> ids = new LinkedList<Long>();
		do{
			if(c.getCount()==0){
				Log.d(ACTIVITY_SERVICE, "zeroRows");
				//builder.append("No results");
				break;
			}
			ids.add(c.getLong(c.getColumnIndexOrThrow(DbAdapter.WORDTAG_ROWID)));
			//builder.append(c.getString(c.getColumnIndexOrThrow(DbAdapter.CHARTAG_ROWID))+"\n");			
		}
		while(c.moveToNext());
		setWordList(ids);
		showingChars=false;
		
	}*/
	
	public void onLessonSearchButtonClick(View view){
		// TODO: Add in Lesson search support
		/*EditText tagEt = (EditText)findViewById(R.id.search_lesson_tag);
		String tagText = tagEt.getText().toString();
		
		Cursor c = mDbHelper.getLessons(tagText);
		List<Long> ids = new LinkedList<Long>();
		do{
			if(c.getCount()==0){
				Log.d(ACTIVITY_SERVICE, "zeroRows");
				//builder.append("No results");
				break;
			}
			ids.add(c.getLong(c.getColumnIndexOrThrow(DbAdapter.WORDTAG_ROWID)));
			//builder.append(c.getString(c.getColumnIndexOrThrow(DbAdapter.CHARTAG_ROWID))+"\n");			
		}
		while(c.moveToNext());
		setWordList(ids);
		showingChars=false;*/
		
	}
	
}
package edu.upenn.cis350.Trace2Learn;

import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
import edu.upenn.cis350.Trace2Learn.Database.LessonItem.ItemType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TagActivity extends Activity {

	private final String PRIVATE_PREFIX = "Private: ";
	
	//Should be able to take BOTH character and word
	
	private DbAdapter mDbHelper;
	
	//Controls
	private EditText editText;
	private EditText editPrivateText;
	private ListView lv;
	private Button addTagButton;
	
	//Variables
	private long id;
	private List<String> currentTags;
	
	ItemType type;
	ArrayAdapter<String> arrAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tag); //tag.xml

        editText = (EditText) findViewById(R.id.edittext);
        editPrivateText = (EditText) findViewById(R.id.editprivate);
        lv = (ListView) findViewById(R.id.list);
        addTagButton = (Button) findViewById(R.id.add_tag_button);
        
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        //Grab the intent/extras. This should be called from CharacterCreation
        id = this.getIntent().getLongExtra("ID", -1); 
        type = ItemType.valueOf(getIntent().getStringExtra("TYPE"));
        
        Log.e("ID",Long.toString(id));
        Log.e("TYPE",type.toString());
        
        switch(type)
        {
        case CHARACTER:
        	currentTags = mDbHelper.getCharacterTags(id);
        	break;
        case WORD:
        	currentTags = mDbHelper.getWordTags(id);
        	break;
        case LESSON:
        	currentTags = mDbHelper.getLessonTags(id);
        	break;
        default:
    		Log.e("Tag", "Unsupported Type");
        }

        //add private tag
        currentTags.add(0, PRIVATE_PREFIX+mDbHelper.getPrivateTag(id, type));
        
        //Populate the ListView
        arrAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, currentTags);
        arrAdapter.notifyDataSetChanged();
       
        lv.setAdapter(arrAdapter);
        
        /*final String [] items = new String[]{"boo","hey","wga","Item4"};
        
        ArrayAdapter ad = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,items);
        lv.setAdapter(ad);*/
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lv.setOnItemClickListener(new OnItemClickListener(){

        	@Override
        	public void onItemClick(AdapterView<?> adapter, View view, int position,
        			long this_id) {
        		String privateTag = mDbHelper.getPrivateTag(id, type);
        		String thisPrivateTag = "Private: " + privateTag;
        		String tag = (String) adapter.getItemAtPosition(position);
        		if(!tag.equals(thisPrivateTag)) {
        			AlertDialog dialog = (AlertDialog) onCreateDialog(tag);
        			dialog.show();
        		}
        	}});
	}
	
	protected Dialog onCreateDialog(String _tag) {
		final String tag = _tag;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to delete this tag?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(type)
		        {
		        case CHARACTER:
		        	mDbHelper.deleteTag(id, "'"+tag+"'");
		        	break;
		        case WORD:
		        	mDbHelper.deleteWordTag(id, "'"+tag+"'");
		        	break;
		        }
				currentTags.remove(tag);
				arrAdapter.notifyDataSetChanged();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
	
	/**
	 * When you want to add a tag to a character/word,
	 * just add to database and then update the list view
	 * to refect that the tag has been added. The tag should 
	 * be at the bottom of the list view.
	 * @param view
	 */
	public void onAddTagButtonClick (View view)
    {
		if (view == addTagButton)
		{
			Editable input = editText.getText();
			String input2 = input.toString(); //This is the string of the tag you typed in
			
			switch(type)
	        {
	        case CHARACTER:
	        	mDbHelper.createTags(id, input2);
	        	break;
	        case WORD:
	        	mDbHelper.createWordTags(id, input2);
	        	break;
	        case LESSON:
	        	mDbHelper.createLessonTags(id, input2);
	        	break;
	        default:
	    		Log.e("Tag", "Unsupported Type");
	        }
			//update the listview --> update the entire view
			//Refactor this, because refreshing the view is inefficient
			currentTags.add(input2);
			//currentTags.clear();
			//currentTags = mDbHelper.getTags(id);
	        arrAdapter.notifyDataSetChanged();

			//Set edit text back to nothing
			editText.setText("");
		}
    }
	
	public void onAddPrivateTagButtonClick(View view){
		Editable input = editPrivateText.getText();
		String input2 = input.toString();
		if (type == ItemType.CHARACTER)
		{
			mDbHelper.updatePrivateTag(id, input2); //added it to db
		}
		else if (type == ItemType.WORD)
		{		
			mDbHelper.updatePrivateWordTag(id, input2);	
		}
		if(currentTags.get(0).contains(PRIVATE_PREFIX))
			currentTags.remove(0);
		currentTags.add(0,PRIVATE_PREFIX+input2);
		arrAdapter.notifyDataSetChanged();
		editPrivateText.setText("");
	}
	

}

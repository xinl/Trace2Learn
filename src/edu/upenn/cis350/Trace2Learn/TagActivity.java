package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem.ItemType;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Word;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class TagActivity extends FragmentActivity implements NewTagDialogFragment.NewTagDialogListener, NewAttrDialogFragment.NewAttrDialogListener {
	
	//Should be able to take BOTH character and word
	
	private DbAdapter mDbHelper;
	
	TraceableItem traceableItem;
	
	//Controls
	private ListView lv;
	
	//Variables
	private long id;
	private List<String> currentTags;
	
	boolean isFromBrowse;
	ItemType type;
	ArrayAdapter<String> arrAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tag); //tag.xml

        lv = (ListView) findViewById(R.id.list);
//        addTagButton = (Button) findViewById(R.id.add_tag_button);
        
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        //Grab the intent/extras. This should be called from CharacterCreation
        id = this.getIntent().getLongExtra("ID", -1); 
        type = ItemType.valueOf(getIntent().getStringExtra("TYPE"));
        isFromBrowse = this.getIntent().getBooleanExtra("FROM", true);
        
        Log.e("ID",Long.toString(id));
        Log.e("TYPE",type.toString());
        
        switch(type)
        {
        case CHARACTER:
        	traceableItem = mDbHelper.getCharacter(id);
        	break;
        case WORD:
        	traceableItem = mDbHelper.getWord(id);
        	break;
        default:
    		Log.e("Tag", "Unsupported Type");
    		return;
        }
        
        currentTags = new ArrayList<String>();

        //add attributes
        
		Map<String, Set<String>> attributes = traceableItem.getAttributes();
		for (String key: attributes.keySet()) {
			Set<String> values = attributes.get(key);
			for(String value: values) {
				currentTags.add(key + ": " + value);
			}
		}
		
		// add tags
		currentTags.addAll(traceableItem.getTags());
		
        
        //Populate the ListView
        arrAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, currentTags);
        arrAdapter.notifyDataSetChanged();
       
        lv.setAdapter(arrAdapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setOnItemClickListener(new OnItemClickListener(){

        	@Override
        	public void onItemClick(AdapterView<?> adapter, View view, int position,
        			long this_id) {
        		String tag = (String) adapter.getItemAtPosition(position);
    			AlertDialog dialog = (AlertDialog) onDeleteDialog(tag);
    			dialog.show();
        	}});
	}
	
	protected Dialog onDeleteDialog(final String str) {
		final String[] strs = str.split(":");
		
		final boolean isTag = (strs.length == 1);
		final boolean isAttr = (strs.length == 2);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_confirm);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(type)
		        {
		        case CHARACTER:
		        	if (isTag) {
			        	traceableItem.removeTag(strs[0]);
		        	} else if (isAttr) {
		        		traceableItem.removeAttribute(strs[0].trim(), strs[1].trim());
		        	}
		        	mDbHelper.updateCharacter((Character)traceableItem);
		        	break;
		        case WORD:
		        	if (isTag) {
			        	traceableItem.removeTag(strs[0]);
		        	} else if (isAttr) {
		        		traceableItem.removeAttribute(strs[0].trim(), strs[1].trim());
		        	}
		        	mDbHelper.updateWord((Word)traceableItem);
		        	break;
		        }
				currentTags.remove(str);
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
	
	public void onNewTagButtonClick(View view)
    {
		DialogFragment newFragment = new NewTagDialogFragment();
	    newFragment.show(getSupportFragmentManager(), "new_tag");
    }
	
	public void onNewAttrButtonClick(View view){
		DialogFragment newFragment = new NewAttrDialogFragment();
	    newFragment.show(getSupportFragmentManager(), "new_attr");
	}
	
	protected void createFinishConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.tag_finish_confirm);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishActivity();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}
	
	public void onFinishClick(View view){
		if (currentTags.size() == 0) {
			createFinishConfirmDialog();
			return;
		} else {
			finishActivity();
		}
	}
	
	public void finishActivity() {
		if(isFromBrowse){
			onBackPressed();
		} else {
			Intent intent = new Intent(TagActivity.this, MainMenuActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		}
	}
	
	@Override
	public void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	/**
	 * This function enables the calling activity to refresh so
	 * it will display updated tags.
	 */
	@Override
	public void onBackPressed() {
	    if(isFromBrowse){
			switch(type)
			{
			case CHARACTER:
				startActivity(new Intent(this, BrowseCharactersActivity.class));
				finish();
				break;
			case WORD:
				startActivity(new Intent(this, BrowseWordsActivity.class));
				finish();
				break;
			case LESSON:
				startActivity(new Intent(this, BrowseCollectionsActivity.class));
				finish();
				break;
			}
	    }
	    else{
	    	super.onBackPressed();
	    }
	}

	@Override
	public void onNewTagDialogPositiveClick(DialogFragment dialog) {
		String tag = ((EditText)dialog.getDialog().findViewById(R.id.tag_text)).getText().toString();
		tag = tag.trim(); //This is the string of the tag you typed in
		if (tag.equals("")) return;
		
		if (tag.contains(":")) {
			Toast.makeText(this, "Tag cannot contain ':'.", 
					Toast.LENGTH_LONG).show();
			return;
		}

		for(String str: currentTags) {
			if(str.equals(tag)){
				Toast.makeText(this, "Cannot add duplicate tag.", 
						Toast.LENGTH_LONG).show();
				return;
			}
		}
		
		switch(type)
        {
        case CHARACTER:
        	traceableItem.addTag(tag);
        	mDbHelper.updateCharacter((Character)traceableItem);
        	break;
        case WORD:
        	traceableItem.addTag(tag);
        	mDbHelper.updateWord((Word)traceableItem);
        	break;
        default:
    		Log.e("Tag", "Unsupported Type");
        }
		
		//update the listview --> update the entire view
		//Refactor this, because refreshing the view is inefficient
		
		currentTags.add(tag);
		//currentTags.clear();
		//currentTags = mDbHelper.getTags(id);
        arrAdapter.notifyDataSetChanged();
		
	}
	
	@Override
	public void onNewAttrDialogPositiveClick(DialogFragment dialog) {
		String key = ((EditText)dialog.getDialog().findViewById(R.id.attr_key)).getText().toString().trim();
		String value = ((EditText)dialog.getDialog().findViewById(R.id.attr_value)).getText().toString().trim();
		
		if (key.length() == 0 || value.length() == 0) {
			Toast.makeText(this, "Invalid attribute format.", 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		for(String str: currentTags) {
			if(str.equals(key + ": " + value)){
				Toast.makeText(this, "Cannot add duplicate key:value pair.", 
						Toast.LENGTH_LONG).show();
				return;
			}
		}
		if (type == ItemType.CHARACTER) {
			traceableItem.addAttribute(key, value);
			mDbHelper.updateCharacter((Character)traceableItem);
		} else if (type == ItemType.WORD) {		
			traceableItem.addAttribute(key, value);
			mDbHelper.updateWord((Word)traceableItem);
		}
		
		currentTags.add(key + ": " + value);
		arrAdapter.notifyDataSetChanged();
	}
	
	
}

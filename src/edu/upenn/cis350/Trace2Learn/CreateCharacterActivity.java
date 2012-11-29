package edu.upenn.cis350.Trace2Learn;

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.R.id;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;

public class CreateCharacterActivity extends Activity {

	private LinearLayout characterViewSlot;
	private CharacterCreationPane creationPane;
	private CharacterPlaybackPane playbackPane;	
	private CharacterTracePane tracePane;   
	
	private TextView tagText;

	private DbAdapter dbHelper;

	private Mode currentMode = Mode.INVALID;

	private long idToPassOn = -1;
	
	boolean isCreate = true;

	public enum Mode {
	    	CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		isCreate = this.getIntent().getBooleanExtra("ISCREATE",true);
		
		if(isCreate)
		    setContentView(R.layout.create_char);
		else
		    setContentView(R.layout.browse_single_char);
		//test
		//if(!isCreate)
		//showToast("isCreate is false");
		/*if(!isCreate){
		    //findViewById(R.id.create_button).setVisibility(View.INVISIBLE);
		    findViewById(R.id.tag_button).setVisibility(View.INVISIBLE);
		    findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
		}
		else{
		    findViewById(R.id.trace_button).setVisibility(View.INVISIBLE);
		    findViewById(R.id.animate_button).setVisibility(View.INVISIBLE);
		}
		*/
		characterViewSlot =(LinearLayout)findViewById(id.character_view_slot);
		creationPane = new CharacterCreationPane(this);
		playbackPane = new CharacterPlaybackPane(this, false, 2); 
		tracePane = new CharacterTracePane(this);  

		setCharacter(new Character());
		tagText = (TextView) this.findViewById(id.tag_list);
		dbHelper = new DbAdapter(this);
		dbHelper.open();
		initializeMode();
		
		setTitle(getTitle() + " È Create Character");
	}

	/**
	 * Initialize the display mode, if the activity was started with intent to
	 * display a character, that character should be displayed
	 */
	
	private void initializeMode() 
	{
		Bundle bun = getIntent().getExtras();
		if (bun != null && bun.containsKey("mode")) 
		{
			String mode = bun.getString("mode");
			if (mode.equals("display")) 
			{
				setCharacter(dbHelper.getCharacter(bun.getLong("charId")));
				setCharacterDisplayPane();
				idToPassOn = bun.getLong("charId");
				updateTags();
			}
		} else 
		{
			setCharacterCreationPane();
		}
	}
	
	
	/**
	 * Switch to creation mode
	 */
	
	private synchronized void setCharacterCreationPane() 
	{
		if (currentMode != Mode.CREATION) 
		{
			currentMode = Mode.CREATION;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(creationPane);
		}
	}
	
	/**
	 * Switches the display mode to display
	 */
	
	private synchronized void setCharacterDisplayPane()
	{
		playbackPane.setAnimated(true);
		if (currentMode != Mode.DISPLAY) 
		{
			Character curChar = creationPane.getCharacter();
			setCharacter(curChar);
			currentMode = Mode.DISPLAY;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(playbackPane);
		}
	}
	
	
	/**
	 * Switches the display mode to display
	 */
	
	private synchronized void setCharacterTracePane()
	{
		tracePane.clearPane();
		if (currentMode != Mode.TRACE) 
		{
			Character curChar = creationPane.getCharacter();
			setCharacter(curChar);
			currentMode = Mode.TRACE;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(tracePane);
		}
	}
	
	
	public void setContentView(View view)
	{
		super.setContentView(view);
	}

	private void setCharacter(Character character)
	{
		creationPane.setCharacter(character);
		playbackPane.setCharacter(character);   
		tracePane.setTemplate(character);   
	}

	private void updateTags()
	{
		if (idToPassOn >= 0)
		{
			Character character = dbHelper.getCharacter(idToPassOn);
			Set<String> tags = character.getTags();
			this.tagText.setText(tagsToString(tags));
			setCharacter(character);
		}
	}
	
	public void onTraceButtonClick(View view)
	{
		setCharacterTracePane();
		
	}
	
	
	@Override
	public void onRestart()
	{
		super.onRestart();
		updateTags();
	}

	private String tagsToString(Set<String> tags)
	{
		StringBuffer buf = new StringBuffer();
		for (String str : tags)
		{
			buf.append(str + ", ");
		}
		if (buf.length() >= 2){
		    return buf.substring(0, buf.length() -2);
		}
		else {
		    return buf.toString();
		}
	}

	public void onCreateButtonClick(View view)
	{
		setCharacterCreationPane();
	}

	public void onSaveButtonClick(View view)
	{
		Character character = creationPane.getCharacter();
		if(character.getNumberOfStrokes()==0){
			showToast("Please add a stroke");
			return;
		}
		long id = character.getId();
		if(id==-1)
			dbHelper.addCharacter(character);
		else
			dbHelper.updateCharacter(character);
		Log.e("Adding to DB", Long.toString(character.getId()));
		idToPassOn = character.getId();
		onTagButtonClick(view);
		updateTags();
	}

	public void onCreateNewButtonClick(View view) //redrqw this character
	{
	    if(isCreate){
	    	creationPane.clearPane();  //Qin
	    	this.tagText.setText("");
		tracePane.clearPane();  //Qin
		playbackPane.clearPane();  //Qin
		idToPassOn = -1;
	    }
	    else{
		//_creationPane.clearPane();
		tracePane.clearPane(); 
		playbackPane.clearPane();
	    }
	}
	
	
	public void onTagButtonClick(View view) 
	{
		Character character = creationPane.getCharacter();
		if (idToPassOn >= 0) 
		{
			Log.e("Passing this CharID", Long.toString(idToPassOn));
			Intent i = new Intent(this, TagActivity.class);

			i.putExtra("ID", idToPassOn);
			i.putExtra("TYPE", character.getClass().getSimpleName().toUpperCase());
			i.putExtra("FROM", false);//Qin
			startActivity(i);
		} else
		{
			showToast("Please save the character before adding tags");
		}

	}
	

	public void onAnimateButtonClick(View view) 
	{
		Log.i("CLICK", "DISPLAY");
		setCharacterDisplayPane();
		
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
		dbHelper.close();
		super.onDestroy();
	}

}

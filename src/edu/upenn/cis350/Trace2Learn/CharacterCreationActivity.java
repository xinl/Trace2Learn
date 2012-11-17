package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.Set;

import edu.upenn.cis350.Trace2Learn.R.id;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CharacterCreationActivity extends Activity {

	private LinearLayout _characterViewSlot;
	private CharacterCreationPane _creationPane;
	//private CharacterPlaybackPane _playbackPane;	//Qin
	//private CharacterTracePane _tracePane;   //Qin
	
	private TextView _tagText;

	private DbAdapter _dbHelper;

	private Mode _currentMode = Mode.INVALID;

	private long id_to_pass = -1;

	public enum Mode {
	    	CREATION, SAVE, INVALID;
		//CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_char);//Qin was test_char_display

		_characterViewSlot =(LinearLayout)findViewById(id.character_view_slot);
		_creationPane = new CharacterCreationPane(this);
		//_playbackPane = new CharacterPlaybackPane(this, false, 2); //Qin
		//_tracePane = new CharacterTracePane(this);  //Qin

		setCharacter(new Character());
		_tagText = (TextView) this.findViewById(id.tag_list);
		_dbHelper = new DbAdapter(this);
		_dbHelper.open();
		setCharacterCreationPane(); //Qin add
		//initializeMode();  //Qin
	}

	/**
	 * Initialize the display mode, if the activity was started with intent to
	 * display a character, that character should be displayed
	 */
	/*
	private void initializeMode() 
	{
		Bundle bun = getIntent().getExtras();
		if (bun != null && bun.containsKey("mode")) 
		{
			String mode = bun.getString("mode");
			if (mode.equals("display")) 
			{
				setCharacter(_dbHelper.getCharacter(bun.getLong("charId")));
				setCharacterDisplayPane();
				id_to_pass = bun.getLong("charId");
				updateTags();
			}
		} else 
		{
			setCharacterCreationPane();
		}
	}
	*/ //Qin
	
	/**
	 * Switch to creation mode
	 */
	
	private synchronized void setCharacterCreationPane() 
	{
		if (_currentMode != Mode.CREATION) 
		{
			_currentMode = Mode.CREATION;
			_characterViewSlot.removeAllViews();
			_characterViewSlot.addView(_creationPane);
		}
	}
	
	/**
	 * Switches the display mode to display
	 */
	/*
	private synchronized void setCharacterDisplayPane()
	{
		_playbackPane.setAnimated(true);
		if (_currentMode != Mode.DISPLAY) 
		{
			Character curChar = _creationPane.getCharacter();
			setCharacter(curChar);
			_currentMode = Mode.DISPLAY;
			_characterViewSlot.removeAllViews();
			_characterViewSlot.addView(_playbackPane);
		}
	}
	 */ //Qin
	
	/**
	 * Switches the display mode to display
	 */
	/*
	private synchronized void setCharacterTracePane()
	{
		_tracePane.clearPane();
		if (_currentMode != Mode.TRACE) 
		{
			Character curChar = _creationPane.getCharacter();
			setCharacter(curChar);
			_currentMode = Mode.TRACE;
			_characterViewSlot.removeAllViews();
			_characterViewSlot.addView(_tracePane);
		}
	}
	*/ //Qin
	
	public void setContentView(View view)
	{
		super.setContentView(view);
	}

	private void setCharacter(Character character)
	{
		_creationPane.setCharacter(character);
		//_playbackPane.setCharacter(character);   //Qin
		//_tracePane.setTemplate(character);   //Qin
	}

	private void updateTags()
	{
		if (id_to_pass >= 0)
		{
			Character character = _dbHelper.getCharacter(id_to_pass);
			Set<String> tags = character.getTags();
			this._tagText.setText(tagsToString(tags));
			setCharacter(character);
		}
	}
	
	/*public void onTraceButtonClick(View view)
	{
		setCharacterTracePane();
		
	}
	*/
	
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

	/*public void onCreateButtonClick(View view)
	{
		setCharacterCreationPane();
	}*/ //Qin

	public void onSaveButtonClick(View view)
	{
		Character character = _creationPane.getCharacter();
		if(character.getNumberOfStrokes()==0){
			showToast("Please add a stroke");
			return;
		}
		long id = character.getId();
		if(id==-1)
			_dbHelper.addCharacter(character);
		else
			_dbHelper.updateCharacter(character);
		Log.e("Adding to DB", Long.toString(character.getId()));
		id_to_pass = character.getId();
		onTagButtonClick(view);
		updateTags();
	}

	public void onCreateNewButtonClick(View view) //redrqw this character
	{
	    	_creationPane.clearPane();  //Qin
	    	this._tagText.setText("");
		//_tracePane.clearPane();  //Qin
		//_playbackPane.clearPane();  //Qin
	}
	
	public void onTagButtonClick(View view) 
	{
		Character character = _creationPane.getCharacter();
		if (id_to_pass >= 0) 
		{
			Log.e("Passing this CharID", Long.toString(id_to_pass));
			Intent i = new Intent(this, TagActivity.class);

			i.putExtra("ID", id_to_pass);
			i.putExtra("TYPE", character.getClass().getSimpleName().toUpperCase());

			startActivity(i);
		} else
		{
			showToast("Please save the character before adding tags");
		}

	}

	/*public void onAnimateButtonClick(View view) 
	{
		Log.i("CLICK", "DISPLAY");
		setCharacterDisplayPane();
		
	}*/  //Qin
	
	public void showToast(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	@Override
	public void onDestroy() {
		_dbHelper.close();
		super.onDestroy();
	}

}

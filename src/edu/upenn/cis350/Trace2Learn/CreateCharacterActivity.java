package edu.upenn.cis350.Trace2Learn;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
	
	private Button traceButton;
	private Button displayButton;
	
	private DbAdapter dbHelper;
	private Mode currentMode = Mode.INVALID;
	private long idToPassOn = -1;
	boolean isCreate = true;

	/**
	 * Different modes for create character and browse each character
	 */
	public enum Mode {
		CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isCreate = this.getIntent().getBooleanExtra("ISCREATE", true);

		if (isCreate) {
			setContentView(R.layout.create_char);
			setTitle(getTitle() + " � Create Character");
		} else {
			setContentView(R.layout.browse_single_char);
			setTitle(getTitle() + " � View Character");
		}

		characterViewSlot = (LinearLayout) findViewById(id.character_view_slot);
		creationPane = new CharacterCreationPane(this);
		playbackPane = new CharacterPlaybackPane(this, false, 2);
		tracePane = new CharacterTracePane(this);
		
		traceButton = (Button) this.findViewById(id.trace_button);
		displayButton = (Button) this.findViewById(id.display_button);

		setCharacter(new Character());
		tagText = (TextView) this.findViewById(id.tag_list);
		dbHelper = new DbAdapter(this);
		dbHelper.open();
		initializeMode();
	}

	/**
	 * Initialize the display mode, if the activity was started with intent to display a character, that character should be displayed
	 */
	private void initializeMode() {
		Bundle bun = getIntent().getExtras();
		if (bun != null && bun.containsKey("mode")) {
			String mode = bun.getString("mode");
			if (mode.equals("display")) {
				setCharacter(dbHelper.getCharacter(bun.getLong("charId")));
				setCharacterDisplayPane();
				idToPassOn = bun.getLong("charId");
				updateTags();
			}
		} else {
			setCharacterCreationPane();
		}
	}

	/**
	 * Switch to creation mode
	 */
	private synchronized void setCharacterCreationPane() {
		if (currentMode != Mode.CREATION) {
			currentMode = Mode.CREATION;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(creationPane);
		}
	}

	/**
	 * Switch to display mode
	 */
	private synchronized void setCharacterDisplayPane() {
		playbackPane.setAnimated(true);
		if (currentMode != Mode.DISPLAY) {
			Character curChar = creationPane.getCharacter();
			setCharacter(curChar);
			currentMode = Mode.DISPLAY;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(playbackPane);
			
			traceButton.setTypeface(null, Typeface.NORMAL);
			displayButton.setTypeface(null, Typeface.BOLD);
		}
	}

	/**
	 * Switch to trace mode
	 */
	private synchronized void setCharacterTracePane() {
		tracePane.clearPane();
		if (currentMode != Mode.TRACE) {
			Character curChar = creationPane.getCharacter();
			setCharacter(curChar);
			currentMode = Mode.TRACE;
			characterViewSlot.removeAllViews();
			characterViewSlot.addView(tracePane);
			
			traceButton.setTypeface(null, Typeface.BOLD);
			displayButton.setTypeface(null, Typeface.NORMAL);
		}
	}

	private void setCharacter(Character character) {
		creationPane.setCharacter(character);
		playbackPane.setCharacter(character);
		tracePane.setTemplate(character);
	}

	/**
	 * Update the tags under the character
	 */
	private void updateTags() {
		if (idToPassOn >= 0) {
			Character character = dbHelper.getCharacter(idToPassOn);
			Set<String> tags = character.getTags();
			Map<String, Set<String>> attributes = character.getAttributes();
			this.tagText.setMovementMethod(new ScrollingMovementMethod());
			this.tagText.setText(tagsToString(attributes, tags));
			setCharacter(character);
		}
	}

	/**
	 * Used for browse each character. Press the practice button to trace the character
	 */
	public void onTraceButtonClick(View view) {
		setCharacterTracePane();
	}
	
	/**
	 * Used for browse each character. Press the playback button to replay the character.
	 */
	public void onDisplayButtonClick(View view) {
		setCharacterDisplayPane();
	}

	/**
	 * Convert the attributes and tags to string that can be displayed under the character
	 */
	private String tagsToString(Map<String, Set<String>> attributes, Set<String> tags) {
	    	StringBuffer buf = new StringBuffer();
		Set<String> keys = attributes.keySet();
		for (String key : keys) {
			buf.append(key + ": ");
			Set<String> values = attributes.get(key);
			int i = 0;
			for (String value : values) {
				i++;
				if (i != values.size())
					buf.append(value + ", ");
				else
					buf.append(value);
			}
			buf.append("\n");
		}
		if (tags.size() != 0) {   
		    buf.append("Tags: ");
		    int i = 0;
		    for (String tag : tags) {
			i++;
			if(i!=tags.size())
			    buf.append(tag + ", ");
			else
			    buf.append(tag);
		    }
		}
		return buf.toString();
	}
	

	/**
	 * Used for create character. Press the save button to save the character.
	 */
	public void onSaveButtonClick(View view) {
		Character character = creationPane.getCharacter();
		if (character.getNumberOfStrokes() == 0) {
			showToast("Please add a stroke");
			return;
		}
		long id = character.getId();
		if (id == -1)
			dbHelper.addCharacter(character);
		else
			dbHelper.updateCharacter(character);
		Log.e("Adding to DB", Long.toString(character.getId()));
		idToPassOn = character.getId();
		editTag(view);
	}

	/**
	 * When used for create character: Press the clear stroke button to clear the stroke When used for browse each character: Press the clear button to retrace
	 * the character
	 */

	public void onClearButtonClick(View view) {
		if (isCreate) {
			creationPane.clearPane();
			tracePane.clearPane();
			playbackPane.clearPane();
		}
	}

	/**
	 * Used for create character. Go to the tag activity.
	 */
	public void editTag(View view) {
		Character character = creationPane.getCharacter();
		if (idToPassOn >= 0) {
			Log.e("Passing this CharID", Long.toString(idToPassOn));
			Intent i = new Intent(this, TagActivity.class);

			i.putExtra("ID", idToPassOn);
			i.putExtra("TYPE", character.getClass().getSimpleName().toUpperCase(Locale.US));
			i.putExtra("FROM", false);
			startActivity(i);
		} else {
			showToast("Please save the character before adding tags");
		}

	}

	public void showToast(String msg) {
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

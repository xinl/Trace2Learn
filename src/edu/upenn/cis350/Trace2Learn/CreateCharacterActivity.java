package edu.upenn.cis350.Trace2Learn;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.R.id;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;

public class CreateCharacterActivity extends Activity {

	private LinearLayout characterViewSlot;
	private CharacterCreationPane creationPane;
		
	private DbAdapter dbHelper;
	private Mode currentMode = Mode.INVALID;
	private long idToPassOn = -1;

	/**
	 * Different modes for create character and browse each character
	 */
	public enum Mode {
		CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

			setContentView(R.layout.create_char);
			setTitle(getTitle() + " » Create Character");

		characterViewSlot = (LinearLayout) findViewById(id.character_view_slot);
		creationPane = new CharacterCreationPane(this);

		creationPane.setCharacter(new Character());
		dbHelper = new DbAdapter(this);
		dbHelper.open();
		setCharacterCreationPane();
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
		creationPane.clearPane();
	}

	/**
	 * Used for create character. Go to the tag activity.
	 */
	public void editTag(View view) {
		Character character = creationPane.getCharacter();
		if (idToPassOn >= 0) {
			Log.e("Passing this CharID", Long.toString(idToPassOn));
			Intent i = new Intent(this, EditTagsActivity.class);

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

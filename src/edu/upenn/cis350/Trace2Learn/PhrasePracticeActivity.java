package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.CharacterTracePane.OnTraceCompleteListener;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
import edu.upenn.cis350.Trace2Learn.Database.LessonWord;
import edu.upenn.cis350.Trace2Learn.R.id;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

public class PhrasePracticeActivity extends Activity {
		
	private TextView tagTextView;
	private TextView infoTextView;

	private DbAdapter dbAdapter;

	private Mode currentMode = Mode.INVALID;
	
	private ArrayList<Long> wordIDs;
	private ArrayList<LessonCharacter> characters;
	private ArrayList<Bitmap> bitmaps;
	
	private int currentCharacterIndex = -1;
	private int currentWordIndex = -1;
	private long currentCollectionID = -1;
	private long currentWordID = -1;
	
	private String currentCollectionName = "";
	
	private ArrayList<SquareLayout> displayLayouts;
	private ArrayList<SquareLayout> traceLayouts;
	
	private ArrayList<CharacterDisplayPane> displayPanes;
	private ArrayList<CharacterTracePane> tracePanes;
	
	private ImageAdapter imgAdapter;
	
	private Gallery gallery;
	
	private ViewAnimator animator;
	
	private OnTraceCompleteListener onTraceCompleteListener = new OnTraceCompleteListener() {
		public void onTraceComplete(View v) {
			selectNextCharacter();
		}
	};
	
	private enum Mode {
		CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.practice_phrase);

		animator = (ViewAnimator)this.findViewById(R.id.view_slot);
		
		wordIDs = new ArrayList<Long>();
		characters = new ArrayList<LessonCharacter>();
		bitmaps = new ArrayList<Bitmap>();
		
		displayLayouts = new ArrayList<SquareLayout>();
		traceLayouts = new ArrayList<SquareLayout>();
		
		displayPanes = new ArrayList<CharacterDisplayPane>();
		tracePanes = new ArrayList<CharacterTracePane>();
		
		
		imgAdapter = new ImageAdapter(this,bitmaps);
        gallery = (Gallery)findViewById(R.id.gallery);
        gallery.setSpacing(0);
        
        gallery.setAdapter(imgAdapter);
		gallery.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setSelectedCharacter(position);
			}
			
		});

		tagTextView = (TextView) this.findViewById(id.tag_list);
		infoTextView = (TextView) this.findViewById(id.info_text);

		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		currentCollectionID = this.getIntent().getLongExtra("collectionId", -1);
		currentWordID = this.getIntent().getLongExtra("wordId", -1); //TODO: add error check
		
		if (currentCollectionID != -1) {
			currentCollectionName = this.getIntent().getStringExtra("collectionName");
			wordIDs = (ArrayList<Long>) dbAdapter.getWordsFromLessonId(currentCollectionID);
			currentWordIndex = wordIDs.indexOf(currentWordID);
		} else {
			wordIDs.add(currentWordID);
			currentWordIndex = 0;
		}

		setSelectedWord(currentWordIndex);

	}

	/**
	 * Initialize the display mode, if the activity was started with intent to
	 * display a character, that character should be displayed
	 */
	private void setSelectedWord(int position) 
	{
		currentWordIndex = position;
		long wordId = wordIDs.get(position);
		currentWordID = wordId;
		LessonWord word = dbAdapter.getWordById(wordId);
		setCharacterList(word.getCharacterIds());
		setSelectedCharacter(0);
		if (currentMode == Mode.TRACE) {
			setDisplayPane();
			setCharacterTracePane();
		} else {
			setDisplayPane();
		}
		if (currentCollectionID != -1) {
			infoTextView.setText(currentCollectionName + " - " + (position + 1) + " of " + wordIDs.size());
		}
		updateTags();
	}

	private void setSelectedCharacter(int position) {
		currentCharacterIndex = position;
		animator.setDisplayedChild(position);
		tracePanes.get(position).clearPane();
		updateTags();
		if (currentMode == Mode.TRACE) {
			setDisplayPane();
			setCharacterTracePane();
		} else {
			setDisplayPane();
		}
	}

	private void setCharacterList(List<Long> ids)
	{
		characters.clear();
		bitmaps.clear();
		tracePanes.clear();
		displayPanes.clear();
		traceLayouts.clear();
		displayLayouts.clear();
		for(long id : ids)
		{
			LessonCharacter ch = dbAdapter.getCharacterById(id);
			Bitmap bmp = BitmapFactory.buildBitmap(ch, 64, 64);
			this.characters.add(ch);
			this.bitmaps.add(bmp);
			SquareLayout disp = new SquareLayout(animator.getContext());
			CharacterPlaybackPane dispPane = new CharacterPlaybackPane(disp.getContext(), false, 2);
			dispPane.setCharacter(ch);
			disp.addView(dispPane);
			
			this.displayLayouts.add(disp);
			this.displayPanes.add(dispPane);
			
			SquareLayout trace = new SquareLayout(animator.getContext());
			CharacterTracePane tracePane = new CharacterTracePane(disp.getContext());
			tracePane.setOnTraceCompleteListener(onTraceCompleteListener);
			tracePane.setTemplate(ch);
			trace.addView(tracePane);
			
			this.traceLayouts.add(trace);
			this.tracePanes.add(tracePane);
		}
		imgAdapter.update(bitmaps);
        imgAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Switches the display mode to display
	 */
	private synchronized void setDisplayPane()
	{
		int curInd = animator.getDisplayedChild();
		if (currentMode != Mode.DISPLAY) 
		{
			animator.removeAllViews();
			for(SquareLayout disp : this.displayLayouts)
			{
				animator.addView(disp);
			}
			animator.setDisplayedChild(curInd);
			currentMode = Mode.DISPLAY;
		}
		SquareLayout sl = (SquareLayout)animator.getChildAt(curInd);
		CharacterPlaybackPane playbackPane;
		playbackPane = (CharacterPlaybackPane)sl.getChildAt(0);
		playbackPane.setAnimated(true);
	}

	/**
	 * Switches the display mode to display
	 */
	private synchronized void setCharacterTracePane()
	{
		if (currentMode != Mode.TRACE) 
		{
			int curInd = animator.getDisplayedChild();
			animator.removeAllViews();
			for(SquareLayout trace : this.traceLayouts)
			{
				animator.addView(trace);
			}
			animator.setDisplayedChild(curInd);
			currentMode = Mode.TRACE;
		}
	}
	
	public void setContentView(View view)
	{
		super.setContentView(view);
	}

	private void updateTags()
	{
		if (characters.size() > 0)
		{
			int ind = animator.getDisplayedChild();
			List<String> tags = dbAdapter.getCharacterTags(characters.get(ind).getId());
			this.tagTextView.setText(tagsToString(tags));
		}
	}

	public void onClearButtonClick(View view)
	{
		int child = animator.getDisplayedChild();
		this.tracePanes.get(child).clearPane();
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

	private String tagsToString(List<String> tags)
	{
		StringBuffer buf = new StringBuffer();
		for (String str : tags)
		{
			buf.append(str + ", ");
		}
		if (buf.length() >= 2) {
			return buf.substring(0, buf.length() - 2);
		} else {
			return buf.toString();
		}
	}

	public void onAnimateButtonClick(View view) 
	{
		Log.i("CLICK", "DISPLAY");
		setDisplayPane();
	}
	
	public void showToast(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public void selectNextCharacter() {
		if (currentCharacterIndex >= characters.size() - 1) {
			// we've reached last char, should move on to next word
			if (currentWordIndex >= wordIDs.size() - 1) {
				// we've reached last word in collection, do nothing
				return;
			} else {
				Log.i("MOVEON", "Move on to next word.");
				setSelectedWord(currentWordIndex + 1);
				gallery.setSelection(0, true);
			}
		} else {
			Log.i("MOVEON", "Move on to next character.");
			setSelectedCharacter(currentCharacterIndex + 1);
			gallery.setSelection(currentCharacterIndex, true);
		}
		
	}
	
	@Override
	public void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
}

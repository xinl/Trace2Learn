package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upenn.cis350.Trace2Learn.CharacterTracePane.OnTraceCompleteListener;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Word;
import edu.upenn.cis350.Trace2Learn.R.id;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

public class PhrasePracticeActivity extends Activity {
		
	private TextView tagTextView;
	private Button traceButton;
	private Button displayButton;

	private DbAdapter dbAdapter;

	private Mode currentMode = Mode.INVALID;
	
	private ArrayList<Long> wordIDs;
	private ArrayList<Character> characters;
	private ArrayList<Bitmap> bitmaps;
	
	private int currentCharacterIndex = -1;
	private int currentWordIndex = -1;
	private long currentCollectionID = -1;
	private Word currentWord = null;
	
	private String currentCollectionName = "";
	
	private ArrayList<SquareLayout> displayLayouts;
	private ArrayList<SquareLayout> traceLayouts;
	
	private ArrayList<CharacterDisplayPane> displayPanes;
	private ArrayList<CharacterTracePane> tracePanes;
	
	private ThumbnailAdapter imgAdapter;
	
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
		characters = new ArrayList<Character>();
		bitmaps = new ArrayList<Bitmap>();
		
		displayLayouts = new ArrayList<SquareLayout>();
		traceLayouts = new ArrayList<SquareLayout>();
		
		displayPanes = new ArrayList<CharacterDisplayPane>();
		tracePanes = new ArrayList<CharacterTracePane>();
		
		
		imgAdapter = new ThumbnailAdapter(this,bitmaps);
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
		
		traceButton = (Button) this.findViewById(id.trace_button);
		displayButton = (Button) this.findViewById(id.display_button);

		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		currentCollectionID = this.getIntent().getLongExtra("collectionId", -1);
		Long currentWordID = this.getIntent().getLongExtra("wordId", -1); //TODO: add error check
		
		if (currentCollectionID != -1) {
			currentCollectionName = this.getIntent().getStringExtra("collectionName");
			Collection currentCollection = dbAdapter.getCollection(currentCollectionID, true);
			wordIDs = (ArrayList<Long>) currentCollection.getWordIds();
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
		currentWord = dbAdapter.getWord(wordId);
		setCharacterList(currentWord.getCharacterIds());
		setSelectedCharacter(0);
		if (currentMode == Mode.TRACE) {
			setDisplayPane();
			setCharacterTracePane();
		} else {
			setDisplayPane();
		}
		if (currentCollectionID != -1) {
			setTitle(getResources().getString(R.string.app_name) + " » " + currentCollectionName + " - " + (position + 1) + " of " + wordIDs.size());
		} else {
			setTitle(getResources().getString(R.string.app_name) + " » View Word");
		}
		updateTags();
	}

	private void setSelectedCharacter(int position) {
		currentCharacterIndex = position;
		animator.setDisplayedChild(position);
		tracePanes.get(position).clearPane();
		if (currentMode == Mode.DISPLAY) {
			setDisplayPane();
		} else {
			setCharacterTracePane();
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
			Character ch = dbAdapter.getCharacter(id);
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

			traceButton.setTypeface(null, Typeface.NORMAL);
			displayButton.setTypeface(null, Typeface.BOLD);
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
			
			traceButton.setTypeface(null, Typeface.BOLD);
			displayButton.setTypeface(null, Typeface.NORMAL);
		}
		int child = animator.getDisplayedChild();
		this.tracePanes.get(child).clearPane();
	}
	
	public void setContentView(View view)
	{
		super.setContentView(view);
	}

	private void updateTags() {
		Set<String> tags = currentWord.getTags();
		Map<String, Set<String>> attrs = currentWord.getAttributes();
		this.tagTextView.setMovementMethod(new ScrollingMovementMethod());
		this.tagTextView.setText(tagsToString(attrs, tags));
	}
	
	public void onTraceButtonClick(View view)
	{
		setCharacterTracePane();
	}
	

	public void onDisplayButtonClick(View view) 
	{
		setDisplayPane();
	}

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

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
		
	private TextView _tagText;

	private DbAdapter _dbHelper;

	private Mode _currentMode = Mode.INVALID;

	private ArrayList<Long> _wordIDs;
	private ArrayList<LessonCharacter> _characters;
	private ArrayList<Bitmap> _bitmaps;
	
	private int _currentCharacterIndex = -1;
	private int _currentWordIndex = -1;
	private long _currentCollectionID = -1;
	private long _currentWordID = -1;
	
	private ArrayList<SquareLayout> _displayLayouts;
	private ArrayList<SquareLayout> _traceLayouts;
	
	private ArrayList<CharacterDisplayPane> _displayPanes;
	private ArrayList<CharacterTracePane> _tracePanes;
	
	private ImageAdapter _imgAdapter;
	
	private Gallery _gallery;
	
	private ViewAnimator _animator;
	
	private OnTraceCompleteListener _onTraceCompleteListener = new OnTraceCompleteListener() {
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

		_animator = (ViewAnimator)this.findViewById(R.id.view_slot);
		
		_wordIDs = new ArrayList<Long>();
		_characters = new ArrayList<LessonCharacter>();
		_bitmaps = new ArrayList<Bitmap>();
		
		_displayLayouts = new ArrayList<SquareLayout>();
		_traceLayouts = new ArrayList<SquareLayout>();
		
		_displayPanes = new ArrayList<CharacterDisplayPane>();
		_tracePanes = new ArrayList<CharacterTracePane>();
		
		
		_imgAdapter = new ImageAdapter(this,_bitmaps);
        _gallery = (Gallery)findViewById(R.id.gallery);
        _gallery.setSpacing(0);
        
        _gallery.setAdapter(_imgAdapter);
		_gallery.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setSelectedCharacter(position);
			}
			
		});

		_tagText = (TextView) this.findViewById(id.tag_list);

		_dbHelper = new DbAdapter(this);
		_dbHelper.open();
		
		_currentCollectionID = this.getIntent().getLongExtra("collectionId", -1);
		_currentWordID = this.getIntent().getLongExtra("wordId", -1); //TODO: add error check
		
		if (_currentCollectionID != -1) {
			_wordIDs = (ArrayList<Long>) _dbHelper.getWordsFromLessonId(_currentCollectionID);
			_currentWordIndex = _wordIDs.indexOf(_currentWordID);
		} else {
			_wordIDs.add(_currentWordID);
			_currentWordIndex = 0;
		}

		setSelectedWord(_currentWordIndex);

	}

	/**
	 * Initialize the display mode, if the activity was started with intent to
	 * display a character, that character should be displayed
	 */
	private void setSelectedWord(int position) 
	{
		_currentWordIndex = position;
		long wordId = _wordIDs.get(position);
		_currentWordID = wordId;
		LessonWord word = _dbHelper.getWordById(wordId);
		setCharacterList(word.getCharacterIds());
		setSelectedCharacter(0);
		setDisplayPane();
		updateTags();
	}

	private void setSelectedCharacter(int position) {
		_currentCharacterIndex = position;
		_animator.setDisplayedChild(position);
		_tracePanes.get(position).clearPane();
		updateTags();
		setDisplayPane();
	}

	private void setCharacterList(List<Long> ids)
	{
		_characters.clear();
		_bitmaps.clear();
		_tracePanes.clear();
		_displayPanes.clear();
		_traceLayouts.clear();
		_displayLayouts.clear();
		for(long id : ids)
		{
			LessonCharacter ch = _dbHelper.getCharacterById(id);
			Bitmap bmp = BitmapFactory.buildBitmap(ch, 64, 64);
			this._characters.add(ch);
			this._bitmaps.add(bmp);
			SquareLayout disp = new SquareLayout(_animator.getContext());
			CharacterPlaybackPane dispPane = new CharacterPlaybackPane(disp.getContext(), false, 2);
			dispPane.setCharacter(ch);
			disp.addView(dispPane);
			
			this._displayLayouts.add(disp);
			this._displayPanes.add(dispPane);
			
			SquareLayout trace = new SquareLayout(_animator.getContext());
			CharacterTracePane tracePane = new CharacterTracePane(disp.getContext());
			tracePane.setOnTraceCompleteListener(_onTraceCompleteListener);
			tracePane.setTemplate(ch);
			trace.addView(tracePane);
			
			this._traceLayouts.add(trace);
			this._tracePanes.add(tracePane);
		}
		_imgAdapter.update(_bitmaps);
        _imgAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Switches the display mode to display
	 */
	private synchronized void setDisplayPane()
	{
		int curInd = _animator.getDisplayedChild();
		if (_currentMode != Mode.DISPLAY) 
		{
			_animator.removeAllViews();
			for(SquareLayout disp : this._displayLayouts)
			{
				_animator.addView(disp);
			}
			_animator.setDisplayedChild(curInd);
			_currentMode = Mode.DISPLAY;
		}
		SquareLayout sl = (SquareLayout)_animator.getChildAt(curInd);
		CharacterPlaybackPane playbackPane;
		playbackPane = (CharacterPlaybackPane)sl.getChildAt(0);
		playbackPane.setAnimated(true);
	}

	/**
	 * Switches the display mode to display
	 */
	private synchronized void setCharacterTracePane()
	{
		if (_currentMode != Mode.TRACE) 
		{
			int curInd = _animator.getDisplayedChild();
			_animator.removeAllViews();
			for(SquareLayout trace : this._traceLayouts)
			{
				_animator.addView(trace);
			}
			_animator.setDisplayedChild(curInd);
			_currentMode = Mode.TRACE;
		}
	}
	
	public void setContentView(View view)
	{
		super.setContentView(view);
	}

	private void updateTags()
	{
		if (_characters.size() > 0)
		{
			int ind = _animator.getDisplayedChild();
			List<String> tags = _dbHelper.getCharacterTags(_characters.get(ind).getId());
			this._tagText.setText(tagsToString(tags));
		}
	}

	public void onClearButtonClick(View view)
	{
		int child = _animator.getDisplayedChild();
		this._tracePanes.get(child).clearPane();
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
		for (int i=0; i<tags.size()-1; i++)
		{
		    buf.append(tags.get(i) + ", ");
		}
		buf.append(tags.get(tags.size()-1));
		return buf.toString();
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
		if (_currentCharacterIndex >= _characters.size() - 1) {
			// we've reached last char, should move on to next word
			if (_currentWordIndex >= _wordIDs.size() - 1) {
				// we've reached last word in collection, do nothing
				return;
			} else {
				Log.i("MOVEON", "Move on to next word.");
				setSelectedWord(_currentWordIndex + 1);
			}
		} else {
			Log.i("MOVEON", "Move on to next character.");
			setSelectedCharacter(_currentCharacterIndex + 1);
			_gallery.setSelection(_currentCharacterIndex, true);
		}
		
	}
	
	@Override
	public void onDestroy() {
		_dbHelper.close();
		super.onDestroy();
	}
}

package edu.upenn.cis350.Trace2Learn.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import edu.upenn.cis350.Trace2Learn.CreateWordActivity;
import edu.upenn.cis350.Trace2Learn.R;
import edu.upenn.cis350.Trace2Learn.Database.Word;
import edu.upenn.cis350.Trace2Learn.Database.Character;

public class CreateWordActivityTest extends ActivityInstrumentationTestCase2<CreateWordActivity>{
	
	public CreateWordActivityTest(){
		super("edu.upenn.cis350.Trace2Learn",CreateWordActivity.class);
	}
	
	private CreateWordActivity activity;
	private ListView list;
	
	
	public void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		list = (ListView)activity.findViewById(R.id.charslist);
	}
	
	public void testOneChar(){
		activity.runOnUiThread(new Runnable() {
			public void run(){
				list.performItemClick(null, 0, list.getAdapter().getItemId(0));
			}
		});
		
		getInstrumentation().waitForIdleSync();
		
		Word word = activity.getWord();
		assertEquals(word.getCharacters().get(0),((Character)list.getItemAtPosition(0)));
	}
}
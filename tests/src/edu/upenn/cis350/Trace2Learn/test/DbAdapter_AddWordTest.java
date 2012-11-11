package edu.upenn.cis350.Trace2Learn.test;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class DbAdapter_AddWordTest extends AndroidTestCase {
	DbAdapter db;
	Character a, b, c;
	Word word1, word2;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new DbAdapter(this.getContext());
		db.openTest();
		a = new Character();
		a.addStroke(new Stroke(1, 1));
		b = new Character();
		b.addStroke(new Stroke(5, 7));
		Stroke s = new Stroke(8, 9);
		s.addPoint(11, 13);
		b.addStroke(s);
		c = new Character();
		c.addStroke(new Stroke(11, 13));
		db.addCharacter(a);
		db.addCharacter(b);
		db.addCharacter(c);
		
		word1 = new Word();
		word1.addCharacter(c);
		word1.addCharacter(a);
		word1.addCharacter(b);
		
		word2 = new Word();
		word2.addCharacter(c);
		word2.addCharacter(a);
		word2.addCharacter(b);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testAddWord() {
		assertEquals(-1, word1.getId());
	    assertTrue(db.addWord(word1));
	    assertEquals(4, word1.getId());
	    
	    assertEquals(-1, word2.getId());
	    assertTrue(db.addWord(word2));
	    assertEquals(5, word2.getId());
	    assertEquals(5, word2.getOrder());
	}
	
}

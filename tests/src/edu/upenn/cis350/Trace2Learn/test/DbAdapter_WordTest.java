package edu.upenn.cis350.Trace2Learn.test;

import java.util.List;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class DbAdapter_WordTest extends AndroidTestCase {
	DbAdapter db;
	Character a, b, c;
	Word word1, word2;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new DbAdapter(this.getContext());
		db.openTest();
		
		a = new Character();
		Stroke s = new Stroke(1, 1);
		s.addPoint(4, 5);
		a.addStroke(s);
		a.addTag("a");
		
		b = new Character();
		s = new Stroke(8, 9);
		s.addPoint(11, 13);
		b.addStroke(s);
		b.addTag("b");
		
		c = new Character();
		s = new Stroke(11, 14);
		s.addPoint(7, 9);
		s.addPoint(0.12F, 0.5F);
		c.addStroke(s);
		s = new Stroke(.05F, .72F);
		s.addPoint(19, 32);
		c.addStroke(s);
		c.addTag("c");
		
		db.addCharacter(a);
		db.addCharacter(b);
		db.addCharacter(c);
		
		word1 = new Word();
		word1.addCharacter(c);
		word1.addCharacter(a);
		word1.addCharacter(b);
		word1.addTag("cab");
		
		word2 = new Word();
		word2.addCharacter(a);
		word2.addCharacter(b);
		word2.addTag("ab");
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testAddGetWord() {
	    assertTrue(db.addWord(word1));
	    assertTrue(word1.getId() > -1);
	    
	    Word newWord = db.getWord(word1.getId());
	    assertEquals(word1, newWord);
	    
	    assertTrue(db.addWord(word2));
	    assertTrue(word2.getId() > word1.getId());
	    newWord = db.getWord(word2.getId());
	    assertEquals(word2, newWord);
	}
	
	public void testUpdateCharacters() {
		word1.addCharacter(a);
		Word oldWord = db.getWord(word1.getId());
		db.updateWord(word1);
		Word newWord = db.getWord(word1.getId());
	    assertEquals(word1, newWord);
	    assertFalse(newWord.equals(oldWord));
	    
	    word1.removeCharacter(1);
	    oldWord = db.getWord(word1.getId());
	    db.updateWord(word1);
	    newWord = db.getWord(word1.getId());
	    assertEquals(word1, newWord);
	    assertFalse(newWord.equals(oldWord));
	}
	
	public void testDeleteWord() {
		word1.addTag("tag");
		word1.addAttribute("key", "value");
		db.addWord(word1);
		word2.addTag("tag");
		word2.addAttribute("key", "value");
		db.addWord(word2);
		
		assertNotNull(db.getWord(word1.getId()));
		assertTrue(db.deleteWord(word1));
		assertNull(db.getWord(word1.getId()));
		Word newWord = db.getWord(word2.getId());
		assertEquals(word2, newWord);
	}
	
	
	public void testGetAllWords() {
		db.addWord(word1);
		db.addWord(word2);
		
		List<Word> all = db.getAllWords();
		assertEquals(word1, all.get(0));
		assertEquals(word2, all.get(1));
		
		word1.setOrder(2);
		word2.setOrder(1);
		
		db.updateWord(word1);
		db.updateWord(word2);
		
		all = db.getAllWords();
		assertEquals(word2, all.get(0));
		assertEquals(word1, all.get(1));
	}
}

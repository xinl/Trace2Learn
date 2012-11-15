package edu.upenn.cis350.Trace2Learn.test;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class DbAdapter_CollectionTest extends AndroidTestCase {
	DbAdapter db;
	Character a, b, c;
	Word word1, word2;
	Collection coll;
	
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
		
		db.addWord(word1);
		db.addWord(word2);
		
		coll = new Collection();
		coll.setName("Collection 1");
		coll.setDescription("The first collection.");
		coll.addWord(word1);
		coll.addWord(word2);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testAddGetCollection() {
	    assertTrue(db.addCollection(coll));
	    assertTrue(coll.getId() > -1);
	    
	    Collection newColl = db.getCollection(coll.getId());
	    assertEquals(coll, newColl);
	    
	    assertEquals(coll.getWords().get(0), newColl.getWords().get(0));
	}
	
	public void testUpdateWords() {
		assertTrue(db.addCollection(coll));
		Collection oldColl = db.getCollection(coll.getId());
		
		coll.addWord(word1);
		assertTrue(db.updateCollection(coll));
		Collection newColl = db.getCollection(coll.getId());
		assertEquals(coll, newColl);
		assertFalse(oldColl.equals(newColl));
		
		coll.removeWord(2);
		assertTrue(db.updateCollection(coll));
		newColl = db.getCollection(coll.getId());
		assertEquals(coll, newColl);
		assertEquals(oldColl, newColl);
	}
	
	public void testUpdateFields() {
		assertTrue(db.addCollection(coll));
		Collection oldColl = db.getCollection(coll.getId());
		
		coll.setName("new name");
		coll.setDescription("new description");
		coll.setOrder(5);
		
		assertTrue(db.updateCollection(coll));
		Collection newColl = db.getCollection(coll.getId());
		assertEquals(coll, newColl);
		assertFalse(oldColl.equals(newColl));
	}
	
	public void testDeleteCollection() {
		db.addCollection(coll);
		
		assertNotNull(db.getCollection(coll.getId()));
		assertTrue(db.deleteCollection(coll));
		assertNull(db.getCollection(coll.getId()));
		Word newWord = db.getWord(word2.getId());
		assertEquals(word2, newWord);
	}
}

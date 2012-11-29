package edu.upenn.cis350.Trace2Learn.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

public class DbAdapter_CharacterTest extends AndroidTestCase {
	DbAdapter db;
	Character a, b, c;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new DbAdapter(this.getContext());
		db.openTest();
		
		a = new Character();
		Stroke s = new Stroke(1, 1);
		s.addPoint(4, 5);
		a.addStroke(s);
		
		b = new Character();
		s = new Stroke(8, 9);
		s.addPoint(11, 13);
		b.addStroke(s);
		
		c = new Character();
		s = new Stroke(11, 14);
		s.addPoint(7, 9);
		s.addPoint(0.12F, 0.5F);
		c.addStroke(s);
		s = new Stroke(.05F, .72F);
		s.addPoint(19, 32);
		c.addStroke(s);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testAddCharacter() {
		assertEquals(-1, a.getId());
		assertTrue(db.addCharacter(a));
		assertEquals(1, a.getId());
		
		assertEquals(-1, b.getId());
		assertTrue(db.addCharacter(b));
		assertEquals(2, b.getId());
		
		assertEquals(-1, c.getId());
		assertTrue(db.addCharacter(c));
		assertEquals(3, c.getId());
		
		assertEquals(1, a.getOrder());
		assertEquals(2, b.getOrder());
		assertEquals(3, c.getOrder());
	}
	
	public void testRetrieveCharacter() {
		assertTrue(db.addCharacter(a));
		assertTrue(db.addCharacter(b));
		assertTrue(db.addCharacter(c));
		
		Character newA = db.getCharacter(a.getId());
		assertFalse(a == newA);
		assertEquals(a, newA);
		
		Character newB = db.getCharacter(b.getId());
		assertEquals(b, newB);
		
		Character newC = db.getCharacter(c.getId());
		assertEquals(c, newC);
	}
	
	public void testUpdateCharacterAttribute() {
		assertTrue(db.addCharacter(a));
		
		a.addAttribute("key", "value");
		assertTrue(db.updateCharacter(a));
		
		Set<String> expectedValues = new HashSet<String>();
		expectedValues.add("value");
		Character newA = db.getCharacter(a.getId());
		assertEquals(expectedValues, newA.getAttributes().get("key"));
		
		a.addAttribute("key2", "value");
		assertTrue(db.updateCharacter(a));
		newA = db.getCharacter(a.getId());
		assertEquals(expectedValues, newA.getAttributes().get("key"));
		assertEquals(expectedValues, newA.getAttributes().get("key2"));
		
		a.addAttribute("key", "value2");
		assertTrue(db.updateCharacter(a));
		newA = db.getCharacter(a.getId());
		expectedValues.add("value2");
		assertEquals(expectedValues, newA.getAttributes().get("key"));
	}
	
	public void testUpdateCharacterTag() {
		db.addCharacter(a);
		assertTrue(db.addCharacter(b));
		
		b.addTag("tag");
		assertTrue(db.updateCharacter(b));
		Set<String> expectedValues = new HashSet<String>();
		expectedValues.add("tag");
		Character newB = db.getCharacter(b.getId());
		assertEquals(expectedValues, newB.getTags());
		
		b.addTag("tag1");
		assertTrue(db.updateCharacter(b));
		expectedValues.add("tag1");
	    newB = db.getCharacter(b.getId());
		assertEquals(expectedValues, newB.getTags());
	}
	
	public void testUpdateStrokes() {
		db.addCharacter(a);
		db.addCharacter(b);
		assertTrue(db.addCharacter(c));
		Stroke s = new Stroke(0.5F, 0.2F);
		s.addPoint(0.3F, 0.25F);
		a.addStroke(s);
		
		assertTrue(db.updateCharacter(c));
		Character newC = db.getCharacter(c.getId());
		assertEquals(c.getStrokes(), newC.getStrokes());
	}
	
	public void testUpdateOrder() {
		db.addCharacter(a);
		assertTrue(db.addCharacter(b));
		
		b.setOrder(15);
		assertTrue(db.updateCharacter(b));
		Character newB = db.getCharacter(b.getId());
		assertEquals(15, newB.getOrder());
	}
	
	public void testRemoveAttributes() {
		assertTrue(db.addCharacter(a));
		a.addAttribute("key", "value");
		a.addAttribute("key", "value2");
		a.addAttribute("key2", "value");
		
		assertTrue(db.updateCharacter(a));
		Character newA = db.getCharacter(a.getId());
		Set<String> expected = new HashSet<String>();
		expected.add("value");
		assertEquals(expected, newA.getAttributes().get("key2"));
		expected.add("value2");
		assertEquals(expected, newA.getAttributes().get("key"));
		
		a.removeAttribute("key", "value");
		assertTrue(db.updateCharacter(a));
		newA = db.getCharacter(a.getId());
		expected.remove("value");
		assertEquals(expected, newA.getAttributes().get("key"));
		
		a.removeAttribute("key2", "value");
		assertTrue(db.updateCharacter(a));
		newA = db.getCharacter(a.getId());
		assertNull(newA.getAttributes().get("key2"));
	}
	
	public void testRemoveTags() {
		a.addTag("tag");
		a.addTag("tag2");
		a.addTag("tag3");
		a.addAttribute("key", "value");
		
		assertTrue(db.updateCharacter(a));
		Set<String> expected = new HashSet<String>();
		expected.add("tag");
		expected.add("tag2");
		expected.add("tag3");
		Character newA = db.getCharacter(a.getId());
		assertEquals(expected, newA.getTags());
		
		assertFalse(a.getTags().isEmpty());
		a.removeTag("tag");
		assertFalse(a.getTags().isEmpty());
		assertTrue(db.updateCharacter(a));
		expected.remove("tag");
		newA = db.getCharacter(a.getId());
		assertEquals(expected, newA.getTags());
		
		a.removeTag("tag2");
		a.addTag("tag4");
		assertTrue(db.updateCharacter(a));
		expected.remove("tag2");
		expected.add("tag4");
		newA = db.getCharacter(a.getId());
		assertEquals(expected, newA.getTags());
	}
	
	public void testDeleteCharacter() {
		a.addTag("tag");
		a.addAttribute("key", "value");
		db.addCharacter(a);
		b.addTag("tag");
		b.addAttribute("key", "value");
		db.addCharacter(b);
		
		assertNotNull(db.getCharacter(a.getId()));
		assertTrue(db.deleteCharacter(a));
		assertNull(db.getCharacter(a.getId()));
		Character newB = db.getCharacter(b.getId());
		assertEquals(b.getAttributes(), newB.getAttributes());
		assertEquals(b.getTags(), newB.getTags());
	}
	
	public void testGetAllCharacters() {
		db.addCharacter(a);
		db.addCharacter(b);
		
		List<Character> all = db.getAllCharacters();
		assertEquals(a, all.get(0));
		assertEquals(b, all.get(1));
		
		a.setOrder(2);
		b.setOrder(1);
		
		db.updateCharacter(a);
		db.updateCharacter(b);
		
		all = db.getAllCharacters();
		assertEquals(b, all.get(0));
		assertEquals(a, all.get(1));
	}
	
	public void testAddCharacterWithId() {
		Character d = new Character();
		Stroke s = new Stroke(11, 14);
		s.addPoint(7, 9);
		s.addPoint(0.12F, 0.5F);
		d.addStroke(s);
		s = new Stroke(.05F, .72F);
		s.addPoint(19, 32);
		d.addStroke(s);
		
		d.setId(17);
		db.updateCharacter(d);
		
		assertEquals(17, d.getId());
		
		Character newD = db.getCharacter(d.getId());
		assertEquals(d, newD);
	}
}

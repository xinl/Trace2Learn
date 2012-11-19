package edu.upenn.cis350.Trace2Learn.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

public class DbAdapter_SearchTest extends AndroidTestCase {

	DbAdapter db;
	Character a;
	Character b;
	Character c;
	String aTag = "WHATATAG";
	String bTag = "aTaBoY";
	String cAttr = "Coolboy";
	String cTag = "C";
	List<Character> expectedValues;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new DbAdapter(this.getContext());
		db.openTest();
		a = new Character();
		Stroke s = new Stroke(1, 1);
		s.addPoint(4, 5);
		a.addStroke(s);
		a.addTag(aTag);
		
		
		b = new Character();
		s = new Stroke(8, 9);
		s.addPoint(11, 13);
		b.addStroke(s);
		b.addTag(bTag);
		
		c = new Character();
		s = new Stroke(11, 14);
		s.addPoint(7, 9);
		s.addPoint(0.12F, 0.5F);
		c.addStroke(s);
		s = new Stroke(.05F, .72F);
		s.addPoint(19, 32);
		c.addStroke(s);
		c.addAttribute("key", cAttr);
		c.addTag(cTag);
		db.addCharacter(a);
		db.addCharacter(b);
		db.addCharacter(c);

		expectedValues = new ArrayList<Character>();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testGetCharsByAttr_exactMatchOnLongTag() {
		expectedValues.add(a);
		assertEquals(expectedValues, db.getCharactersByAttribute("WHATATAG"));
	}
	
	public void testGetCharsByAttr_caseInsensitiveSeachOnLongTags() {
		expectedValues.add(b);
		assertEquals(expectedValues, db.getCharactersByAttribute("ataboy"));
		assertEquals(expectedValues, db.getCharactersByAttribute("ATABOY"));
		assertEquals(expectedValues, db.getCharactersByAttribute("AtAbOy"));
	}
	
    public void testGetCharsByAttr_searchesValues() {
    	expectedValues.add(c);
    	assertEquals(expectedValues, db.getCharactersByAttribute("CoolBoy"));
    }
	
	public void test_getCharsByTag_matchMultipleTags() {
		expectedValues.add(a);
		expectedValues.add(b);
		assertEquals(expectedValues, db.getCharactersByAttribute("ata"));
		
		expectedValues.add(c);
		expectedValues.remove(a);
		assertEquals(expectedValues, db.getCharactersByAttribute("boy"));
	}
	
	public void test_getCharsByTag_exactMatchShortTag() {
		expectedValues.add(c);
		assertEquals(expectedValues, db.getCharactersByAttribute("C"));
		assertEquals(expectedValues, db.getCharactersByAttribute("c"));
	}
	
	public void test_getCharsByTag_noMatchShortTag() {
		assertEquals(expectedValues, db.getCharactersByAttribute("at"));
	}
}

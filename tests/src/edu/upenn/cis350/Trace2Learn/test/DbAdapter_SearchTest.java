package edu.upenn.cis350.Trace2Learn.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;

public class DbAdapter_SearchTest extends AndroidTestCase {

	DbAdapter db;
	LessonCharacter a;
	LessonCharacter b;
	LessonCharacter c;
	String aTag = "WHATATAG";
	String bTag = "aTaBoY";
	String cTag = "C";
	List<Long> expectedValues;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new DbAdapter(this.getContext());
		db.openTest();
		a = new LessonCharacter();
		b = new LessonCharacter();
		c = new LessonCharacter();
		db.addCharacter(a);
		db.addCharacter(b);
		db.addCharacter(c);
		db.createTags(a.getId(), aTag);
		db.createTags(b.getId(), bTag);
		db.createTags(c.getId(), cTag);
		expectedValues = new ArrayList<Long>();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void test_getCharsByTag_exactMatchOnLongTag() {
		expectedValues.add(a.getId());
		assertEquals(expectedValues, db.getCharsByTag("WHATATAG"));
	}
	
	public void test_getCharsByTag_caseInsensitiveSeachOnLongTags() {
		expectedValues.add(b.getId());
		assertEquals(expectedValues, db.getCharsByTag("ataboy"));
		assertEquals(expectedValues, db.getCharsByTag("ATABOY"));
		assertEquals(expectedValues, db.getCharsByTag("AtAbOy"));
	}
	
	public void test_getCharsByTag_matchMultipleTags() {
		expectedValues.add(a.getId());
		expectedValues.add(b.getId());
		assertEquals(expectedValues, db.getCharsByTag("ata"));
	}
	
	public void test_getCharsByTag_exactMatchShortTag() {
		expectedValues.add(c.getId());
		assertEquals(expectedValues, db.getCharsByTag("C"));
		assertEquals(expectedValues, db.getCharsByTag("c"));
	}
	
	public void test_getCharsByTag_noMatchShortTag() {
		assertEquals(expectedValues, db.getCharsByTag("at"));
	}
}

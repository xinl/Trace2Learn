package edu.upenn.cis350.Trace2Learn.test;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import android.test.AndroidTestCase;

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
}

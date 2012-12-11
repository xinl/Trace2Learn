package edu.upenn.cis350.Trace2Learn.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.test.AndroidTestCase;
import edu.upenn.cis350.Trace2Learn.ExportActivity;
import edu.upenn.cis350.Trace2Learn.ImportActivity;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class ExportImportTest extends AndroidTestCase {

	DbAdapter db;
	Character a, b, c;
	File file;
	
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
		a.addAttribute("language", "English");
		
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
		
		String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
	    File folderPath = new File(extStorageDirectory + "/Trace2Learn/Test");
		if(!folderPath.exists()) {
			folderPath.mkdirs();
		}
		file = new File(folderPath + "/test");
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.closeTest();
	}
	
	public void testExportCharacters() throws Exception {
		ExportActivity.exportToXML(file, false, 0, db);
		file.createNewFile();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = in.readLine();
		String expectedLine = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data>" +
				"<Characters><character id=\"1\" order=\"1\"><tags><tag>a</tag></tags>" +
				"<attributes><attribute type=\"language\">English</attribute></attributes>" +
				"<strokes>";
		assertTrue(line.startsWith(expectedLine));
		String finalLine = line;
		while ((line = in.readLine()) != null) {
			finalLine = line;
		}
		assertTrue(finalLine.endsWith("</data>"));
		in.close();
		file.delete();
	}
	
	public void testImportCharacters() throws Exception {
		ExportActivity.exportToXML(file, false, 0, db);
		file.createNewFile();
		
		db.deleteCharacter(a);
		db.deleteCharacter(b);
		db.deleteCharacter(c);
		
		List<Character> characters = new ArrayList<Character>();
		List<Word> words = new ArrayList<Word>();
		List<Collection> collections = new ArrayList<Collection>();
		
		ImportActivity.importXML(file, characters, words, collections);
		file.delete();
		
		assertEquals(0, collections.size());
		assertEquals(0, words.size());
		assertEquals(3, characters.size());
		
		assertEquals(a, characters.get(0));
		assertEquals(b, characters.get(1));
		assertEquals(c, characters.get(2));

		assertNull(db.getCharacter(c.getId()));
		
		ImportActivity.saveData(db, characters, words, collections);
		
		Character newA = db.getCharacter(a.getId());
		Character newB = db.getCharacter(b.getId());
		Character newC = db.getCharacter(c.getId());
		
		assertEquals(a, newA);
		assertEquals(b, newB);
		assertEquals(c, newC);
	}
	
	public void testExportImportCollections() throws Exception {
		Word word1 = new Word();
		word1.addCharacter(c);
		word1.addCharacter(a);
		word1.addCharacter(b);
		word1.addTag("cab");
		
		Word word2 = new Word();
		word2.addCharacter(a);
		word2.addCharacter(b);
		word2.addTag("ab");
		
		db.addWord(word1);
		db.addWord(word2);
		
		Collection coll = new Collection();
		coll.setName("Collection 1");
		coll.setDescription("The first collection.");
		coll.addWord(word1);
		coll.addWord(word2);
		
		db.addCollection(coll);
		
		//export characters
		ExportActivity.exportToXML(file, false, 0, db);
		file.createNewFile();
		long charactersLength = file.length();
		file.delete();
		
		//export collection
		ExportActivity.exportToXML(file, true, coll.getId(), db);
		file.createNewFile();
		long collectionLength = file.length();
		
		assertTrue(charactersLength < collectionLength);
		
		db.deleteWord(word1);
		assertNull(db.getWord(word1.getId()));
		db.deleteWord(word2);
		assertNull(db.getWord(word2.getId()));
		
		db.deleteCharacter(a);
		assertNull(db.getCharacter(a.getId()));
		
		db.deleteCollection(coll);
		assertNull(db.getCollection(coll.getId()));
		
		List<Character> characters = new ArrayList<Character>();
		List<Word> words = new ArrayList<Word>();
		List<Collection> collections = new ArrayList<Collection>();
		
		ImportActivity.importXML(file, characters, words, collections);
		file.delete();
		
		assertEquals(1, collections.size());
		assertEquals(2, words.size());
		assertEquals(3, characters.size());

		assertTrue(characters.contains(a));
		assertTrue(characters.contains(b));
		assertTrue(characters.contains(c));

		assertTrue(words.contains(word1));
		assertTrue(words.contains(word2));
		
		assertEquals(coll, collections.get(0));
		
		ImportActivity.saveData(db, characters, words, collections);
		
		Character newA = db.getCharacter(a.getId());
		Character newB = db.getCharacter(b.getId());
		Character newC = db.getCharacter(c.getId());
		
		assertEquals(a, newA);
		assertEquals(b, newB);
		assertEquals(c, newC);
		
		Word newWord1 = db.getWord(word1.getId());
		Word newWord2 = db.getWord(word2.getId());
		
		assertEquals(word1, newWord1);
		assertEquals(word2, newWord2);
		
		Collection newColl = db.getCollection(coll.getId());
		assertEquals(coll, newColl);
	}
}

package edu.upenn.cis350.Trace2Learn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class ImportActivity extends Activity {

	private DbAdapter dba;
	private ArrayAdapter<String> arrAdapter;
	private ArrayList<String> fileList = new ArrayList<String>();
	private String subFolder, extStorageDirectory;
	private File folderPath;
	private Context thisContext;
	
	//Controls
	private ListView lv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisContext = this;

		dba = new DbAdapter(this);
		dba.open();

		setContentView(R.layout.import_xml);

		lv = (ListView) findViewById(R.id.importList);

		subFolder = "/Trace2Learn/Collection";
		extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		folderPath = new File(extStorageDirectory + subFolder);
		if(!folderPath.exists()) {
			folderPath.mkdirs();
		}

		File[] files = folderPath.listFiles();
		for(File f: files) {
			String[] tempArray = f.toString().split("/");
			fileList.add(tempArray[tempArray.length - 1]);
		}

		arrAdapter = new ArrayAdapter<String>(thisContext, 
				android.R.layout.simple_list_item_1, fileList);
		arrAdapter.notifyDataSetChanged();

		lv.setAdapter(arrAdapter);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long this_id) {
				AlertDialog dialog = (AlertDialog) onCreateDialog(position);
				dialog.show();
			}});
		setTitle(getTitle() + " È Import");
	}
	
	protected Dialog onCreateDialog(int position) {
		final int index = position;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.importXml_confirm);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String fileName = fileList.get(index);
				File importFile = new File(folderPath + "/" + fileName);
				try {
					importXML(importFile);
					Toast.makeText(thisContext, "Import successful.", 
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
				    Log.e("IMPORTING", e.getMessage());
				    Toast.makeText(thisContext, "Import failed.", 
							Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
	
	public void importXML(File xmlFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		NodeList characterNodes = doc.getElementsByTagName("character");
		NodeList wordNodes = doc.getElementsByTagName("word");
		NodeList collectionNodes = doc.getElementsByTagName("collection");
		
		List<Character> characters = new ArrayList<Character>();
		for (int i = 0; i < characterNodes.getLength(); i++) {
			Element element = (Element) characterNodes.item(i);
			characters.add(createCharacter(element));
		}
		
		List<Word> words = new ArrayList<Word>();
		for (int i = 0; i < wordNodes.getLength(); i++) {
			Element element = (Element) wordNodes.item(i);
			words.add(createWord(element));
		}
		
		List<Collection> collections = new ArrayList<Collection>();
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			Element element = (Element) collectionNodes.item(i);
			collections.add(createCollection(element));
		}
		
		//TODO: verify data integrity here
		
		for (Character c: characters) {
			dba.updateCharacter(c);
		}
		
		for (Word w: words) {
			dba.updateWord(w);
		}
		
		for (Collection cl: collections) {
			dba.updateCollection(cl);
		}
	}
	
	/**
	 * Create a single character item
	 * @param character an element in an xml document representing a character
	 * @return the character based on the xml
	 */
	static Character createCharacter(Element character) {
		Character c = new Character();
		//get attributes
	    long id = Long.valueOf(character.getAttribute("id"));
	    c.setId(id);
	    long order = Long.valueOf(character.getAttribute("order"));
	    c.setOrder(order);
	    
	    String strokesString = getValue(character.getElementsByTagName("strokes").item(0));
	    byte[] strokesData = Base64.decode(strokesString, Base64.DEFAULT);
	    List<Stroke> strokes = Stroke.decodeStrokesData(strokesData);
	    c.setStrokes(strokes);
	    
	    NodeList tagNodes = character.getElementsByTagName("tag");
	    for (int i = 0; i < tagNodes.getLength(); i++) {
	    	String tag = getValue(tagNodes.item(i));
	    	c.addTag(tag);
	    }
	    
	    NodeList attributeNodes = character.getElementsByTagName("attribute");
	    for (int i = 0; i < attributeNodes.getLength(); i++) {
	    	Element attribute = (Element) attributeNodes.item(i);
	    	String type = attribute.getAttribute("type");
	    	String attValue = getValue(attribute);
	    	c.addAttribute(type, attValue);
	    }
	    
		return c;
	}
	
	/**
	 * Create a single word item
	 * @param ele an element in an xml document representing a word
	 * @return the word based on the xml
	 */
	static Word createWord(Element ele) {
		Word word = new Word();
		
		//get attributes
	    long id = Long.valueOf(ele.getAttribute("id"));
	    word.setId(id);
	    long order = Long.valueOf(ele.getAttribute("order"));
	    word.setOrder(order);
	    
	    NodeList tagNodes = ele.getElementsByTagName("tag");
	    for (int i = 0; i < tagNodes.getLength(); i++) {
	    	String tag = getValue(tagNodes.item(i));
	    	word.addTag(tag);
	    }
	    
	    NodeList attributeNodes = ele.getElementsByTagName("attribute");
	    for (int i = 0; i < attributeNodes.getLength(); i++) {
	    	Element attribute = (Element) attributeNodes.item(i);
	    	String type = attribute.getAttribute("type");
	    	String attValue = getValue(attribute);
	    	word.addAttribute(type, attValue);
	    }
	    
	    NodeList charidNodes = ele.getElementsByTagName("charid");
	    for (int i = 0; i < charidNodes.getLength(); i++) {
	    	long charid = Long.valueOf(getValue(charidNodes.item(i)));
	    	Character ch = new Character();
	    	ch.setId(charid);
	    	word.addCharacter(ch);
	    }
	    
		return word;
	}
	
	/**
	 * Create a single collection item
	 * @param ele an element in an xml document representing a collection
	 * @return the collection based on the xml
	 */
	static Collection createCollection(Element ele) {
		Collection collection = new Collection();
		
		//get attributes
	    long id = Long.valueOf(ele.getAttribute("id"));
	    collection.setId(id);
	    
	    NodeList nameNodes = ele.getElementsByTagName("name");
	    collection.setName(getValue(nameNodes.item(0)));
	    
	    NodeList descriptionNodes = ele.getElementsByTagName("description");
	    collection.setDescription(getValue(descriptionNodes.item(0)));
	    
	    NodeList wordidNodes = ele.getElementsByTagName("wordid");
	    for (int i = 0; i < wordidNodes.getLength(); i++) {
	    	long wordid = Long.valueOf(getValue(wordidNodes.item(i)));
	    	Word w = new Word();
	    	w.setId(wordid);
	    	collection.addWord(w);
	    }
	    
		return collection;
	}
	
	/**
	 * Retrieve the value of a node from a document
	 * @param node the node whose value will be retrieved
	 * @return the value of the node as a string
	 */
	static private String getValue(Node node) {
		NodeList children = node.getChildNodes();
		if (children.getLength() == 0) {
			return "";
		}
	    return children.item(0).getNodeValue();
	}

	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
}

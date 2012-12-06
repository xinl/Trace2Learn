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
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

public class ImportCharacterActivity extends Activity {

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

		setContentView(R.layout.import_chars);

		lv = (ListView) findViewById(R.id.importList);

		subFolder = "/Trace2Learn/Character";
		extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		folderPath = new File(extStorageDirectory + subFolder);
		if(!folderPath.exists()) {
			folderPath.mkdir();
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
					List<Character> characters = importCharacters(importFile);
					for (Character c: characters) {
						dba.updateCharacter(c);
					}
					Toast.makeText(thisContext, "Import successful.", 
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
				    Log.e("CHARACTER IMPORTING", e.getMessage());
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
	
	/**
	 * Creates a list of characters represented by the xmlFile.
	 * @param xmlFile file containing information about characters.
	 * @return a list of characters based on the file.
	 * @throws Exception if the file could not be parsed.
	 */
	static List<Character> importCharacters(File xmlFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		NodeList characterNodes = doc.getElementsByTagName("character");
		List<Character> characters = new ArrayList<Character>();
		for (int i = 0; i < characterNodes.getLength(); i++) {
			Element element = (Element) characterNodes.item(i);
			characters.add(createCharacter(element));
		}
		return characters;
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
	 * Retrieve the value of a node from a document
	 * @param node the node whose value will be retrieved
	 * @return the value of the node as a string
	 */
	static private String getValue(Node node) {
	    return node.getChildNodes().item(0).getNodeValue();
	}

	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
}

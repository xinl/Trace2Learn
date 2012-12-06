package edu.upenn.cis350.Trace2Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import edu.upenn.cis350.Trace2Learn.Database.Word;

public class ExportActivity extends Activity {

	private DbAdapter dba;
	ArrayAdapter<String> arrAdapter;
	ArrayList<String> fileList = new ArrayList<String>();
	String subFolder, extStorageDirectory;
	File folderPath;
	Context thisContext;
	List<edu.upenn.cis350.Trace2Learn.Database.Character> listOfCharacters;
	boolean collectionMode;
	long collectionId;
	List<Word> listOfWords;
	
	//Controls
	private EditText editExportText;
	private ListView lv;
	private Button exportButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dba = new DbAdapter(this);
        dba.open();
        
        collectionId = this.getIntent().getLongExtra("ID", -1);
        if(collectionId == -1) collectionMode = false;
        else collectionMode = true;
        
        //set up the screen depending on the the mode
        if(collectionMode) setContentView(R.layout.export_collection);
        else setContentView(R.layout.export_character);
        
		thisContext = this;
		
		editExportText = (EditText) findViewById(R.id.editExport);
        lv = (ListView) findViewById(R.id.exportList);
        exportButton = (Button) findViewById(R.id.export_button);

        if(collectionMode) subFolder = "/Trace2Learn/Collection";
        else subFolder = "/Trace2Learn/Character";
        
		extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		folderPath = new File(extStorageDirectory + subFolder);
		if(!folderPath.exists()) {
			folderPath.mkdir();
		}
		
		File[] listOfFiles = folderPath.listFiles();
		for(File f: listOfFiles) {
			String[] tempArray = f.toString().split("/");
			fileList.add(tempArray[tempArray.length - 1]);
		}
		
		arrAdapter = new ArrayAdapter<String>(this, 
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
		builder.setMessage(R.string.deleteXML_confirm);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String fileName = fileList.get(index);
				File fileToDelete = new File(folderPath + "/" + fileName);
				if(fileToDelete.delete()) {
					Toast.makeText(thisContext, "File Successfully Deleted", 
							Toast.LENGTH_LONG).show();
					fileList.remove(index);
					arrAdapter.notifyDataSetChanged();
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
	
	
	public void onExportButtonClick(View view) {
		if(!(view == exportButton)) return;
		Editable input = editExportText.getText();
		editExportText.setText("");
		String fileName = input.toString().trim();
		boolean xmlIncluded = false;
		
		//File Validation
		if(fileName.equals("")) {
			Toast.makeText(thisContext, "Export file must have a name", 
					Toast.LENGTH_LONG).show();
			return;
		}
		else if(fileName.contains(" ")) {
			Toast.makeText(thisContext, "No spaces allowed in the file name", 
					Toast.LENGTH_LONG).show();
			return;
		}
		else if(fileName.endsWith(".xml")) {
			xmlIncluded = true;
		}
		
		if(!xmlIncluded) fileName = fileName + ".xml";
		
		File file = new File(folderPath + "/" + fileName);
		if(file.exists()) {
			Toast.makeText(thisContext, "Error: the same file name exists", 
					Toast.LENGTH_LONG).show();
			return;
		}

		exportToXML(file);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Print the contents of the file for the debugging purpose
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String text;
			while((text = in.readLine()) != null)
				System.out.println(text); 
			in.close();
			System.out.println("file size:" + file.length());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Toast.makeText(thisContext, "file successfully exported", 
				Toast.LENGTH_LONG).show();
		fileList.add(fileName);
		arrAdapter.notifyDataSetChanged();
	}

	
	/**
	 * Export all characters in the database to the file
	 * @param file the file to be written
	 */
	private void exportToXML(File file) {
		Collection collection = null;
		
		//Populate necessary data structure
		if(collectionMode) {
			listOfCharacters = new ArrayList<Character>();
			collection = dba.getCollection(collectionId);
			listOfWords = collection.getWords();
			System.out.println("word list size:" + listOfWords.size());
			//populate the list of chars
			for(Word word: listOfWords) {
				List<Character> listOfChars = word.getCharacters();
				System.out.println("char list size:" + listOfChars.size());
				for(int i = 0; i < listOfChars.size(); i++) {
					Character character = listOfChars.get(i);
					if(!listOfCharacters.contains(character)) {
						listOfCharacters.add(character);
					}
				}		
			}
		}
		else listOfCharacters = dba.getAllCharacters();
		
		edu.upenn.cis350.Trace2Learn.Database.Character thisCharacter;
		
		DocumentBuilder docBuilder = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		Document doc = docBuilder.newDocument();
		Element data = doc.createElement("data");
		Element characters = doc.createElement("Characters");
		
		doc.appendChild(data);
//		data.setAttribute("version", XXX);
		data.appendChild(characters);
		
		for(int i = 0; i < listOfCharacters.size(); i++) {
			thisCharacter = listOfCharacters.get(i);
			convertCharacterToXML(doc, thisCharacter, characters);
		} 

		//create <words> ... </collections> elements
		if(collectionMode) {
			Element words = doc.createElement("words");
			data.appendChild(words);

			for(int i = 0; i < listOfWords.size(); i++) {
				Word thisWord = listOfWords.get(i);
				convertWordToXML(doc, thisWord, words);
			}// word loop 
			
			//collections
			Element collections = doc.createElement("collections");
			Element collectionEle = doc.createElement("collection");
			data.appendChild(collections);
			
			//collection id attribute
			Long collectionId = collection.getId();
			collectionEle.setAttribute("id", collectionId.toString());
			
			//collection name
			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(collection.getName()));
			collectionEle.appendChild(name);
			
			//description
			Element description = doc.createElement("description");
			String descrp = collection.getDescription();
			if(!(descrp == null)) description.appendChild(
					doc.createTextNode(collection.getDescription()));
			collectionEle.appendChild(description);
			
			//word ids
			Element wordids = doc.createElement("wordids");
			
			collectionEle.appendChild(wordids);

			for(Word wd: listOfWords) {
				Long thisWordid = wd.getId();
				Element wordid = doc.createElement("wordid");
				wordid.appendChild(doc.createTextNode(thisWordid.toString()));			
				wordids.appendChild(wordid);
			}
			
			collections.appendChild(collectionEle);
		}		
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);

			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper function. Convert a single char into the file
	 * 
	 * @param doc 
	 * @param thisChar A character to be XML converted
	 * @param characters a container of thisChar 
	 */
	private void convertCharacterToXML(Document doc, Character thisChar,
			Element characters) {
		Element character = doc.createElement("character");
		
		//Character attribute
		Long idValue = Long.valueOf(thisChar.getId());
		character.setAttribute("id", idValue.toString());
		Long orderValue = Long.valueOf(thisChar.getOrder());
		character.setAttribute("order", orderValue.toString());
		
		//tags
		Element tags = doc.createElement("tags");
		
		character.appendChild(tags);
		
		Set<String> tagSet = thisChar.getTags();
		for(String str: tagSet) {
			Element tag = doc.createElement("tag");
			tag.appendChild(doc.createTextNode(str));
			tags.appendChild(tag);
		}
		
		//attributes
		Element attributes = doc.createElement("attributes");
		
		Map<String, Set<String>> attributeSet = thisChar.getAttributes();
		for (Map.Entry<String, Set<String>> entry : attributeSet.entrySet()) {
			
		    Set<String> attributeValues = entry.getValue();
		    for(String str: attributeValues) {
		    	Element attribute = doc.createElement("attribute");
			    String attributeType = entry.getKey();
			    attribute.setAttribute("type", attributeType);
		    	attribute.appendChild(doc.createTextNode(str));
		    	attributes.appendChild(attribute);
		    }
		    
		}
		character.appendChild(attributes);
		
		//strokes
		Element strokes = doc.createElement("strokes");
		byte[] byteStroke = Stroke.encodeStrokesData(thisChar.getStrokes());
		String encodedStroke = Base64.encodeToString(byteStroke, Base64.DEFAULT);
		strokes.appendChild(doc.createTextNode(encodedStroke));
		character.appendChild(strokes);
		
		characters.appendChild(character);
	}
	
	
	/**
	 * Helper function. Convert a single word into the file
	 * @param doc
	 * @param thisWord word to be converted
	 * @param words a container of thisWord
	 */
	private void convertWordToXML(Document doc, Word thisWord,
			Element words) {
		Element word = doc.createElement("word");

		//word id and order attribute
		Long wordId = thisWord.getId();
		word.setAttribute("id", wordId.toString());
		Long wordOrder = thisWord.getId();
		word.setAttribute("order", wordOrder.toString());

		//tags
		Element tags = doc.createElement("tags");

		word.appendChild(tags);

		Set<String> tagSet = thisWord.getTags();
		for(String str: tagSet) {
			Element tag = doc.createElement("tag");
			tag.appendChild(doc.createTextNode(str));
			tags.appendChild(tag);
		}

		//attributes
		Element attributes = doc.createElement("attributes");

		Map<String, Set<String>> attributeSet = thisWord.getAttributes();
		for (Map.Entry<String, Set<String>> entry : attributeSet.entrySet()) {

			Set<String> attributeValues = entry.getValue();
			for(String str: attributeValues) {
				Element attribute = doc.createElement("attribute");
				String attributeType = entry.getKey();
				attribute.setAttribute("type", attributeType);
				attribute.appendChild(doc.createTextNode(str));
				attributes.appendChild(attribute);
			}

		}
		word.appendChild(attributes);
		
		//character ID
		Element charIds = doc.createElement("charids");
        List<Long> listOfCharIds = new ArrayList<Long>();
        List<Character> charsOfThisWord = thisWord.getCharacters();
        for (Character character: charsOfThisWord) {
        	listOfCharIds.add(character.getId());
        }
		
        for(Long id: listOfCharIds) {
        	Element charId = doc.createElement("charid");
        	charId.appendChild(doc.createTextNode(id.toString()));
        	charIds.appendChild(charId);
        }
        word.appendChild(charIds);
        
        words.appendChild(word);
	}
	
	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
	
}

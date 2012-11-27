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

import edu.upenn.cis350.Trace2Learn.Database.Base64EncodeDecode;
import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ExportCharacterActivity extends Activity {

	private DbAdapter dba;
	ArrayAdapter<String> arrAdapter;
	ArrayList<String> fileList = new ArrayList<String>();
	String subFolder, extStorageDirectory;
	File folderPath;
	Context thisContext;
	List<edu.upenn.cis350.Trace2Learn.Database.Character> listOfCharacters;
	
	//Controls
	private EditText editExportText;
	private ListView lv;
	private Button exportButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dba = new DbAdapter(this);
        dba.open();
        
		setContentView(R.layout.export);
		thisContext = this;
		
		editExportText = (EditText) findViewById(R.id.editExport);
        lv = (ListView) findViewById(R.id.exportList);
        exportButton = (Button) findViewById(R.id.export_button);

        subFolder = "/Trace2Learn";
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
			Toast.makeText(thisContext, "Export file must have a name", Toast.LENGTH_LONG).show();
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

		exportCharactersToXML(file);
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

	private void exportCharactersToXML(File file) {
		listOfCharacters = dba.getAllCharacters();
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
			Element character = doc.createElement("Character");
			
			//Character attribute
			Long idValue = Long.valueOf(thisCharacter.getId());
			character.setAttribute("id", idValue.toString());
			Long orderValue = Long.valueOf(thisCharacter.getOrder());
			character.setAttribute("order", orderValue.toString());
			
			//tags
			Element tags = doc.createElement("tags");
			
			character.appendChild(tags);
			
			Set<String> tagSet = thisCharacter.getTags();
			for(String str: tagSet) {
				Element tag = doc.createElement("tag");
				tag.appendChild(doc.createTextNode(str));
				tags.appendChild(tag);
			}
			
			//attributes
			Element attributes = doc.createElement("attributes");
			
			Map<String, Set<String>> attributeSet = thisCharacter.getAttributes();
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
			byte[] byteStroke = Stroke.encodeStrokesData(thisCharacter.getStrokes());
			String encodedStroke = Base64EncodeDecode.encode(byteStroke);
			strokes.appendChild(doc.createTextNode(encodedStroke));
			character.appendChild(strokes);
			
			characters.appendChild(character);
		} // Characters loop
		
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
	
	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}
	
}

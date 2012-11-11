package edu.upenn.cis350.Trace2Learn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

	ArrayAdapter<String> arrAdapter;
	ArrayList<String> fileList = new ArrayList<String>();
	String subFolder, extStorageDirectory;
	File folderPath;
	Context thisContext;
	
	//Controls
	private EditText editExportText;
	private ListView lv;
	private Button exportButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		String fileName = input.toString();
		if(fileName.trim().equals("")) return;
		fileName = fileName + ".xml";
		
		//Set edit text back to nothing
		editExportText.setText("");
		
		//XXX  Populating all data into the xml file
		
		File file = new File(folderPath + "/" + fileName);
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		fileList.add(fileName);
		arrAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
}

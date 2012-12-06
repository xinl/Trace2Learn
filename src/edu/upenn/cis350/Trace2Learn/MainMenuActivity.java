package edu.upenn.cis350.Trace2Learn;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainMenuActivity extends ListActivity {
	
	static final String[] APPS = new String[]
		{ 
			"Create Character", 
			"Create Word",
			"Browse All Characters",
			"Browse All Words",
			"Browse All Collections",
			"Export Character Library",
			"Import Character Library",
			"Import Collection"
		};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_menu,APPS));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		final Context c=this;
		
		listView.setOnItemClickListener(
			new OnItemClickListener() 
			{
				public void onItemClick(
						AdapterView<?> parent,
						View view,
						int position,
						long id) 
				{
					CharSequence clicked = ((TextView) view).getText();
					if(clicked.equals(APPS[0]))
					{
						Intent i = new Intent().setClass(c, CreateCharacterActivity.class);
						startActivity(i);
					}
					else if(clicked.equals(APPS[1]))
					{
	
						Intent i = new Intent(c, CreateWordActivity.class);
						i.putExtra("ISCREATE", true);
						startActivity(i);
					
					}
					else if(clicked.equals(APPS[2])){
						Intent i = new Intent(c, BrowseCharactersActivity.class);
						startActivity(i);
					}
					else if(clicked.equals(APPS[3])){
						Intent i = new Intent(c, BrowseWordsActivity.class);
						startActivity(i);
					}
					else if(clicked.equals(APPS[4])){
						Intent i = new Intent(c, BrowseCollectionsActivity.class);
						startActivity(i);
					}
					else if(clicked.equals(APPS[5])){
						Intent i = new Intent(c, ExportActivity.class);
						i.putExtra("ID", -1L);
						startActivity(i);
					}
					else if(clicked.equals(APPS[6])){
						Intent i = new Intent(c, ImportCharacterActivity.class);
						startActivity(i);
					}
					else if(clicked.equals(APPS[7])){
						Intent i = new Intent(c, ImportActivity.class);
						startActivity(i);
					}
					else {
						//XXX
					}
				}
			}
		);
	}
}

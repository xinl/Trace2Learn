package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
import edu.upenn.cis350.Trace2Learn.Database.Collection;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem;
import edu.upenn.cis350.Trace2Learn.Database.Word;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseWordsActivity extends ListActivity implements Filterable {
	private DbAdapter dba;
	private ListView collectionList; // list of words to display in listview
	private List<TraceableItem> items;
	private View layout;
	private PopupWindow window;
	private Word lw;
	private long id;
	private boolean fromCollection;
	private Collection collection;
	private String collectionName = "";

	static final List<String> COLLECTION_ITEM_CONTEXT_MENU = new ArrayList<String>(Arrays.asList(new String[] { 
			"Remove from collection" }));
	static final List<String> WORDLIST_ITEM_CONTEXT_MENU = new ArrayList<String>(Arrays.asList(new String[] { 
			"Add to Collection",
			"Edit Tags",
			"Move Up",
			"Move Down",
			"Delete" }));

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_words);
		dba = new DbAdapter(this);
		dba.open();

		// Set up the ListView
		items = new ArrayList<TraceableItem>(); // items to show in ListView to choose from
		id = this.getIntent().getLongExtra("ID", -1);
		fromCollection = this.getIntent().getBooleanExtra("fromCollection", false);

		// id=1;
		if (id == -1) {

			List<Word> words = dba.getAllWords();
			for (Word w : words) {
				items.add(w);
			}
			setTitle(getTitle() + " » All Words");
			Button b = new FilterButton(this);
			LinearLayout layout = (LinearLayout) findViewById(R.id.button_panel);
			layout.addView(b);
		} else {
			collection = dba.getCollection(id);
			collectionName = collection.getName();

			for (Word w : collection.getWords()) {
				items.add(w);
			}
			updateBrowseCollectionTitle();
		}
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new TraceableListAdapter(this, items, vi));

		registerForContextMenu(getListView());
	}
	
	private void updateBrowseCollectionTitle() {
		setTitle(getResources().getString(R.string.app_name) + " » " + collectionName + " (" + collection.size() + " words)");
	}

	@Override
	public void filterView(String filter) {
		List<Word> words = dba.getWordsByAttribute(filter);
		items.clear();
		for (Word w : words) {
			items.add(w);
		}
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new TraceableListAdapter(this, items, vi));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		clickOnItem(items.get(position));
	}

	// when character is clicked, it starts the display mode for that char
	public void clickOnItem(TraceableItem li) {
		Intent intent = new Intent();
		Bundle bun = new Bundle();

		bun.putString("mode", "display");
		bun.putLong("wordId", li.getId());
		if (id != -1) {
			bun.putLong("collectionId", id);
			bun.putString("collectionName", collectionName);
		}

		intent.setClass(this, ViewWordActivity.class);
		intent.putExtras(bun);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Options");
		List<String> menuItems;
		if (fromCollection) {
			menuItems = COLLECTION_ITEM_CONTEXT_MENU;
		} else {
			menuItems = WORDLIST_ITEM_CONTEXT_MENU;
		}
		for (int i = 0; i < menuItems.size(); i++) {
			menu.add(Menu.NONE, i, i, menuItems.get(i));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		lw = (Word) items.get(info.position);
		if (fromCollection) {
			if (menuItemIndex == 0) {
				int targetWordIndex = -1;
				for (Word word : collection.getWords()) {
					targetWordIndex++;
					if (lw.getId() == word.getId()) {
						break;
					}
				}
				if (targetWordIndex < 0) {
					return false;
				}
				collection.removeWord(targetWordIndex);
				if (dba.updateCollection(collection)) {
					showToast("Successfully removed.");
					items.remove(info.position);
					resetListView();
					updateBrowseCollectionTitle();
					return true;
				} else {
					showToast("Word could not be removed.");
					return false;
				}
			}
		} else {
			// add to collection
			if (menuItemIndex == 0) {
				initiatePopupWindow();
				return true;
			}

			else if (menuItemIndex == 1) {
				Intent i = new Intent(this, EditTagsActivity.class);
				i.putExtra("ID", lw.getId());
				i.putExtra("TYPE", "WORD");
				startActivity(i);
				finish();
				return true;
			}
			// Qin move up
			else if (menuItemIndex == 2) {
				if (info.position == 0) {
					showToast("Word can't be moved up");
					return false;
				} else {
					Word above = (Word) items.get(info.position - 1);
					swapPositions(lw, above, info.position, info.position - 1);

					showToast("Successfully moved up");
					return true;
				}
			}
			// Qin move down
			else if (menuItemIndex == 3) {
				if (info.position == items.size() - 1) {
					showToast("Word can't be moved down");
					return false;
				} else {
					Word above = (Word) items.get(info.position + 1);
					swapPositions(lw, above, info.position, info.position + 1);
					showToast("Successfully moved down");
					return true;
				}
			}
			// delete
			else if (menuItemIndex == 4) {
				if (dba.deleteWord(lw)) {
					showToast("Successfully deleted");
					items.remove(info.position);
					resetListView();
					return true;
				} else {
					showToast("Word could not be deleted.");
					return false;
				}
			}
		}
		
		return false;
	}

	public void resetListView() {
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new TraceableListAdapter(this, items, vi));
	}

	private void swapPositions(Word w1, Word w2, int position1, int position2) {
		long temp = w2.getOrder();
		w2.setOrder(w1.getOrder());
		w1.setOrder(temp);
		dba.updateWord(w1);
		dba.updateWord(w2);

		items.set(position1, w2);
		items.set(position2, w1);

		resetListView();
	}

	public void showToast(String msg) {
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	private void initiatePopupWindow() {
		try {
			Display display = getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight(); // deprecated
			// We need to get the instance of the LayoutInflater, use the context of this activity
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Inflate the view from a predefined XML layout
			layout = inflater.inflate(R.layout.add_to_collection_popup, (ViewGroup) findViewById(R.id.popup_layout));
			layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			// create a 300px width and 470px height PopupWindow
			List<Collection> collections = dba.getAllCollections();
			collectionList = (ListView) layout.findViewById(R.id.collectionlist);
			CollectionListAdapter adapter = new CollectionListAdapter(this, collections, inflater);
			collectionList.setAdapter(adapter);
			window = new PopupWindow(layout, (int) (width * 0.8), (int) (height * 0.8), true);

			// display the popup in the center
			window.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

			collectionList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Collection collection = ((Collection) collectionList.getItemAtPosition(position));
					Log.e("name", collection.getName());
					collection.addWord(lw);
					boolean success = dba.updateCollection(collection);
					Log.e("adding word", "" + success);
					showToast("Successfully Added");
					window.dismiss();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onSkipButtonClick(View view) {
		window.dismiss();
	}
	
	@Override
	public void onBackPressed() {
		if (fromCollection) {
			startActivity(new Intent(this, BrowseCollectionsActivity.class));
			finish();
		}
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		dba.close();
		super.onDestroy();
	}

	public void onNewCollectionButtonClick(View view) {
		EditText editText = (EditText) layout.findViewById(R.id.newcollection);
		Editable edit = editText.getText();
		String name = edit.toString();
		if (name.equals("")) {
			showToast("Please name the collection.");
			return;
		}
		List<Collection> allCollections = dba.getAllCollections();
		for (Collection existedcoll : allCollections) {
			if (name.equals(existedcoll.getName())) {
				showToast(name + " already exists. Please choose a different name.");
				return;
			}
		}
		Collection collection = new Collection();
		collection.setName(name);
		collection.addWord(lw);
		dba.addCollection(collection);
		showToast("Successfully Created");
		window.dismiss();
	}
}

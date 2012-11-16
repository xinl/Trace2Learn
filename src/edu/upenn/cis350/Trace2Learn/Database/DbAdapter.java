package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.util.Log;
import edu.upenn.cis350.Trace2Learn.Database.TraceableItem.ItemType;

public class DbAdapter {
	//below is the schema for the database
	private static final String DATABASE_NAME = "Trace2LearnDB";
	private static final String TEST_DATABASE_NAME = "TestDB";
	
	public static final String ATTR_TYPE_TABLE = "AttributeType";
	public static final String ATTR_TYPE_ID = "id";
	public static final String ATTR_TYPE_NAME = "name";
	public static final String ATTR_TYPE_TAG = "tag"; //reserved name for tags
	
	public static final String ATTR_TABLE = "Attribute";
	public static final String ATTR_ID = "id";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_NAME = "name";
    
	public static final String CHAR_TABLE = "Character";
	public static final String CHAR_ID = "id";
    public static final String CHAR_ORDER = "ordering";
    public static final String CHAR_STROKES = "strokes";
    
    public static final String CHAR_ATTR_TABLE = "CharacterToAttribute";
	public static final String CHAR_ATTR_CHARID = "characterId";
	public static final String CHAR_ATTR_ATTRID = "attributeId";
    
    public static final String WORD_TABLE = "Word";
    public static final String WORD_ID = "id";
    public static final String WORD_ORDER = "ordering";
    
    public static final String WORD_ATTR_TABLE = "WordToAttribute";
	public static final String WORD_ATTR_WORDID = "wordId";
	public static final String WORD_ATTR_ATTRID = "attributeId";
	
	public static final String WORD_CHAR_TABLE = "WordToCharacter";
	public static final String WORD_CHAR_WORDID = "wordId";
	public static final String WORD_CHAR_CHARID = "characterId";
	public static final String WORD_CHAR_ORDER = "ordering";
	
	public static final String COLL_TABLE = "Collection";
    public static final String COLL_ID = "id";
    public static final String COLL_ORDER = "ordering";
    public static final String COLL_NAME = "name";
    public static final String COLL_DESCRIPTION = "description";
    
    public static final String COLL_WORD_TABLE = "CollectionToWord";
	public static final String COLL_WORD_WORDID = "wordId";
	public static final String COLL_WORD_COLLID = "collectionId";
	public static final String COLL_WORD_ORDER = "ordering";

    // Table creation statements
    private static final String CREATE_ATTR_TYPE_TABLE =
    		"CREATE TABLE " + ATTR_TYPE_TABLE + " (" +
            ATTR_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		ATTR_TYPE_NAME + " TEXT NOT NULL);";
    
    private static final String CREATE_ATTR_TABLE =
    		"CREATE TABLE " + ATTR_TABLE + " (" +
            ATTR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		ATTR_TYPE + " INTEGER, " +
    		ATTR_TYPE_NAME + " TEXT NOT NULL, " +
    		"FOREIGN KEY (" + ATTR_TYPE + ") REFERENCES " +
    		ATTR_TYPE_TABLE + "(" + ATTR_TYPE_ID + "));";
    
    private static final String CREATE_CHAR_TABLE =
    		"CREATE TABLE " + CHAR_TABLE + " (" +
            CHAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		CHAR_ORDER + " INTEGER NOT NULL, " +
    		CHAR_STROKES + " BLOB);";
    
    private static final String CREATE_CHAR_ATTR_TABLE =
    		"CREATE TABLE " + CHAR_ATTR_TABLE + " ( " +
    		CHAR_ATTR_CHARID + " INTEGER NOT NULL, " +
    		CHAR_ATTR_ATTRID + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + CHAR_ATTR_ATTRID + ") REFERENCES " +
    		ATTR_TABLE + "(" + ATTR_ID + "), " +
    		"FOREIGN KEY (" + CHAR_ATTR_CHARID + ") REFERENCES " +
    		CHAR_TABLE + "(" + CHAR_ID + ") ON DELETE CASCADE, " +
    		"PRIMARY KEY (" + CHAR_ATTR_CHARID + ", " +
    		CHAR_ATTR_ATTRID + "));";

    private static final String CREATE_WORD_TABLE = 
    		"CREATE TABLE " + WORD_TABLE + " (" + WORD_ID +
    		" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		WORD_ORDER + " INTEGER NOT NULL);";
    
    private static final String CREATE_WORD_ATTR_TABLE =
    		"CREATE TABLE " + WORD_ATTR_TABLE + " ( " +
    		WORD_ATTR_WORDID + " INTEGER NOT NULL, " +
    		WORD_ATTR_ATTRID + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + WORD_ATTR_ATTRID + ") REFERENCES " +
    		ATTR_TABLE + "(" + ATTR_ID + "), " +
    		"FOREIGN KEY (" + WORD_ATTR_WORDID + ") REFERENCES " +
    		WORD_TABLE + "(" + WORD_ID + ") ON DELETE CASCADE, "+
    		"PRIMARY KEY (" + WORD_ATTR_WORDID + ", " +
    		WORD_ATTR_ATTRID + "));";
    
    private static final String CREATE_WORD_CHAR_TABLE =
    		"CREATE TABLE " + WORD_CHAR_TABLE + " ( " +
    		WORD_CHAR_WORDID + " INTEGER NOT NULL, " +
    		WORD_CHAR_CHARID + " INTEGER NOT NULL, " +
    		WORD_CHAR_ORDER + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + WORD_CHAR_CHARID + ") REFERENCES " +
    		CHAR_TABLE + "(" + CHAR_ID + ") ON DELETE CASCADE, " +
    		"FOREIGN KEY (" + WORD_CHAR_WORDID + ") REFERENCES " +
    		WORD_TABLE + "(" + WORD_ID + ") ON DELETE CASCADE, "+
    		"PRIMARY KEY (" + WORD_CHAR_WORDID + ", " +
    		WORD_CHAR_CHARID + ", " + WORD_CHAR_ORDER + "));";
    
    private static final String CREATE_COLL_TABLE = 
    		"CREATE TABLE " + COLL_TABLE + " (" + COLL_ID +
    		" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		COLL_ORDER + " INTEGER NOT NULL, " +
    		COLL_NAME + " TEXT NOT NULL, " +
    		COLL_DESCRIPTION + " TEXT);";
    
    private static final String CREATE_COLL_WORD_TABLE =
    		"CREATE TABLE " + COLL_WORD_TABLE + " ( " +
    		COLL_WORD_COLLID + " INTEGER NOT NULL, " +
    		COLL_WORD_WORDID + " INTEGER NOT NULL, " +
    		COLL_WORD_ORDER + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + COLL_WORD_COLLID + ") REFERENCES " +
    		COLL_TABLE + "(" + COLL_ID + ") ON DELETE CASCADE, " +
    		"FOREIGN KEY (" + COLL_WORD_WORDID + ") REFERENCES " +
    		WORD_TABLE + "(" + WORD_ID + ") ON DELETE CASCADE, "+
    		"PRIMARY KEY (" + COLL_WORD_COLLID + ", " +
    		COLL_WORD_WORDID + ", " + COLL_WORD_ORDER + "));";


    //Drop Table Statements
    private static final String DATABASE_DROP_CHAR = 
    		"DROP TABLE IF EXISTS " + CHAR_TABLE;
    private static final String DATABASE_DROP_WORD = 
    		"DROP TABLE IF EXISTS " + WORD_TABLE;
    private static final String DATABASE_DROP_COLL = 
    		"DROP TABLE IF EXISTS " + COLL_TABLE;
    private static final String DATABASE_DROP_ATTR_TYPE = 
    		"DROP TABLE IF EXISTS " + ATTR_TYPE_TABLE;
    private static final String DATABASE_DROP_ATTR = 
    		"DROP TABLE IF EXISTS " + ATTR_TABLE;
    private static final String DATABASE_DROP_CHAR_ATTR= 
    		"DROP TABLE IF EXISTS " + CHAR_ATTR_TABLE;
    private static final String DATABASE_DROP_WORD_CHAR = 
    		"DROP TABLE IF EXISTS " + WORD_CHAR_TABLE;
    private static final String DATABASE_DROP_WORD_ATTR = 
    		"DROP TABLE IF EXISTS " + WORD_ATTR_TABLE;
    private static final String DATABASE_DROP_COLL_WORD= 
    		"DROP TABLE IF EXISTS " + COLL_WORD_TABLE;

    // old constants
    private static final String CHAR_DETAILS_TABLE = "CharacterDetails";
    private static final String CHARTAG_TABLE = "CharacterTag";
    private static final String WORDTAG_TABLE = "WordsTag";
    private static final String WORDS_TABLE = "Words";
    private static final String WORDS_DETAILS_TABLE = "WordsDetails";
    private static final String LESSONS_TABLE = "Lessons";
    private static final String LESSONS_DETAILS_TABLE = "LessonsDetails";
    private static final String LESSONTAG_TABLE = "LessonTag";
    
    public static final String LESSONS_ROWID = "_id";
    public static final String LESSONTAG_ROWID = "_id";
    
    public static final String CHARTAG_ROWID = "_id";
    public static final String CHARTAG_TAG= "tag";
    
    public static final String WORDTAG_ROWID = "_id";
    public static final String WORDTAG_TAG= "tag";

    private static final String TAG = "TagsDbAdapter";
    private static final int DATABASE_VERSION = 4;

    //class to help create a Database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        DatabaseHelper(Context context, boolean isTest) {
            super(context, TEST_DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CHAR_TABLE);
            db.execSQL(CREATE_WORD_TABLE);
            db.execSQL(CREATE_ATTR_TYPE_TABLE);
            db.execSQL(CREATE_ATTR_TABLE);
            db.execSQL(CREATE_CHAR_ATTR_TABLE);
            db.execSQL(CREATE_WORD_ATTR_TABLE);
            db.execSQL(CREATE_WORD_CHAR_TABLE);
            db.execSQL(CREATE_COLL_TABLE);
            db.execSQL(CREATE_COLL_WORD_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            dropTables(db);
            onCreate(db);
        }
        
        private void dropTables(SQLiteDatabase db) {
        	db.execSQL(DATABASE_DROP_CHAR);
            db.execSQL(DATABASE_DROP_WORD);
            db.execSQL(DATABASE_DROP_ATTR_TYPE);
            db.execSQL(DATABASE_DROP_ATTR);
            db.execSQL(DATABASE_DROP_CHAR_ATTR);
            db.execSQL(DATABASE_DROP_WORD_ATTR);
            db.execSQL(DATABASE_DROP_WORD_CHAR);
            db.execSQL(DATABASE_DROP_COLL);
            db.execSQL(DATABASE_DROP_COLL_WORD);  
        }
    }

    // object fields
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private boolean isTest = false;
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened nor created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Create a test database if one needs to be created.
     * @return this
     * @throws SQLException if test database could be neither opened nor created
     */
    public DbAdapter openTest() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx, true);
        mDb = mDbHelper.getWritableDatabase();
        isTest = true;
        return this;
    }
    
    /**
     * Wipe the data from test database before closing adapter.
     */
    public void closeTest() {
    	if (isTest) {
    		mDbHelper.dropTables(mDb);   
    		mDbHelper.onCreate(mDb);
    	}
        mDbHelper.close();
    }
    
    /**
     * Create a new word tag. If the tag is
     * successfully created return the new rowId for that tag, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the row_id of the tag
     * @param tag the text of the tag
     * @return rowId or -1 if failed
     */
    public long createWordTags(long id, String tag) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(WORDTAG_ROWID, id);
        initialValues.put(WORDTAG_TAG, tag);

        return mDb.insert(WORDTAG_TABLE, null, initialValues);
    }
    
    /**
     * Create a new lesson tag. If the lesson tag is
     * successfully created return the new rowId for that tag, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the row_id of the tag
     * @param tag the text of the tag
     * @return rowId or -1 if failed
     */
    public long createLessonTags(long id, String tag) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put("_id", id);
        initialValues.put("tag", tag);

        return mDb.insert(LESSONTAG_TABLE, null, initialValues);
    }
    
    /**
     * Create a new char tag. If the tag is
     * successfully created return the new rowId for that tag, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the row_id of the tag
     * @param tag the text of the tag
     * @return rowId or -1 if failed
     */
    public long createTags(long id, String tag) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(CHARTAG_ROWID, id);
        initialValues.put(CHARTAG_TAG, tag);

        return mDb.insert(CHARTAG_TABLE, null, initialValues);
    }
    
    /**
     * Delete the tag with the given rowId and tag
     * 
     * @param rowId id of tag to delete
     * @param tag text of tag to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteTag(long rowId, String tag) {
        return mDb.delete(CHARTAG_TABLE, CHARTAG_ROWID + "=" + rowId + " AND " + CHARTAG_TAG+"="+tag, null) > 0;
    }
    
    /**
     * Delete the word tag with the given rowId and tag
     * 
     * @param rowId id of tag to delete
     * @param tag text of tag to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteWordTag(long rowId, String tag) {
        return mDb.delete(WORDTAG_TABLE, WORDTAG_ROWID + "=" + rowId + " AND " + WORDTAG_TAG+"="+tag, null) > 0;
    }
    
    /**
     * Delete the lesson tag with the given rowId and tag
     * 
     * @param rowId id of tag to delete
     * @param tag text of tag to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteLessonTag(long rowId, String tag) {
        return mDb.delete(LESSONTAG_TABLE, LESSONTAG_ROWID + "=" + rowId + " AND " + "tag="+tag, null) > 0;
    }
   
    /**
     * Modify a character already in the database
     * @param c character to be modified to the database
     * @return true if change is pushed to DB.  False on error.
     */
    public boolean modifyCharacter(Character c)
    {
    	mDb.beginTransaction();
    	long charId = c.getId();
    	//drop the current details
    	mDb.delete(CHAR_DETAILS_TABLE, "CharId = " + charId, null);
    	
    	//add each stroke to CHAR_DETAILS_TABLE
    	List<Stroke> l = c.getStrokes();
    	//stroke ordering
    	int strokeNumber=0;
    	for(Stroke s:l)
    	{
    		ContentValues strokeValues = new ContentValues();
    		strokeValues.put("CharId", charId);
    		strokeValues.put("Stroke", strokeNumber);
    		//point ordering
    		int pointNumber=0;
    		for(PointF p : s.getAllPoints())
    		{
    			strokeValues.put("PointX", p.x);
        		strokeValues.put("PointY", p.y);
        		strokeValues.put("OrderPoint", pointNumber);
        		long success = mDb.insert(CHAR_DETAILS_TABLE, null, strokeValues);
        		if(success == -1)
        		{	
        			//if error
        			Log.e(CHAR_DETAILS_TABLE,"cannot add stroke");
        			mDb.endTransaction();
        			return false;
        		}
        		pointNumber++;
    		}
    		strokeNumber++;
    	}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    	
    }
    
    /**
     * Add an attribute type, or return id of existing attribute.
     * @param type name of attribute type
     * @return row of attribute type in database.
     */
    private long addAttributeType(String type) {
    	Cursor c = mDb.query(ATTR_TYPE_TABLE, null,
    			"upper(" + ATTR_TYPE_NAME + ") = '" + type.toUpperCase() + "'",
    			null, null, null, null);
    	long typeId = -1;
    	if (c != null) c.moveToFirst();
    	if (c == null || c.getCount() == 0) {
    		if (c != null) c.close();
    		ContentValues typeValues = new ContentValues();
    		typeValues.put(ATTR_TYPE_NAME, type);
    		typeId = mDb.insert(ATTR_TYPE_TABLE, null, typeValues);
    		if (typeId == -1){
        		//if error
        		Log.e(ATTR_TYPE_TABLE, "Cannot add attribute type, " + type + ", to table.");
        	}
    	} else {
    		typeId = c.getInt(c.getColumnIndexOrThrow(ATTR_TYPE_ID));
    		c.close();
    	}

    	return typeId;
    }
    
    private long getAttributeTypeId(String name) {
    	Cursor c = mDb.query(ATTR_TYPE_TABLE, null,
    			"upper(" + ATTR_TYPE_NAME + ") = '" + name.toUpperCase() + "'",
    			null, null, null, null);
    	if (c == null) return -1;
    	long result = -1;
    	c.moveToFirst();
    	if (c.getCount() > 0) {
    		result = c.getLong(c.getColumnIndexOrThrow(ATTR_TYPE_ID));
    	}
    	c.close();
    	return result;
    }
    
    /**
     * Add all attributes associated with a typeId to the table.
     * @param attributes set of attribute names
     * @param typeId the type id they are associated with
     * @return set of attributes ids or null if failed.
     */
    private Set<Long> addAttributes(Set<String> attributes, long typeId) {
    	Set<Long> tagIds = new HashSet<Long>();
    	Cursor c = mDb.query(ATTR_TABLE, null, ATTR_TYPE + "=" + typeId,
				null, null, null, null);
    	if (c != null) c.moveToFirst();
    	
    	//If none of the attributes exist.
    	if (c == null || c.getCount() == 0) {
    		if (c != null) c.close();
    		for (String attribute: attributes) {
    			ContentValues attrValues = new ContentValues();
        		attrValues.put(ATTR_TYPE, typeId);
        		attrValues.put(ATTR_NAME, attribute);
        		long tagId = mDb.insert(ATTR_TABLE, null, attrValues);
        		if (tagId == -1){
            		Log.e(ATTR_TABLE, "Cannot add attribute, " + attribute +", to table.");
            		return null;
            	}
        		tagIds.add(tagId);
    		}
    		
        //If some of the attributes already exist.
    	} else {
    		//ignore attributes that already exist. ignore case.
    		do {
    			String oldAttr = c.getString(c.getColumnIndexOrThrow(ATTR_NAME));
    			if (attributes.contains(oldAttr)) {
    				tagIds.add((long) c.getInt(c.getColumnIndexOrThrow(ATTR_ID)));
    				attributes.remove(oldAttr);
    			} else {
    				Set<String> temp = new HashSet<String>(attributes);
    				for (String attribute: temp) {
    					if (attribute.equalsIgnoreCase(oldAttr)) {
    						tagIds.add(c.getLong(c.getColumnIndexOrThrow(ATTR_ID)));
    	    				attributes.remove(attribute);
    	    				break;
    					}
    				}
    			}
    		} while(c.moveToNext());
    		c.close();
    		//insert attributes which did not exist.
    		for (String attribute: attributes) {
    			ContentValues attrValues = new ContentValues();
        		attrValues.put(ATTR_TYPE, typeId);
        		attrValues.put(ATTR_NAME, attribute);
        		long tagId = mDb.insert(ATTR_TABLE, null, attrValues);
        		if (tagId == -1){
            		Log.e(ATTR_TABLE, "Cannot add attribute, " + attribute +", to table.");
            		return null;
            	}
        		tagIds.add(tagId);
    		}
    	}
    	return tagIds;
    }
    
    /**
     * Delete the attributes associated with an item. Only deletes from
     * itemAttrTable.
     * @param itemId id of item
     * @param itemAttrTable table linking items and attribute
     * @param itemColumn column in table for item id
     * @param attrColumn column in table for attributes id
     * @param type the type of attribute being deleted
     * @param attributes names of attributes being deleted
     * @return true if deletion is successful.
     */
    private boolean deleteAttributes(long itemId, String itemAttrTable,
    		String itemColumn, String attrColumn,
    		String type, Set<String> attributes) {
    	long typeId = getAttributeTypeId(type);
    	if (typeId == -1) {
    		Log.e(itemAttrTable, "Type, " + type + ", does not exist.");
    		return false;
    	}
    	for (String attribute: attributes) {
    		Cursor c = mDb.query(ATTR_TABLE, null,
    				ATTR_TYPE + "=" + typeId + " AND " +
        			"upper(" + ATTR_NAME + ") = '" + attribute.toUpperCase() + "'",
        			null, null, null, null);
    		if (c == null || c.getCount() == 0) {
    			if (c != null) c.close();
    			Log.e(ATTR_TABLE, "Cannot find attribute, " + attribute + ", to delete");
    			return false;
    		}
    		c.moveToFirst();
    		long attrId = c.getLong(c.getColumnIndexOrThrow(ATTR_ID));
    		c.close();
    		int rowsDeleted = mDb.delete(itemAttrTable, itemColumn + " = " + itemId + " AND " +
    		        attrColumn + " = " + attrId, null);
    		if (rowsDeleted != 1) {
    			Log.e(itemAttrTable, "Cannot delete attribute, " + attribute + ", from table");
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Update all of the attributes associated with an item
     * @param itemId id of the item
     * @param attributes map of attributes
     * @param itemAttrTable table matching attributes with item
     * @param itemColumn table column representing the item
     * @param attrColumn table column representing the attributes
     * @return true if update was successful.
     */
    private boolean updateAttributes(long itemId, Map<String,Set<String>> attributes,
    		Map<String, Set<String>> oldAttributes, String itemAttrTable,
    		String itemColumn, String attrColumn) {
    	//add new values to the set
    	for(String type: attributes.keySet()) {
    		Set<String> newValues = minus(
    				attributes.get(type), oldAttributes.get(type));
    		if (newValues.isEmpty()) continue;
    		if (addAttributesToItem(itemId, type, newValues,
    				itemAttrTable, itemColumn, attrColumn) == false) {
    			return false;
    		}
    	}
    	//delete old values
    	for(String type: oldAttributes.keySet()) {
    		Set<String> oldValues = minus(
    				oldAttributes.get(type), attributes.get(type));
    		if (oldValues.isEmpty()) continue;
    		if (deleteAttributes(itemId, itemAttrTable, itemColumn,
    				attrColumn, type, oldValues) == false) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private Set<String> minus(Set<String> a, Set<String> b) {
    	if (a == null || b == null) return a;
    	Set<String> retVal = new HashSet<String>(a);
    	for(String s : a) {
    		if (b.contains(s)) {
    			retVal.remove(s);
    		} else {
    			for (String s2: b) {
    				if (s2.equalsIgnoreCase(s)) {
    					retVal.remove(s2);
    					break;
    				}
    			}
    		}
    	}
    	return retVal;
    }
    
    /**
     * Update all of the attributes associated with an item
     * @param itemId id of the item
     * @param type name of the attribute type
     * @param attribute set of attribute names
     * @param itemAttrTable table matching attributes with item
     * @param itemColumn table column representing the item
     * @param attrColumn table column representing the attributes
     * @return true if update was successful.
     */
    private boolean addAttributesToItem(long itemId, String type,
    		Set<String> attributes, String itemAttrTable,
    		String itemColumn, String attrColumn) {
    	//put type in attribute type table
    	long typeId = addAttributeType(type);
    	if (typeId == -1) {
    		return false;
    	}
    	
    	//put attributes in attribute table
    	Set<Long> tagIds = addAttributes(attributes, typeId);
    	if (tagIds == null) {
    		return false;
    	}
    	
    	//associate item with attributes
    	Cursor c = mDb.query(itemAttrTable, null, itemColumn + "=" + itemId, null, null, null, null);
    	if (c != null) c.moveToFirst();
    	//if all attributes are new to item.
    	if (c == null || c.getCount() == 0) {
    		if (c != null) c.close();
    		for (long tagId: tagIds) {
    			ContentValues values = new ContentValues();
    			values.put(itemColumn, itemId);
    			values.put(attrColumn, tagId);
    			if (mDb.insert(itemAttrTable, null, values) == -1) {
    				Log.e(itemAttrTable, "Cannot add row to table for: " + itemId + " " + tagId);
    				return false;
    			}
    		}
    	//if some items already existed.
    	} else {
    		//ignore items that already exist.
    		do {
    			long oldId = c.getLong(c.getColumnIndexOrThrow(attrColumn));
    			if (tagIds.contains(oldId)) {
    				tagIds.remove(oldId);
    			}
    		} while (c.moveToNext());
    		c.close();
    		//add items that did not exist.
    		for(Long tagId: tagIds) {
    			ContentValues values = new ContentValues();
    			values.put(itemColumn, itemId);
    			values.put(attrColumn, tagId);
    			if (mDb.insert(itemAttrTable, null, values) == -1) {
    				Log.e(itemAttrTable, "Cannot add row to table: " + itemId + " " + tagId );
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    /**
     * Returns a cursor for table with column ATTR_NAME and ATTR_TYPE,
     * representing the attribute and attributetype names respectively
     * @param id char or word id associated with attributes.
     * @return a cursor
     */
    private Cursor getAttributesCursor(long id, String attrItemTable,
    		String itemColumn, String attrColumn) {
    	String query = "SELECT A." + ATTR_NAME + " AS " + ATTR_NAME + ", " +
                "T." + ATTR_TYPE_NAME + " AS " + ATTR_TYPE +
         " FROM " + ATTR_TYPE_TABLE + " T, " + ATTR_TABLE + " A, " +
                attrItemTable + " I" +
         " WHERE I." + itemColumn + " = " + id + " AND " +
                "I." + attrColumn + " = " + "A." + ATTR_ID + " AND " + 
                "A." + ATTR_TYPE + " = " + "T." + ATTR_TYPE_ID + ";";
    	Cursor cursor = mDb.rawQuery(query, null);
    	
    	return cursor;
    }
    
    /**
     * Add a character to the database
     * @param c character to be added to the database
     * @return true if character is added to DB.  False on error.
     */
    public boolean addCharacter(Character c) {
    	if (c.getId() != -1) {
    		throw new IllegalArgumentException("Character must be new!");
    	} else if (c.getStrokes() == null || c.getStrokes().size() == 0) {
    		throw new IllegalArgumentException("Character must contain strokes!");
    	}
    	
    	// insert char into table
    	mDb.beginTransaction();
    	ContentValues charValues = new ContentValues();
    	charValues.put(CHAR_ORDER, c.getOrder());
    	charValues.put(CHAR_STROKES, Stroke.encodeStrokesData(c.getStrokes()));
    	
    	long id = mDb.insert(CHAR_TABLE, null, charValues);
    	if (id == -1) {
    		//if error
    		Log.e(CHAR_TABLE, "Cannot add new char to table:" + c);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// update char to reflect new id and order.
    	c.setId(id);
    	c.setOrder(id); //id represents location in database
    	charValues = new ContentValues();
    	charValues.put(CHAR_ORDER, c.getOrder());
    	int rowsAffected = mDb.update(CHAR_TABLE, charValues, CHAR_ID + "=" + c.getId(), null);
    	if (rowsAffected != 1) {
    		//if error
    		Log.e(CHAR_TABLE, "Cannot set order of char in table: " + c);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// add char as word.
    	Word word = new Word();
    	word.addCharacter(c);
    	word.setAttributes(c.getAttributes());
    	word.setTags(c.getTags());
    	boolean success = addWord(word);
    	if (! success) {
    		//if error
    		Log.e(CHAR_TABLE, "Cannot add char, " + c + ", as word in " + WORD_TABLE + ".");
    		mDb.endTransaction();
    		return false;
    	}
    	
    	//add attributes;
    	if (! c.getTags().isEmpty()) {
    		c.addAttributes(ATTR_TYPE_TAG, c.getTags());
    	}
    	if (! c.getAttributes().isEmpty()) {
    		Map<String, Set<String>> emptyMap = new HashMap<String, Set<String>>();
    		boolean result = updateAttributes(c.getId(), c.getAttributes(),
    				emptyMap, CHAR_ATTR_TABLE, CHAR_ATTR_CHARID, CHAR_ATTR_ATTRID);
    		if (result == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	c.removeAttributes(ATTR_TYPE_TAG);
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /** Get a character from the database.
     *  @param id id for the character
     *  @return the character or null if unsuccessful.
     */
    public Character getCharacter(long id) {
    	if (id == -1) return null;
    	Cursor cursor = mDb.query(CHAR_TABLE, null, CHAR_ID + "=" + id,
    			null, null, null, null);
    	if (cursor == null) {
    		Log.e(CHAR_TABLE, "Cannot find char, " + id + ", in table.");
    		return null;
    	}
    	cursor.moveToFirst();
    	if (cursor.getCount() != 1) {
    		Log.e(CHAR_TABLE, "Did not find exactly one " +
    				          "Character when searching for " + id + " in table.");
    		cursor.close();
    		return null;
    	} else if (id != cursor.getInt(cursor.getColumnIndexOrThrow(CHAR_ID))) {
    		Log.e(CHAR_TABLE, "Returned wrong Character when searching for " +
    	                       id + " in table.");
    		cursor.close();
    		return null;
    	}
    	
    	//create character
    	Character c = new Character();
    	c.setId(id);
    	c.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(CHAR_ORDER)));
    	byte[] strokeData = cursor.getBlob(cursor.getColumnIndexOrThrow(CHAR_STROKES));
    	c.setStrokes(Stroke.decodeStrokesData(strokeData));
    	cursor.close();
    	
    	//get attributes of character
    	Cursor attrCursor = getAttributesCursor(
    			id, CHAR_ATTR_TABLE, CHAR_ATTR_CHARID, CHAR_ATTR_ATTRID);
    	if (attrCursor != null) {
    		attrCursor.moveToFirst();
    	}
    	if (attrCursor != null && attrCursor.getCount() != 0) {
    		int typeIndex = attrCursor.getColumnIndexOrThrow(ATTR_TYPE);
    		int nameIndex = attrCursor.getColumnIndexOrThrow(ATTR_NAME);
    		do {
    			String type = attrCursor.getString(typeIndex);
    			String attr = attrCursor.getString(nameIndex);
    			if (type.equalsIgnoreCase(ATTR_TYPE_TAG)) {
    				c.addTag(attr);
    			} else {
    				c.addAttribute(type, attr);
    			}
    		} while (attrCursor.moveToNext());
    	}
    	if (attrCursor != null) attrCursor.close();
    	return c;
    }
    
    /**
     * Get all characters in the database
     * @return returns the characters, ordered by order.
     */
    public List<Character> getAllCharacters() {
    	List<Character> characters = new ArrayList<Character>();
    	Cursor cursor = mDb.query(CHAR_TABLE, new String[] {CHAR_ID},
    			null, null, null, null, CHAR_ORDER);
    	if (cursor == null) return null;
    	cursor.moveToFirst();
    	if (cursor.getCount() == 0) return characters;
    	do {
    		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CHAR_ID));
    		Character c = getCharacter(id);
    		if (c == null) {
    			Log.e(CHAR_TABLE, "Could not retrieve character with id " + id);
    			cursor.close();
    			return null;
    		}
    		characters.add(c);
    	} while (cursor.moveToNext());
    	cursor.close();
    	return characters;
    }
    
    /**
     * Retrieve all characters that have the attribute.
     * Employ partial matching if the attr is 3 characters or longer.
     * @param attr the attribute to match against
     * @return characters matching that attribute.
     */
    public List<Character> getCharactersByAttribute(String attr) {
        attr = attr.trim().toUpperCase();
        if (attr.length() == 0) return getAllCharacters();
        //return only exact matches if the tag is two or less characters
        String query = "SELECT C." + CHAR_ID + " AS " + CHAR_ID + " " +
                       "FROM " + CHAR_TABLE + " C, " + ATTR_TABLE + " A, " +
        		                 CHAR_ATTR_TABLE + " L " +
                       "WHERE C." + CHAR_ID + " = " + "L." + CHAR_ATTR_CHARID +
                            " AND " + "A." + ATTR_ID + " = " + "L." + CHAR_ATTR_ATTRID +
                            " AND ";
                       
        if (attr.length() < 3) {
        	query += "upper(" + ATTR_NAME + ") = '" + attr + "' ";
        } else {
        	query += "upper(" + ATTR_NAME + ") LIKE '%" + attr + "%' ";
        }
        
        query += "ORDER BY C." + CHAR_ORDER + ";"; 
        
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor == null) {
        	Log.e(CHAR_TABLE, "Could not process query " + query);
        	return null;
        }
        cursor.moveToFirst();
        List<Character> characters = new ArrayList<Character>();
        if (cursor.getCount() == 0) {
        	cursor.close();
        	return characters;
        }
		do{
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(CHAR_ID));
			Character c = getCharacter(id);
			if (c == null) {
				Log.e(CHAR_TABLE, "Could not retrieve character with id " + id);
				return null;
			}
			characters.add(c);
		}
		while (cursor.moveToNext());
        cursor.close();
        
        return characters;
    }
    
    /**
     * Update a character in the Database. Call this method when the in memory
     * character has changed from the database character
     * @param c the character to be updated
     * @return true if the update was successful.
     */
    public boolean updateCharacter(Character c) {
    	Character oldCharacter = getCharacter(c.getId());
    	if (oldCharacter == null) return addCharacter(c);
    	if (c.equals(oldCharacter)) return true; //no need to update
    	mDb.beginTransaction();
    	if (! c.getAttributes().equals(oldCharacter.getAttributes())) {
    		if (updateAttributes(c.getId(), c.getAttributes(),
    				oldCharacter.getAttributes(), CHAR_ATTR_TABLE,
    				CHAR_ATTR_CHARID, CHAR_ATTR_ATTRID) == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	if (! c.getTags().equals(oldCharacter.getTags())) {
    		Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>();
    		tagMap.put(ATTR_TYPE_TAG, c.getTags());
    		Map<String, Set<String>> oldTags = new HashMap<String, Set<String>>();
    		oldTags.put(ATTR_TYPE_TAG, oldCharacter.getTags());
    		if (updateAttributes(c.getId(), tagMap, oldTags,
    				CHAR_ATTR_TABLE, CHAR_ATTR_CHARID, CHAR_ATTR_ATTRID) == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	if (! c.getStrokes().equals(oldCharacter.getStrokes())) {
    		ContentValues charValues = new ContentValues();
    		charValues.put(CHAR_STROKES, Stroke.encodeStrokesData(c.getStrokes()));
    		int numRows = mDb.update(CHAR_TABLE, charValues, CHAR_ID + "=" + c.getId(), null);
    		if (numRows != 1) {
    			Log.e(CHAR_TABLE, "Unable to update strokes for char, " + c);
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	if (c.getOrder() != oldCharacter.getOrder()) {
    		ContentValues charValues = new ContentValues();
    		charValues.put(CHAR_ORDER, c.getOrder());
    		int numRows = mDb.update(CHAR_TABLE, charValues, CHAR_ID + "=" + c.getId(), null);
    		if (numRows != 1) {
    			Log.e(CHAR_TABLE, "Unable to update order for char, " + c);
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /**
     * Delete the character from the database. Cascading gets rid of attributes.
     * @param c the character to be deleted.
     * @return true if deletion was successful.
     */
    public boolean deleteCharacter(Character c) {
    	mDb.beginTransaction();
    	int rowsDeleted = mDb.delete(CHAR_TABLE, CHAR_ID + "=" + c.getId(), null);
    	if (rowsDeleted != 1) {
    		mDb.endTransaction();
    		Log.e(CHAR_TABLE, "Unable to delete char, " + c);
    		return false;
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }

    /**
     * Add a character to the database
     * @param c character to be added to the database
     * @return true if character is added to DB.  False on error.
     */
    public boolean addCharacter(Character c)
    {
    	mDb.beginTransaction();
    	//add to CHAR_TABLE
    	ContentValues initialCharValues = new ContentValues();
    	initializePrivateTag(c,initialCharValues);
    	initialCharValues.put("charOrder", 0);//Qin
    	long id = mDb.insert(CHAR_TABLE, null, initialCharValues);
    	if(id == -1)
    	{
    		//if error
    		Log.e(CHAR_TABLE, "cannot add new character to table "+CHAR_TABLE);
    		mDb.endTransaction();
    		return false;
    	}
    	Cursor x = mDb.query(CHAR_TABLE, new String[]{CHAR_ID}, null, null, null, null, CHAR_ID+" DESC", "1");
    	if (x != null) {
            x.moveToFirst();
        }
    	long rowid = x.getInt(x.getColumnIndexOrThrow(CHAR_ID));
    	c.setId(rowid);
    	//Qin
    	ContentValues newCharValues = new ContentValues();
    	newCharValues.put("charOrder", rowid);
    	int result = mDb.update(CHAR_TABLE, newCharValues, "_id = "+rowid, null);
    	if(result==0){
    	    mDb.delete(CHAR_TABLE, CHAR_ID+"="+rowid, null);
    	    mDb.endTransaction();
    	    return false;
    	}
    	//add each stroke to CHAR_DETAILS_TABLE
    	List<Stroke> l = c.getStrokes();
    	//stroke ordering
    	int strokeNumber=0;
    	for(Stroke s:l)
    	{
    		ContentValues strokeValues = new ContentValues();
    		strokeValues.put("CharId", id);
    		strokeValues.put("Stroke", strokeNumber);
    		//point ordering
    		int pointNumber=0;
    		for(PointF p : s.getAllPoints())
    		{
    			strokeValues.put("PointX", p.x);
        		strokeValues.put("PointY", p.y);
        		strokeValues.put("OrderPoint", pointNumber);
        		long success = mDb.insert(CHAR_DETAILS_TABLE, null, strokeValues);
        		if(success == -1)
        		{	
        			//if error
        			Log.e(CHAR_DETAILS_TABLE,"cannot add stroke");
        			mDb.endTransaction();
        			return false;
        		}
        		pointNumber++;
    		}
    		strokeNumber++;
    	}
    	//need to add character as a word so that we can add them to lessons as not part of a word
    	ContentValues initialWordValue = new ContentValues();
    	initialWordValue.put("name", "");
    	initialWordValue.put("wordOrder", 0);//Qin
    	long word_id = mDb.insert(WORDS_TABLE, null, initialWordValue);
    	if(word_id == -1)
    	{
    		//if error
    		Log.e(WORDS_TABLE, "cannot add new character to table "+WORDS_TABLE);
    		mDb.endTransaction();
    		return false;
    	}
    	Cursor cur = mDb.query(WORDS_TABLE, new String[]{"_id"}, null, null, null, null, "_id DESC", "1");
    	if (cur != null) {
            cur.moveToFirst();
        }
    	word_id = cur.getInt(cur.getColumnIndexOrThrow("_id"));
    	//Qin
    	ContentValues newWordValues = new ContentValues();
    	newWordValues.put("wordOrder", word_id);
    	int result2 = mDb.update(WORDS_TABLE, newWordValues, "_id = "+word_id, null);
    	if(result2==0){
    	    mDb.delete(WORDS_TABLE, WORD_ID+"="+word_id, null);
    	    mDb.endTransaction();
    	    return false;
    	}
    	
    	ContentValues wordValues = new ContentValues();
    	wordValues.put("_id", word_id);
    	wordValues.put("CharId", id);
    	wordValues.put("WordOrder", 0);
    	wordValues.put("FlagUserCreated", 0);
    	long success = mDb.insert(WORDS_DETAILS_TABLE, null, wordValues);
		if(success == -1)
		{	
			//if error
			Log.e(WORDS_DETAILS_TABLE,"cannot add to table");
			mDb.endTransaction();
			return false;
		}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;	
    }
    
    //Qin
    public long moveupCharacter(long id){
	Cursor x =
                mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID, "charOrder"}, CHAR_ID + "=" + id, null,
                        null, null, null, null);
	if(x==null || x.getCount()==0)
	    return -2;
	else{
	    x.moveToFirst();
	    long charOrder = x.getInt(x.getColumnIndexOrThrow("charOrder"));
	    long charId = x.getInt(x.getColumnIndexOrThrow(CHAR_ID));
	    Cursor y = mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID, "charOrder"}, "charOrder<"+charOrder +" AND charOrder>0", 
		    null, null, null, "charOrder DESC", "1");
	    if(y==null || y.getCount()==0)
		return -1;//do not have smaller order
	    else{
		y.moveToFirst();
		long charOrderUp = y.getInt(y.getColumnIndexOrThrow("charOrder"));
		long charIdUp = y.getInt(y.getColumnIndexOrThrow(CHAR_ID));
		
		ContentValues swapValues = new ContentValues();
		swapValues.put("charOrder", charOrderUp);
		mDb.update(CHAR_TABLE, swapValues, "_id="+charId, null);
		
		ContentValues swapValuesUp = new ContentValues();
		swapValuesUp.put("charOrder", charOrder);
		mDb.update(CHAR_TABLE, swapValuesUp, "_id="+charIdUp, null);
	    }
	}
	return id;
    }
    
    //Qin
    public long movedownCharacter(long id){
	Cursor x =
                mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID, "charOrder"}, CHAR_ID + "=" + id, null,
                        null, null, null, null);
	if(x==null || x.getCount()==0)
	    return -2;
	else{
	    x.moveToFirst();
	    long charOrder = x.getInt(x.getColumnIndexOrThrow("charOrder"));
	    long charId = x.getInt(x.getColumnIndexOrThrow(CHAR_ID));
	    Cursor y = mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID, "charOrder"}, "charOrder>"+charOrder, 
		    null, null, null, "charOrder ASC", "1");
	    if(y==null || y.getCount()==0)
		return -1;//do not have smaller order
	    else{
		y.moveToFirst();
		long charOrderDown = y.getInt(y.getColumnIndexOrThrow("charOrder"));
		long charIdDown = y.getInt(y.getColumnIndexOrThrow(CHAR_ID));
		
		ContentValues swapValues = new ContentValues();
		swapValues.put("charOrder", charOrderDown);
		mDb.update(CHAR_TABLE, swapValues, "_id="+charId, null);
		
		ContentValues swapValuesUp = new ContentValues();
		swapValuesUp.put("charOrder", charOrder);
		mDb.update(CHAR_TABLE, swapValuesUp, "_id="+charIdDown, null);
	    }
	}
	return id;
    }
    
    public long deleteCharacter(long id){
    	Cursor mCursor =
                mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID}, CHAR_ID + "=" + id, null,
                        null, null, null, null);
    	 if (mCursor == null) {
             return -2;
         }
    	 
    	 mCursor =  mDb.query(true, WORDS_DETAILS_TABLE, new String[] {"CharId"}, "CharId =" + id +" AND FlagUserCreated=1", null,
                 null, null, null, null);
    	 if(mCursor.getCount()>0){
    		 //Some word is using the character
    		 return -1;
    	 }
    	 else{
    		 mDb.delete(CHAR_TABLE, CHAR_ID + "=" + id, null);
    		 mDb.delete(CHAR_DETAILS_TABLE, "CharId = " + id, null);
    		 mCursor =  mDb.query(true, WORDS_DETAILS_TABLE, new String[] {WORD_ID}, "CharId =" + id, null,
                     null, null, null, null);
    		 mCursor.moveToFirst();
    		 do {
 	        	if(mCursor.getCount()==0){
 	        		break;
 	        	}
 	        	long wordId = (mCursor.getLong(mCursor.getColumnIndexOrThrow(WORD_ID)));
 	        	mDb.delete(WORDS_TABLE, WORD_ID + "=" + wordId, null);
 	         }
 	         while(mCursor.moveToNext());
    		 mDb.delete(WORDS_DETAILS_TABLE, "CharId="+id, null);
    		 mDb.delete(CHARTAG_TABLE, CHAR_ID + "=" + id, null);
    	 }
    	return id;
    }
    
    /**
     * Get a LessonCharacter from the database
     * @param id id of the LessonCharacter
     * @return The LessonCharacter if id exists, null otherwise.
     */
    public Character getCharacterById(long id)
    {
        Cursor mCursor =
            mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID}, CHAR_ID + "=" + id, null,
                    null, null, null, null);
        Character c = new Character();
        //if the character doesn't exists
        if (mCursor == null) {
            return null;
        }
        
        //grab its details (step one might not be necessary and might cause slow downs
        // but it is for data consistency.
        mCursor =
            mDb.query(true, CHAR_DETAILS_TABLE, new String[] {"CharId", "Stroke","PointX","PointY"}, "CharId = "+ id, null,
                    null, null, "Stroke ASC, OrderPoint ASC", null);
        mCursor.moveToFirst();
        Stroke s = new Stroke();
        int strokeNumber = mCursor.getInt(mCursor.getColumnIndexOrThrow("Stroke"));
        do {
        	if(mCursor.getCount()==0){
        		c.addStroke(s);
        		break;
        	}
        	if(strokeNumber != mCursor.getInt(mCursor.getColumnIndexOrThrow("Stroke")))
        	{
        		c.addStroke(s);
        		strokeNumber = mCursor.getInt(mCursor.getColumnIndexOrThrow("Stroke"));
        		s = new Stroke();
        	}
        	s.addPoint(mCursor.getFloat(mCursor.getColumnIndexOrThrow("PointX")),
        			mCursor.getFloat(mCursor.getColumnIndexOrThrow("PointY")));
        }
        while(mCursor.moveToNext());
        c.addStroke(s);
        c.setId(id);
        
        mCursor =
                mDb.query(true, CHAR_TABLE, new String[] {"name"}, CHAR_ID + " = "+ id, null,
                        null, null, null, null);
        mCursor.moveToFirst();
        String privateTag = mCursor.getString(mCursor.getColumnIndexOrThrow("name"));
        c.setPrivateTag(privateTag);
        
        return c;
    }
    

    /**
     * Get a LessonCharacter from the database
     * @param id id of the LessonCharacter
     * @return The LessonCharacter if id exists, null otherwise.
     */
    public Word getWordById(long id)
    {
        Cursor mCursor =
            mDb.query(true, WORDS_TABLE, new String[] {WORD_ID}, WORD_ID + "=" + id, null,
                    null, null, null, null);
        Word w = new Word();
        //if the character doesn't exists
        if (mCursor == null) {
            return null;
        }
        
        //grab its details (step one might not be necessary and might cause slow downs
        // but it is for data consistency.
        mCursor =
            mDb.query(true, WORDS_DETAILS_TABLE, new String[] {WORD_ID, "CharId", "WordOrder"}, WORD_ID + "=" + id, null,
                    null, null, "WordOrder ASC", null);
        mCursor.moveToFirst();
        do {
        	if(mCursor.getCount()==0){
        		break;
        	}
        	long charId = mCursor.getLong(mCursor.getColumnIndexOrThrow("CharId"));
        	Log.i("LOAD", "Char: " + charId);
        	w.addCharacter(charId);
        } while(mCursor.moveToNext());
        w.setId(id);
        
        mCursor =
                mDb.query(true, WORDS_TABLE, new String[] {"name"}, WORD_ID + " = "+ id, null,
                        null, null, null, null);
        mCursor.moveToFirst();
        String privateTag = mCursor.getString(mCursor.getColumnIndexOrThrow("name"));
        w.setPrivateTag(privateTag);
        
        w.setDatabase(this);
        return w;
    }
    
    /**
     * Add a word to the database
     * @param w word to be added to the database
     * @return true if worded is added db. false if error occurs.
     */
    public boolean addWord(Word w) {
    	if (w.getId() != -1) {
    		throw new IllegalArgumentException("Word must be new!");
    	} else if (w.getCharacters() == null || w.getCharacters().size() == 0) {
    		throw new IllegalArgumentException("Word must contain characters!");
    	}
    	
    	mDb.beginTransaction();
    	ContentValues wordValues = new ContentValues();
    	wordValues.put(WORD_ORDER, w.getOrder());
    	long id = mDb.insert(WORD_TABLE, null, wordValues);
    	if (id == -1){
    		//if error
    		Log.e(WORD_TABLE, "Cannot add new word to table " + w);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// update word to reflect new id and order
    	w.setId(id);
    	w.setOrder(id); //id represents location in database
    	wordValues = new ContentValues();
    	wordValues.put(WORD_ORDER, w.getOrder());
    	int rowsAffected = mDb.update(WORD_TABLE, wordValues, WORD_ID + "=" + w.getId(), null);
    	if (rowsAffected != 1) {
    		//if error
    		Log.e(WORD_TABLE, "Cannot set order of word in table " + w);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// add characters to word
    	if (addCharsToWord(w) == false) {
    		mDb.endTransaction();
    		return false;
    	}
    	
    	//add attributes
    	if (! w.getTags().isEmpty()) {
    		w.addAttributes(ATTR_TYPE_TAG, w.getTags());
    	}
    	if (! w.getAttributes().isEmpty()) {
    		Map<String, Set<String>> emptyMap = new HashMap<String, Set<String>>();
    		boolean result = updateAttributes(w.getId(), w.getAttributes(),
    				emptyMap, WORD_ATTR_TABLE, WORD_ATTR_WORDID, WORD_ATTR_ATTRID);
    		if (result == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	w.removeAttributes(ATTR_TYPE_TAG);
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    private boolean addCharsToWord(Word w) {
    	List<Character> chars = w.getCharacters();
    	for (int i = 0; i < chars.size(); i++) {
    		Character c = chars.get(i);
    		ContentValues wordToCharValues = new ContentValues();
    		wordToCharValues.put(WORD_CHAR_WORDID, w.getId());
    		wordToCharValues.put(WORD_CHAR_CHARID, c.getId());
    		wordToCharValues.put(WORD_CHAR_ORDER, i);
    		long success = mDb.insert(WORD_CHAR_TABLE, null, wordToCharValues);
    		if (success == -1) {
    			//if error
    			Log.e(WORD_CHAR_TABLE, "Cannot add character, " + c + ", to table.");;
    			return false;
    		}
    	}
    	return true;
    }
    
    /** 
     *  Get a word from the database.
     *  @param id for the word
     *  @return the word or null if unsuccessful.
     */
    public Word getWord(long id) {
    	return getWord(id, false);
    }
    
    /** 
     *  Get a word from the database.
     *  @param id for the word
     *  @param shallow if true, only gets character ids.
     *  @return the word or null if unsuccessful.
     */
    public Word getWord(long id, boolean shallow) {
    	if (id == -1) return null;
    	Cursor cursor = mDb.query(WORD_TABLE, null, WORD_ID + "=" + id,
    			null, null, null, null);
    	if (cursor == null) {
    		Log.e(WORD_TABLE, "Cannot find word, " + id + ", in table.");
    		return null;
    	}
    	cursor.moveToFirst();
    	if (cursor.getCount() != 1) {
    		Log.e(WORD_TABLE, "Did not find exactly one " +
    				          "word when searching for " + id + " in table.");
    		cursor.close();
    		return null;
    	} else if (id != cursor.getInt(cursor.getColumnIndexOrThrow(WORD_ID))) {
    		Log.e(WORD_TABLE, "Returned wrong word when searching for " +
    	                       id + " in table.");
    		cursor.close();
    		return null;
    	}
    	
    	//create word
    	Word w = new Word();
    	w.setId(id);
    	w.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(WORD_ORDER)));
    	cursor.close();
    	
    	//get attributes of word
    	Cursor attrCursor = getAttributesCursor(
    			id, WORD_ATTR_TABLE, WORD_ATTR_WORDID, WORD_ATTR_ATTRID);
    	if (attrCursor != null) {
    		attrCursor.moveToFirst();
    	}
    	if (attrCursor != null && attrCursor.getCount() > 0) {
    		int typeIndex = attrCursor.getColumnIndexOrThrow(ATTR_TYPE);
    		int nameIndex = attrCursor.getColumnIndexOrThrow(ATTR_NAME);
    		do {
    			String type = attrCursor.getString(typeIndex);
    			String attr = attrCursor.getString(nameIndex);
    			if (type.equalsIgnoreCase(ATTR_TYPE_TAG)) {
    				w.addTag(attr);
    			} else {
    				w.addAttribute(type, attr);
    			}
    		} while (attrCursor.moveToNext());
    	}
    	if (attrCursor != null) attrCursor.close();
    	
    	//get characters of word
    	cursor = mDb.query(WORD_CHAR_TABLE, null,
    			WORD_CHAR_WORDID + "=" + w.getId(), null, null, null, WORD_CHAR_ORDER);
    	if (cursor != null) {
    		cursor.moveToFirst();
    	}
    	if (cursor != null && cursor.getCount() > 0) {
    		int charIndex = cursor.getColumnIndexOrThrow(WORD_CHAR_CHARID);
    		do {
    			long charId = cursor.getLong(charIndex);
    			Character c = null;
    			if (shallow) {
    				c = new Character();
    				c.setId(charId);
    			} else {
    				c = getCharacter(charId);
    			}
    			if (c == null) {
    				Log.e("WORD_CHAR_WORDID", "Could not find character with id " + charId);
    				cursor.close();
    				return null;
    			} else {
    				w.addCharacter(c);
    			}
    		} while (cursor.moveToNext());
    	}
    	return w;
    }
    
    /**
     * Get all words in the database
     * @return returns the words, ordered by order.
     */
    public List<Word> getAllWords() {
    	List<Word> words = new ArrayList<Word>();
    	Cursor cursor = mDb.query(WORD_TABLE, new String[] {WORD_ID},
    			null, null, null, null, WORD_ORDER);
    	if (cursor == null) return null;
    	cursor.moveToFirst();
    	if (cursor.getCount() == 0) return words;
    	do {
    		long id = cursor.getLong(cursor.getColumnIndexOrThrow(WORD_ID));
    		Word w = getWord(id);
    		if (w == null) {
    			Log.e(WORD_TABLE, "Could not retrieve word with id " + id);
    			cursor.close();
    			return null;
    		}
    		words.add(w);
    	} while (cursor.moveToNext());
    	cursor.close();
    	return words;
    }
    
    /**
     * Update a word in the Database. Call this method when the in memory
     * word has changed from the database word.
     * @param w the word to be updated
     * @return true if the update was successful.
     */
    public boolean updateWord(Word w) {
    	Word oldWord = getWord(w.getId(), true);
    	if (oldWord == null) return addWord(w);
    	if (w.equals(oldWord)) return true; //no need to update
    	mDb.beginTransaction();
    	if (! w.getAttributes().equals(oldWord.getAttributes())) {
    		//update attributes;
    		if (updateAttributes(w.getId(), w.getAttributes(),
    				oldWord.getAttributes(), WORD_ATTR_TABLE,
    				WORD_ATTR_WORDID, WORD_ATTR_ATTRID) == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	if (! w.getTags().equals(oldWord.getTags())) {
    		//update tags
    		Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>();
    		tagMap.put(ATTR_TYPE_TAG, w.getTags());
    		Map<String, Set<String>> oldTags = new HashMap<String, Set<String>>();
    		oldTags.put(ATTR_TYPE_TAG, oldWord.getTags());
    		if (updateAttributes(w.getId(), tagMap, oldTags,
    				WORD_ATTR_TABLE, WORD_ATTR_WORDID, WORD_ATTR_ATTRID) == false) {
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	//see if the characters are different
    	boolean differentChars = false;
    	if (w.getCharacters().size() != oldWord.getCharacters().size()) {
    		differentChars = true;
    	} else {
    		for (int i = 0; i < w.getCharacters().size(); i++) {
        		if (w.getCharacters().get(i).getId() !=
        				oldWord.getCharacters().get(i).getId()) {
        			differentChars = true;
        			break;
        		}
        	}
    	}
    	if (differentChars) {
    		//delete old characters
    		int rowsDeleted = mDb.delete(
    				WORD_CHAR_TABLE, WORD_CHAR_WORDID + "=" + w.getId(), null);
    		if (rowsDeleted != oldWord.getCharacters().size()) {
    			Log.e(WORD_CHAR_TABLE,
    					"Could not delete all characters in word " + oldWord);
    			mDb.endTransaction();
    			return false;
    		}
    		
    		// add characters to word
        	if (addCharsToWord(w) == false) {
        		mDb.endTransaction();
        		return false;
        	}
    	}
    	if (w.getOrder() != oldWord.getOrder()) {
    		//update order
    		ContentValues charValues = new ContentValues();
    		charValues.put(WORD_ORDER, w.getOrder());
    		int numRows = mDb.update(WORD_TABLE, charValues, WORD_ID + "=" + w.getId(), null);
    		if (numRows != 1) {
    			Log.e(WORD_TABLE, "Unable to update order for word, " + w);
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /**
     * Delete the word from the database.
     * Cascading gets rid of attributes, link to characters.
     * @param w the word to be deleted.
     * @return true if deletion was successful.
     */
    public boolean deleteWord(Word w) {
    	mDb.beginTransaction();
    	int rowsDeleted = mDb.delete(WORD_TABLE, WORD_ID + "=" + w.getId(), null);
    	if (rowsDeleted != 1) {
    		mDb.endTransaction();
    		Log.e(WORD_TABLE, "Unable to delete word, " + w);
    		return false;
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
     
    /**
     * Add a word to the database
     * @param w word to be added to the database
     * @return true if word is added to DB.  False on error.
     */
    public boolean addWord(Word w)
    {
    	mDb.beginTransaction();
    	//add to WORDS_TABLE
    	ContentValues initialWordsValues = new ContentValues();
    	initializePrivateTag(w, initialWordsValues);
    	initialWordsValues.put("wordOrder", 0);//Qin
    	long id = mDb.insert(WORDS_TABLE, null, initialWordsValues);
    	if(id == -1)
    	{
    		//if error
    		Log.e(WORDS_TABLE, "cannot add new character to table "+WORDS_TABLE);
    		mDb.endTransaction();
    		return false;
    	}
    	Cursor x = mDb.query(WORDS_TABLE, new String[]{"_id"}, null, null, null, null, "_id DESC", "1");
    	if (x != null) {
            x.moveToFirst();
        }
    	long rowid = x.getInt(x.getColumnIndexOrThrow(WORD_ID));
    	w.setId(rowid);
    	//Qin
    	ContentValues newWordsValues = new ContentValues();
    	newWordsValues.put("wordOrder", rowid);
    	int result = mDb.update(WORDS_TABLE, newWordsValues, "_id = "+rowid, null);
    	if(result==0){
    	    mDb.delete(WORDS_TABLE, WORD_ID+"="+rowid, null);
    	    mDb.endTransaction();
    	    return false;
    	}
    	//add each character to WORDS_DETAILS_TABLE
    	List<Long> l = w.getCharacterIds();
    	//character ordering
    	int charNumber=0;
    	for(Long c:l)
    	{
    		ContentValues characterValues = new ContentValues();
    		characterValues.put("_id", id);
    		characterValues.put("CharId", c.intValue());
    		characterValues.put("WordOrder", charNumber);
    		characterValues.put("FlagUserCreated", 1);
    		long success = mDb.insert(WORDS_DETAILS_TABLE, null, characterValues);
    		if(success == -1)
    		{	
    			//if error
    			Log.e(WORDS_DETAILS_TABLE,"cannot add to table");
    			mDb.endTransaction();
    			return false;
    		}
    		charNumber++;
    	}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    	
    }
    
  //Qin
    public long moveupWord(long id){
	Cursor x =
                mDb.query(true, WORDS_TABLE, new String[] {WORD_ID, "wordOrder"}, WORD_ID + "=" + id, null,
                        null, null, null, null);
	if(x==null || x.getCount()==0)
	    return -2;
	else{
	    x.moveToFirst();
	    long wordOrder = x.getInt(x.getColumnIndexOrThrow("wordOrder"));
	    long wordId = x.getInt(x.getColumnIndexOrThrow(WORD_ID));
	    Cursor y = mDb.query(true, WORDS_TABLE, new String[] {WORD_ID, "wordOrder"}, "wordOrder<"+wordOrder +" AND wordOrder>0", 
		    null, null, null, "wordOrder DESC", "1");
	    if(y==null || y.getCount()==0)
		return -1;//do not have smaller order
	    else{
		y.moveToFirst();
		long wordOrderUp = y.getInt(y.getColumnIndexOrThrow("wordOrder"));
		long wordIdUp = y.getInt(y.getColumnIndexOrThrow(WORD_ID));
		
		ContentValues swapValues = new ContentValues();
		swapValues.put("wordOrder", wordOrderUp);
		mDb.update(WORDS_TABLE, swapValues, "_id="+wordId, null);
		
		ContentValues swapValuesUp = new ContentValues();
		swapValuesUp.put("wordOrder", wordOrder);
		mDb.update(WORDS_TABLE, swapValuesUp, "_id="+wordIdUp, null);
	    }
	}
	return id;
    }
    
    //Qin
    public long movedownWord(long id){
	Cursor x =
                mDb.query(true, WORDS_TABLE, new String[] {WORD_ID, "wordOrder"}, WORD_ID + "=" + id, null,
                        null, null, null, null);
	if(x==null || x.getCount()==0)
	    return -2;
	else{
	    x.moveToFirst();
	    long wordOrder = x.getInt(x.getColumnIndexOrThrow("wordOrder"));
	    long wordId = x.getInt(x.getColumnIndexOrThrow(WORD_ID));
	    Cursor y = mDb.query(true, WORDS_TABLE, new String[] {WORD_ID, "wordOrder"}, "wordOrder>"+wordOrder, 
		    null, null, null, "wordOrder ASC", "1");
	    if(y==null || y.getCount()==0)
		return -1;//do not have smaller order
	    else{
		y.moveToFirst();
		long wordOrderDown = y.getInt(y.getColumnIndexOrThrow("wordOrder"));
		long wordIdDown = y.getInt(y.getColumnIndexOrThrow(WORD_ID));
		
		ContentValues swapValues = new ContentValues();
		swapValues.put("wordOrder", wordOrderDown);
		mDb.update(WORDS_TABLE, swapValues, "_id="+wordId, null);
		
		ContentValues swapValuesUp = new ContentValues();
		swapValuesUp.put("wordOrder", wordOrder);
		mDb.update(WORDS_TABLE, swapValuesUp, "_id="+wordIdDown, null);
	    }
	}
	return id;
    }
    
    public long deleteWord(long id){
    	Cursor mCursor =
                mDb.query(true, WORDS_TABLE, new String[] {WORD_ID}, WORD_ID + "=" + id, null,
                        null, null, null, null);
    	 if (mCursor == null) {
             return -1;
         }
    	 
		 mDb.delete(WORDS_TABLE, WORD_ID + "=" + id, null);
		 mDb.delete(WORDS_DETAILS_TABLE, "_id = " + id, null);
		 mDb.delete(WORDTAG_TABLE, "_id="+id, null);
		 mDb.delete(LESSONS_DETAILS_TABLE, "WordId="+id, null);
    	 
    	return id;
    }
    
    public String getPrivateTag(long id, ItemType type)
    {
    	String tableName;
    	switch(type)
    	{
    	case CHARACTER:
    		tableName=CHAR_TABLE;
    		break;
    	case WORD:
    		tableName=WORDS_TABLE;
    		break;
    	case LESSON:
    		tableName=LESSONS_TABLE;
    		break;
    	default:
    		Log.e("Tag", "Unsupported Type");
    		return "";
    	}
    	Cursor mCursor =
    			mDb.query(true, tableName, new String[] {"_id","name"}, "_id=" + id, null,
    					null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
        	return (mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
    	}

    	return "";
    }
    
    /**
     * Return a List of tags that matches the given character's charId
     * 
     * @param charId id of character whose tags we want to retrieve
     * @return List of tags
     * @throws SQLException if character could not be found/retrieved
     */
    public List<String> getCharacterTags(long charId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, CHARTAG_TABLE, new String[] {CHARTAG_TAG}, CHARTAG_ROWID + "=" + charId, null,
                    null, null, CHARTAG_TAG+" ASC", null);
        List<String> tags = new ArrayList<String>();
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        do {
        	if(mCursor.getCount()==0){
        		break;
        	}
        	tags.add(mCursor.getString(mCursor.getColumnIndexOrThrow(CHARTAG_TAG)));
        }
        while(mCursor.moveToNext());
        return tags;

    }
    
    /**
     * Return a list of char ids that are associated with the input tag
     * 
     * @param tag text of tag to match
     * @return List of char ids, or null if cursor not created.
     * @throws SQLException if character could not be found/retrieved
     */
    public List<Long> getCharsByTag(String tag) throws SQLException {
    
        Cursor mCursor = null;
        tag = tag.toUpperCase();
        //return only exact matches if the tag is two or less characters
        if (tag.length() < 3) {
            mCursor = mDb.query(true, CHARTAG_TABLE, new String[] {CHARTAG_ROWID}, 
            		"upper(" + CHARTAG_TAG + ") = '" + tag + "'", null,
                    null, null, CHARTAG_ROWID + " ASC", null);
        } else {
        	mCursor = mDb.query(true, CHARTAG_TABLE, new String[] {CHARTAG_ROWID}, 
            		"upper(" + CHARTAG_TAG + ") LIKE '%" + tag + "%'", null,
                    null, null, CHARTAG_ROWID + " ASC", null);
        }
        if (mCursor == null) return null;
        
        mCursor.moveToFirst();
        List<Long> ids = new LinkedList<Long>();
		do{
			if(mCursor.getCount()==0){
				break;
			}
			ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(DbAdapter.CHARTAG_ROWID)));
			//builder.append(c.getString(c.getColumnIndexOrThrow(DbAdapter.CHARTAG_ROWID))+"\n");			
		}
		while(mCursor.moveToNext());
        mCursor.close();
        
        return ids;

    }
   
    /**
     * Return a List of tags that matches the given Lesson's id
     * 
     * @param lessonId id of lesson whose tags we want to retrieve
     * @return List of tags
     * @throws SQLException if lesson could not be found/retrieved
     */
    public List<String> getLessonTags(long lessonId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, LESSONTAG_TABLE, new String[] {"tag"}, "_id" + "=" + lessonId, null,
                    null, null, "tag"+" ASC", null);
        List<String> tags = new ArrayList<String>();
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        do {
        	if(mCursor.getCount()==0){
        		break;
        	}
        	tags.add(mCursor.getString(mCursor.getColumnIndexOrThrow("tag")));
        }
        while(mCursor.moveToNext());
        return tags;

    }
    
    /**
     * Return a List of tags that matches the given word's wordId
     * 
     * @param wordId id of word whose tags we want to retrieve
     * @return List of tags
     * @throws SQLException if word could not be found/retrieved
     */
    public List<String> getWordTags(long wordId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, WORDTAG_TABLE, new String[] {WORDTAG_TAG}, WORDTAG_ROWID + "=" + wordId, null,
                    null, null, WORDTAG_TAG+" ASC", null);
        List<String> tags = new ArrayList<String>();
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        do {
        	if(mCursor.getCount()==0){
        		break;
        	}
        	tags.add(mCursor.getString(mCursor.getColumnIndexOrThrow(WORDTAG_TAG)));
        }
        while(mCursor.moveToNext());
        return tags;

    }
    
    /**
     * Return a Cursor positioned at the word that matches the given tag
     * 
     * @param tag text of tag to match
     * @return Cursor positioned to matching word, if found
     * @throws SQLException if word could not be found/retrieved
     */
    public Cursor getWords(String tag) throws SQLException {

        Cursor mCursor =

            mDb.query(true, WORDTAG_TABLE, new String[] {WORDTAG_ROWID}, WORDTAG_TAG + "='" + tag+"'", null,
                    null, null, WORDTAG_ROWID + " ASC", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    /**
     * Return a list of char ids from the database
     * @return ids list of all char ids
     */
    public List<Long> getAllCharIds(){
    	 Cursor mCursor =

	            mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID}, null, null,
	                    null, null, CHAR_ID+" ASC", null);
	        List<Long> ids = new ArrayList<Long>();
	        if (mCursor != null) {
	            mCursor.moveToFirst();
	        }
	        do {
	        	if(mCursor.getCount()==0){
	        		break;
	        	}
	        	ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(CHAR_ID)));
	        }
	        while(mCursor.moveToNext());
	        return ids;
    }
    //Qin
    public List<Long> getAllCharIdsByOrder(){
	Cursor mCursor = 
		mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID}, null, null,
	                    null, null, "charOrder ASC", null);
	List<Long> ids = new ArrayList<Long>();
	if (mCursor != null){
	    mCursor.moveToFirst();
	}
	do{
	    if(mCursor.getCount()==0){
		break;
	}
	ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(CHAR_ID)));
	}
	while(mCursor.moveToNext());
        return ids;
    }
    
    /**
     * Return a Cursor positioned at all characters
     * @return Cursor positioned to characters
     */
    public Cursor getAllCharIdsCursor(){
   	 Cursor mCursor =

	            mDb.query(true, CHAR_TABLE, new String[] {CHAR_ID}, null, null,
	                    null, null, CHAR_ID+" ASC", null);
	        if (mCursor != null) {
	            mCursor.moveToFirst();
	        }
	        return mCursor;
   }
    
    /**
     * Updates a private tag for a character. 
     * 
     * @param id row id for a character
     * @param tag the text of the tag to add
     * @return number of rows that were affected, 0 on no rows affected
     */
    public long updatePrivateTag(long id, String tag){
    	ContentValues initialValues = new ContentValues();
        initialValues.put(CHAR_ID, id); 
        initialValues.put("name", tag);
        Log.e("Adding Private Tag",tag);
        return mDb.update(CHAR_TABLE, initialValues, CHAR_ID+"="+id,null);
    }
    
    /**
     * Updates a private tag for a word. Returns row id on 
     * 
     * @param id row id for a word
     * @param tag the text of the tag to add
     * @return number of rows that were affected, 0 on no rows affected
     */
    public long updatePrivateWordTag(long id, String tag){
    	ContentValues initialValues = new ContentValues();
        //initialValues.put(CHAR_ROWID, id);
        initialValues.put("name", tag);

        return mDb.update(WORDS_TABLE, initialValues, "_id="+id,null);
    }
    
    /**
     * Updates a private tag for a lesson. Returns row id on 
     * 
     * @param id row id for a word
     * @param tag the text of the tag to add
     * @return number of rows that were affected, 0 on no rows affected
     */
    public long updatePrivateLessonTag(long id, String tag){
    	ContentValues initialValues = new ContentValues();
        initialValues.put("name", tag);
        return mDb.update(LESSONS_TABLE, initialValues, "_id="+id,null);
    }
    
    /**
     * Return a list of word ids from the database
     * @return ids list of all word ids
     */
    public List<Long> getAllWordIds() {
    	 Cursor mCursor =

 	            mDb.query(true, WORDS_TABLE, new String[] {WORD_ID}, null, null,
 	                    null, null, WORD_ID+" ASC", null);
 	        List<Long> ids = new ArrayList<Long>();
 	        if (mCursor != null) {
 	            mCursor.moveToFirst();
 	        }
 	        do {
 	        	if(mCursor.getCount()==0){
 	        		break;
 	        	}
 	        	ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(WORD_ID)));
 	        }
 	        while(mCursor.moveToNext());
 	        return ids;
    }
    
    //Qin
    public List<Long> getAllWordIdsByOrder(){
	Cursor mCursor = 
		mDb.query(true, WORDS_TABLE, new String[] {WORD_ID}, null, null,
	                    null, null, "wordOrder ASC", null);
	List<Long> ids = new ArrayList<Long>();
	if (mCursor != null){
	    mCursor.moveToFirst();
	}
	do{
	    if(mCursor.getCount()==0){
		break;
	}
	ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(WORD_ID)));
	}
	while(mCursor.moveToNext());
        return ids;
    }
    
    public List<String> getAllLessonNames(){
    	 Cursor mCursor =

  	            mDb.query(true, LESSONS_TABLE, new String[] {"name"}, null, null,
  	                    null, null, "name ASC", null);
  	        List<String> names = new ArrayList<String>();
  	      if (mCursor != null) {
	            mCursor.moveToFirst();
	        }
	        do {
	        	if(mCursor.getCount()==0){
	        		break;
	        	}
	        	names.add(mCursor.getString((mCursor.getColumnIndexOrThrow("name"))));
	        }
	        while(mCursor.moveToNext());
	        return names;
    }
    
    public long addWordToLesson(String lessonName, long wordId){
    	mDb.beginTransaction();
    	Cursor x = mDb.query(LESSONS_TABLE, new String[]{"_id"}, "name='"+lessonName+"'", null, null, null, null, null);
    	if (x != null) {
            x.moveToFirst();
        }
    	else{
    		return -1;
    	}
    	int lessonId = x.getInt(x.getColumnIndexOrThrow("_id"));
    	
    	x = mDb.query(LESSONS_DETAILS_TABLE, new String[]{"LessonOrder"}, null, null, null, null, "LessonOrder DESC", "1");
    	if (x != null) {
            x.moveToFirst();
        }
    	else{
    		return -1;
    	}
    	int lessonOrder = x.getInt(x.getColumnIndexOrThrow("LessonOrder"));
    	ContentValues values = new ContentValues();
    	values.put("LessonId", lessonId);
    	values.put("WordId", wordId);
    	values.put("LessonOrder",lessonOrder);
    	long ret = mDb.insert(LESSONS_DETAILS_TABLE, null, values);
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return ret;
    }
    
    /**
     * Add a collection to the database
     * @param c collection to be added to the database
     * @return true if collection is added. false if error occurs.
     */
    public boolean addCollection(Collection c) {
    	if (c.getId() != -1) {
    		throw new IllegalArgumentException("Collection must be new!");
    	} else if (c.getWords() == null || c.getWords().size() == 0) {
    		throw new IllegalArgumentException("Collection must contain characters!");
    	} else if (c.getName() ==  null) {
    		throw new IllegalArgumentException("Collection must have a name!");
    	}
    	
    	mDb.beginTransaction();
    	ContentValues collValues = new ContentValues();
    	collValues.put(COLL_ORDER, c.getOrder());
    	collValues.put(COLL_NAME, c.getName());
    	collValues.put(COLL_DESCRIPTION, c.getDescription());
    	long id = mDb.insert(COLL_TABLE, null, collValues);
    	if (id == -1){
    		Log.e(COLL_TABLE, "Cannot add new collection to table " + c);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// update collection to reflect new id and order
    	c.setId(id);
    	c.setOrder(id); //id represents location in database
    	collValues = new ContentValues();
    	collValues.put(COLL_ORDER, c.getOrder());
    	int rowsAffected = mDb.update(COLL_TABLE, collValues, COLL_ID + "=" + c.getId(), null);
    	if (rowsAffected != 1) {
    		//if error
    		Log.e(COLL_TABLE, "Cannot set order of collection in table " + c);
    		mDb.endTransaction();
    		return false;
    	}
    	
    	// add words to collection
    	if (addWordsToColl(c) == false) {
    		mDb.endTransaction();
    		return false;
    	}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    private boolean addWordsToColl(Collection coll) {
    	List<Word> words = coll.getWords();
    	for (int i = 0; i < words.size(); i++) {
    		Word w = words.get(i);
    		ContentValues values = new ContentValues();
    		values.put(COLL_WORD_COLLID, coll.getId());
    		values.put(COLL_WORD_WORDID, w.getId());
    		values.put(COLL_WORD_ORDER, i);
    		long success = mDb.insert(COLL_WORD_TABLE, null, values);
    		if (success == -1) {
    			//if error
    			Log.e(COLL_WORD_TABLE, "Cannot add word, " + w + ", to table.");;
    			return false;
    		}
    	}
    	return true;
    }
    
    /** 
     *  Get a collection from the database including words.
     *  @param id for the collection
     *  @return the collection or null if unsuccessful.
     */
    public Collection getCollection(long id) {
    	return getCollection(id, false);
    }
    
    
    /** 
     *  Get a collection from the database.
     *  @param id for the collection
     *  @param shallow if true, only gathers the ids for associated words
     *  @return the collection or null if unsuccessful.
     */
    private Collection getCollection(long id, boolean shallow) {
    	if (id == -1) return null;
    	Cursor cursor = mDb.query(COLL_TABLE, null, COLL_ID + "=" + id,
    			null, null, null, null);
    	if (cursor == null) {
    		Log.e(COLL_TABLE, "Cannot find collection, " + id + ", in table.");
    		return null;
    	}
    	cursor.moveToFirst();
    	if (cursor.getCount() != 1) {
    		Log.e(COLL_TABLE, "Did not find exactly one " +
    				          "collection when searching for " + id + " in table.");
    		cursor.close();
    		return null;
    	} else if (id != cursor.getInt(cursor.getColumnIndexOrThrow(COLL_ID))) {
    		Log.e(COLL_TABLE, "Returned wrong collection when searching for " +
    	                       id + " in table.");
    		cursor.close();
    		return null;
    	}
    	
    	//create collection
    	Collection c = new Collection();
    	c.setId(id);
    	c.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(COLL_ORDER)));
    	c.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLL_NAME)));
    	c.setDescription(cursor.getString(
    			cursor.getColumnIndexOrThrow(COLL_DESCRIPTION)));
    	cursor.close();
    	
    	//get words for collection
    	cursor = mDb.query(COLL_WORD_TABLE, null,
    			COLL_WORD_COLLID + "=" + c.getId(),
    			null, null, null, COLL_WORD_ORDER);
    	if (cursor != null) {
    		cursor.moveToFirst();
    	}
    	if (cursor != null && cursor.getCount() > 0) {
    		int wordIndex = cursor.getColumnIndexOrThrow(COLL_WORD_WORDID);
    		do {
    			long wordId = cursor.getLong(wordIndex);
    			Word w = null;
    			if (shallow) {
    				w = new Word();
    				w.setId(wordId);
    			} else {
    				w = getWord(wordId);
    			}
    			if (w == null) {
    				Log.e(COLL_WORD_TABLE, "Could not find word with id " + wordId);
    				cursor.close();
    				return null;
    			} else {
    				c.addWord(w);
    			}
    		} while (cursor.moveToNext());
    	}
    	return c;
    }
    
    /**
     * Get all (shallow) collections in the database.
     * @return returns the words, ordered by order.
     */
    public List<Collection> getAllCollections() {
    	List<Collection> collections = new ArrayList<Collection>();
    	Cursor cursor = mDb.query(COLL_TABLE, new String[] {COLL_ID},
    			null, null, null, null, COLL_ORDER);
    	if (cursor == null) return null;
    	cursor.moveToFirst();
    	if (cursor.getCount() == 0) return collections;
    	do {
    		long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLL_ID));
    		Collection c = getCollection(id, true);
    		if (c == null) {
    			Log.e(COLL_TABLE, "Could not retrieve word with id " + id);
    			cursor.close();
    			return null;
    		}
    		collections.add(c);
    	} while (cursor.moveToNext());
    	cursor.close();
    	return collections;
    }
    
    /**
     * Update a collection in the Database. Call this method when the in memory
     * collection has changed from the database collection.
     * @param c the collection to be updated
     * @return true if the update was successful.
     */
    public boolean updateCollection(Collection c) {
    	Collection oldColl = getCollection(c.getId(), true);
    	if (oldColl == null) return addCollection(c);
    	if (c.equals(oldColl)) return true; //no need to update
    	mDb.beginTransaction();
    	
    	//see if the words are different
    	boolean differentWords = false;
    	List<Word> newWords = c.getWords();
    	List<Word> oldWords = oldColl.getWords();
    	if (newWords.size() != oldWords.size()) {
    		differentWords = true;
    	} else {
    		for (int i = 0; i < newWords.size(); i++) {
        		if (newWords.get(i).getId() != oldWords.get(i).getId()) {
        			differentWords = true;
        			break;
        		}
        	}
    	}
    	if (differentWords) {
    		//delete old words
    		int rowsDeleted = mDb.delete(
    				COLL_WORD_TABLE, COLL_WORD_COLLID + "=" + c.getId(), null);
    		if (rowsDeleted != oldColl.getWords().size()) {
    			Log.e(COLL_WORD_TABLE,
    					"Could not delete all words in collection" + oldColl);
    			mDb.endTransaction();
    			return false;
    		}
    		
    		// add new words to collection
        	if (addWordsToColl(c) == false) {
        		mDb.endTransaction();
        		return false;
        	}
    	}
    	
    	//update columns in Collection table;
    	String newDesc = c.getDescription();
    	String oldDesc = oldColl.getDescription();
    	boolean diffDesc = (newDesc == null && oldDesc == null) || 
    			           (newDesc != null && newDesc.equals(oldDesc));
    	if (diffDesc || c.getOrder() != oldColl.getOrder() ||
    			oldColl.getName().equals(c.getName())) {
    		ContentValues collValues = new ContentValues();
    		collValues.put(COLL_ORDER, c.getOrder());
    		collValues.put(COLL_NAME, c.getName());
    		collValues.put(COLL_DESCRIPTION, c.getDescription());
    		int numRows = mDb.update(COLL_TABLE, collValues, COLL_ID + "=" + c.getId(), null);
    		if (numRows != 1) {
    			Log.e(COLL_TABLE, "Unable to update order for coll, " + c);
    			mDb.endTransaction();
    			return false;
    		}
    	}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /**
     * Delete the word from the database.
     * Cascading gets rid of attributes, link to characters.
     * @param c the word to be deleted.
     * @return true if deletion was successful.
     */
    public boolean deleteCollection(Collection c) {
    	mDb.beginTransaction();
    	int rowsDeleted = mDb.delete(COLL_TABLE, COLL_ID + "=" + c.getId(), null);
    	if (rowsDeleted != 1) {
    		mDb.endTransaction();
    		Log.e(COLL_TABLE, "Unable to delete collection, " + c);
    		return false;
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /**
     * Add a Lesson to the database
     * @param les lesson to be added to the database
     * @return true if lesson is added to DB.  False on error.
     */
    public boolean addLesson(Lesson les)
    {
    	mDb.beginTransaction();
    	//add to WORDS_TABLE
    	ContentValues initialLessonValues = new ContentValues();
    	initializePrivateTag(les,initialLessonValues);
    	long id = mDb.insert(LESSONS_TABLE, null, initialLessonValues);
    	if(id == -1)
    	{
    		//if error
    		Log.e(LESSONS_TABLE, "cannot add new character to table "+LESSONS_TABLE);
    		mDb.endTransaction();
    		return false;
    	}
    	Cursor x = mDb.query(LESSONS_TABLE, new String[]{"_id"}, null, null, null, null, "_id DESC", "1");
    	if (x != null) {
            x.moveToFirst();
        }
    	les.setId(x.getInt(x.getColumnIndexOrThrow("_id")));
    	
    	//add each word to LESSONS_DETAILS_TABLE
    	List<Long> l = les.getWordIds();
    	//word ordering
    	int wordNumber=0;
    	for(Long wordId:l)
    	{
    		ContentValues lessonValues = new ContentValues();
    		lessonValues.put("LessonId", id);
    		lessonValues.put("WordId", wordId);
    		lessonValues.put("LessonOrder", wordNumber);
    		long success = mDb.insert(LESSONS_DETAILS_TABLE, null, lessonValues);
    		if(success == -1)
    		{	
    			//if error
    			Log.e(LESSONS_DETAILS_TABLE,"cannot add to table");
    			mDb.endTransaction();
    			return false;
    		}
    		wordNumber++;
    	}
    	
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    public List<Long> getWordsFromLessonId(long id){
    	Cursor mCursor =
    			mDb.query(true, LESSONS_DETAILS_TABLE, new String[] {"WordId"}, "LessonId="+id, null,
    					null, null, "LessonOrder ASC", null);
    	List<Long> ids = new ArrayList<Long>();
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	do {
    		if(mCursor.getCount()==0){
    			break;
    		}
    		ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow("WordId")));
    	}
    	while(mCursor.moveToNext());
    	return ids;
    	//return null;
    }
    
    /**
     * Return a list of lesson ids from the database
     * @return ids list of all lesson ids
     */
    public List<Long> getAllLessonIds() {
    	Cursor mCursor =
    			mDb.query(true, LESSONS_TABLE, new String[] {LESSONS_ROWID}, null, null,
    					null, null, LESSONS_ROWID+" ASC", null);
    	List<Long> ids = new ArrayList<Long>();
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	do {
    		if(mCursor.getCount()==0){
    			break;
    		}
    		ids.add(mCursor.getLong(mCursor.getColumnIndexOrThrow(LESSONS_ROWID)));
    	}
    	while(mCursor.moveToNext());
    	return ids;
    }
    
    /**
     * Deletes the lesson by lesson id
     * @param id 
     * @return id if found, -1 if not
     */
    public long deleteLesson(long id){
    	Cursor mCursor =
                mDb.query(true, LESSONS_TABLE, new String[] {LESSONS_ROWID}, LESSONS_ROWID + "=" + id, null,
                        null, null, null, null);
    	int rowsDeleted=0;
    	if (mCursor == null) {
             return -1;
         }
    	 else{
    		 rowsDeleted += mDb.delete(LESSONS_TABLE, LESSONS_ROWID + "=" + id, null);
    		 rowsDeleted += mDb.delete(LESSONS_DETAILS_TABLE, "LessonId = " + id, null);
    		 rowsDeleted += mDb.delete(LESSONTAG_TABLE, LESSONTAG_ROWID + "=" + id, null);
    	 }
    	 if(rowsDeleted>0)
    		 return id;
    	 else
    		 return -1;

    }
    
    /**
     * @param id
     * @return
     */
    public Lesson getLessonById(long id) {
    	Cursor mCursor =
    			mDb.query(true, LESSONS_TABLE, new String[] {LESSONS_ROWID, "name"}, LESSONS_ROWID + "=" + id, null,
    					null, null, null, null);
    	Lesson le = new Lesson();
    	//if the Lesson doesn't exists
    	if (mCursor == null) {
    		return null;
    	}else{
    		mCursor.moveToFirst();
    		le.setName(mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
    	}

    	//SUSPECT: grab its details (step one might not be necessary and might cause slow downs
    	// but it is for data consistency.
    	mCursor =
    			mDb.query(true, LESSONS_DETAILS_TABLE, new String[] { "LessonId", "WordId", "LessonOrder"}, "LessonId" + "=" + id, null,
    					null, null, "LessonOrder ASC", null);
    	mCursor.moveToFirst();
    	do {
    		if(mCursor.getCount()==0){
    			break;
    		}
    		long wordId = mCursor.getLong(mCursor.getColumnIndexOrThrow("WordId"));
    		Log.i("LOAD", "Word: " + wordId);
    		le.addWord(wordId);
    	} while(mCursor.moveToNext());
    	le.setId(id);
    	le.setDatabase(this);
    	return le;
    }
    
    /**
     * Initializes a private tag
     * 
     * @param i the LessonItem
     * @param v ContentValues
     */
    private void initializePrivateTag(TraceableItem i, ContentValues v)
    {
    	if(i.getPrivateTag()!=null)
    		v.put("name",i.getPrivateTag());
    	else	
    		v.put("name","");
    }
}


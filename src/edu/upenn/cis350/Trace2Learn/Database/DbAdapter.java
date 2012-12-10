package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    		"CREATE TABLE IF NOT EXISTS " + ATTR_TYPE_TABLE + " (" +
            ATTR_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		ATTR_TYPE_NAME + " TEXT NOT NULL);";
    
    private static final String CREATE_ATTR_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + ATTR_TABLE + " (" +
            ATTR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		ATTR_TYPE + " INTEGER, " +
    		ATTR_TYPE_NAME + " TEXT NOT NULL, " +
    		"FOREIGN KEY (" + ATTR_TYPE + ") REFERENCES " +
    		ATTR_TYPE_TABLE + "(" + ATTR_TYPE_ID + "));";
    
    private static final String CREATE_CHAR_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + CHAR_TABLE + " (" +
            CHAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		CHAR_ORDER + " INTEGER NOT NULL, " +
    		CHAR_STROKES + " BLOB);";
    
    private static final String CREATE_CHAR_ATTR_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + CHAR_ATTR_TABLE + " ( " +
    		CHAR_ATTR_CHARID + " INTEGER NOT NULL, " +
    		CHAR_ATTR_ATTRID + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + CHAR_ATTR_ATTRID + ") REFERENCES " +
    		ATTR_TABLE + "(" + ATTR_ID + "), " +
    		"FOREIGN KEY (" + CHAR_ATTR_CHARID + ") REFERENCES " +
    		CHAR_TABLE + "(" + CHAR_ID + ") ON DELETE CASCADE, " +
    		"PRIMARY KEY (" + CHAR_ATTR_CHARID + ", " +
    		CHAR_ATTR_ATTRID + "));";

    private static final String CREATE_WORD_TABLE = 
    		"CREATE TABLE IF NOT EXISTS " + WORD_TABLE + " (" + WORD_ID +
    		" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		WORD_ORDER + " INTEGER NOT NULL);";
    
    private static final String CREATE_WORD_ATTR_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + WORD_ATTR_TABLE + " ( " +
    		WORD_ATTR_WORDID + " INTEGER NOT NULL, " +
    		WORD_ATTR_ATTRID + " INTEGER NOT NULL, " +
    		"FOREIGN KEY (" + WORD_ATTR_ATTRID + ") REFERENCES " +
    		ATTR_TABLE + "(" + ATTR_ID + "), " +
    		"FOREIGN KEY (" + WORD_ATTR_WORDID + ") REFERENCES " +
    		WORD_TABLE + "(" + WORD_ID + ") ON DELETE CASCADE, "+
    		"PRIMARY KEY (" + WORD_ATTR_WORDID + ", " +
    		WORD_ATTR_ATTRID + "));";
    
    private static final String CREATE_WORD_CHAR_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + WORD_CHAR_TABLE + " ( " +
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
    		"CREATE TABLE IF NOT EXISTS " + COLL_TABLE + " (" + COLL_ID +
    		" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		COLL_ORDER + " INTEGER NOT NULL, " +
    		COLL_NAME + " TEXT NOT NULL, " +
    		COLL_DESCRIPTION + " TEXT);";
    
    private static final String CREATE_COLL_WORD_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + COLL_WORD_TABLE + " ( " +
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
            createTables(db);
        }
        
        private void createTables(SQLiteDatabase db) {
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
            Log.w(DATABASE_NAME, "Upgrading database from version " + oldVersion + " to "
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
    private final Locale locale = Locale.US;
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
    		mDbHelper.createTables(mDb);
    	}
        mDbHelper.close();
    }
    
    /**
     * Get all attribute type names in the database
     * @return a list of all attribute type names.
     */
    public List<String> getAllAttributeKeys() {
    	List<String> keys = new ArrayList<String>();
    	Cursor cursor = mDb.query(ATTR_TYPE_TABLE, new String[] {ATTR_TYPE_NAME},
    			null, null, null, null, null);
    	if (cursor == null) return null;
    	cursor.moveToFirst();
    	if (cursor.getCount() == 0) return keys;
    	do {
    		String key = cursor.getString(cursor.getColumnIndexOrThrow(ATTR_TYPE_NAME));
    		if (!key.equals(ATTR_TYPE_TAG)) {
    			keys.add(key);
    		}
    	} while (cursor.moveToNext());
    	cursor.close();
    	return keys;
    }
    
    /**
     * Add an attribute type, or return id of existing attribute.
     * @param type name of attribute type
     * @return row of attribute type in database.
     */
    private long addAttributeType(String type) {
    	Cursor c = mDb.query(ATTR_TYPE_TABLE, null,
    			"upper(" + ATTR_TYPE_NAME + ") = '" + type.toUpperCase(locale) + "'",
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
    			"upper(" + ATTR_TYPE_NAME + ") = '" + name.toUpperCase(locale) + "'",
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
        			"upper(" + ATTR_NAME + ") = '" + attribute.toUpperCase(locale) + "'",
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
    	if (c.getStrokes() == null || c.getStrokes().size() == 0) {
    		throw new IllegalArgumentException("Character must contain strokes!");
    	}
    	
    	// insert char into table
    	mDb.beginTransaction();
    	ContentValues charValues = new ContentValues();
    	charValues.put(CHAR_ORDER, c.getOrder());
    	charValues.put(CHAR_STROKES, Stroke.encodeStrokesData(c.getStrokes()));
    	if (c.getId() != -1) charValues.put(CHAR_ID, c.getId());
    	
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
        attr = attr.trim().toUpperCase(locale);
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
    	if (isCharacterInWords(c)) return false;
    	mDb.beginTransaction();
    	int rowsDeleted = mDb.delete(CHAR_TABLE, CHAR_ID + "=" + c.getId(), null);
    	if (rowsDeleted != 1) {
    		mDb.endTransaction();
    		Log.e(CHAR_TABLE, "Unable to delete char, " + c);
    		return false;
    	}
    	mDb.delete(WORD_CHAR_TABLE, WORD_CHAR_CHARID + "=" + c.getId(), null);
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    private boolean isCharacterInWords(Character c) {
    	// String query =
    	//		"SELECT " + WORD_CHAR_WORDID + " " +
    	//		"FROM " + WORD_CHAR_TABLE + " " +
    	//		"WHERE " + WORD_CHAR_CHARID + " = " + c.getId() + " " +
    	//		"GROUP BY " + WORD_CHAR_WORDID + " " +
    	//		"HAVING COUNT(DISTINCT " + WORD_CHAR_CHARID + ") > 1;";
    	
    	//Cursor cursor = mDb.rawQuery(query, null);
    	Cursor cursor = mDb.query(WORD_CHAR_TABLE, null, WORD_CHAR_CHARID + " = " + c.getId(),
    			null, null, null, null);
    	if (cursor == null) {
    		Log.e(WORD_CHAR_TABLE, "Could not execute query for words containing " + c);
    		return true; //don't execute deletion
    	}
    	cursor.moveToFirst();
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return false;
    	}
    	cursor.close();
    	return true;
    }
    
    /**
     * Add a word to the database
     * @param w word to be added to the database
     * @return true if worded is added db. false if error occurs.
     */
    public boolean addWord(Word w) {
    	if (w.getCharacters() == null || w.getCharacters().size() == 0) {
    		throw new IllegalArgumentException("Word must contain characters!");
    	}
    	
    	mDb.beginTransaction();
    	ContentValues wordValues = new ContentValues();
    	wordValues.put(WORD_ORDER, w.getOrder());
    	if (w.getId() != -1) wordValues.put(WORD_ID, w.getId());
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
    	if (cursor == null || cursor.getCount() == 0) {
    		Log.e(WORD_CHAR_TABLE, "Did not find characters for word " + w);
    		deleteWord(w);
    		return null;
    	}
    	cursor.moveToFirst();
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
    	if (cursor != null) cursor.close();
    	
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
    	mDb.delete(COLL_WORD_TABLE, COLL_WORD_WORDID + "=" + w.getId(), null);
    	mDb.delete(WORD_CHAR_TABLE, WORD_CHAR_WORDID + "=" + w.getId(), null);
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
    
    /**
     * Add a collection to the database
     * @param c collection to be added to the database
     * @return true if collection is added. false if error occurs.
     */
    public boolean addCollection(Collection c) {
        if (c.getWords() == null || c.getWords().size() == 0) {
    		throw new IllegalArgumentException("Collection must contain characters!");
    	} else if (c.getName() ==  null) {
    		throw new IllegalArgumentException("Collection must have a name!");
    	}
    	
    	mDb.beginTransaction();
    	ContentValues collValues = new ContentValues();
    	collValues.put(COLL_ORDER, c.getOrder());
    	collValues.put(COLL_NAME, c.getName());
    	collValues.put(COLL_DESCRIPTION, c.getDescription());
    	if (c.getId() != -1) collValues.put(COLL_ID, c.getId());
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
    public Collection getCollection(long id, boolean shallow) {
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
    		cursor.close();
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
    	int rowsDeleted = mDb.delete(
				COLL_WORD_TABLE, COLL_WORD_COLLID + "=" + c.getId(), null);
    	rowsDeleted = mDb.delete(COLL_TABLE, COLL_ID + "=" + c.getId(), null);
    	if (rowsDeleted != 1) {
    		mDb.endTransaction();
    		Log.e(COLL_TABLE, "Unable to delete collection, " + c);
    		return false;
    	}
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
    	return true;
    }
}
package com.vishwa.picspeak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StatsDbAdapter extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;
 
    // Database Name
    private static final String DATABASE_NAME = "gamestats";
 
    // Stats table name
    private static final String TABLE_STATS = "stats";
 
    // Stats Table Columns names
    private static final String KEY_WORD = "word";
    private static final String KEY_HINT_WORD = "hint_word";
    private static final String KEY_HINT_PHRASE = "hint_phrase";
    private static final String KEY_HINT_RHYME = "hint_rhyme";
    private static final String KEY_NUM_HINTS = "numHints";
    private static final String KEY_NUM_TRIES = "numTries";
    private static final String KEY_GUESS = "guess";
    private static final String KEY_SUCCESS = "sucess";
    
    private SQLiteDatabase mSqliteDb;
    
    public StatsDbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase mSqliteDb) {
    	String CREATE_STATS_TABLE = String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY NOT NULL, " +
    			"%s INTEGER NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL, %s TEXT NOT NULL, %s INTEGER NOT NULL)",
    			TABLE_STATS, KEY_WORD, KEY_NUM_TRIES, KEY_NUM_HINTS, KEY_HINT_WORD, KEY_HINT_PHRASE,
    			KEY_HINT_RHYME, KEY_GUESS, KEY_SUCCESS);
        mSqliteDb.execSQL(CREATE_STATS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase mSqliteDb, int oldVersion, int newVersion) {
        // Drop older table if existed
        mSqliteDb.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
 
        // Create tables again
        onCreate(mSqliteDb);
    }
    
    public void open()
    {
    	mSqliteDb = this.getWritableDatabase();
    }
    
    public void close()
    {
    	mSqliteDb.close();
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void addStat(String word, int numTries, int numHints, int hint_word, int hint_phrase,
    		int hint_rhyme, String guess, int success)
    {
    	ContentValues values = new ContentValues();
    	values.put(KEY_WORD, word);
    	values.put(KEY_NUM_TRIES, numTries);
    	values.put(KEY_NUM_HINTS, numHints);
    	values.put(KEY_HINT_WORD, hint_word);
    	values.put(KEY_HINT_PHRASE, hint_phrase);
    	values.put(KEY_HINT_RHYME, hint_rhyme);
    	values.put(KEY_GUESS, guess);
    	values.put(KEY_SUCCESS, success);
    	
    	mSqliteDb.replace(TABLE_STATS, null, values);
    }
    
    public String getStats()
    {
    	Cursor cursor = mSqliteDb.query(TABLE_STATS, new String[] {KEY_WORD, KEY_NUM_TRIES, KEY_NUM_HINTS, KEY_HINT_WORD, KEY_HINT_PHRASE,
    			KEY_HINT_RHYME, KEY_GUESS, KEY_SUCCESS}, null, null, null, null, null);
    	
    	StringBuffer emailBody = new StringBuffer();
        if(cursor != null)
        {
        	while(cursor.moveToNext())
        	{
        		String word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
        		String guess = cursor.getString(cursor.getColumnIndex(KEY_GUESS));
        	    int numTries = cursor.getInt(cursor.getColumnIndex(KEY_NUM_TRIES));
        		int numHints = cursor.getInt(cursor.getColumnIndex(KEY_NUM_HINTS));
        		String rhymeUsed = (cursor.getInt(cursor.getColumnIndex(KEY_HINT_RHYME)) == 1) ? "yes" : "no";
        		String phraseUsed = (cursor.getInt(cursor.getColumnIndex(KEY_HINT_PHRASE)) == 1) ? "yes" : "no";
        		String wordUsed = (cursor.getInt(cursor.getColumnIndex(KEY_HINT_WORD)) == 1) ? "yes" : "no";
        	    String success = (cursor.getInt(cursor.getColumnIndex(KEY_SUCCESS)) == 1) ? "yes" : "no";
        		
        	    emailBody.append(String.format("Word: %s\n", word));
        	    emailBody.append(String.format("User guessed: %s\n", guess));
        	    emailBody.append(String.format("Number of tries: %s\n", numTries));
        	    emailBody.append(String.format("Number of hints used: %s\n", numHints));
        	    emailBody.append(String.format("Rhyme hint used: %s\n", rhymeUsed));
        	    emailBody.append(String.format("Phrase hint used: %s\n", phraseUsed));
        	    emailBody.append(String.format("Word hint used: %s\n", wordUsed));
        	    emailBody.append(String.format("User succeded: %s\n", phraseUsed));
        	    emailBody.append(String.format("Succeded: %s\n", success));
        	    emailBody.append("\n\n");
        	}
        	String result = emailBody.toString();
        	return result;
        }
        
        return new String();
 
    }
    
}
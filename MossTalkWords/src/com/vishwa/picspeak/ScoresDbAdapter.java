package com.vishwa.picspeak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ScoresDbAdapter extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "gamescores";
 
    // Scores table name
    private static final String TABLE_SCORES = "scores";
 
    // Scores Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";
    
    private SQLiteDatabase db;
 
    public ScoresDbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_NAME + " TEXT PRIMARY KEY NOT NULL," + KEY_SCORE + " INTEGER NOT NULL)";
        db.execSQL(CREATE_SCORES_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
 
        // Create tables again
        onCreate(db);
    }
    
    public void open()
    {
    	db = this.getWritableDatabase();
    }
    
    public void close()
    {
    	db.close();
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    
    public void addScore(String name, int score)
    {
    	ContentValues values = new ContentValues();
    	values.put(KEY_NAME, name);
    	values.put(KEY_SCORE, score);
    	
    	db.insert(TABLE_SCORES, null, values);
    }
    
    public int getScore(String name)
    {
    	Cursor cursor = db.query(TABLE_SCORES, new String[] {KEY_NAME, KEY_SCORE}, KEY_NAME + "= '" + name + "'", null, null, null, null);
    	if (cursor != null)
    	{
    		if(cursor.moveToFirst())
    		{
	    		int score = cursor.getInt(cursor.getColumnIndex(KEY_SCORE));
	    		cursor.close();
	    		return score;
    		}
    	}
    	return -1;
    }
    
    public void updateScore(String name, int score)
    {
    	ContentValues values = new ContentValues();
    	values.put(KEY_NAME, name);
    	values.put(KEY_SCORE, score);
    	
    	db.update(TABLE_SCORES, values, KEY_NAME + "= '" + name + "'", null);
    }
    
}

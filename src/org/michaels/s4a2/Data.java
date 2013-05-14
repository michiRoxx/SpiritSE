package org.michaels.s4a2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class Data {
	public static SharedPreferences preferences = null;
	public static SQLiteDatabase db = null;
	
	public static void init(Context c){
		if(preferences != null)
			preferences = null;
		if(db != null)
			db.close();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(c);
		
		DBOpenHelper dboh = new DBOpenHelper(c);
		db = dboh.getWritableDatabase();
	}
	
	static class DBOpenHelper extends SQLiteOpenHelper {

		private final static int DB_VERSION = 3;
		
		public DBOpenHelper(Context context) {
			super(context,"db",null,DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("PRAGMA foreign_keys = ON;");
			
			db.execSQL("CREATE TABLE Lecture ("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"title TEXT NOT NULL, "+
					"ltype INTEGER NOT NULL, "+
					"lecturer TEXT NOT NULL, "+
					"lgroup INTEGER);");
			
			db.execSQL("CREATE TABLE Event ("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"week INTEGER NOT NULL, "+
					"start INTEGER NOT NULL, "+
					"len INTEGER NOT NULL, "+
					"room TEXT NOT NULL, "+
					"building TEXT NOT NULL, "+
					"lecture INTEGER NOT NULL," +
					"FOREIGN KEY(lecture) REFERENCES Lecture(id));");
			
			db.execSQL("CREATE TABLE Sparetime ("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"reason TEXT UNIQUE NOT NULL, "+
					"startutc INTEGER NOT NULL, "+
					"len INTEGER NOT NULL);");
			
			db.execSQL("CREATE TABLE News ("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"title TEXT NOT NULL, "+
					"content TEXT NOT NULL, "+
					"readstate INTEGER NOT NULL, "+
					"dateutc INTEGER NOT NULL, "+
					"invalidatutc INTEGER NOT NULL);");
			
			db.execSQL("CREATE TABLE Usergroup ("+
					"lecture INTEGER PRIMARY KEY, "+
					"ugroup INTEGER," +
					"FOREIGN KEY(lecture) REFERENCES Lecture(id));");
			
			db.execSQL("CREATE TABLE Sparetime_blocks_Lecture ("+
					"lecture INTEGER, "+
					"sparetime INTEGER, "+
					"PRIMARY KEY(lecture, sparetime), "+
					"FOREIGN KEY(lecture) REFERENCES Lecture(id), "+
					"FOREIGN KEY(sparetime) REFERENCES Sparetime(id))");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion < 2){
				db.execSQL("CREATE TABLE News ("+
						"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
						"title TEXT NOT NULL, "+
						"content TEXT NOT NULL, "+
						"readstate INTEGER NOT NULL, "+
						"dateutc INTEGER NOT NULL, "+
						"invalidatutc INTEGER NOT NULL);");
				db.execSQL("CREATE TABLE Usergroup ("+
						"lecture INTEGER PRIMARY KEY, "+
						"ugroup INTEGER," +
						"FOREIGN KEY(lecture) REFERENCES Lecture(id));");
			}
			if(oldVersion < 3){
				db.execSQL("CREATE TABLE Sparetime_blocks_Lecture ("+
						"lecture INTEGER, "+
						"sparetime INTEGER, "+
						"PRIMARY KEY(lecture, sparetime), "+
						"FOREIGN KEY(lecture) REFERENCES Lecture(id), "+
						"FOREIGN KEY(sparetime) REFERENCES Sparetime(id))");
			}
		}
		
	}
}
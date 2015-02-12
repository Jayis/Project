package com.sagax.player;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper{
	private static final int VERSION=3;
	public DBHelper(Context context, String name, CursorFactory factory,
			int version){
		super(context,name,factory,version); 
	}
	public DBHelper(Context context){
		this(context,"Player.db",null,VERSION);
	}
	
	public void onCreate(SQLiteDatabase db){
		String create = 
				"create table genreDB("
					       + "_ID INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
					             + "filePath VARCHAR,"
					             + "eqon INTEGER,"
					             + "genre VARCHAR"
					         + ")";
		db.execSQL(create);
		
	}
	
	public void onUpgrade(SQLiteDatabase db,int old_v,int new_v){
		db.execSQL("DROP TABLE IF EXISTS genreDB"); 
		onCreate(db);
	}
	
} 

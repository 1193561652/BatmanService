package com.example.batservice.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "bat.db";
    private static final int VERSION = 1;
    private static final String CREATE_TABLE_BAT_WORKING = "create table bat_working(id INTEGER PRIMARY KEY, name text, working text, issue text, reqTime integer)";
    private static final String DROP_TABLE_BAT_WORKING = "DROP TABLE IF EXISTS bat_working";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BAT_WORKING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_BAT_WORKING);
        db.execSQL(CREATE_TABLE_BAT_WORKING);
    }
}

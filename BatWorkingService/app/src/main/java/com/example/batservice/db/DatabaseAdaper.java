package com.example.batservice.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DatabaseAdaper {
    private DatabaseHelper databaseHelper;
    public DatabaseAdaper(Context context){
        databaseHelper = new DatabaseHelper(context, "name", null, 1);
    }
    //添加操作
    public long add(BatWorkingInfo item){
        long result = -1;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", item.id);
        values.put("name", item.name);
        values.put("working", item.working);
        values.put("issue", item.issue);
        values.put("reqTime", item.reqTime);

        //参数（表名，可以为null的列名，更新字段的集合ContentValues）
        //合法：insert into dog(name,age) values('xx',2)
        //不合法：insert into dog() values()
        result = db.insert("bat_working",null, values);
        db.close();
        return result;
    }

    //凭id查询
    public BatWorkingInfo findById(long id){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] colums = {"id", "name", "working", "issue", "reqTime"};
        //是否去除重复记录，参数（表名，要查询的列，查询条件，查询条件的值，分组条件，分组条件的值，排序，分页）
        Cursor c = db.query(true, "bat_working", colums,"id" + "=?", new String[]{String.valueOf(id)},null,null,null,null);
        BatWorkingInfo batWorkingInfo = null;
        if (c.moveToNext()){
            batWorkingInfo = new BatWorkingInfo();
            batWorkingInfo.id = id;
            batWorkingInfo.issue = c.getString(c.getColumnIndex("issue"));
            batWorkingInfo.name = c.getString(c.getColumnIndex("name"));
            batWorkingInfo.working = c.getString(c.getColumnIndex("working"));
            batWorkingInfo.reqTime = c.getLong(c.getColumnIndex("reqTime"));
        }
        c.close();
        db.close();
        return batWorkingInfo;
    }

    public void insertAndUpdate(BatWorkingInfo item) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", item.id);
        values.put("name", item.name);
        values.put("working", item.working);
        values.put("issue", item.issue);
        values.put("reqTime", item.reqTime);

        db.insertWithOnConflict("bat_working", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public int deleteById(long id) {
        int result = -1;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String whereClause = "id"+"=?";
        String[] whereArgs = {String.valueOf(id)};
        //表名，删除条件，条件的值
        result = db.delete("bat_working", whereClause, whereArgs);
        db.close();
        return result;
    }

    //查询所有
    public ArrayList<BatWorkingInfo> findAll(){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] colums = {"id", "name", "working", "issue", "reqTime"};
        //是否去除重复记录，参数（表名，要查询的列，查询条件，查询条件的值，分组条件，分组条件的值，排序，分页）
        Cursor c = db.query(true, "bat_working", colums,null,null,null,null,null,null);
        ArrayList<BatWorkingInfo> array = new ArrayList<>();
        BatWorkingInfo batWorkingInfo = null;
        while (c.moveToNext()){
            batWorkingInfo = new BatWorkingInfo();
            batWorkingInfo.id = c.getLong(c.getColumnIndex("id"));
            batWorkingInfo.issue = c.getString(c.getColumnIndex("issue"));
            batWorkingInfo.name = c.getString(c.getColumnIndex("name"));
            batWorkingInfo.working = c.getString(c.getColumnIndex("working"));
            batWorkingInfo.reqTime = c.getLong(c.getColumnIndex("reqTime"));
            array.add(batWorkingInfo);
        }
        c.close();
        db.close();
        return array;
    }
}

package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper4 extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "as.db";
    public static final String TABLE_NAME = "zx";
    public  static  final String COL_1 = "id";
    public  static  final String COL_2 = "itemname";
    public  static  final String COL_3 = "quantity";
    public  static  final String COL_4 = "type";
    public  static  final String COL_5 = "status";
    public  static  final String COL_6 = "uom";
    public DatabaseHelper4(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemname TEXT, quantity FLOAT,type TEXT,status INTEGER,uom TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public boolean insertData(String itemName, Double quantity, String type, Integer status, String uom){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, itemName);
        contentValues.put(COL_3, quantity);
        contentValues.put(COL_4, type);
        contentValues.put(COL_5, status);
        contentValues.put(COL_6, uom);
        long resultQuery = db.insert(TABLE_NAME,null,contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public Integer deleteType(String type){
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        result = db.delete(TABLE_NAME, "type = ?", new  String[] {type});
        return result;
    }

    public  Integer deleteData(String id){
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        result = db.delete(TABLE_NAME, "id = ?", new  String[] {id});
        return result;
    }

    public Cursor getAllData(String type){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE type='" + type + "';", null);
        return cursor;
    }

    public Cursor getAllWhereItem(String type,String itemName){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE type='" + type + "' AND itemname='" +itemName + "';", null);
        return  cursor;
    }

    public Integer countItems(String type){
        int resultPrice = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_NAME + " WHERE type='" + type + "';", null);
        if(result.moveToFirst()){
            do{
                resultPrice = Integer.parseInt(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultPrice;
    }



    public boolean checkItem(String itemName,String type){
        boolean result = false;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT id FROM " + TABLE_NAME + " WHERE itemname=? AND type='" + type + "';", new String[]{itemName});
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    result = true;
                }
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return result;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }

}

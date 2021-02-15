package com.example.atlanticgrains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "ghaj.db";
    public  static  final String TABLE_NAME = "ghaj";
    public  static  final String COL_2 = "itemname";
    public  static  final String COL_3 = "price";
    public  static  final String COL_4 = "quantity";
    public  static  final String COL_5 = "discountpercent";
    public  static  final String COL_6 = "totalprice";
    public  static  final String COL_7 = "free";
    public  static  final String COL_8 = "inventory_type";
    public  static  final String COL_9 = "uom";
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemname TEXT, quantity FLOAT,price FLOAT, discountpercent FLOAT, totalprice FLOAT, free INTEGER,inventory_type TEXT,uom TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(Double quantity, Double discountpercent, Double price, Integer free,Double totalprice, String item_name,String inv_type,String uom){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, item_name);
        contentValues.put(COL_3, price);
        contentValues.put(COL_4, quantity);
        contentValues.put(COL_5, discountpercent);
        contentValues.put(COL_6, totalprice);
        contentValues.put(COL_7, free);
        contentValues.put(COL_8, inv_type);
        contentValues.put(COL_9, uom);
        long resultQuery = db.insert(TABLE_NAME,null, contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public Cursor getAllData(){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return cursor;
    }


    public boolean checkItem(String itemName){
        boolean result = false;
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery("SELECT id FROM " + TABLE_NAME + " WHERE itemname= ?;", new String[]{itemName});
        if(cursor.moveToFirst()){
            do{
                result = true;
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public Double geTotalItems(){
        double resultSubTotal = 0.00;
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery("SELECT IFNULL(COUNT(itemname),0) FROM " + TABLE_NAME + ";", null);
        if(cursor.moveToFirst()){
            do{
                resultSubTotal = Double.parseDouble(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return resultSubTotal;
    }

    public Double getSubTotal(){
        double resultSubTotal = 0.00;
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery("SELECT IFNULL(SUM(totalprice),0) FROM " + TABLE_NAME + ";", null);
        if(cursor.moveToFirst()){
            do{
                resultSubTotal = Double.parseDouble(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return resultSubTotal;
    }


    public Integer deleteData(String id){
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        result = db.delete(TABLE_NAME, "id = ?", new  String[] {id});
        return result;
    }

    public Integer countItems(){
        int resultPrice = 0;
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_NAME + ";", null);
        if(cursor.moveToFirst()){
            do{
                resultPrice = Integer.parseInt(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return resultPrice;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}

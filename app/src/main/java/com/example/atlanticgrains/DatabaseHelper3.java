package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper3 extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "qqz.db";
    public static final String TABLE_NAME = "tbl_qqz";
    public  static  final String COL_1 = "id";
    public  static  final String COL_2 = "sap_number";
    public  static  final String COL_3 = "fromBranch";
    public  static  final String COL_4 = "item_name";
    public  static  final String COL_5 = "quantity";
    public  static  final String COL_6 = "actual_quantity";
    public  static  final String COL_7 = "isSelected";
    public  static  final String COL_8 = "isSAPIT";
    public  static  final String COL_9 = "toBranch";
    public  static  final String COL_10 = "base_id";
    public  static  final String COL_11 = "fromModule";
    public  static  final String COL_12 = "uom";
    public  static  final String COL_13 = "received_quantity";
    public  static  final String COL_14 = "item_id";
    public  static  final String COL_15 = "objtype";
    public  static  final String COL_16 = "isClose";
    public DatabaseHelper3(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,sap_number INTEGER, fromBranch TEXT,item_name TEXT,quantity FLOAT,actual_quantity FLOAT,isSelected INTEGER,isSAPIT INTEGER,toBranch TEXT, base_id INTEGER,fromModule TEXT,uom TEXT,received_quantity INTEGER,item_id INTEGER, objtype INTEGER, isClosed INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String sapNumber, String fromBranch,  String itemName,Double quantity,Double actual_quantity,int isSAPIT,String toBranch,int baseID, String fromModule,int isSelected, String uom,int received_quantity, int item_id, int objtype, int isClosed){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, sapNumber);
        contentValues.put(COL_3, fromBranch);
        contentValues.put(COL_4, itemName);
        contentValues.put(COL_5, quantity);
        contentValues.put(COL_6, actual_quantity);
        contentValues.put(COL_7, isSelected);
        contentValues.put(COL_8, isSAPIT);
        contentValues.put(COL_9, toBranch);
        contentValues.put(COL_10, baseID);
        contentValues.put(COL_11, fromModule);
        contentValues.put(COL_12, uom);
        contentValues.put(COL_13, received_quantity);
        contentValues.put(COL_14, item_id);
        contentValues.put(COL_15, isClosed);
        long resultQuery = db.insert(TABLE_NAME,null,contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public Cursor getAllData(String fromModule){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE fromModule='" + fromModule +"';", null);
        return  cursor;
    }

    public  boolean deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, "id = ?", new  String[] {id});
        boolean bResult = result <= 0 ? false : true;
        return bResult;
    }

    public boolean removeData(String id){
        SQLiteDatabase db  = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_7, 0);
        db.update(TABLE_NAME, contentValues, "id= ?", new String[]{id});
        return true;
    }

    public boolean updateSelected(String id,Integer isSelected, Double actual_quantity){
        SQLiteDatabase db  = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_7, isSelected);
        contentValues.put(COL_6, actual_quantity);
        db.update(TABLE_NAME, contentValues, "id= ?", new String[]{id});
        return true;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
    public boolean checkItem(String itemName,String fromModule){
        boolean result = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT id FROM " + TABLE_NAME + " WHERE item_name= ? AND fromModule='" + fromModule + "';",new String[]{itemName});
        if(cursor.moveToFirst()){
            do{
                result = true;
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public Integer countItems(String fromModule){
        int resultPrice = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_NAME + " WHERE fromModule='" + fromModule + "';", null);
        if(result.moveToFirst()){
            do{
                resultPrice = Integer.parseInt(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultPrice;
    }

    public Integer countSAPItems(String fromModule){
        int resultPrice = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_NAME + " WHERE fromModule='" + fromModule + "' AND isSelected=1;", null);
        if(result.moveToFirst()){
            do{
                resultPrice = Integer.parseInt(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultPrice;
    }
}

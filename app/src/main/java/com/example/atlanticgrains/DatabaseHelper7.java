package com.example.atlanticgrains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHelper7 extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "db_offlineee.db";
    public  static  final String TABLE_NAME = "tbl_offlineee";
    public  static  final String COL1 = "id";
    public  static  final String COL_2 = "URL";
    public  static  final String COL_3 = "method";
    public  static  final String COL_4 = "body";
    public  static  final String COL_5 = "from_module";
    public  static  final String COL_6 = "hidden_from_module";
    public  static  final String COL_7 = "date_created";
    public DatabaseHelper7(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,URL TEXT, method TEXT, body TEXT,from_module TEXT,hidden_from_module TEXT, date_created TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllData(){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY from_module,id", null);
        return cursor;
    }

    public double getDecreaseQuantity(String itemName){
        double result = 0.00;
        try{
            Cursor cursor;
            SQLiteDatabase db = this.getWritableDatabase();
            cursor = db.rawQuery("SELECT body,from_module FROM " + TABLE_NAME + " WHERE from_module IN ('Sales', 'Transfer Item');" , null);
            if(cursor != null) {
                while (cursor.moveToNext()) {
//                    if(cursor.getString(1).equals("Transfer Item")){
////                        System.out.println("ano moduleee " + cursor.getString(0));
//                    }
                    JSONObject jsonObjectBody = new JSONObject(cursor.getString(0));
                    JSONArray jsonArray = jsonObjectBody.getJSONArray((cursor.getString(1).equals("Sales") ? "rows" : "details"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectRows = jsonArray.getJSONObject(i);
                        if(itemName.contains(jsonObjectRows.getString("item_code"))){
                            result += jsonObjectRows.getDouble("quantity");
                        }
                    }
                }
            }
            cursor.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return result;
    }

    public double getIncreaseQuantity(String itemName){
        double result = 0.00;
        try{
            Cursor cursor;
            SQLiteDatabase db = this.getWritableDatabase();
            cursor = db.rawQuery("SELECT body,from_module FROM " + TABLE_NAME + " WHERE from_module IN ('Received Item', 'Item Request');" , null);
            if(cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject jsonObjectBody = new JSONObject(cursor.getString(0));
                    JSONArray jsonArray = jsonObjectBody.getJSONArray((cursor.getString(1).equals("Item Request") ? "rows" : "details"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectRows = jsonArray.getJSONObject(i);
                        if(itemName.contains(jsonObjectRows.getString("item_code"))){
                            result += jsonObjectRows.getDouble("quantity");
                        }
                    }
                }
            }
            cursor.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    public Cursor addQuantity() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT body,from_module FROM " + TABLE_NAME + " WHERE from_module IN ('Received Item', 'Item Request');", null);
        return cursor;
    }

    public Cursor getAllDataByModule(String module, String name){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + name +"='" + module + "';", null);
        return cursor;
    }

    public  boolean deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, "id = ?", new  String[] {id});
        boolean bResult = result <= 0 ? false : true;
        return bResult;
    }


    public boolean insertData(String sURL, String method,String body, String fromModule, String hiddenFromModule, String dateCreated){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, sURL);
        contentValues.put(COL_3, method);
        contentValues.put(COL_4, body);
        contentValues.put(COL_5, fromModule);
        contentValues.put(COL_6, hiddenFromModule);
        contentValues.put(COL_7, dateCreated);
        long resultQuery = db.insert(TABLE_NAME,null,contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}

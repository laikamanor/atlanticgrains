package com.example.atlanticgrains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper6 extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "db_login.db";
    public  static  final String TABLE_NAME = "tbl_login";
    public  static  final String COL1 = "id";
    public  static  final String COL_2 = "username";
    public  static  final String COL_3 = "password";
    public  static  final String COL_4 = "data";
    public  static  final String COL_5 = "date_created";
    public DatabaseHelper6(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT, password TEXT, data TEXT,date_created TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor checkUsernamePassword(String username, String password){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM" + TABLE_NAME + " WHERE username='" + username + "' AND password='" + password +"';", null);
        return cursor;
    }

    public boolean updatePassword(String id,String username){
        SQLiteDatabase db  = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, username);
        int result = db.update(TABLE_NAME, contentValues, "id= ?", new String[]{id});
        return result > 0;
    }

    public Cursor getAllDataByUsername(String username){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM" + TABLE_NAME + " WHERE username='" + username + "';", null);
        return cursor;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}

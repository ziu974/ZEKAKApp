package com.example.zekak;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ItemsDBHelper extends SQLiteOpenHelper {

    //public static String DATABASE_PATH = "";
    public static final String DATABASE_NAME = "items.db";
    public static final String TABLE_NAME = "ITEMS";
    public static final int DATABASE_VERSION = 1;

    //private final Context context;

    public final String ID = "id";              //PK
    public final String NAME = "name";
    public final String PRODUCT = "product";
    public final String BARCODE = "barcode";
    public final String EXP = "exp";            // format: YEAR.MM.DD
    public final String PORTION = "portion";    // format: (0~9)(#) (ex."92": 10회 중 2)
    public final String CATEGORY = "category";
    public final String PHOTO = "photo";        // format:"zekak_(시간)_*.jpg" (path: mainfest참고)
    public final String MEMO = "memo";
    public final String FLAG = "flag";          // 1: on / 0: off


    public ItemsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("데이테베이스 생성!", "ㅎㅎ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME +" TEXT, " +
                PRODUCT +" TEXT, " +
                BARCODE +" TEXT, " +
                EXP +" TEXT, " +
                PORTION +" INTEGER, " +
                CATEGORY +" TEXT, " +
                PHOTO +" TEXT, " +
                MEMO +" TEXT, " +
                FLAG +" INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
        onCreate(db);
    }
}

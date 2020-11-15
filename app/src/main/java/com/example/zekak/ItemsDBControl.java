package com.example.zekak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zekak.AppMain.Item;

public class ItemsDBControl extends AppCompatActivity {
    Context context;
    ItemsDBHelper helper;
    SQLiteDatabase db;
    Cursor cursor;



    public ItemsDBControl(Context context){
        this.context = context;
        helper = new ItemsDBHelper(context);
        db = context.openOrCreateDatabase(helper.DATABASE_NAME, MODE_ENABLE_WRITE_AHEAD_LOGGING, null);
    }



    public boolean insert(Item item){
        //db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(helper.NAME, item.name);
        values.put(helper.PRODUCT, item.product);
        values.put(helper.BARCODE, item.barcode);
        values.put(helper.EXP, item.exp);
        values.put(helper.PORTION, item.portion);
        values.put(helper.CATEGORY, item.category);
        values.put(helper.PHOTO, item.photo);
        values.put(helper.MEMO, item.memo);
        values.put(helper.FLAG, item.flag);

        if(db.insert(helper.TABLE_NAME, null, values) > 0){
            Log.i("데이터베이스에 데이터 insert 됨", item.name);
            //db.close();
            return true;
        } else {
            //db.close();
            return  false;
        }
    }

    //11/10 더 검색할 것 있으면 searchType 케이스 추가해서 여기에 selection 설정하면 됨
    public Cursor search(String searchType, String value){
        db = helper.getWritableDatabase();
        String selection = null;
        String[] selectionArgs = {value};
        switch (searchType) {
            // 아직은 카테고리 search()만 필요해서
            case "category":
                if(value.equals("CATEGORY")){       // 전체 아이템 목록 불러오려는 경우
                    selectionArgs = null;
                } else {
                    selection = helper.CATEGORY + "= ? ";
                }
                break;
            default:
                selection = null;       // define 안되면 그냥 전체 아이템 가져옴
                break;
        }
        cursor = db.query(helper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        return cursor;
        // TODO: but where---?     db.close();
    }

    public boolean edit(Item item, int itemID){
        db = helper.getWritableDatabase();
        String whereClause = "ID = "+ itemID;
        ContentValues values = new ContentValues();

        values.put(helper.NAME, item.name);
        values.put(helper.EXP, item.exp);
        values.put(helper.PORTION, item.portion);
        values.put(helper.CATEGORY, item.category);
        values.put(helper.PHOTO, item.photo);
        values.put(helper.MEMO, item.memo);
        values.put(helper.FLAG, item.flag);


        if(db.update(helper.TABLE_NAME, values, whereClause, null)>0){
            db.close();
            return true;
        } else{
            db.close();
            return false;

        }
    }

    public boolean usePortion(int itemID, int value) {
        db = helper.getWritableDatabase();
        String whereClause = "ID = "+ itemID;
        ContentValues values = new ContentValues();
        values.put(helper.PORTION, value);

        if(db.update(helper.TABLE_NAME, values, whereClause, null)>0){
            db.close();
            return true;
        } else{
            db.close();
            return false;
        }
    }


    public int delete(String deleteType, String value) {
        db = helper.getWritableDatabase();
        String selection;
        String whereClause;
        String[] selectionArgs = {value};

        switch (deleteType) {                 //11/10 더 검색할 것 있으면 searchBy 케이스 추가해서 여기에 selection 설정하면 됨
            case "item":
                selection = helper.ID;
                break;
            case "category":
                selection = helper.CATEGORY;
                break;
            default:
                selection = helper.CATEGORY;
                break;
        }

        whereClause = selection + "= ?";
        int itemsDeleted = db.delete(helper.TABLE_NAME, whereClause, selectionArgs);
        db.close();
        return itemsDeleted;
    }


//    public int statistics(String deleteType, String value) {
//        // TODO: statistics.java에 적어놓음
//        int itemsDeleted = db.delete(helper.TABLE_NAME, whereClause, selectionArgs);
//        db.close();
//        return itemsDeleted;
//    }


    // SQLite Close
    public void db_close() {
        db.close();
        helper.close();
    }
}


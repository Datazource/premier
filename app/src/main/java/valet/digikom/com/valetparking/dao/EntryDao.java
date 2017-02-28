package valet.digikom.com.valetparking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import valet.digikom.com.valetparking.domain.EntryCheckin;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;
import valet.digikom.com.valetparking.util.ValetDbHelper;

/**
 * Created by DIGIKOM-EX4 on 1/18/2017.
 */

public class EntryDao {

    private ValetDbHelper dbHelper;
    private static EntryDao entryDao;
    private Gson gson;

    private EntryDao(ValetDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        gson = new Gson();
    }

    public static EntryDao getInstance(Context context) {
        if (entryDao == null) {
            entryDao = new EntryDao(new ValetDbHelper(context));
        }
        return entryDao;
    }


    public void insertEntry(int id, String jsonEntry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Table.COL_ID_CHECKIN, id);
        cv.put(Table.COL_JSON_ENTRY, jsonEntry);

        db.insert(Table.TABLE_NAME,null,cv);

        db.close();
    }

    public void insertEntryResponse(EntryCheckinResponse response, int flagUpload) {
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(response);
        String platNo = response.getData().getAttribute().getPlatNo();
        String noTrans = response.getData().getAttribute().getIdTransaksi();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(EntryCheckinResponse.Table.COL_RESPONSE_ID, response.getData().getAttribute().getId());
        cv.put(EntryCheckinResponse.Table.COL_JSON_RESPONSE, jsonResponse);
        cv.put(EntryCheckinResponse.Table.COL_PLAT_NO, platNo);
        cv.put(EntryCheckinResponse.Table.COL_NO_TRANS, noTrans);
        cv.put(EntryCheckinResponse.Table.COL_IS_UPLOADED, flagUpload);

        db.insert(EntryCheckinResponse.Table.TABLE_NAME,null,cv);

        db.close();
    }

    public void updateUploadFlag(int id, int flag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String[] args = new String[] {String.valueOf(flag)};
        cv.put(EntryCheckinResponse.Table.COL_IS_UPLOADED, flag);
        db.update(EntryCheckinResponse.Table.TABLE_NAME, cv, EntryCheckinResponse.Table.COL_RESPONSE_ID + "=?", args);
    }

    public List<EntryCheckinResponse> fetchAllCheckinResponse() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Cursor c = db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_IS_CHECKOUT + "=? AND " + EntryCheckinResponse.Table.COL_IS_CALLED + "=0",new String[]{"0"},null,null,EntryCheckinResponse.Table.COL_RESPONSE_ID + " DESC");
        Cursor c = db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_IS_CHECKOUT + "=? AND " + EntryCheckinResponse.Table.COL_IS_READY_CHECKOUT + "=0",new String[]{"0"},null,null,EntryCheckinResponse.Table.COL_ID + " DESC");
        List<EntryCheckinResponse> responseList = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                String jsonResponse = c.getString(c.getColumnIndex(EntryCheckinResponse.Table.COL_JSON_RESPONSE));
                EntryCheckinResponse checkinResponse = gson.fromJson(jsonResponse, EntryCheckinResponse.class);
                responseList.add(checkinResponse);
            }while (c.moveToNext());
        }


        c.close();
        db.close();

        return responseList;
    }

    public Cursor getAllParkedCars() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_IS_CHECKOUT + "=? AND " + EntryCheckinResponse.Table.COL_IS_READY_CHECKOUT + "=0",new String[]{"0"},null,null,EntryCheckinResponse.Table.COL_RESPONSE_ID + " DESC");
    }

    public Cursor getParkedCarsByPlatNo(String platNo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = new String[] {platNo};
        return db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_PLAT_NO + " LIKE '?' AND "
                 + EntryCheckinResponse.Table.COL_IS_CHECKOUT + "=0 AND "
                + EntryCheckinResponse.Table.COL_IS_READY_CHECKOUT + "=0", args,null,null,EntryCheckinResponse.Table.COL_RESPONSE_ID + " DESC");
    }

    public EntryCheckinResponse getEntryByIdResponse(int id) {
        EntryCheckinResponse checkinResponse = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = new String[] {String.valueOf(id)};
        // Cursor c = db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_RESPONSE_ID + "=? AND " + EntryCheckinResponse.Table.COL_IS_CHECKOUT + " = 0",args,null,null,null);
        Cursor c = db.query(EntryCheckinResponse.Table.TABLE_NAME,null, EntryCheckinResponse.Table.COL_RESPONSE_ID + "=?",args,null,null,null);


        if (c.moveToFirst()) {
            do {
                String jsonResponse = c.getString(c.getColumnIndex(EntryCheckinResponse.Table.COL_JSON_RESPONSE));
                checkinResponse = gson.fromJson(jsonResponse, EntryCheckinResponse.class);

            }while (c.moveToNext());
        }

        return checkinResponse;
    }

    public int removeEntryById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = new String[] {String.valueOf(id)};
        return db.delete(EntryCheckinResponse.Table.TABLE_NAME, EntryCheckinResponse.Table.COL_RESPONSE_ID + "=?", args);
    }

    public static class Table {
        public static final String TABLE_NAME = "entry_json";

        public static final String COL_ID = "id";
        public static final String COL_ID_CHECKIN = "id_checkin";
        public static final String COL_JSON_ENTRY = "json_entry";

        public static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COL_ID + " INTEGER PRIMARY KEY, " +
                COL_ID_CHECKIN + " INTEGER, " +
                COL_JSON_ENTRY + " TEXT)";
    }

    public void deleteUncheckedOutEntry() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + EntryCheckinResponse.Table.TABLE_NAME + " WHERE " + EntryCheckinResponse.Table.COL_IS_CHECKOUT + " != 0");
    }

}

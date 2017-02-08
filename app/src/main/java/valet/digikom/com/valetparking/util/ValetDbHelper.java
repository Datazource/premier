package valet.digikom.com.valetparking.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import valet.digikom.com.valetparking.dao.EntryDao;
import valet.digikom.com.valetparking.domain.AdditionalItems;
import valet.digikom.com.valetparking.domain.CarMaster;
import valet.digikom.com.valetparking.domain.Checkin;
import valet.digikom.com.valetparking.domain.ColorMaster;
import valet.digikom.com.valetparking.domain.DefectMaster;
import valet.digikom.com.valetparking.domain.DropPointMaster;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;
import valet.digikom.com.valetparking.domain.EntryCheckoutCont;
import valet.digikom.com.valetparking.domain.FineFee;
import valet.digikom.com.valetparking.domain.ValetTypeJson;

/**
 * Created by DIGIKOM-EX4 on 12/20/2016.
 */

public class ValetDbHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DB_NAME  = "valetdb";
    private static final int DB_VERSION = 1;
    private static ValetDbHelper dbHelper;


    public ValetDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    public static ValetDbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new ValetDbHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Checkin.Table.CREATE_TABLE);
        db.execSQL(DefectMaster.Table.CREATE);
        db.execSQL(AdditionalItems.Table.CREATE);
        db.execSQL(CarMaster.Table.CREATE);
        db.execSQL(ColorMaster.Table.CREATE);
        db.execSQL(DropPointMaster.Table.CREATE);
        db.execSQL(EntryDao.Table.CREATE);
        db.execSQL(EntryCheckinResponse.Table.CREATE);
        db.execSQL(EntryCheckoutCont.Table.CREATE);
        db.execSQL(FineFee.Table.CREATE);
        db.execSQL(ValetTypeJson.Table.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Checkin.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DefectMaster.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AdditionalItems.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CarMaster.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ColorMaster.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DropPointMaster.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EntryDao.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EntryCheckinResponse.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EntryCheckoutCont.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FineFee.Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ValetTypeJson.Table.TABLE_NAME);

        onCreate(db);
    }

    public Context getContext() {
        return this.context;
    }
}

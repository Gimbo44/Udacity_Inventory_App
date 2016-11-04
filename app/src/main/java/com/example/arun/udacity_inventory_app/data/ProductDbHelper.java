package com.example.arun.udacity_inventory_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.arun.udacity_inventory_app.data.ProductContract.ProductEntry;

/**
 * Created by arun on 1/11/16.
 * This class has been created to be responsible for the creation/updating of the sqlite3 database
 * on the android device.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    /** class log tag **/
    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    /** database name **/
    private static final String DATABASE_NAME = "products.db";

    /** database version number **/
    private static final int DATABASE_VERSION = 1;


    /**
     * Constructs a new instance of {@link ProductDbHelper}
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // The sql creation script to produce the habit sqlite database
        String SQL_CREATE_HABIT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_IMG + " BLOB, "
                + ProductEntry.COlUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, "
                + ProductEntry.COlUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0.00, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        // Create the database
        db.execSQL(SQL_CREATE_HABIT_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME + ";");

        // Create tables again
        onCreate(db);
    }
}

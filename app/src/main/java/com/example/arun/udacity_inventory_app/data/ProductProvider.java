package com.example.arun.udacity_inventory_app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.arun.udacity_inventory_app.data.ProductContract.ProductEntry;

import static android.R.attr.id;


/**
 * Created by arun on 1/11/16.
 */

public class ProductProvider extends ContentProvider{

    /** Tag for the log messages */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();


    private ProductDbHelper mProductDbHelper;

    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PRODUCT_ID = 101;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        // TODO: Add 2 content URIs to URI matcher
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }


    @Override
    public boolean onCreate() {
        mProductDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mProductDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch(sUriMatcher.match(uri)){
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public Uri insertProduct(Uri uri, ContentValues values){

        SQLiteDatabase db = mProductDbHelper.getWritableDatabase();

        if(!values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)){
            throw new IllegalArgumentException("Product requires a name");
        }
        if(!values.containsKey(ProductEntry.COlUMN_PRODUCT_SUPPLIER)){
            throw new IllegalArgumentException("Product requires a supplier");
        }

        long newID = db.insert(ProductEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newID == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // uri: content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, newID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if(values.size() == 0){
            return 0;
        }

        if(values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)){
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        /** todo implement image check **/
        if(values.containsKey(ProductEntry.COlUMN_PRODUCT_SUPPLIER)){
            String supplier = values.getAsString(ProductEntry.COlUMN_PRODUCT_SUPPLIER);
            if(supplier == null){
                throw new IllegalArgumentException("Product requires a supplier");
            }
        }
        if(values.containsKey(ProductEntry.COlUMN_PRODUCT_PRICE)){
            Float price = values.getAsFloat(ProductEntry.COlUMN_PRODUCT_PRICE);
            if(price == null || price < 0){
                throw new IllegalArgumentException("Product requires an appropriate price value");
            }
        }
        if(values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)){
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if(quantity == null || quantity < 0){
                throw new IllegalArgumentException("Quantity must be a valid integer value");
            }
        }

        SQLiteDatabase db = mProductDbHelper.getWritableDatabase();

        long rowsUpdated = db.update(ProductEntry.TABLE_NAME,values,selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return (int) rowsUpdated;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
}

package com.example.arun.udacity_inventory_app.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by arun on 1/11/16.
 */

public class ProductContract {

    private ProductContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.arun.udacity_inventory_app";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /** Name of the products database **/
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Image/photo of the product.
         * Type: BLOB
         */
        public final static String COLUMN_PRODUCT_IMG = "image";

        /**
         * Name of the product supplier.
         * Type: TEXT
         */
        public final static String COlUMN_PRODUCT_SUPPLIER = "supplier";

        /**
         * Price of the product.
         * Type: REAL
         */
        public final static String COlUMN_PROUDCT_PRICE = "price";

        /**
         * The amount of the product available in stock
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
    }
}

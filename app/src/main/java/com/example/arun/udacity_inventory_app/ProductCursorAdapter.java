package com.example.arun.udacity_inventory_app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.arun.udacity_inventory_app.data.ProductContract.ProductEntry;


/**
 * Created by arun on 1/11/16.
 * handles the listview used to store the product data
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_list_item_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColIndex = cursor.getColumnIndex(ProductEntry.COlUMN_PRODUCT_PRICE);
        int quantityColIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        if(nameColIndex != -1 && priceColIndex != -1 && quantityColIndex != -1){

            TextView nameTextView = (TextView) view.findViewById(R.id.ProductNameTextView);
            TextView quantityTextView = (TextView) view.findViewById(R.id.QuantityTextView);
            TextView priceTextView = (TextView) view.findViewById(R.id.PriceTextView);

            nameTextView.setText(cursor.getString(nameColIndex));
            quantityTextView.setText(String.valueOf(cursor.getInt(quantityColIndex)));
            priceTextView.setText(String.format(context.getString(R.string.priceFormat), cursor.getFloat(priceColIndex)));
        }
    }
}

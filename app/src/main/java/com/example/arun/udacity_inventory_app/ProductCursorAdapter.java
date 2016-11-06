package com.example.arun.udacity_inventory_app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arun.udacity_inventory_app.data.ProductContract.ProductEntry;

import static android.R.attr.id;
import static com.example.arun.udacity_inventory_app.R.string.quantity;

/**
 * Created by arun on 1/11/16.
 * handles the listview used to store the menu_product data
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
    public void bindView(View view, final Context context, final Cursor cursor) {

        final int idColIndex = cursor.getColumnIndex(ProductEntry._ID);
        final int quantityColIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        final long id = cursor.getLong(idColIndex);
        final int quantity = cursor.getInt(quantityColIndex);
        int nameColIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColIndex = cursor.getColumnIndex(ProductEntry.COlUMN_PRODUCT_PRICE);

        if(nameColIndex != -1 && priceColIndex != -1 && quantityColIndex != -1){

            TextView nameTextView = (TextView) view.findViewById(R.id.ProductNameTextView);
            final TextView quantityTextView = (TextView) view.findViewById(R.id.QuantityTextView);
            TextView priceTextView = (TextView) view.findViewById(R.id.PriceTextView);
            final Button saleButton = (Button) view.findViewById(R.id.ListSaleButton);
            saleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(quantity -1 >= 0){
                        Toast.makeText(context, "Sale!", Toast.LENGTH_SHORT).show();
                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY,quantity - 1);
                        Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                        context.getContentResolver().update(
                                uri,
                                values,
                                ProductEntry._ID+"=?",
                                new String[]{String.valueOf(id)}
                                );
                    }
                }
            });

            if(quantity <= 0){
                saleButton.setEnabled(false);
                saleButton.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.saleDeactivated));
            }else{
                saleButton.setEnabled(true);
                saleButton.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.colorAccent));
            }
            saleButton.setFocusable(false);
            nameTextView.setText(cursor.getString(nameColIndex));
            quantityTextView.setText(String.valueOf(cursor.getInt(quantityColIndex)));
            priceTextView.setText(String.format(context.getString(R.string.priceFormat), cursor.getFloat(priceColIndex)));
        }
    }
}

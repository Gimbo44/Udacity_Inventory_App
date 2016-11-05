package com.example.arun.udacity_inventory_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arun.udacity_inventory_app.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.arun.udacity_inventory_app.R.string.quantity;
import static java.lang.Integer.parseInt;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductActivity.class.getSimpleName();

    private boolean mHasSetImage = false;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private int mQuantity;

    private static final int PICK_IMAGE_REQUEST = 0;

    // Identifies a particular Loader being used in this component
    private static final int PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private ImageView mProductImageView;
    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private TextView mQuantityTextView;

    private Button mSaleButton;
    private ImageButton mIncrementButton;
    private ImageButton mDecrementButton;
    private Button mOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Initializing all the view variables associated to both product activity "modes".
        mNameEditText = (EditText) findViewById(R.id.ProductNameEditText);
        mSupplierEditText = (EditText) findViewById(R.id.SupplierEditView);
        mPriceEditText = (EditText) findViewById(R.id.PriceEditText);
        mQuantityEditText = (EditText) findViewById(R.id.QuantityEditText);
        mProductImageView = (ImageView) findViewById(R.id.ProductImageView);

        // Set onTouchListeners for all editviews/imageviews
        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);

        // If data is passed by intent, then display the current product in questions information\
        // Also known as "Edit mode"
        if(getIntent().getData() != null){

            // Defining all the variables required for just the editing process
            mQuantityTextView = (TextView) findViewById(R.id.QuantityTextView);
            mSaleButton = (Button) findViewById(R.id.SaleButton);
            mIncrementButton = (ImageButton) findViewById(R.id.IncrementButton);
            mDecrementButton = (ImageButton) findViewById(R.id.DecrementButton);
            mOrderButton = (Button) findViewById(R.id.OrderButton);

            // Adding additional onTouchListeners to the newly tracked variables:
            mSaleButton.setOnTouchListener(mTouchListener);
            mIncrementButton.setOnTouchListener(mTouchListener);
            mDecrementButton.setOnTouchListener(mTouchListener);

            // Since we are in editmode, can hide the quantity editText and replace it with the
            // quantity controller view layout.
            mQuantityEditText.setVisibility(View.GONE);
            findViewById(R.id.QantityLayout).setVisibility(View.VISIBLE);

            mCurrentProductUri = getIntent().getData();
            setTitle(getString(R.string.edit_product));

            // Handle the various quantity alteration options
            mSaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(-1);
                }
            });
            mIncrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(1);
                }
            });
            mDecrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(-1);
                }
            });

            // Handle the order button request
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEmail(mSupplierEditText.getText().toString());
                }
            });

            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        }else{
            mQuantityEditText.setOnTouchListener(mTouchListener);
            setTitle(getString(R.string.add_product));
            findViewById(R.id.productQuantityOptions).setVisibility(View.INVISIBLE);
            findViewById(R.id.QantityLayout).setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        }

        mProductImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });



    }

    /**
     * A simple method which takes a string email parameter and initiates an intent to open
     * an email application on the users phone.
     * @param supplierEmail the suppliers email
     */
    private void sendEmail(String supplierEmail){

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + supplierEmail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Product Order");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "I would like to order some new stock.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ProductActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Method designed to handle quantity value alterations which could occur inside of the
     * "edit" mode.
     * @param increment, either a positive or negative number.
     */
    private void alterQuantity(int increment){
        if(mQuantity + increment == 0){
            mSaleButton.setBackgroundColor(
                    ContextCompat.getColor(getBaseContext(), R.color.saleDeactivated));
            mSaleButton.setEnabled(false);
            mDecrementButton.setEnabled(false);
            mDecrementButton.setVisibility(View.INVISIBLE);
        }else{
            mSaleButton.setBackgroundColor(
                    ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
            mSaleButton.setEnabled(true);
            mDecrementButton.setEnabled(true);
            mDecrementButton.setVisibility(View.VISIBLE);
        }
        if(quantity + increment >= 0){
            mQuantity += increment;
            mQuantityTextView.setText(String.valueOf(mQuantity));
        }



    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if(mCurrentProductUri == null){
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(true);
        }else{
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_save_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(validateProductInfo()){
                    saveProduct();
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();

                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ProductActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(String str){
        try{
            parseInt(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Handles verification of user entered information
     * @return boolean representing overall valid value.
     */
    private boolean validateProductInfo(){
        String productName = mNameEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();

        /** Stage one of testing, need to make sure all the required fields are not empty */
        if(productName.isEmpty()){
            mNameEditText.setError(getString(R.string.invalid_product_name));
            return false;
        }
        if(supplier.isEmpty()){
            mSupplierEditText.setError(getString(R.string.invalid_supplier));
            return false;
        }


        return true;
    }


    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct(){
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, mNameEditText.getText().toString());

        if(!mHasSetImage){
            values.putNull(ProductEntry.COLUMN_PRODUCT_IMG);
        }else{
            Bitmap bitmap = ((BitmapDrawable)mProductImageView.getDrawable()).getBitmap();
            values.put(ProductEntry.COLUMN_PRODUCT_IMG, getBitmapAsByteArray(bitmap));
        }

        values.put(ProductEntry.COlUMN_PRODUCT_SUPPLIER, mSupplierEditText.getText().toString());

        boolean success = false;

        String priceStr = mPriceEditText.getText().toString();
        if(!priceStr.isEmpty() && isDouble(priceStr)){
            values.put(ProductEntry.COlUMN_PRODUCT_PRICE, Double.parseDouble(priceStr));
        }
        if(mCurrentProductUri != null){
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        }else{
            String quantityStr = mQuantityEditText.getText().toString();
            if(!quantityStr.isEmpty() && isInteger(quantityStr)){
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, parseInt(quantityStr));
            }
        }

        if(mCurrentProductUri != null){
            int numUpdated = getContentResolver().update(mCurrentProductUri, values, null,null);
            // Show a toast message depending on whether or not the insertion was successful
            if (numUpdated != -1) {
                // If the row ID is -1, then there was an error with insertion.
                success = true;
            }
        }else{
            // Insert a new row for pet in the database, returning the ID of that new row.
            Uri newRowUri = getContentResolver().insert(ProductEntry.CONTENT_URI,values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newRowUri != null) {
                success = true;
            }
        }

        if (!success) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, R.string.operation_error, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, R.string.operation_successful, Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteProduct(){
        // Only proceed if the current activity is in "edit" mode.
        if(mCurrentProductUri != null){
            int deleteCount = getContentResolver().delete(mCurrentProductUri, null, null);

            if(deleteCount == 0){
                Toast.makeText(this, "Error deleting product", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Product deleted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Take a byte array and decode the stream into a bitmap
     * @param imgByte
     * @return
     */
    public Bitmap getImage(byte[] imgByte){
        ByteArrayInputStream imageStream = new ByteArrayInputStream(imgByte);
        return BitmapFactory.decodeStream(imageStream);
    }


    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }



    public void openImageSelector() {
        Intent intent;
        intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Method handles the activity result from invoking an ACTION_PICK intent
     * Main body of the method was copied from the udacity's mentors code found here:
     *      https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param requestCode , the intent request code
     * @param resultCode , status of the result
     * @param resultData , intent containing the returned data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + uri.toString());

                int padding = 5;
                mProductImageView.setPadding(padding, padding, padding, padding);
                mProductImageView.setImageBitmap(getBitmapFromUri(uri));
                mHasSetImage = true;
            }
        }
    }

    /**
     * Function designed to extract a bitmap from the uri returned from an image gallery activity.
     * Function extracted from udacity mentors example for image selection:
     *      https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param uri returned from the intent activity
     * @return bitmap for the image passed back
     */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImageView.getWidth();
        int targetH = mProductImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
//            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PRODUCT_LOADER:
                String[] projection = {
                        ProductEntry._ID,
                        ProductEntry.COLUMN_PRODUCT_NAME,
                        ProductEntry.COlUMN_PRODUCT_PRICE,
                        ProductEntry.COLUMN_PRODUCT_QUANTITY,
                        ProductEntry.COlUMN_PRODUCT_SUPPLIER,
                        ProductEntry.COLUMN_PRODUCT_IMG};

                // Returns a new CursorLoader
                return new CursorLoader(
                        this,                       // Parent activity context
                        mCurrentProductUri,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int nameColIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int imgColIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMG);
            int priceColIndex = data.getColumnIndex(ProductEntry.COlUMN_PRODUCT_PRICE);
            int supplierColIndex = data.getColumnIndex(ProductEntry.COlUMN_PRODUCT_SUPPLIER);
            int quantityColIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);


            if(nameColIndex != -1 && imgColIndex != -1 && priceColIndex != -1
                    && supplierColIndex != -1 && quantityColIndex != -1){

                mNameEditText.setText(data.getString(nameColIndex));
                float price = Float.parseFloat(data.getString(priceColIndex));
                mPriceEditText.setText(String.format(getString(R.string.priceFormat), price));
                mSupplierEditText.setText(data.getString(supplierColIndex));

                mQuantity = data.getInt(quantityColIndex);
                if(mQuantity == 0){
                    mSaleButton.setBackgroundColor(
                            ContextCompat.getColor(getBaseContext(), R.color.saleDeactivated));
                }

                mQuantityTextView.setText(String.valueOf(mQuantity));


                byte[] image = data.getBlob(imgColIndex);
                if(image != null){
                    mProductImageView.setImageBitmap(getImage(image));
                    int padding = 5;
                    mProductImageView.setPadding(padding, padding, padding, padding);
                }
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mQuantityEditText.setText("");
        mProductImageView.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
        int padding = 20;
        mProductImageView.setPadding(padding, padding, padding, padding);
    }
}

package com.zebra.ssmdatapersist;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SSMDataProcessor extends AppCompatActivity {

    Uri cpUri;
    ContentProviderClient cpClient;
    private final String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";
    static final String TAG = "SSMDataProcessor";

    private final String COLUMN_ORIG_APP_PACKAGE = "orig_app_package";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_INPUT_FORM = "data_input_form";
    private final String COLUMN_DATA_OUTPUT_FORM = "data_output_form";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_MULTI_INSTANCE_REQUIRED = "multi_instance_required";

    LocalContentObserver myContentObserver;
    LocalDataSetObserver myDataSetObserver;
    Spinner persisFlagSpinner;
    Context mContext = null;

    private final String signature = "MIIC5DCCAcwCAQEwDQYJKoZIhvcNAQEFBQAwNzEWMBQGA1UEAwwNQW5kcm9pZCBEZWJ1ZzEQMA4GA1UECgwHQW5kcm9pZDELMAkGA1UEBhMCVVMwIBcNMjAxMjA4MDYzNjU0WhgPMjA1MDEyMDEwNjM2NTRaMDcxFjAUBgNVBAMMDUFuZHJvaWQgRGVidWcxEDAOBgNVBAoMB0FuZHJvaWQxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsit7mll4gtsFMSHC1YAVZcev0dmZe8UKnFLALEobJg7mBeRYXQ900cQflBq0H2vwU7Vsm2uh+uEiGgf/MJUt9gGPMhrmKhlabtH+x0tUYWoZX68V0mfbaH0PDsBk1i1cBciqPG/ihYOavpQvFHxrLX/JvsPp9kAKJ625p9dcVrFNgO6wp/nwTcLzY4RtifrtlhxON9JP3fISVNEgxdi51RCieLCPHSCbDUtStAvNJ3sClS4ohmxn2mEU21IcoQv63LlMFpouCZKv+2lMp3LkPMW1Xw1X/I27VUzKpUmagp8Qounfc+tWa7mrcS/NPHsvI9kCRMox+2c6Xuy3sqza1wIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQCW+oBfzA11Io6Q8mnqkfYW/tfRCKf1kjoeOx8TadJQ3HvGMgCAd0ucVsQlhjWQqfPgACGsIWFlp9sU7zl7vpr39+5goMf19WaQrBS8aNEWJPRcn/UsPSHQtbeECObBwL6KqpdNzA3iXP/KbrQuNhI6wZaJ9k4sBX0G3G2qF7pEwoISukwHEp7dy7scTsI2CgQdC9ZFveposX6xDF2VsrU5anjKbCsFhBGWeTJuc/32RDwhX1PGBedwyylUUXVpVUTBDwSRIznPYBhRL1l/DtfNt9XvaV6NFJsl13wp2hicLdrXkMbyV7NlB41M4zbyBbYjDfpgYds1RpmYBcuSYiBB";
    TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         mContext = this.getApplicationContext();
         resultView = findViewById(R.id.result);

        initializePersistFlagSpinner();
        cpUri = Uri.parse(AUTHORITY);
        cpClient = getContentResolver().acquireContentProviderClient(cpUri);

        myContentObserver = new LocalContentObserver(null);
        myDataSetObserver = new LocalDataSetObserver();
    }

    private void initializePersistFlagSpinner() {
        persisFlagSpinner = (Spinner)findViewById(R.id.persistFlagSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.persist_flag, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        persisFlagSpinner.setAdapter(adapter);
    }


    public void onClickInsertData(View view) {
        insertData();
    }

    public void insertData() {
        try {
            insertUserNameAndPassword();
        } catch (Exception e) {
            Log.d(TAG, "Insert - error: " + e.getMessage());
        }
    }

    private void insertUserNameAndPassword() {
        String username = ((EditText)findViewById(R.id.et_insertUserName)).getText().toString();
        String password = ((EditText)findViewById(R.id.et_insertPassword)).getText().toString();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TARGET_APP_PACKAGE, "{\"pkgs_sigs\":[{\"pkg\":\""+ mContext.getPackageName() +"\",\"sig\":\"" + signature + "\"}]}");
        values.put(COLUMN_DATA_NAME, "username and password");
        values.put(COLUMN_DATA_VALUE, "[{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}]");
        values.put(COLUMN_DATA_TYPE,"1");
        values.put(COLUMN_DATA_INPUT_FORM, "1");
        values.put(COLUMN_DATA_OUTPUT_FORM, "1");
        values.put(COLUMN_DATA_PERSIST_REQUIRED, persisFlagSpinner.getSelectedItem().toString());
        values.put(COLUMN_MULTI_INSTANCE_REQUIRED, "false");

        Uri createdRow = getContentResolver().insert(cpUri, values);
        Log.d(TAG, "Row Created : " + createdRow.toString());
        Toast.makeText(mContext, "New Record Inserted", Toast.LENGTH_SHORT).show();
        resultView.setText("Query Result");
    }

    @SuppressLint("Range")
    public void onClickQueryData(View view) {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'" +
                " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder strBuild = new StringBuilder();
                while (!cursor.isAfterLast()) {
                    strBuild.append("\n" + "COLUMN_ORIG_APP_PACKAGE :" + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_ORIG_APP_PACKAGE)) + " \n " +
                            "COLUMN_TARGET_APP_PACKAGE :" + "\n"  + cursor.getString(cursor.getColumnIndex(COLUMN_TARGET_APP_PACKAGE)) + " \n " +
                            "COLUMN_DATA_NAME: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NAME)) + " \n " +
                            "COLUMN_DATA_VALUE: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_VALUE)) + " \n " +
                            "COLUMN_DATA_TYPE: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE)) + " \n " +
                            "COLUMN_DATA_INPUT_FORM: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_INPUT_FORM)) + " \n " +
                            "COLUMN_DATA_OUTPUT_FORM: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_OUTPUT_FORM)) + " \n " +
                            "COLUMN_DATA_PERSIST_REQUIRED: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_PERSIST_REQUIRED)) + " \n " +
                            "COLUMN_MULTI_INSTANCE_REQUIRED: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_MULTI_INSTANCE_REQUIRED)));

                    String targetData = cursor.getString(cursor.getColumnIndex(COLUMN_TARGET_APP_PACKAGE));
                    Log.d(TAG, "Target Data Received: " + targetData);
                    strBuild.append("\n ----------------------").append("\n");
                    cursor.moveToNext();
                }
                Log.d(TAG, "Query data: " + strBuild);
                resultView.setText(strBuild);
            } else {
                resultView.setText("No Records Found");
            }
        } catch (Exception e) {
            Log.d(TAG, "Query data error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onClickUpdateData(View view) {
        String username = ((EditText) findViewById(R.id.et_insertUserName)).getText().toString();
        String password = ((EditText) findViewById(R.id.et_insertPassword)).getText().toString();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TARGET_APP_PACKAGE, "{\"pkgs_sigs\":[{\"pkg\":\"" + mContext.getPackageName() + "\",\"sig\":\"" + signature + "\"}]}");
            values.put(COLUMN_DATA_NAME, "username and password");
            values.put(COLUMN_DATA_VALUE, "[{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}]");
            values.put(COLUMN_DATA_PERSIST_REQUIRED, persisFlagSpinner.getSelectedItem().toString());
            int rowNumbers = getContentResolver().update(cpUri, values, null, null);
            Log.d(TAG, "Records updated: " + rowNumbers);
            if (rowNumbers > 0) {
                Toast.makeText(mContext, "User info updated", Toast.LENGTH_SHORT).show();
                resultView.setText("Query Result");
            } else {
                Toast.makeText(mContext, "No records to update", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Update - error: " + e.getMessage());
            Toast.makeText(mContext, "Error updating User info " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    public void onClickDeleteData(View view) {
        try {
            Uri cpUriDelete = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");

            String whereClause = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'";
            int i = getContentResolver().delete(cpUriDelete, whereClause, null);
            if (i > 0) {
                Toast.makeText(mContext, "User Info Deleted", Toast.LENGTH_SHORT).show();
                resultView.setText("No Records Found");
            } else {
                Toast.makeText(mContext, "No User Info to be deleted", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Delete - error: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(cpUri, true, myContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(myContentObserver);
    }
}


class LocalContentObserver extends ContentObserver {
    public LocalContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
        Log.d(SSMDataProcessor.TAG, "### received self change notification from uri: ");
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(SSMDataProcessor.TAG, "### received notification from uri: " + uri.toString());
    }
}

class LocalDataSetObserver extends DataSetObserver {
    public LocalDataSetObserver() {

    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        Log.d(SSMDataProcessor.TAG, "onInvalidate");
    }

    @Override
    public void onChanged() {
        super.onChanged();
        Log.d(SSMDataProcessor.TAG, "onChanged");
    }
}

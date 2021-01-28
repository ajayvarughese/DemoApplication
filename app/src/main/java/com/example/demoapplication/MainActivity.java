package com.example.demoapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FILENAME = MainActivity.class.getSimpleName();

    public static final String PREFERENCES = "PREFERENCES_FILE";
    public static final String PREFERENCE_KEY = "PREFERENCE_KEY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        writeToFile("Sample text", this);
        readFromFile(this);

        storeInPref(2);
        readPref();
    }

    private void writeToFile(String data, Context context) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(
                    context.openFileOutput(FILENAME, Context.MODE_PRIVATE
                    )
                )
            );

            bufferedWriter.write(data);

        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(FILENAME);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                Log.d(TAG, "Read file data is : " + ret);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }

    private void storeInPref(int data) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        preferences.edit().putInt(PREFERENCE_KEY, data).apply();
    }

    private void readPref() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        int data = preferences.getInt(PREFERENCE_KEY, 0);
        Log.d(TAG, "Read pref data is : " + data);
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.loadButton:
                Toast.makeText(this, getString(R.string.loading_data), Toast.LENGTH_LONG).show();
                loadData();
                break;
            case R.id.deleteButton:
                Toast.makeText(this, getString(R.string.deleting_data), Toast.LENGTH_LONG).show();
                deleteData();
                break;
            case R.id.insert:
                Toast.makeText(this, getString(R.string.inserting_data), Toast.LENGTH_LONG).show();
                insertUpdateData();
                break;
            case R.id.next:
                startActivity(new Intent(this, AsyncTaskActivity.class));
                break;
        }
    }

    private void loadData() {
        TextView tv = findViewById(R.id.textView);
        tv.setText("");
        // content URI
        Cursor cursor = getContentResolver().query(SampleContentProvider.CONTENT_URI,
                                                   null,
                                                   null,
                                                   null,
                                                   null);

        // iteration of the cursor
        // to print whole table
        StringBuilder sb = new StringBuilder();
        try {

            while (cursor.moveToNext()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(
                    cursor.getString(cursor.getColumnIndex(SampleContentProvider.DatabaseHelper.COLUMN_ID)) +
                        "-" +
                        cursor.getString(cursor.getColumnIndex(SampleContentProvider.DatabaseHelper.COLUMN_NAME)));
            }
        } finally {
            cursor.close();
        }
        if (sb.length() > 0) {
            tv.setText(sb.toString());
        } else {
            tv.setText(R.string.no_records);
        }
    }

    private void insertUpdateData() {
        // class to add values in the database
        ContentValues values = new ContentValues();

        // fetch text from user
        String value = ((EditText) findViewById(R.id.input)).getText().toString();
        values.put(SampleContentProvider.DatabaseHelper.COLUMN_NAME, value);

        // inserting into database through content URI
        getContentResolver().insert(SampleContentProvider.CONTENT_URI, values);
    }

    private void deleteData() {
        // fetch text from user
        String value = ((EditText) findViewById(R.id.input)).getText().toString();
        // inserting into database through content URI
        getContentResolver().delete(SampleContentProvider.CONTENT_URI, SampleContentProvider.DatabaseHelper.COLUMN_NAME + " = ? ", new String[]{
            value});
    }

}

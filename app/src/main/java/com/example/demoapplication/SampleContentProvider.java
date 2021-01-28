package com.example.demoapplication;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class SampleContentProvider extends ContentProvider {

    // defining authority so that other application can access it

    static final String PROVIDER_NAME = "com.example.demoapplication.provider";

    // defining content URI
    public static final String URL = "content://" + PROVIDER_NAME + "/users";

    // parsing the content URI
    public static final Uri CONTENT_URI = Uri.parse(URL);

    static final UriMatcher uriMatcher;

    // creating object of database
    // to perform query
    private SQLiteDatabase db;

    static {

        // to match the content URI
        // every time user access table under content provider
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


        // to access whole table
        uriMatcher.addURI(PROVIDER_NAME, "users", 1);

        // to access a particular row
        // of the table
        uriMatcher.addURI(PROVIDER_NAME, "users/#", 2);

    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/user";
            case 2:
                return "vnd.android.cursor.item/user";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // creating the database
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseHelper.TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case 1:
                break;
            case 2:
                qb.appendWhere(DatabaseHelper.COLUMN_ID + "="
                                   + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor c = qb.query(db,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    // adding data to the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(DatabaseHelper.TABLE_NAME, "", values);
        if (rowID > 0) {
            return ContentUris.withAppendedId(CONTENT_URI, rowID);
        }
        throw new SQLiteException("Record insertion failure" + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = db.update(DatabaseHelper.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = db.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    // creating a database
    public static class DatabaseHelper extends SQLiteOpenHelper {


        // name of the database
        static final String DATABASE_NAME = "Database";

        // table name
        static final String TABLE_NAME = "UsersTable";

        // database version
        static final int DATABASE_VERSION = 1;

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_NAME = "name";

        // sql query to create the table
        static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL);";

        // defining a constructor
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // creating a table in the database
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // sql query to drop a table
            // having similar name
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }


        // example of raw query
        public String getData() {
            String[] params = new String[]{"ajay"};
            Cursor c = getReadableDatabase().rawQuery("SELECT * FROM SMS_TABLE_RCV WHERE name = ?",
                                                      params);
            StringBuilder sb = new StringBuilder();
            if (c.moveToFirst()) {
                if (sb.length() > 0) {
                    sb.append("_");
                }
                sb.append(c.getString(c.getColumnIndex("name")));
            }
            return sb.toString();
        }
    }
}

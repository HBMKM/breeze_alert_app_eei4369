package com.s23010175.breezealert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BreezeAlertDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "breeze_alert_db";
    private static final int DB_VERSION = 2;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Alias for backward compatibility
    public static final String TABLE_NAME = TABLE_USERS;

    // Alerts table
    public static final String TABLE_ALERTS = "alerts";
    public static final String COLUMN_SENSOR = "sensor";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_SUGGESTION = "suggestion";
    public static final String COLUMN_BUTTON = "button";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public BreezeAlertDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Create Alerts table
        String createAlertsTable = "CREATE TABLE " + TABLE_ALERTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENSOR + " TEXT, " +
                COLUMN_VALUE + " TEXT, " +
                COLUMN_SUGGESTION + " TEXT, " +
                COLUMN_BUTTON + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT)";
        db.execSQL(createAlertsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        onCreate(db);
    }
}


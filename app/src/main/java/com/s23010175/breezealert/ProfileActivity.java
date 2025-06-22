package com.s23010175.breezealert;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ImageView profilePicture;
    private EditText usernameInput, emailInput, passwordInput;
    private Button saveButton, logoutButton;

    private BreezeAlertDBHelper dbHelper;
    private SQLiteDatabase db;
    private int USER_ID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        USER_ID = prefs.getInt("userId", -1);

        backButton = findViewById(R.id.backButton);
        profilePicture = findViewById(R.id.profilePicture);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);

        dbHelper = new BreezeAlertDBHelper(this);
        db = dbHelper.getWritableDatabase();

        loadUserData();

        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateUserData();
            }
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private void loadUserData() {
        Cursor cursor = db.query(BreezeAlertDBHelper.TABLE_USERS,
                new String[]{BreezeAlertDBHelper.COLUMN_USERNAME, BreezeAlertDBHelper.COLUMN_EMAIL, BreezeAlertDBHelper.COLUMN_PASSWORD},
                BreezeAlertDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(USER_ID)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            usernameInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_USERNAME)));
            emailInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_EMAIL)));
            passwordInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_PASSWORD)));
            cursor.close();
        } else {
            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        if (usernameInput.getText().toString().trim().isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (passwordInput.getText().toString().trim().isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        return true;
    }

    private void updateUserData() {
        ContentValues values = new ContentValues();
        values.put(BreezeAlertDBHelper.COLUMN_USERNAME, usernameInput.getText().toString().trim());
        values.put(BreezeAlertDBHelper.COLUMN_EMAIL, emailInput.getText().toString().trim());
        values.put(BreezeAlertDBHelper.COLUMN_PASSWORD, passwordInput.getText().toString().trim());

        int rows = db.update(BreezeAlertDBHelper.TABLE_USERS, values,
                BreezeAlertDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(USER_ID)});

        if (rows > 0) {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        dbHelper.close();
        super.onDestroy();
    }
}

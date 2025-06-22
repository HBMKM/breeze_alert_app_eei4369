package com.s23010175.breezealert;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button clearHistoryButton;
    private ListView historyListView;
    private BreezeAlertDBHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> alertList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        backButton = findViewById(R.id.backButton);
        clearHistoryButton = findViewById(R.id.clearHistoryButton);
        historyListView = findViewById(R.id.historyListView);

        dbHelper = new BreezeAlertDBHelper(this);
        alertList = new ArrayList<>();

        loadAlertHistory(); // Load and display alerts

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alertList);
        historyListView.setAdapter(adapter);

        // Back to Home
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Clear history
        clearHistoryButton.setOnClickListener(v -> {
            clearAlertHistory();
            loadAlertHistory();
            adapter.notifyDataSetChanged();
        });
    }

    private void loadAlertHistory() {
        alertList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                BreezeAlertDBHelper.TABLE_ALERTS,
                null, null, null, null, null,
                BreezeAlertDBHelper.COLUMN_TIMESTAMP + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_TIMESTAMP));
                String sensor = cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_SENSOR));
                String value = cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_VALUE));
                String suggestion = cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_SUGGESTION));
                String button = cursor.getString(cursor.getColumnIndexOrThrow(BreezeAlertDBHelper.COLUMN_BUTTON));

                String alertEntry = "📅 " + timestamp +
                        "\nSensor: " + sensor +
                        "\nValue: " + value +
                        "\nSuggestion: " + suggestion +
                        "\nAction: " + button;

                alertList.add(alertEntry);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            alertList.add("No alert history found.");
        }

        db.close();
    }

    private void clearAlertHistory() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(BreezeAlertDBHelper.TABLE_ALERTS, null, null);
        db.close();
        Toast.makeText(this, "History cleared (" + deletedRows + " record(s))", Toast.LENGTH_SHORT).show();
    }
}

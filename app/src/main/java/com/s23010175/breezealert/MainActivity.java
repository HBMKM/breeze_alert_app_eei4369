package com.s23010175.breezealert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Splash screen layout

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
            boolean isSignedIn = prefs.getBoolean("isSignedIn", false);

            Intent intent = isSignedIn ?
                    new Intent(MainActivity.this, HomeActivity.class) :
                    new Intent(MainActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}

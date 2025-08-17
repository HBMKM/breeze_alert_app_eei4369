package com.s23010175.breezealert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private Sensor humiditySensor;
    private Sensor lightSensor;
    private Sensor accelerometerSensor;

    private TextView humidityText, brightnessText, motionText, locationText, safetyNoteText;
    private ImageView profileIcon, settingsIcon;

    private float[] accelValues = new float[3];
    private boolean accelReady = false;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "BreezeAlertSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        humidityText = findViewById(R.id.humidityText);
        brightnessText = findViewById(R.id.brightnessText);
        motionText = findViewById(R.id.motionText);
        locationText = findViewById(R.id.locationText);
        safetyNoteText = findViewById(R.id.safetyNoteText);

        profileIcon = findViewById(R.id.profileIcon);
        settingsIcon = findViewById(R.id.settingsIcon);

        Button alertButton = findViewById(R.id.alertButton);
        Button locationButton = findViewById(R.id.locationButton);
        Button tipsButton = findViewById(R.id.tipsButton);
        Button historyButton = findViewById(R.id.historyButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //locationText.setText("📍 Current Location : Coastal Risk Zone");
        //safetyNoteText.setText("Move indoors for safety");

        alertButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AlertActivity.class)));
        locationButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LocationActivity.class)));
        tipsButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, TipsAndGuidesActivity.class)));
        historyButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));

        profileIcon.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        settingsIcon.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Update HomeActivity location status based on LocationActivity
        boolean inRiskZone = prefs.getBoolean("isCoastal", false);

        if (inRiskZone) {
            locationText.setText("📍 Current Location : Coastal Risk Zone");
            safetyNoteText.setText("Move indoors for safety");
        } else {
            locationText.setText("📍 Current Location : Safe Zone");
            safetyNoteText.setText("Your device is safe");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Read user preferences for sensors
        boolean humidityEnabled = prefs.getBoolean("humidity", true);
        boolean brightnessEnabled = prefs.getBoolean("brightness", true);
        boolean motionEnabled = prefs.getBoolean("motion", true);

        if (humiditySensor != null && humidityEnabled) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            humidityText.setText("Humidity sensor disabled");
        }

        if (lightSensor != null && brightnessEnabled) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            brightnessText.setText("Light sensor disabled");
        }

        if (accelerometerSensor != null && motionEnabled) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            motionText.setText("Motion sensor disabled");
        }

        // Update live location status dynamically
        boolean inRiskZone = prefs.getBoolean("isCoastal", false);

        if (inRiskZone) {
            locationText.setText("📍 Current Location : Coastal Risk Zone");
            safetyNoteText.setText("Move indoors for safety");
        } else {
            locationText.setText("📍 Current Location : Safe Zone");
            safetyNoteText.setText("Your device is safe");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                float humidity = event.values[0];
                humidityText.setText(String.format("💧 Humidity : %.1f%%", humidity));
                break;

            case Sensor.TYPE_LIGHT:
                float lightLux = event.values[0];
                String lightLevel = "Low";
                if (lightLux > 10000) {
                    lightLevel = "Bright";
                } else if (lightLux > 1000) {
                    lightLevel = "Moderate";
                }
                brightnessText.setText(String.format("☀ Brightness : %s (%.0f lux)", lightLevel, lightLux));
                break;

            case Sensor.TYPE_ACCELEROMETER:
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelReady = true;

                float magnitude = (float) Math.sqrt(accelValues[0] * accelValues[0]
                        + accelValues[1] * accelValues[1]
                        + accelValues[2] * accelValues[2]);

                if (magnitude > 11) {
                    motionText.setText("📉 Motion Detected : Yes");
                } else {
                    motionText.setText("📉 Motion Detected : None");
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No changes here
    }
}
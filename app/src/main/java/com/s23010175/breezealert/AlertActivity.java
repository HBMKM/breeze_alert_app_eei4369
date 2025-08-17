package com.s23010175.breezealert;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private Sensor humiditySensor;
    private Sensor lightSensor;
    private Sensor accelerometerSensor;

    private TextView warningMessage, humidityValue, brightnessValue, motionValue, locationValue, suggestionMessage;
    private ImageView warningImage;
    private ImageButton backButton;
    private Button acknowledgeButton, dismissButton;

    private float currentHumidity = -1;
    private float currentLightLux = -1;
    private float[] accelValues = new float[3];
    private boolean accelReady = false;

    private BreezeAlertDBHelper dbHelper;
    private SharedPreferences prefs;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // Initialize UI elements
        warningMessage = findViewById(R.id.warningMessage);
        humidityValue = findViewById(R.id.humidityValue);
        brightnessValue = findViewById(R.id.brightnessValue);
        motionValue = findViewById(R.id.motionValue);
        locationValue = findViewById(R.id.locationValue);
        suggestionMessage = findViewById(R.id.suggestionMessage);
        warningImage = findViewById(R.id.warningImage);
        backButton = findViewById(R.id.backButton);
        acknowledgeButton = findViewById(R.id.acknowledgeButton);
        dismissButton = findViewById(R.id.dismissButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        dbHelper = new BreezeAlertDBHelper(this);
        prefs = getSharedPreferences("BreezeAlertSettings", MODE_PRIVATE);

        // Get current location status from SharedPreferences
        //boolean inRiskZone = prefs.getBoolean("isCoastal", false);

        //if (inRiskZone) {
            //locationValue.setText("📍 Location : Coastal Risk Zone");
            //suggestionMessage.setText("Move indoors for safety");
            //suggestionMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        //} else {
            //locationValue.setText("📍 Location : Safe Zone");
            //suggestionMessage.setText("You are safe");
            //suggestionMessage.setTextColor(getResources().getColor(android.R.color.black));
        //}


        backButton.setOnClickListener(v -> finish());

        acknowledgeButton.setOnClickListener(v -> {
            saveAlertToDB("Acknowledge");
            Toast.makeText(AlertActivity.this, "Acknowledged", Toast.LENGTH_SHORT).show();
            stopSoundIfPlaying();
            finish();
        });

        dismissButton.setOnClickListener(v -> {
            saveAlertToDB("Dismiss");
            Toast.makeText(AlertActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            stopSoundIfPlaying();
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (humiditySensor != null) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Update location immediately when screen is visible
        boolean inRiskZone = prefs.getBoolean("isCoastal", false);

        if (inRiskZone) {
            locationValue.setText("📍 Location : Coastal Risk Zone");
        } else {
            locationValue.setText("📍 Location : Safe Zone");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopSoundIfPlaying();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                currentHumidity = event.values[0];
                humidityValue.setText(String.format("💧 Humidity : %.1f%%", currentHumidity));
                updateWarningAndSuggestion();
                break;

            case Sensor.TYPE_LIGHT:
                currentLightLux = event.values[0];
                String lightLevel = "Low";
                if (currentLightLux > 10000) {
                    lightLevel = "Bright";
                } else if (currentLightLux > 1000) {
                    lightLevel = "Moderate";
                }
                brightnessValue.setText(String.format("☀ Brightness : %s (%.0f lux)", lightLevel, currentLightLux));
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
                    motionValue.setText("📉 Motion Detected : Yes");
                } else {
                    motionValue.setText("📉 Motion Detected : None");
                }
                break;
        }
    }

    private void updateWarningAndSuggestion() {
        // Get location risk
        boolean inRiskZone = prefs.getBoolean("isCoastal", false);

        // Update location text
        if (inRiskZone) {
            locationValue.setText("📍 Location : Coastal Risk Zone");
        } else {
            locationValue.setText("📍 Location : Safe Zone");
        }

        // Determine warning and suggestion
        if (currentHumidity >= 70 && inRiskZone) {
            // High humidity AND in risk zone → critical
            warningMessage.setText("⚠ High Humidity & Coastal Risk!");
            warningMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            suggestionMessage.setText("Move indoors immediately!");
            suggestionMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            playAlertSound();
        } else if (currentHumidity >= 70) {
            // High humidity only
            warningMessage.setText("High Humidity Detected");
            warningMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            suggestionMessage.setText("Move indoors immediately");
            suggestionMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            playAlertSound();
        } else if (inRiskZone) {
            // Coastal risk only
            warningMessage.setText("Coastal Risk Zone");
            warningMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            suggestionMessage.setText("Be cautious");
            suggestionMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            stopSoundIfPlaying();
        } else {
            // Safe zone + normal humidity
            warningMessage.setText("Humidity Normal");
            warningMessage.setTextColor(getResources().getColor(android.R.color.black));
            suggestionMessage.setText("Your device is safe");
            suggestionMessage.setTextColor(getResources().getColor(android.R.color.black));
            stopSoundIfPlaying();
        }
    }


    private void playAlertSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);
            float volume = prefs.getInt("sound_level", 50) / 100f;
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopSoundIfPlaying() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void saveAlertToDB(String buttonPressed) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(BreezeAlertDBHelper.COLUMN_SENSOR, "Humidity");
            values.put(BreezeAlertDBHelper.COLUMN_VALUE, String.format("%.1f%%", currentHumidity));
            values.put(BreezeAlertDBHelper.COLUMN_SUGGESTION, suggestionMessage.getText().toString());
            values.put(BreezeAlertDBHelper.COLUMN_BUTTON, buttonPressed);
            values.put(BreezeAlertDBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

            long rowId = db.insert(BreezeAlertDBHelper.TABLE_ALERTS, null, values);
            if (rowId == -1) {
                Toast.makeText(this, "Error saving alert", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving alert", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) db.close();
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}

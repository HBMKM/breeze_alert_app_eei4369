package com.s23010175.breezealert;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BreezeAlertSettings";
    private SharedPreferences prefs;
    private SeekBar soundSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Back button
        ImageView backButton = findViewById(R.id.back_button_settings);
        backButton.setOnClickListener(v -> finish());  // Close activity on back pressed

        // Switches
        Switch humiditySwitch = findViewById(R.id.switch_humidity);
        Switch brightnessSwitch = findViewById(R.id.switch_brightness);
        Switch motionSwitch = findViewById(R.id.switch_motion);
        Switch locationSwitch = findViewById(R.id.switch_location);

        humiditySwitch.setChecked(prefs.getBoolean("humidity", true));
        brightnessSwitch.setChecked(prefs.getBoolean("brightness", true));
        motionSwitch.setChecked(prefs.getBoolean("motion", true));
        locationSwitch.setChecked(prefs.getBoolean("location", true));

        humiditySwitch.setOnCheckedChangeListener(this::onSwitchChanged);
        brightnessSwitch.setOnCheckedChangeListener(this::onSwitchChanged);
        motionSwitch.setOnCheckedChangeListener(this::onSwitchChanged);
        locationSwitch.setOnCheckedChangeListener(this::onSwitchChanged);

        // Sound SeekBar
        soundSeekBar = findViewById(R.id.seekbar_sound);
        int savedLevel = prefs.getInt("sound_level", 50);
        soundSeekBar.setProgress(savedLevel);

        soundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("sound_level", progress);
                editor.apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void onSwitchChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = prefs.edit();

        int id = buttonView.getId();
        if (id == R.id.switch_humidity) {
            editor.putBoolean("humidity", isChecked);
        } else if (id == R.id.switch_brightness) {
            editor.putBoolean("brightness", isChecked);
        } else if (id == R.id.switch_motion) {
            editor.putBoolean("motion", isChecked);
        } else if (id == R.id.switch_location) {
            editor.putBoolean("location", isChecked);
        }

        editor.apply();
    }
}

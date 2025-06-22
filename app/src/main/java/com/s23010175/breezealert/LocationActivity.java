package com.s23010175.breezealert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText editTextSearch;
    private Button buttonShowLocation;
    private TextView textStatus;
    private ImageView backButton;

    private final List<Polygon> coastalZones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        editTextSearch = findViewById(R.id.editText_search);
        buttonShowLocation = findViewById(R.id.button_show_location);
        textStatus = findViewById(R.id.text_status);
        backButton = findViewById(R.id.back_button_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backButton.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonShowLocation.setOnClickListener(v -> {
            String locationName = editTextSearch.getText().toString().trim();
            if (!locationName.isEmpty()) {
                searchLocation(locationName);
            } else {
                Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupMap();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            LatLng currentLatLng;

            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                //  Use Unawatuna as fallback mock location for emulator/demo
                currentLatLng = new LatLng(6.0094, 80.2448);
                Toast.makeText(this, "Using mock location: Unawatuna", Toast.LENGTH_SHORT).show();
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
            addCoastalRiskZones();
            updateRiskStatus(currentLatLng);
        });
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 14));
                updateRiskStatus(searchedLatLng);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Geocoder service unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCoastalRiskZones() {
        coastalZones.clear();

        // Colombo zone
        Polygon colomboZone = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(6.9200, 79.8400),
                        new LatLng(6.9400, 79.8400),
                        new LatLng(6.9400, 79.8700),
                        new LatLng(6.9200, 79.8700))
                .strokeColor(0xAAFF0000)
                .fillColor(0x44FF0000));
        coastalZones.add(colomboZone);

        // Galle zone
        Polygon galleZone = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(6.0400, 80.2050),
                        new LatLng(6.0700, 80.2050),
                        new LatLng(6.0700, 80.2400),
                        new LatLng(6.0400, 80.2400))
                .strokeColor(0xAAFF0000)
                .fillColor(0x44FF0000));
        coastalZones.add(galleZone);

        // Unawatuna zone
        Polygon unawatunaZone = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(6.0000, 80.2400),
                        new LatLng(6.0200, 80.2400),
                        new LatLng(6.0200, 80.2600),
                        new LatLng(6.0000, 80.2600))
                .strokeColor(0xAAFF0000)
                .fillColor(0x44FF0000));
        coastalZones.add(unawatunaZone);

        // Trincomalee zone
        Polygon trincoZone = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(8.5750, 81.2050),
                        new LatLng(8.6000, 81.2050),
                        new LatLng(8.6000, 81.2250),
                        new LatLng(8.5750, 81.2250))
                .strokeColor(0xAAFF0000)
                .fillColor(0x44FF0000));
        coastalZones.add(trincoZone);
    }

    private void updateRiskStatus(LatLng latLng) {
        boolean inRiskZone = false;

        for (Polygon zone : coastalZones) {
            List<LatLng> points = zone.getPoints();
            if (points.size() >= 4) {
                LatLng p1 = points.get(0);
                LatLng p3 = points.get(2);

                double minLat = Math.min(p1.latitude, p3.latitude);
                double maxLat = Math.max(p1.latitude, p3.latitude);
                double minLng = Math.min(p1.longitude, p3.longitude);
                double maxLng = Math.max(p1.longitude, p3.longitude);

                if (latLng.latitude >= minLat && latLng.latitude <= maxLat &&
                        latLng.longitude >= minLng && latLng.longitude <= maxLng) {
                    inRiskZone = true;
                    break;
                }
            }
        }

        if (inRiskZone) {
            textStatus.setText("Status: Coastal Risk Zone");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            textStatus.setText("Status: Safe Zone");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


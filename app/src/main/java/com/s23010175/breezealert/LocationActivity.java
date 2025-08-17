package com.s23010175.breezealert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker liveMarker;

    private boolean followLiveLocation = true;
    private boolean onRoute = false;

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

        mMap.setMyLocationEnabled(false);

        addCoastalRiskZones();

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null && followLiveLocation) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (liveMarker == null) {
                        liveMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("Your Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    } else {
                        liveMarker.setPosition(currentLatLng);
                    }

                    if (!onRoute) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                    }

                    updateLiveLocationRiskStatus(currentLatLng);

                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                followLiveLocation = false;

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 14));
                updateRiskStatus(searchedLatLng);

                mMap.addMarker(new MarkerOptions()
                        .position(searchedLatLng)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Geocoder service unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCoastalRiskZones() {
        coastalZones.clear();

        // Existing coastal zones
        coastalZones.add(addPolygon(6.9100, 79.8450, 6.9400, 79.8750)); // Colombo
        coastalZones.add(addPolygon(6.0200, 80.2100, 6.0700, 80.2450)); // Galle
        coastalZones.add(addPolygon(5.9800, 80.2400, 6.0200, 80.2700)); // Unawatuna
        coastalZones.add(addPolygon(6.1400, 80.0900, 6.1700, 80.1200)); // Hikkaduwa
        coastalZones.add(addPolygon(7.1900, 79.8300, 7.2200, 79.8600)); // Negombo
        coastalZones.add(addPolygon(8.5700, 81.2000, 8.6050, 81.2300)); // Trincomalee
        coastalZones.add(addPolygon(7.7100, 81.6700, 7.7400, 81.7000)); // Batticaloa
        coastalZones.add(addPolygon(8.0200, 79.6600, 8.0500, 79.6900)); // Kalpitiya
        coastalZones.add(addPolygon(8.9400, 79.9000, 8.9700, 79.9300)); // Mannar
        coastalZones.add(addPolygon(9.6500, 80.0000, 9.6800, 80.0300)); // Jaffna
        coastalZones.add(addPolygon(7.3300, 81.8500, 7.3600, 81.8800)); // Pasikudah
        coastalZones.add(addPolygon(5.9400, 80.4500, 5.9700, 80.4800)); // Mirissa
        coastalZones.add(addPolygon(6.0250, 80.7850, 6.0320, 80.8050)); // Tangalle (corrected)
        coastalZones.add(addPolygon(5.9500, 80.2500, 6.0550, 80.2650)); // Walahanduwa (corrected)
        coastalZones.add(addPolygon(6.1000, 81.1100, 6.1600, 81.1700)); // Hambantota
        coastalZones.add(addPolygon(5.9400, 80.5200, 5.9800, 80.5700)); // Matara
    }

    private Polygon addPolygon(double lat1, double lng1, double lat3, double lng3) {
        return mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(lat1, lng1), new LatLng(lat3, lng1),
                        new LatLng(lat3, lng3), new LatLng(lat1, lng3))
                .strokeColor(0xAAFF0000)
                .fillColor(0x44FF0000));
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

        // Save risk status to SharedPreferences
        //SharedPreferences prefs = getSharedPreferences("BreezeAlertSettings", MODE_PRIVATE);
        //SharedPreferences.Editor editor = prefs.edit();
        //editor.putBoolean("isCoastal", inRiskZone);
        //editor.apply();

    }

    private void updateLiveLocationRiskStatus(LatLng latLng) {
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

        // Update UI
        if (inRiskZone) {
            textStatus.setText("Status: Coastal Risk Zone");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            textStatus.setText("Status: Safe Zone");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // ✅ Save live location risk status
        SharedPreferences prefs = getSharedPreferences("BreezeAlertSettings", MODE_PRIVATE);
        prefs.edit().putBoolean("isCoastal", inRiskZone).apply();
    }


    private void startRoute() {
        onRoute = true;
        followLiveLocation = false;
    }

    private void endRoute() {
        onRoute = false;
        followLiveLocation = true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}

package com.example.rideapp;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.example.rideapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.rideapp.databinding.ActivityMaps2Binding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    List<Location> myLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myLocations = MyRideActivity.getSavedLocations();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng currentPosition;
        List<LatLng> lastPositionList = new ArrayList<>();
        for (int i = 0; i < myLocations.size(); i++){
            if(myLocations.size() > 0){
                currentPosition = new LatLng(myLocations.get(myLocations.size()-1).getLatitude(), myLocations.get(myLocations.size()-1).getLongitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currentPosition);
                mMap.addMarker(markerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14));

                lastPositionList.add(currentPosition);
            }
        }

        Intent intent = getIntent();
        int value = intent.getIntExtra("Value", 0);
        if(value == 10){
            PolylineOptions options = new PolylineOptions();
            for(Location location : myLocations){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                options.add(latLng);
                options.width(12);
                options.color(Color.GREEN);
                options.geodesic(true);
                mMap.addPolyline(options);
            }
        }
    }
}
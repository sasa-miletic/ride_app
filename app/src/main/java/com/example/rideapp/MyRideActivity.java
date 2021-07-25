package com.example.rideapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyRideActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 10000;
    public static final int FAST_UPDATE_INTERVAL = 1000;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private TextView speedTv, topSpeedTv, distanceTv;
    private Button startBtn, finishBtn, showMapBtn, saveBtn;
    private Chronometer chronometer;

    private Location currentLocation;
    private static List<Location> savedLocations;

    private FusedLocationProviderClient fusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    private String latitude, longitude;
    private List<Float> topSpeeds;
    private boolean running;
    private float topSpeed;
    String distance, time, tSpeed;
    //double old, curr;

    private FirebaseAuth auth;
    private DatabaseReference ref;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ride);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("My ride");

        savedLocations = new ArrayList<>();
        topSpeeds = new ArrayList<>();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);

        initViews();
        disableButtons();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        Location myLocation = locationResult.getLastLocation();
                        updatePosition(myLocation);
                        startChronometer();
                        finishBtn.setEnabled(true);
                        startBtn.setEnabled(false);
                    }
                };

                startLocationUpdates();
                saveBtn.setEnabled(false);
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdating();
                stopChronometer();
                saveBtn.setEnabled(true);
                finishBtn.setEnabled(false);
                startBtn.setEnabled(true);
            }
        });

        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyRideActivity.this, MapsActivity2.class);
                intent.putExtra("Value", 10);
                startActivity(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveRide();

                resetUiValues();
                saveBtn.setEnabled(false);
            }
        });

        updateGps();
    }

    private void saveRide() {
        distance = distanceTv.getText().toString();
        time = chronometer.getText().toString();
        tSpeed = topSpeedTv.getText().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("distance", distance);
        hashMap.put("time", time);
        hashMap.put("top speed", tSpeed);
        hashMap.put("timestamp", timestamp);

        ref.child("rides").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MyRideActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateGps() {
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(null != location){
                        updatePosition(location);
                        currentLocation = location;
                    }
                }
            });
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void stopLocationUpdating() {
        fusedClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGps();
    }

    private void updatePosition(Location myLocation) {
        savedLocations.add(myLocation);

        if(savedLocations.size() > 0){
            distanceCount(savedLocations);
        }

        updateUiValues(myLocation);
    }

    private void distanceCount(List<Location> locations){
        Location origin;
        Location destination;
        float distance = 0;

        origin = locations.get(0);
        for(int i = 1; i < locations.size(); i++){
            destination = locations.get(i);
            distance += origin.distanceTo(destination);
            if(origin != locations.get(locations.size() - 1)){
                origin = destination;
            }
        }

        float distanceInKm = distance / 1000;
        DecimalFormat df = new DecimalFormat("0.00");

        distanceTv.setText("" + df.format(distanceInKm));
    }

    private void getDataAndSave(){

        double newDistance = Double.parseDouble(distanceTv.getText().toString());

        Query query = ref.orderByChild("uid").equalTo(auth.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    //oldTopSpeed = "" + ds.child("topSpeed").getValue();
                    //distance =  ds.child("distance").getValue().toString();
                    double old = (double) ds.child("distance").getValue(Double.class);

                    //newTopSpeed = topSpeedTv.getText().toString();

                    double x = old + newDistance;

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("distance", "" + x);
//                    if(d > c ){
//                        hashMap.put("topSpeed", newTopSpeed);
//                    }
//                    else{
//                        hashMap.put("topSpeed", oldTopSpeed);
//                    }

                    ref.child(auth.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MyRideActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUiValues(Location myLocation) {
        latitude = String.valueOf(myLocation.getLatitude());
        longitude = String.valueOf(myLocation.getLongitude());

        DecimalFormat df = new DecimalFormat("0.00");

        if(myLocation.hasSpeed()){

            float speed = (float) ((myLocation.getSpeed() * 3600) / 1000);

            topSpeeds.add(speed);
            String s = df.format(speed);
            speedTv.setText(s);

            //top speed
            if(topSpeeds.size() > 0){
                topSpeed = getTopSpeed(speed);
            }
            String ts = df.format(topSpeed);
            topSpeedTv.setText(ts);
        }
    }

    private float getTopSpeed(float speed){
        float top = topSpeeds.get(0);
        if(topSpeeds.size() > 1){
            for (int i = 1; i < topSpeeds.size(); i++){
                if(top < topSpeeds.get(i)){
                    top = topSpeeds.get(i);
                }
            }
        }

        return top;
    }

    private void resetUiValues(){
        speedTv.setText("0.0");
        topSpeedTv.setText("0.0");
        distanceTv.setText("0.0");
        resetChronometer();
    }

    private void initViews() {
        speedTv = findViewById(R.id.speedTv);
        topSpeedTv = findViewById(R.id.topSpeedTv);
        distanceTv = findViewById(R.id.distanceTv);
        chronometer = findViewById(R.id.chronometer);
        //timeTv = findViewById(R.id.timeTv);
        startBtn = findViewById(R.id.startBtn);
        finishBtn = findViewById(R.id.finishBtn);
        showMapBtn = findViewById(R.id.showMap);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void startChronometer(){
        if(!running){
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    private void stopChronometer(){
        if(running){
            chronometer.stop();
            running = false;
        }
    }

    private void resetChronometer(){
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void disableButtons(){
        finishBtn.setEnabled(false);
        saveBtn.setEnabled(false);
    }

    public static List<Location> getSavedLocations(){
        return savedLocations;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
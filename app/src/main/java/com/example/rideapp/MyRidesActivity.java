package com.example.rideapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyRidesActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference reference;

    private RecyclerView recyclerView;

    private String uid;

    private RideAdapter adapter;
    private List<ModelRide> rideList;
    private ModelRide modelRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("My rides");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

        recyclerView = findViewById(R.id.recView);
        rideList = new ArrayList<>();

        showRides();
    }

    private void showRides() {

        uid = user.getUid();

        DatabaseReference ref = reference.child(uid).child("rides");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rideList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    String distance = "" + ds.child("distance").getValue();
                    String duration = "" + ds.child("time").getValue();
                    String topSpeed = "" + ds.child("top speed").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();

                    modelRide = new ModelRide(distance, duration, topSpeed, timestamp);
                    do{
                        rideList.add(modelRide);
                    }
                    while (modelRide.getTimestamp() != timestamp);

                    //rideList.add(modelRide);
                }

                adapter = new RideAdapter(MyRidesActivity.this, rideList);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(MyRidesActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
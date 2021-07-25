package com.example.rideapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference reference;

    private TextView userNameTv, userEmailTv;
    private Button startBtn;
    private FloatingActionButton fab;

    private String uid;

    private RideAdapter adapter;
    private List<ModelRide> rideList;
    private ModelRide modelRide;

    public ProfileActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar bar = getSupportActionBar();
        bar.setTitle("Profile");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

        initViews();

        rideList = new ArrayList<>();

        checkUserStatus();

        getUserDetails();

        findViewById(R.id.myRidesTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MyRidesActivity.class));
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDialog();
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MyRideActivity.class));
            }
        });
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
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void showEditNameDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit name");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        EditText editText = new EditText(this);
        editText.setHint("Enter your name");
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("name", value);

                    reference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void getUserDetails() {
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();

                    userNameTv.setText(name);
                    userEmailTv.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = auth.getCurrentUser();

        if(user != null){
            uid = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", uid);
            editor.apply();
        }
        else{
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            auth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    private void initViews() {
        userNameTv = findViewById(R.id.UserNameTv);
        userEmailTv = findViewById(R.id.MailTv);
        startBtn = findViewById(R.id.StartBtn);
        fab = findViewById(R.id.editName);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
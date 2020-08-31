package com.km0_compro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // to add the name of the user on the top
    TextView username;
    Button btn_maps;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    int num_max_orders = 3; // 6
    int hour_start_service = -1; // 7
    int hour_finish_service = 25; // 18
    int sunday = 20; // 1
    int saturday = 21;   // 7

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for the logout menu and the username on the top
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // get permissions for location tracking
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        // to add username and image on the top
        username = findViewById(R.id.username);
        btn_maps = findViewById(R.id.btn_maps);

        btn_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get date
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                Date c = Calendar.getInstance().getTime();
                String date = df.format(c);

                // get name of week
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                // get hour (0-23 format)
                Calendar rightNow = Calendar.getInstance();
                int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);

                // check if service is "on"
                DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("Service");
                serviceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String check = dataSnapshot.getValue(String.class);
                        if (check.equals("on")) {
                            // no activity change if saturday, sunday, before 8 or after 17
                            if (currentHourIn24Format > hour_start_service && currentHourIn24Format < hour_finish_service) {
                                if (day == sunday || day == saturday) {
                                    Toast.makeText(MainActivity.this, "Il servizio non è disponibile il sabato e la domenica", Toast.LENGTH_LONG).show();
                                } else {
                                    DatabaseReference referenceTotalOrders = FirebaseDatabase.getInstance().getReference("Orders").child(date).child("ordersinfo");
                                    referenceTotalOrders.child("totalorders").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Integer numberTotalOrders = dataSnapshot.getValue(Integer.class);

                                            if (numberTotalOrders == null) {
                                                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                                                //Toast.makeText(MainActivity.this, "Primo ordine", Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (numberTotalOrders.equals(num_max_orders)) {
                                                    referenceTotalOrders.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            boolean alreadyOrdered = false;
                                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                // get customer's uid
                                                                FirebaseAuth auth;
                                                                auth = FirebaseAuth.getInstance();  // authentication for firebase
                                                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                                                assert firebaseUser != null;
                                                                String userid = firebaseUser.getUid();

                                                                if (!snapshot.getKey().equals("totalorders")) {
                                                                    String user = (String) snapshot.getValue();
                                                                    if (user.equals(userid)) {
                                                                        alreadyOrdered = true;
                                                                    }
                                                                }
                                                            }
                                                            if (!alreadyOrdered) {
                                                                startActivity(new Intent(MainActivity.this, Pop.class));
                                                                //Toast.makeText(MainActivity.this, "Numero massimo di ordini raggiunto", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                } else {
                                                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                                                    //startActivity(new Intent(MainActivity.this, Pop.class));
                                                    //Toast.makeText(MainActivity.this, "Numero massimo di ordini raggiunto", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Il servizio è disponibile dalle 8 alle 17", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            startActivity(new Intent(MainActivity.this, Pop.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText("Ciao " + user.getUsername() + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



    }

    // to logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // to logout
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
                return true;
        }
        return false;
    }
}

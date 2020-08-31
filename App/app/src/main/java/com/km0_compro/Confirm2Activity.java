package com.km0_compro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Confirm2Activity extends AppCompatActivity {

    String nameShop;
    String idShop;
    String realname;
    String phone;
    String address;
    String total;

    TextView user_realname;
    TextView user_phone;
    TextView user_address;

    private Spinner spinner;
    String timeSelection;
    Integer numberOrders = 0;
    Integer numberTotalOrders = 0;
    boolean alreadyOrdered = false;

    Button btn_done;
    Button btn_changeCredentials;

    private long backPressedTime;
    private Toast backToast;

    Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm2);

        user_realname = findViewById(R.id.user_realname);
        user_phone = findViewById(R.id.user_phone);
        user_address = findViewById(R.id.user_address);
        btn_done = findViewById(R.id.btn_done2);
        spinner = findViewById(R.id.spinner_hour2);
        btn_changeCredentials = findViewById(R.id.btn_changeCredentials);

        if (getIntent().hasExtra("com.km0_vendo.SHOP")) {
            nameShop = Objects.requireNonNull(getIntent().getExtras()).getString("com.km0_vendo.SHOP");
            idShop = getIntent().getExtras().getString("com.km0_vendo.SHOPID");
            realname = getIntent().getExtras().getString("com.km0_vendo.REALNAME");
            phone = getIntent().getExtras().getString("com.km0_vendo.PHONE");
            address = getIntent().getExtras().getString("com.km0_vendo.ADDRESS");
            total = getIntent().getExtras().getString("com.km0_vendo.TOTAL");
            price = getIntent().getExtras().getDouble("com.km0_vendo.PRICE");
        }

        user_realname.setText("Nome sul campanello: " + realname);
        user_phone.setText("Cellulare: " + phone);
        user_address.setText("Indirizzo: " + address);

        // get date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date c = Calendar.getInstance().getTime();
        String date = df.format(c);

        // get customer's uid
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();  // authentication for firebase
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        // spinner --> check if the hours are already got
        List<String> timeList = new ArrayList<>();
        String defaultTime = "Seleziona ora";
        timeList.add(defaultTime);
        String time1 = "19:00";
        timeList.add(time1);
        String time2 = "19:10";
        timeList.add(time2);
        String time3 = "19:20";
        timeList.add(time3);
        String time4 = "19:30";
        timeList.add(time4);
        String time5 = "19:40";
        timeList.add(time5);
        String time6 = "19:50";
        timeList.add(time6);
        String time7 = "20:00";
        timeList.add(time7);
        DatabaseReference timeRef = FirebaseDatabase.getInstance().getReference("Orders").child(date).child("ordersinfo");

        // check if the user has already ordered
        timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String oneTime = "0";
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.getKey().equals("totalorders")) {
                        String uid = (String) snapshot.getValue();
                        if (uid.equals(userid)) {
                            alreadyOrdered = true;
                            oneTime = snapshot.getKey();
                        }
                    }
                }
                if (alreadyOrdered) {
                    timeList.clear();
                    String defaultTime = "Seleziona ora";
                    timeList.add(defaultTime);
                    timeList.add(oneTime);
                }
                if (!alreadyOrdered) {
                    timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String time = snapshot.getKey();
                                assert time != null;

                                if (time.equals("19:00")) {
                                    timeList.remove(time1);
                                }
                                if (time.equals("19:10")) {
                                    timeList.remove(time2);
                                }
                                if (time.equals("19:20")) {
                                    timeList.remove(time3);
                                }
                                if (time.equals("19:30")) {
                                    timeList.remove(time4);
                                }
                                if (time.equals("19:40")) {
                                    timeList.remove(time5);
                                }
                                if (time.equals("19:50")) {
                                    timeList.remove(time6);
                                }
                                if (time.equals("20:00")) {
                                    timeList.remove(time7);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, timeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                timeSelection = parent.getSelectedItem().toString();
                //Toast.makeText(ConfirmActivity.this, timeSelection, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timeSelection.equals("Seleziona ora")) {
                    // increase number of total daily orders
                    DatabaseReference referenceTotalOrders = FirebaseDatabase.getInstance().getReference("Orders").child(date).child("ordersinfo");
                    referenceTotalOrders.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean firstOrder = true;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String time = snapshot.getKey();
                                assert time != null;
                                if (!time.equals("totalorders")) {
                                    String user = (String) snapshot.getValue();
                                    if (user.equals(userid)) {
                                        firstOrder = false;
                                        //Toast.makeText(ConfirmActivity.this, user, Toast.LENGTH_LONG).show();
                                    }
                                }

                            }

                            if (firstOrder) {
                                referenceTotalOrders.child("totalorders").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        numberTotalOrders = dataSnapshot.getValue(Integer.class);
                                        if (numberTotalOrders != null) {
                                            HashMap<String, Object> hashMapTot = new HashMap<>();
                                            hashMapTot.put("totalorders", numberTotalOrders + 1);
                                            hashMapTot.put(timeSelection, userid);
                                            referenceTotalOrders.updateChildren(hashMapTot);
                                        } else {
                                            HashMap<String, Object> hashMapTot = new HashMap<>();
                                            hashMapTot.put("totalorders", 1);
                                            hashMapTot.put(timeSelection, userid);
                                            referenceTotalOrders.setValue(hashMapTot).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //Toast.makeText(ConfirmActivity.this, "primo ordine del giorno", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    // read number of today orders in the selected shop
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop).child("sellerinfo");
                    reference.child("numberorders").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            numberOrders = dataSnapshot.getValue(Integer.class);
                            if (numberOrders != null) {
                                //txt_numberOrders = Integer.toString(numberOrders + 1);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("numberorders", numberOrders + 1);
                                hashMap.put("sellername", nameShop);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //Toast.makeText(ProductListActivity.this, "ordine " + txt_numberOrders, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("numberorders", 1);
                                hashMap.put("sellername", nameShop);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //Toast.makeText(ProductListActivity.this, "ordine 1", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // register total income and total orders
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(idShop);
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);
                            String totalIncome = user.getTotalincome();
                            String totalOrders = user.getTotalorders();
                            if (totalIncome.equals("default") && totalOrders.equals("default")) {
                                HashMap<String, Object> hashMap4 = new HashMap<>();
                                hashMap4.put("totalincome", String.valueOf(price));
                                hashMap4.put("totalorders", "1");
                                usersRef.updateChildren(hashMap4);
                            } else {
                                Double updateIncome = Double.parseDouble(totalIncome) + price;
                                int updateOrders = Integer.parseInt(totalOrders) + 1;
                                HashMap<String, Object> hashMap4 = new HashMap<>();
                                hashMap4.put("totalincome", String.valueOf(updateIncome));
                                hashMap4.put("totalorders", String.valueOf(updateOrders));
                                usersRef.updateChildren(hashMap4);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // fill buyer info
                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop).child(userid).child("buyerinfo");
                    HashMap<String, String> hashMap3 = new HashMap<>();
                    hashMap3.put("buyerid", userid);
                    hashMap3.put("sent", "no");
                    hashMap3.put("address", address);
                    hashMap3.put("realname", realname);
                    hashMap3.put("phone", phone);
                    hashMap3.put("time", timeSelection);
                    hashMap3.put("total", total);
                    reference2.setValue(hashMap3).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Confirm2Activity.this, "Ordine completato!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(Confirm2Activity.this, LastActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }
                        }
                    });


                } else {
                    Toast.makeText(Confirm2Activity.this, "Selezionare ora di consegna", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_changeCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Confirm2Activity.this, ConfirmActivity.class);
                i.putExtra("com.km0_vendo.SHOP", nameShop);
                i.putExtra("com.km0_vendo.SHOPID", idShop);
                i.putExtra("com.km0_vendo.PRICE", price);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });


    }

    // define action for back button
    @Override
    public void onBackPressed() {
        // it compares the time of the second click and the first click (less than 2s)
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();

            // get date
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            Date c = Calendar.getInstance().getTime();
            String date = df.format(c);

            // get customer's uid
            FirebaseAuth auth;
            auth = FirebaseAuth.getInstance();  // authentication for firebase
            FirebaseUser firebaseUser = auth.getCurrentUser();
            assert firebaseUser != null;
            String userid = firebaseUser.getUid();

            DatabaseReference ordersCheck = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop);
            ordersCheck.child(userid).removeValue();
            Intent i = new Intent(Confirm2Activity.this, ProductListActivity.class);
            i.putExtra("com.km0_vendo.SHOP", nameShop);
            i.putExtra("com.km0_vendo.SHOPID", idShop);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Premi di nuovo per uscire. ATTENZIONE: la lista della tua spesa verrà cancellata", Toast.LENGTH_LONG);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();


    }

}

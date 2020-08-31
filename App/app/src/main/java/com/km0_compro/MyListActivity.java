package com.km0_compro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Adapter.MyListAdapter;
import com.km0_compro.Adapter.ProductAdapter;
import com.km0_compro.Model.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyListActivity extends AppCompatActivity {

    private RecyclerView recyclerView_mylist;
    private MyListAdapter MyListAdapter;
    private List<Product> mProducts1;

    String nameShop;
    String idShop;
    String userid;
    String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        if (getIntent().hasExtra("com.km0_vendo.SHOP")) {
            nameShop = getIntent().getExtras().getString("com.km0_vendo.SHOP");
            idShop = getIntent().getExtras().getString("com.km0_vendo.SHOPID");
            userid = getIntent().getExtras().getString("com.km0_vendo.USERID");
            date = getIntent().getExtras().getString("com.km0_vendo.DATE");
        }


        // for the product list
        recyclerView_mylist = findViewById(R.id.recycler_view_mylist);
        recyclerView_mylist.setHasFixedSize(true);
        recyclerView_mylist.setLayoutManager(new LinearLayoutManager(MyListActivity.this));
        mProducts1 = new ArrayList<>();
        readProducts();
    }

    private void readProducts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop).child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProducts1.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.getKey().equals("buyerinfo")) {
                        Product product = snapshot.getValue(Product.class);
                        //Toast.makeText(MyListActivity.this, product.getDescription(), Toast.LENGTH_LONG).show();
                        mProducts1.add(product);
                    }

                }

                MyListAdapter = new MyListAdapter(MyListActivity.this, mProducts1);
                recyclerView_mylist.setAdapter(MyListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // define action for back button
    @Override
    public void onBackPressed() {

        Intent i = new Intent(MyListActivity.this, ProductListActivity.class);
        i.putExtra("com.km0_vendo.SHOP", nameShop);
        i.putExtra("com.km0_vendo.SHOPID", idShop);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();

    }
}

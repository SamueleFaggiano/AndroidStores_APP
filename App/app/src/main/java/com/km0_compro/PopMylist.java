package com.km0_compro;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Adapter.MyListAdapter;
import com.km0_compro.Model.Product;

import java.util.ArrayList;
import java.util.List;

public class PopMylist extends Activity {

    private RecyclerView recyclerView_mylist;
    private com.km0_compro.Adapter.MyListAdapter MyListAdapter;
    private List<Product> mProducts1;

    String nameShop;
    String idShop;
    String userid;
    String date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set size of the pop up window
        setContentView(R.layout.pop_mylist);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.6));

        if (getIntent().hasExtra("com.km0_vendo.SHOP")) {
            nameShop = getIntent().getExtras().getString("com.km0_vendo.SHOP");
            idShop = getIntent().getExtras().getString("com.km0_vendo.SHOPID");
            userid = getIntent().getExtras().getString("com.km0_vendo.USERID");
            date = getIntent().getExtras().getString("com.km0_vendo.DATE");
        }


        // for the product list
        recyclerView_mylist = findViewById(R.id.recycler_view_mylist);
        recyclerView_mylist.setHasFixedSize(true);
        recyclerView_mylist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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

                MyListAdapter = new MyListAdapter(PopMylist.this, mProducts1);
                recyclerView_mylist.setAdapter(MyListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

package com.km0_compro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PopPresentation extends Activity {

    private Spinner spinner;
    String quantitySelected = "1";
    String imageUri;
    String productNumber;
    String price;
    String disc;
    String description;
    ImageView product_image2;
    TextView product_name2;
    TextView product_price2;
    TextView receive_discount;
    Button add_to_cart;
    String discounted_price;
    TextView discounted_price2;
    String selectedShop;
    String selectedShopId;
    String total;

    DatabaseReference reference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set size of the pop up window
        setContentView(R.layout.pop_presentation);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*0.9), (int) (height*0.6));

        spinner = findViewById(R.id.spinner_quantity);
        product_image2 = findViewById(R.id.product_image2);
        product_name2 = findViewById(R.id.product_name2);
        product_price2 = findViewById(R.id.product_price2);
        receive_discount = findViewById(R.id.receive_discount2);
        add_to_cart = findViewById(R.id.add_to_cart);
        discounted_price2 = findViewById(R.id.discounted_price2);

        // spinner discounts
        List<Quantity> quantityList = new ArrayList<>();
        Quantity quantity1 = new Quantity("1");
        quantityList.add(quantity1);
        Quantity quantity2 = new Quantity("2");
        quantityList.add(quantity2);
        Quantity quantity3 = new Quantity("3");
        quantityList.add(quantity3);
        Quantity quantity4 = new Quantity("4");
        quantityList.add(quantity4);
        Quantity quantity5 = new Quantity("5");
        quantityList.add(quantity5);

        ArrayAdapter<Quantity> adapter = new ArrayAdapter<Quantity>(this,android.R.layout.simple_spinner_item, quantityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Quantity discountSelection = (Quantity) parent.getSelectedItem();
                quantitySelected = discountSelection.getQuantity();
                DecimalFormat formatter = new DecimalFormat("0.00");
                Float quantityFloat = Float.parseFloat(quantitySelected);
                if (disc.equals("0")) {
                    String price_replace = price.replace(",", ".");
                    Float priceNoDisc = Float.parseFloat(price_replace);
                    total = formatter.format(quantityFloat * priceNoDisc);
                    discounted_price2.setText(total + "€");
                } else {
                    String discounted_price_replace = discounted_price.replace(",", ".");
                    Float priceDisc = Float.parseFloat(discounted_price_replace);
                    total = formatter.format(quantityFloat * priceDisc);
                    discounted_price2.setText(total + "€");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });


        // receive data from ProductAdapter
        if (getIntent().hasExtra("com.km0_vendo.GETIMAGE")) {
            imageUri = getIntent().getExtras().getString("com.km0_vendo.GETIMAGE");
            productNumber = getIntent().getExtras().getString("com.km0_vendo.GETNUMBER");
            description = getIntent().getExtras().getString("com.km0_vendo.GETNAME");
            price = getIntent().getExtras().getString("com.km0_vendo.GETPRICE");
            discounted_price = getIntent().getExtras().getString("com.km0_vendo.GETDISCOUNTEDPRICE");
            disc = getIntent().getExtras().getString("com.km0_vendo.GETDISCOUNT");
            selectedShop = getIntent().getExtras().getString("com.km0_vendo.GETSELLERNAME");
            selectedShopId = getIntent().getExtras().getString("com.km0_vendo.GETSELLERID");
        } else {
            Toast.makeText(PopPresentation.this, "Error", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(PopPresentation.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Glide.with(getApplicationContext()).load(imageUri).into(product_image2);
        product_name2.setText(description);
        if (!disc.equals("0")) {
            product_price2.setText(price + "€");
            product_price2.setPaintFlags(product_price2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            discounted_price2.setText("€" + discounted_price);
            receive_discount.setText("-" + disc + "%");
        } else {
            product_price2.setText(price + "€");
            discounted_price2.setText(price);
        }

        // add product to database
        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get date
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                Date c = Calendar.getInstance().getTime();
                String date = df.format(c);

                // save in db
                auth = FirebaseAuth.getInstance();  // authentication for firebase
                FirebaseUser firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                String userid = firebaseUser.getUid();

                // save name buyer in db
                reference = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(selectedShopId);
                DatabaseReference reference2 = reference.child(userid).child(productNumber);
                HashMap<String, String> hashMap2 = new HashMap<>();
                hashMap2.put("description", description);
                hashMap2.put("number", quantitySelected);
                hashMap2.put("price", total);
                hashMap2.put("sellerId", selectedShopId);
                reference2.setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        /*if (task.isSuccessful()){
                            Toast.makeText(PresentationActivity.this, "prodotto aggiunto correttamente", Toast.LENGTH_SHORT).show();
                        }*/
                    }
                });


                // come back to prev activity
                Toast.makeText(PopPresentation.this, "Prodotto aggiunto al carrello", Toast.LENGTH_SHORT).show();
                //Intent i = new Intent(PopPresentation.this, ProductListActivity.class);
                //i.putExtra("com.km0_vendo.SHOP", selectedShop);
                //i.putExtra("com.km0_vendo.SHOPID", selectedShopId);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(i);
                //finish();
            }
        });


    }
}


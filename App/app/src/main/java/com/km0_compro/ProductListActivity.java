package com.km0_compro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Adapter.ProductAdapter;
import com.km0_compro.Model.Product;
import com.km0_compro.Model.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    String nameShop;
    String idShop;
    String total;

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> mProducts;

    TextView totalExp;
    Double minExp = 25.00;
    Double price;

    EditText search_products;

    Button btn_buy;

    TextView list_expense;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Toolbar on the top
        if (getIntent().hasExtra("com.km0_vendo.SHOP")) {
            nameShop = getIntent().getExtras().getString("com.km0_vendo.SHOP");
            idShop = getIntent().getExtras().getString("com.km0_vendo.SHOPID");
        } else {
            nameShop = "Negozio";
        }
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(nameShop + " - Lista dei prodotti");

        btn_buy = findViewById(R.id.btn_buy);
        list_expense = findViewById(R.id.list_expense);

        // for the product list
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProductListActivity.this));
        mProducts = new ArrayList<>();
        readProducts();


        // get date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date c = Calendar.getInstance().getTime();
        String date = df.format(c);

        // get customer's uid
        auth = FirebaseAuth.getInstance();  // authentication for firebase
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        // calculate expense
        totalExp = findViewById(R.id.total_expense);
        DatabaseReference refMinExpense = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop).child(userid);
        refMinExpense.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                price = 0.0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String numberProduct = snapshot.getKey();
                    if (!numberProduct.equals("buyerinfo")) {
                        Product product = snapshot.getValue(Product.class);
                        assert product != null;
                        //Double newPrice = Double.parseDouble(product.getPrice());
                        String newPrice = product.getPrice();
                        String price_replace = newPrice.replace(",", ".");
                        price = price + Double.parseDouble(price_replace);
                        DecimalFormat f = new DecimalFormat("##.00");
                        totalExp.setText("Totale spesa: " + f.format(price) + "€");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // search a product
        search_products = findViewById(R.id.search_products);
        search_products.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                productAdapter.getFilter().filter(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // go to the list of selected products
        list_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProductListActivity.this, PopMylist.class);
                i.putExtra("com.km0_vendo.SHOP", nameShop);
                i.putExtra("com.km0_vendo.SHOPID", idShop);
                i.putExtra("com.km0_vendo.USERID", userid);
                i.putExtra("com.km0_vendo.DATE", date);
                startActivity(i);
            }
        });

        // confirm products
        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if expense is higher than minimum value
                DatabaseReference minExpenseCheck = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop).child(userid);
                minExpenseCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        price = 0.0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String numberProduct = snapshot.getKey();
                            if (!numberProduct.equals("buyerinfo")) {
                                Product product = snapshot.getValue(Product.class);
                                assert product != null;
                                String newPrice = product.getPrice();
                                String price_replace = newPrice.replace(",", ".");
                                price = price + Double.parseDouble(price_replace);
                            }
                        }
                        if (price < minExp) {
                            DecimalFormat f = new DecimalFormat("##.00");
                            Toast.makeText(ProductListActivity.this, "Non hai ancora raggiunto la spesa minima per ordinare. Spesa minima: " + f.format(minExp) + "€", Toast.LENGTH_LONG).show();
                        } else {
                            total = String.valueOf(price);
                            // check if user info are already saved from previous orders
                            DatabaseReference updateInfo = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            Double finalPrice = price;
                            updateInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user.getPhone().equals("default")) {
                                        // user has to insert data
                                        Intent i = new Intent(ProductListActivity.this, ConfirmActivity.class);
                                        i.putExtra("com.km0_vendo.SHOP", nameShop);
                                        i.putExtra("com.km0_vendo.SHOPID", idShop);
                                        i.putExtra("com.km0_vendo.TOTAL", total);
                                        i.putExtra("com.km0_vendo.PRICE", price);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        // we already have user data
                                        Intent i = new Intent(ProductListActivity.this, Confirm2Activity.class);
                                        i.putExtra("com.km0_vendo.SHOP", nameShop);
                                        i.putExtra("com.km0_vendo.SHOPID", idShop);
                                        i.putExtra("com.km0_vendo.REALNAME", user.getRealname());
                                        i.putExtra("com.km0_vendo.PHONE", user.getPhone());
                                        i.putExtra("com.km0_vendo.ADDRESS", user.getAddress());
                                        i.putExtra("com.km0_vendo.TOTAL", total);
                                        i.putExtra("com.km0_vendo.PRICE", price);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();

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

            }

        });

    }


    // for the product list
    private void readProducts(){

        //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProducts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);

                    assert product != null;
                    if (!product.getDescription().equals("Default") && nameShop.equals(product.getSellerName())) {
                    //if (!product.getDescription().equals("Default") && idShop.equals(product.getSellerId())) {

                        mProducts.add(product);
                    }

                }

                productAdapter = new ProductAdapter(ProductListActivity.this, mProducts);
                recyclerView.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // define action for back button
    @Override
    public void onBackPressed() {
        // get date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date c = Calendar.getInstance().getTime();
        String date = df.format(c);

        // get customer's uid
        auth = FirebaseAuth.getInstance();  // authentication for firebase
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        DatabaseReference ordersCheck = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(idShop);
        ordersCheck.child(userid).removeValue();
        startActivity(new Intent(ProductListActivity.this, MapsActivity.class));

    }


}

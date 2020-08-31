package com.km0_compro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SimpleTimeZone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationListener locationListener;

    private LocationManager locationManager;
    private final long MIN_TIME = 60000;    // ms
    private final long MIN_DIST = 5;    // meters
    float zoomLevel = 14.0f;            // the lower the farther
    double dMax = 0.030;    // CAMBIA ANCHE IN CONFIRMACTIVITY.JAVA
    double centerLat = 43.606032;   // CAMBIA ANCHE IN CONFIRMACTIVITY.JAVA
    double centerLng = 13.365574;   // CAMBIA ANCHE IN CONFIRMACTIVITY.JAVA

    private LatLng latLng;
    String latitude;
    String longitude;
    FirebaseAuth auth;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    String username;

    String selectedShop = "nessun negozio";
    String selectedShopId;

    private FusedLocationProviderClient fusedLocationProviderClient;
    boolean locFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // to visualize the name on the map
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username = user.getUsername();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        auth = FirebaseAuth.getInstance();  // authentication for firebase

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        final ProgressDialog pd = new ProgressDialog(MapsActivity.this);
        pd.setMessage("Aspetta qualche secondo... Sto cercando i negozi vicini a te!");
        pd.setCancelable(false);
        pd.show();

        //Toast.makeText(MapsActivity.this, "Aspetta qualche secondo...", Toast.LENGTH_LONG).show();

        // hide other point of interest
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
        } catch (Resources.NotFoundException e) {
            Toast.makeText(MapsActivity.this, "Errore", Toast.LENGTH_LONG).show();
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    locFound = true;
                    getShops(location);
                    pd.dismiss();
                }
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!locFound) {
                    try {
                        getShops(location);
                        pd.dismiss();

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }


        // show shop's name if taped
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().equals(username)) {
                    selectedShop = marker.getTitle();
                    selectedShopId = marker.getSnippet();
                    marker.setSnippet("Tocca qui per entrare nel negozio");
                    marker.hideInfoWindow();
                }
                return false;
            }
        });

        // change activity taping on the snippet
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(MapsActivity.this, ProductListActivity.class);
                i.putExtra("com.km0_vendo.SHOP", selectedShop);
                i.putExtra("com.km0_vendo.SHOPID", selectedShopId);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }

        });

    }

    private void getShops(Location location) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date c = Calendar.getInstance().getTime();
        String date = df.format(c);

        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();


        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(username));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        // draw circle around user
        LatLng center = new LatLng(centerLat, centerLng);
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(dMax*111111)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5));
        //.fillColor(Color.BLUE));

        // salva i dati dell'utente
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        if ((longitude != null) && (latitude != null)) {

            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("lat", latitude);
            hashMap.put("lng", longitude);
            reference.updateChildren(hashMap);
            //Toast.makeText(MapsActivity.this, "GPS coordinates saved correctly", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MapsActivity.this, "no GPS disponibile", Toast.LENGTH_LONG).show();
        }


        // mostra negozi
        double latMax = centerLat - (Double.parseDouble(latitude));   // center latitude - customer latitude
        double lngMax = centerLng - (Double.parseDouble(longitude));  // center longitude - customer longitude
        double distance = Math.sqrt(Math.pow(latMax, 2) + Math.pow(lngMax, 2));
        if (distance < dMax) {
            DatabaseReference referenceUsers = FirebaseDatabase.getInstance().getReference("Users");
            referenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        String shopId = snapshot.getKey();
                        assert user != null;
                        if (user.getType().equals("Seller")) {
                            // delete from map the shop if the user has already ordered
                            DatabaseReference ordersCheck = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(shopId);
                            ordersCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean ordered = false;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String content = snapshot.getKey();
                                        assert content != null;
                                        if (content.equals(userid)) {
                                            ordered = true;
                                        }

                                    }
                                    if (!ordered) {
                                        Double latUser = Double.parseDouble(user.getLat());  // shop latitude
                                        Double lngUser = Double.parseDouble(user.getLng());  // shop longitude
                                        //double latMax = latUser - (Double.parseDouble(latitude));   // shop latitude - customer latitude
                                        //double lngMax = lngUser - (Double.parseDouble(longitude));  // shop longitude - customer longitude
                                        //double distance = Math.sqrt(Math.pow(latMax, 2) + Math.pow(lngMax, 2));
                                        //if (distance < dMax) {
                                        String nameUser = user.getUsername();
                                        String idUser = user.getId();
                                        LatLng shop = new LatLng(latUser, lngUser);
                                        if (user.getImageURL().equals("default")) {
                                            mMap.addMarker(new MarkerOptions().position(shop).title(nameUser).snippet(idUser).icon(generateBitmapDescriptorFromRes(MapsActivity.this, R.mipmap.ic_store_foreground)));
                                        } else {
                                            ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(MapsActivity.this, R.color.colorPrimaryDark));
                                            Glide.with(getApplicationContext()).asBitmap().load(user.getImageURL()).override(100, 100).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).listener(new RequestListener<Bitmap>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                    mMap.addMarker(new MarkerOptions().position(shop).title(nameUser).snippet(idUser).icon(BitmapDescriptorFactory.fromBitmap(resource)));
                                                    return true;
                                                }
                                            })
                                                    .placeholder(cd)
                                                    .centerCrop()
                                                    .preload();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            }

                            //}
                        //}
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MapsActivity.this, "Spiacenti, sei fuori dall'area servita. Presto provvederemo ad allargare il raggio. A presto!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MapsActivity.this, MainActivity.class));
        }

    }

    // to change maps icon marker
    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // define action for back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(MapsActivity.this, MainActivity.class));
    }


}


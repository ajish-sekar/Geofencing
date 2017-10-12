package com.example.android.geofencing;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.R.attr.radius;
import static android.icu.lang.UCharacter.JoiningGroup.E;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Circle circle;
    private LatLng setLocation;
    private Button setButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setButton = (Button) findViewById(R.id.set_btn);
        try {


            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {

                        Log.v("Test","Location Available");
                        setLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(setLocation));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(setLocation));
                        circle = mMap.addCircle(new CircleOptions().center(setLocation).radius(500).strokeColor(Color.parseColor("#0d47a1")).fillColor(0x550000ff));


                    } else {
                        Log.v("Test", "GPS not working");

                    }
                }
            });
        }catch (SecurityException e){
            Log.v("Error", "Permission Denied");
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                setLocation = latLng;
                circle= mMap.addCircle(new CircleOptions().center(latLng).radius(500).strokeColor(Color.parseColor("#0d47a1")).fillColor(0x550000ff));
                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("latitude",setLocation.latitude);
                intent.putExtra("longitude",setLocation.longitude);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

   }

package com.example.android.geofencing;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.RemoteMessage;

import java.security.Permission;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> locations;
    private ArrayAdapter locationAdapter;
    private ListView listView;
    private Button clear;
    private Button set;
    private boolean isLocationEnabled;
    private GeofencingClient geofencingClient;
    private ArrayList<Geofence> geofences;
    PendingIntent mGeofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        if(!checkLocationEnabled()){
            enableLocationDialog();
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            createChannels();
        }
        geofences = new ArrayList<>();
        geofencingClient = LocationServices.getGeofencingClient(this);
        setContentView(R.layout.activity_main);
        locations = new ArrayList<>();
        listView = (ListView) findViewById(R.id.locations);
        clear = (Button) findViewById(R.id.reset_btn);
        set = (Button) findViewById(R.id.choose_btn);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, 101);

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setAdapter(null);
                locations = new ArrayList<String>();
                geofences = new ArrayList<Geofence>();
                geofencingClient.removeGeofences(mGeofencePendingIntent)
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.v("Test","Removed Geofences");
                            }
                        })
                        .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v("Test","Did not remove Geofences");
                            }
                        });

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==101 && resultCode == RESULT_OK){
            double lat = data.getDoubleExtra("latitude",0);
            double longitude = data.getDoubleExtra("longitude",0);
            locations.add("Latitiude: "+lat+" Longitude: "+longitude);
            locationAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, locations);
            listView.setAdapter(locationAdapter);
            geofences.add(new Geofence.Builder()
                    .setRequestId("Places")
                    .setCircularRegion(lat,longitude,500f)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()
            );
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofences);

            if(mGeofencePendingIntent==null) {
                Intent intent = new Intent(this, GeoFenceTransitions.class);
                mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                        FLAG_UPDATE_CURRENT);
            }
            try {
                geofencingClient.addGeofences(builder.build(), mGeofencePendingIntent)
                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences added
                                // ...
                                Log.v("Test","Geofences Added");
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to add geofences
                                // ...
                                Log.v("Test","Geofences Not Added");
                            }
                        });
            }catch (SecurityException e){
                Log.v("Test","Security Exception");
            }

        }
        if(requestCode == 150){

        }
    }

    private boolean checkLocationEnabled() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void enableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enable Location?")
                .setMessage("You need to enable location to show your location on map")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        );
                        startActivityForResult(intent,150);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isLocationEnabled = false;
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }
    @TargetApi(26)
    void createChannels(){
        NotificationChannel notificationChannel = new NotificationChannel("location.services","location alert", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.WHITE);
        notificationChannel.setShowBadge(true);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes.Builder audioAttributes = new AudioAttributes.Builder();
        audioAttributes.setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE);
        notificationChannel.setSound(alarmSound, audioAttributes.build());
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}

package com.example.android.geofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class GeoFenceTransitions extends IntentService {


    public GeoFenceTransitions() {
        super("GeoFenceTransitions");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent event = GeofencingEvent.fromIntent(intent);
            if (event.hasError()) {
                Log.v("Test", "Error");
                return;
            }
            int geofencingtransition = event.getGeofenceTransition();


            if (geofencingtransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v("Test", "Entered geofence");
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                    Notification.Builder builder = new Notification.Builder(GeoFenceTransitions.this,"location.services")
                            .setContentTitle("Alert")
                            .setContentText("You have entered the location")
                            .setSmallIcon(R.mipmap.ic_launcher_round);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1,builder.build());
                }
                else{
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(GeoFenceTransitions.this);
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("Alert");
                    builder.setContentText("You have entered the location");
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    builder.setSound(alarmSound);
                    builder.setLights(Color.WHITE, 3000, 3000);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(2, builder.build());
                }

            }
        }
    }

}

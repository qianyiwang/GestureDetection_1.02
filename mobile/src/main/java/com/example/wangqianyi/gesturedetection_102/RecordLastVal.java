package com.example.wangqianyi.gesturedetection_102;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by wangqianyi on 2016-10-04.
 */
public class RecordLastVal extends Service {

    LocationManager locationManager;
    SharedPreferences.Editor locationEditor;
    public static final String LAST_VALUE_FILE = "LastValueFile";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationEditor = getSharedPreferences(LAST_VALUE_FILE, MODE_PRIVATE).edit();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        float latitude=0;
        float longitude=0;
        latitude = (float) location.getLatitude();
        longitude = (float) location.getLongitude();
        Log.v("Last Lat", String.valueOf(latitude));
        Log.v("Last Lng", String.valueOf(longitude));
        locationEditor.putFloat("last_lat", latitude).commit();
        locationEditor.putFloat("last_lng", longitude).commit();
    }
}

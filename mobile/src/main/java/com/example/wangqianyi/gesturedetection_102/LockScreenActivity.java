package com.example.wangqianyi.gesturedetection_102;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.Locale;

public class LockScreenActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";

    private static final String WEAR_PATH = "/from-watch";
    Intent intent = new Intent(BROADCAST_ACTION);

    TextView locationText, placeText, placeAddress, placeType;
    MediaPlayer mediaPlayer;
    GoogleApiClient apiClient;
    int songIdx = 0;

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    String myProvider;
    Criteria myCriteria;
    float lat, lng;
    final String severUrl = "https://roads.googleapis.com/v1/snapToRoads?path=";
    final String YOUR_API_KEY = "AIzaSyDuXDG-AvGOkzV0xb9WVIENpJM7YB9jNeA";

    String placeInfo = "";
    String pAddress = "";
    String pType = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        locationText = (TextView) findViewById(R.id.location);
        placeText = (TextView)findViewById(R.id.place);
        placeAddress = (TextView)findViewById(R.id.address);
        placeType = (TextView)findViewById(R.id.type);
//        apiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build();
//        apiClient.connect();
//        Wearable.MessageApi.addListener(apiClient, this);//very important
        mediaPlayer = MediaPlayer.create(this, R.raw.kalimba);
//        mediaPlayer.start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = (float) location.getLatitude();
                lng = (float) location.getLongitude();
                locationText.setText("lat: " + lat + "lng: " + lng);
                mMap.clear();
                LatLng myPosition = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(myPosition));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15.0f));

                String msg = severUrl+lat+","+lng+"&interpolate=true&key="+YOUR_API_KEY;
//                Log.v("check url",msg);
//                RequestRoadID requestRoadID = new RequestRoadID(msg);
//                requestRoadID.execute();
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
        try{
            myProvider = locationManager.getBestProvider(myCriteria, true);
        }catch(Exception e){
            locationText.setText("Waiting For Location Signals Available");
        }

        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,5000,0, locationListener);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}

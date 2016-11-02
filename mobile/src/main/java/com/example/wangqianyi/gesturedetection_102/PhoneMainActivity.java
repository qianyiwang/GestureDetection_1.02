package com.example.wangqianyi.gesturedetection_102;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class PhoneMainActivity extends AppCompatActivity implements OnMapReadyCallback, OnConnectionFailedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    TextView locationText, placeText, placeAddress, placeType;
    MediaPlayer mediaPlayer;
    int songIdx = 0;
    private static final String WEAR_PATH = "/from-watch";

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    String myProvider;
    Criteria myCriteria;
    float lat, lng, carLat, carLng;
    final String severUrl = "https://roads.googleapis.com/v1/snapToRoads?path=";
    final String googlePlaceApiServerUrl = "https://maps.googleapis.com/maps/api/place/radarsearch/json?";
    final String YOUR_API_KEY = "AIzaSyC_UFTul4Ji-GUkPuF97qTHCx65k08fYNE";

    // google place api
    private GoogleApiClient mGoogleApiClient, apiClient;
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    Button myCarLocation, button_searchPlace, myLocation;
    BroadcastReceiver broadcastReceiver;

    boolean initialLocationManager;
    Spinner dropDown, searchRange;
    String[] item_type = new String[]{"gas_station", "car_repair", "car_rental"};
    String[] item_range = new String[]{"2000", "3500", "5000"};
    String searchType, searchRangeVal;
    int icon = R.mipmap.icon_person;
    SharedPreferences locationPref;
    public static final String LAST_VALUE_FILE = "LastValueFile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);

        locationPref = getSharedPreferences(LAST_VALUE_FILE, MODE_PRIVATE);
        carLat = locationPref.getFloat("last_lat",42);
        carLng = locationPref.getFloat("last_lng",-83);

        locationText = (TextView) findViewById(R.id.location);
        placeText = (TextView) findViewById(R.id.place);
        placeAddress = (TextView) findViewById(R.id.address);
        placeType = (TextView) findViewById(R.id.type);
        myCarLocation = (Button) findViewById(R.id.showPlaceInfo);
        myCarLocation.setOnClickListener(this);
        button_searchPlace = (Button) findViewById(R.id.searchPlace);
        button_searchPlace.setOnClickListener(this);
        myLocation = (Button)findViewById(R.id.myLocation);
        myLocation.setOnClickListener(this);
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

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // spinner
        dropDown = (Spinner)findViewById(R.id.searchType);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, item_type);
        dropDown.setAdapter(adapter);
        dropDown.setOnItemSelectedListener(this);

        searchRange = (Spinner)findViewById(R.id.searchRange);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, item_range);
        searchRange.setAdapter(adapter2);
        searchRange.setOnItemSelectedListener(this);

        // reset sdl
    }

    private void initialLocation() {
        if (!initialLocationManager) {
            initialLocationManager = true;
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

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = (float) location.getLatitude();
                    lng = (float) location.getLongitude();
                    locationText.setText("lat: " + lat + "lng: " + lng);
                    mMap.clear();
                    LatLng myPosition = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(myPosition).icon(BitmapDescriptorFactory.fromResource(icon)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17.0f));

                    String msg = severUrl + lat + "," + lng + "&interpolate=true&key=" + YOUR_API_KEY;
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

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 0, locationListener);
            try {
                myProvider = locationManager.getBestProvider(myCriteria, true);
            } catch (Exception e) {
                locationText.setText("Waiting For Location Signals Available");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(getBaseContext(), RecordLastVal.class));
//        showPlaceInfo();
        initialLocation();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String location = intent.getStringExtra("location");
//                Log.v("From request class", location);
                int commaIdx = location.indexOf(',');
                int colonIdx = location.indexOf(':');
                float lat = Float.parseFloat(location.substring(colonIdx + 1, commaIdx));
                float lng = Float.parseFloat(location.substring(commaIdx + 7, location.length() - 1));
                Log.v("lat lng", lat + "_" + lng);
                LatLng myPosition = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(myPosition).icon(BitmapDescriptorFactory.fromResource(icon)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(RequestPlace.BROADCAST_ACTION));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void showPlaceInfo() {
        // google place
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            float likeHood = 0;
            String placeInfo = "";
            String pAddress = "";
            String pType = "";

            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i("Place Info", String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));

                    if (placeLikelihood.getLikelihood() >= likeHood) {
                        likeHood = placeLikelihood.getLikelihood();
                        placeInfo = (String) placeLikelihood.getPlace().getName();
                        pAddress = (String) placeLikelihood.getPlace().getAddress();
                        pType = placeLikelihood.getPlace().getId();
                    }
                }
                likelyPlaces.release();
                placeText.setText(placeInfo);
                placeType.setText(pType);
                placeAddress.setText(pAddress);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myLocation:
                icon = R.mipmap.icon_person;
                initialLocation();
                showPlaceInfo();
                break;
            case R.id.showPlaceInfo:
                showPlaceInfo();
                mMap.clear();
                stopLocationUpdate();
                icon = R.mipmap.icon_car;
                LatLng myPosition = new LatLng(carLat, carLng);
                mMap.addMarker(new MarkerOptions().position(myPosition).icon(BitmapDescriptorFactory.fromResource(icon)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(carLat, carLng), 17.0f));
                break;
            case R.id.searchPlace:
                String msg = googlePlaceApiServerUrl + "location=" + lat + "," + lng + "&radius=" + searchRangeVal + "&type="+ searchType + "&key=" + YOUR_API_KEY;
                Log.v("check url", msg);
                if(searchType.equals("gas_station")){
                    icon = R.mipmap.icon_gas2;
                }
                else{
                    icon = R.mipmap.ic_launcher;
                }
                RequestPlace requestPlace = new RequestPlace(msg, this);
                requestPlace.execute();
                stopLocationUpdate();
                mMap.clear();
                break;
        }
    }

    private void stopLocationUpdate()
    {
        if(initialLocationManager){
            initialLocationManager = false;
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
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.searchType:
                searchType = item_type[position];
                break;
            case R.id.searchRange:
                searchRangeVal = item_range[position];
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

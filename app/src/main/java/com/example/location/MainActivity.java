package com.example.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements LocationListener {

    double latitude, longitude;
    TextView textView;

    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    LocationManager locationManager;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSIONS_ALL = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        textView = findViewById(R.id.textView);
        //to get location permission is requred
        if(Build.VERSION.SDK_INT >= 23){
            requestPermissions(PERMISSIONS,PERMISSIONS_ALL);
        }
        else{
            requestLocation(); //request location if permission is granted and also if device version is more then 23 which is 'mashmallow'
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void onOpenMapButtonClicked(View view) {
        // Open in Google_Map App
//        String locationUriString = "geo:" + latitude + "," + longitude + "?q=()@" + latitude + "," + longitude;
        // Open MapActivity
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lat", latitude);
        intent.putExtra("lon", longitude);
        startActivity(intent);
    }


    public void onGetMyLocationButtonClicked(View view) {
        // method to get the location
        getLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last location from FusedLocationClient object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Toast.makeText(MainActivity.this, "Last known location", Toast.LENGTH_SHORT).show();
                            textView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available, request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Toast.makeText(MainActivity.this, "Current location", Toast.LENGTH_SHORT).show();
            textView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // If everything is alright then
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Request_location function will be call here
            //Handler will be used to recall onlocation_update in time intevall
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestLocation();
                    handler.postDelayed(this,1000 * 10);//this will be call every 10 sec
                }
            },1000); //first time when the apps get the first location, it will take ~1 sec
            requestLocation(); //this is request location function that has been create down below
        }

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    //Request_location function
    public void requestLocation(){
        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        //will only be used if location permission is enabled
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,1000,this);
            }
        }
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //if user pause the app, the app will automaticly stop fetching uses locations
    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
    // Will be used every time when location is updated or change
    @Override
    public void onLocationChanged(@NonNull Location location) {

        Log.d("mylog", "Get Location" + location.getLatitude() + "," + location.getLongitude());
        //Will be show pop up on screen on location change taking and send to database
        Toast.makeText(this, "Get Location" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        //location will be removed
        //Which later will be need a handler function to recall location changed update once every sec or min as desired
        locationManager.removeUpdates(this);

    }
}

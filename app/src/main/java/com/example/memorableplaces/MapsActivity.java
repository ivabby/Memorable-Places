package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    public void centerMapOnLocation(Location location , String title){
        LatLng userLocation = new LatLng(location.getLatitude() , location.getLongitude());

        //  Clear the marker on map if any
        mMap.clear();

        if(!title.equals("Your location")) {
            //  Point to the current user location on map
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        }

        //  Zoom in to current user location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation , 10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0 , locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                centerMapOnLocation(lastKnownLocation , "Your location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        int position = intent.getIntExtra("placeNumber" , 0);
        Log.d(TAG, "onMapReady: position " + position);

        if(position == 0){
            //  Zoom in to user's location
            Log.d(TAG, "onMapReady: set new location ");

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
//                    centerMapOnLocation(location , "Your location");
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

            if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0 , locationListener);

                Location lastKnownLocation = getLastKnownLocation();
                Log.d(TAG, "onMapReady: " + lastKnownLocation);
                if(lastKnownLocation != null)
                    centerMapOnLocation(lastKnownLocation , "Your location");
            }
            else{
                ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , 0);
            }
        }
        else{
            Log.d(TAG, "onMapReady: setting marker ");

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(position).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(position).longitude);
            centerMapOnLocation(placeLocation , MainActivity.places.get(position));

        }


    }

    private Location getLastKnownLocation() {
//        loca = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /**
     * Add a new marker by long clicking at new place
     * @param latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();

        Geocoder geocoder = new Geocoder(getApplicationContext() , Locale.getDefault());
        String mapAddress = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Log.d(TAG, "onMapLongClick: size " + addresses.toString());

            if(addresses.size() > 0){
                if(addresses.get(0).getThoroughfare() != null)
                {
                    if(addresses.get(0).getSubThoroughfare() != null)
                    {
                        mapAddress+=addresses.get(0).getSubThoroughfare();
                    }

                    mapAddress+=addresses.get(0).getThoroughfare();
                    Log.d(TAG, "onMapLongClick: address " + mapAddress);
                }
            }
        } catch (Exception e){
            Log.e(TAG, "onMapLongClick: error " + e.getMessage() );
        }

        if(mapAddress.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("mm:HH yyyyMMdd");
            mapAddress = sdf.format(new Date());
        }

        Log.d(TAG, "onMapLongClick: address " + mapAddress);
        mMap.addMarker(new MarkerOptions().position(latLng).title(mapAddress));

        MainActivity.locations.add(latLng);
        MainActivity.places.add(mapAddress);

        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(this , "Location Saved" , Toast.LENGTH_SHORT).show();
    }
}

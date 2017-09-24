package com.example.alexander.reportecalle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GenerateActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{

    private Button btnGuardarReporte, btnCancelarReporte;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    double latitude, longitude;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        inicializar();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void inicializar ()
    {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        int Permission_All = 1;
        String[] Permissions = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, Permission_All);
        }


        createLocationRequest();

        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        btnGuardarReporte = (Button) findViewById(R.id.btnGuardarReportar);
        btnCancelarReporte = (Button) findViewById(R.id.btnCancelarReporte);

        btnGuardarReporte.setOnClickListener(this);
        btnCancelarReporte.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnGuardarReporte)
        {
            msg("Guardar reporte");
        }
        if (view == btnCancelarReporte)
        {
            msg("Cancelar reporte");
            finish();
        }
    }

    public void msg (String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void createLocationRequest()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(20000);
            mLocationRequest.setFastestInterval(10000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);
        String address = getAddress(getApplicationContext(), latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(sydney).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        //mMap.setMinZoomPreference(6.0f);
    }

    public String getAddress(Context context, Double latitude, Double longitude)
    {
        String fullAddress = null;
        try
        {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if (addresses.size() > 0)
            {
                Address address = addresses.get(0);
                fullAddress = address.getAddressLine(0);

                /*String Location = address.getLocality();
                String Zip = address.getPostalCode();
                String Country = address.getCountryName();*/
            }
        }catch (IOException ex) { ex.printStackTrace(); }

        return fullAddress;
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        if(mGoogleApiClient.isConnected())
        {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        stopLocationUpdate();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        stopLocationUpdate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
        if(mGoogleApiClient.isConnected())
        {
            stopLocationUpdate();
        }
    }

    protected void stopLocationUpdate()
    {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void startLocationUpdates()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null )
        {
            for (String permission: permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation != null)
            {
                mMap.clear();
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                mapFragment.getMapAsync(this);
            }
            if(mGoogleApiClient.isConnected())
            {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            mMap.clear();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}


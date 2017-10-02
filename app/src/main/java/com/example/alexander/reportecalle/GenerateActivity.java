package com.example.alexander.reportecalle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GenerateActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText edtComentario;
    private Button btnGuardarReporte, btnCancelarReporte;
    private ImageButton imgBtnReporte;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    double latitude, longitude;
    private LocationRequest mLocationRequest;
    private String address;

    private Integer REQUEST_CAMERA = 100, SELECT_FILE = 0;

    private DatabaseReference databaseReference;

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
        try
        {
            mAuth = FirebaseAuth.getInstance();

            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null)
                    {
                        finish();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                }
            };

            FirebaseUser user = mAuth.getCurrentUser();
        }catch (ExceptionInInitializerError error) {
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
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

        edtComentario = (EditText) findViewById(R.id.idtComentario_reporte);
        btnGuardarReporte = (Button) findViewById(R.id.btnGuardarReportar);
        btnCancelarReporte = (Button) findViewById(R.id.btnCancelarReporte);
        imgBtnReporte = (ImageButton) findViewById(R.id.imgBtnReporte);

        btnGuardarReporte.setOnClickListener(this);
        btnCancelarReporte.setOnClickListener(this);
        imgBtnReporte.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnGuardarReporte)
        {
            guardarInformacion();
        }
        if (view == btnCancelarReporte)
        {
            msg("Cancelado");
            finish();
        }
        if (view == imgBtnReporte)
        {
            selectImage();
        }
    }

    private void guardarInformacion()
    {
        try
        {
            String direccion = address;
            String comentario = edtComentario.getText().toString();
            //AGREGAR IMAGEN A LA BASE DE DATOS
            String imagen = "Imagen texto de prueba";

            ReportInformation reportInformation = new ReportInformation(direccion, comentario, imagen);

            FirebaseUser user = mAuth.getCurrentUser();

            databaseReference.child(user.getUid()).setValue(reportInformation);

            msg("Reporte guardado");
        }
        catch (Exception e)
        {
            msg("Surgio un error inesperado");
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
        address = getAddress(getApplicationContext(), latitude, longitude);

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
    public void onPointerCaptureChanged(boolean hasCapture) { }

    private void selectImage() {
        final CharSequence[] items = {"Camara", "Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(GenerateActivity.this);
        builder.setTitle("Agregar imagen");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @TargetApi(21)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        try {
                            Intent inteCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(inteCapture, REQUEST_CAMERA);
                        }catch (Exception e){ e.printStackTrace();}
                        break;
                    case 1:
                        Intent inteImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        inteImage.setType("image/*");
                        startActivityForResult(inteImage.createChooser(inteImage,"Seleccione una imagen"), SELECT_FILE);
                        break;
                    case 2:
                        dialogInterface.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_CAMERA)
            {
                Bundle bundle = data.getExtras();
                Bitmap bmp = (Bitmap) bundle.get("data");
                imgBtnReporte.setImageBitmap(bmp);
            }
            else if (requestCode == SELECT_FILE)
            {
                Uri selectedImage = data.getData();
                imgBtnReporte.setImageURI(selectedImage);
            }
        }
    }
}


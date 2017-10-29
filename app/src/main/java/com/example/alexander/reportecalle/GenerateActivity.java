package com.example.alexander.reportecalle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GenerateActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
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
    private String address, photo;
    private Uri selectedImage;
    private StorageReference storage;

    private ProgressDialog pDialog;

    private Integer REQUEST_CAMERA = 100, SELECT_FILE = 0;

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

        storage = FirebaseStorage.getInstance().getReference();
        edtComentario = (EditText) findViewById(R.id.idtComentario_reporte);
        btnGuardarReporte = (Button) findViewById(R.id.btnGuardarReportar);
        btnCancelarReporte = (Button) findViewById(R.id.btnCancelarReporte);
        imgBtnReporte = (ImageButton) findViewById(R.id.imgBtnReporte);

        btnGuardarReporte.setOnClickListener(this);
        btnCancelarReporte.setOnClickListener(this);
        imgBtnReporte.setOnClickListener(this);
        imgBtnReporte.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnGuardarReporte)
        {
            guardarInformacion();
            finish();
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

    @Override
    public boolean onLongClick(View view)
    {
        if (view == imgBtnReporte)
        {
            imgBtnReporte.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_a_photo));
            return true;
        }
        return false;
    }

    private void guardarInformacion()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final DatabaseReference reportesReference = database.getReference("basedatos");
        Time hoy = new Time(Time.getCurrentTimezone());
        hoy.setToNow();
        String fecha = ((hoy.monthDay < 10) ? "0" + hoy.monthDay : hoy.monthDay)+ "/" + (((hoy.month+1) < 10) ? "0" + (hoy.month+1) : (hoy.month+1)) + "/" + hoy.year;
        String hora = ((hoy.hour < 10) ? "0" + hoy.hour : hoy.hour)+ ":" + ((hoy.minute < 10) ? "0" + hoy.minute : hoy.minute) + ":" + ((hoy.second < 10) ? hoy.second + "0" : hoy.second);


        ReportInformation rinformation = new ReportInformation(user.getEmail(), address.trim(), edtComentario.getText().toString().trim(), photo, getResources().getString(R.string.txt_msg_nuevo), fecha, hora, FirebaseInstanceId.getInstance().getToken().toString());
        reportesReference.child("reporte").push().setValue(rinformation);

        StorageReference fillPath = storage.child("reporte").child("foto").child(photo);
        fillPath.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                msg("Acción exitosa. ¡Datos guardados!");
            }
        });
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
                selectedImage = data.getData();
                imgBtnReporte.setImageURI(selectedImage);
                photo = selectedImage.getLastPathSegment();
            }
        }
    }
}


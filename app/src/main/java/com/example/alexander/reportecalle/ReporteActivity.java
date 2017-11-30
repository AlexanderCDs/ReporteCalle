package com.example.alexander.reportecalle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ReporteActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    DatabaseReference repRef = db.child("reportes");

    private static final String TAG_FECHA = "fecha";
    private static final String TAG_HORA = "hora";
    private static final String TAG_ID = "id";
    private static final String TAG_ESTADO= "estado";
    private static final String TAG_COMENTARIO = "comentario";
    private static final String TAG_DIRECCION = "direccion";

    private TextView ttvFolioReporte, ttvEstadoReporte, ttvComentarioReporte, ttvFechaReporte, ttvHoraReporte, ttvDireccionReporte;
    private ImageView ievImagenReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        inicializar();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void inicializar()
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
        ttvFolioReporte = (TextView) findViewById(R.id.ttvFolio);
        ievImagenReporte = (ImageView) findViewById(R.id.ievImagen);
        ttvEstadoReporte = (TextView) findViewById(R.id.ttvEstado);
        ttvComentarioReporte = (TextView) findViewById(R.id.ttvComentario);
        ttvFechaReporte = (TextView) findViewById(R.id.ttvFecha);
        ttvHoraReporte = (TextView) findViewById(R.id.ttvHora);
        ttvDireccionReporte = (TextView) findViewById(R.id.ttvDireccion);
    }

    @Override
    protected void onStart(){
        super.onStart();
        msg(getIntent().getExtras().getString("folio"));
        repRef.child(getIntent().getExtras().getString("folio")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //For para recorrer cada dato de la base de datos de firebase
                for (DataSnapshot reporteSnapshot : dataSnapshot.getChildren()){
                    HashMap map = new HashMap();
                    ttvFolioReporte.setText(getResources().getString(R.string.folio)+ " " + reporteSnapshot.child(TAG_ID).getValue());
                    ttvEstadoReporte.setText(getResources().getString(R.string.estado)+ " " + reporteSnapshot.child(TAG_ESTADO).getValue());
                    ttvComentarioReporte.setText(getResources().getString(R.string.comentario)+ " " + reporteSnapshot.child(TAG_COMENTARIO).getValue());
                    ttvFechaReporte.setText(getResources().getString(R.string.fecha)+ " " + reporteSnapshot.child(TAG_FECHA).getValue());
                    ttvHoraReporte.setText(getResources().getString(R.string.hora)+ " " + reporteSnapshot.child(TAG_HORA).getValue());
                    ttvDireccionReporte.setText(getResources().getString(R.string.direccion)+ " " + reporteSnapshot.child(TAG_DIRECCION).getValue());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                msg( "Error en la lectura " + databaseError.getCode());
            }
        });
    }
    public void msg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}

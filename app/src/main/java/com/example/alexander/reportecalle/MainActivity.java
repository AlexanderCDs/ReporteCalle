package com.example.alexander.reportecalle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListView listaReportesUsuario;
    ArrayList<HashMap<String, String>> listaReportes;
    private TextView textView;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    DatabaseReference repRef = db.child("reportes");

    private static final String TAG_FECHA = "fecha";
    private static final String TAG_HORA = "hora";
    private static final String TAG_ID = "id";
    private static final String TAG_ESTADO= "estado";
    private static final String TAG_CORREO= "correo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializar();
    }

    public void inicializar()
    {
        listaReportes = new ArrayList<HashMap<String, String>>();
        listaReportesUsuario = (ListView) findViewById(R.id.ltvReportes);

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
            textView = (TextView) findViewById(R.id.ttvMailUser);

            FirebaseUser user = mAuth.getCurrentUser();

            textView.setText("Bienvenido " + user.getDisplayName());
        }catch (ExceptionInInitializerError error) {
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

    }

    @Override
    protected void onStart(){
        super.onStart();

        repRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = mAuth.getCurrentUser();
                //For para recorrer cada dato de la base de datos de firebase
                for (DataSnapshot reporteSnapshot : dataSnapshot.getChildren()){
                    //ReportInformation reporte = reporteSnapshot.getValue(ReportInformation.class);
                    if(reporteSnapshot.child(TAG_CORREO).getValue().equals(user.getEmail().toString()))
                    {
                        HashMap map = new HashMap();
                        map.put(TAG_ID, reporteSnapshot.child(TAG_ID).getValue());
                        map.put(TAG_ESTADO, reporteSnapshot.child(TAG_ESTADO).getValue());
                        map.put(TAG_FECHA, reporteSnapshot.child(TAG_FECHA).getValue());
                        map.put(TAG_HORA, reporteSnapshot.child(TAG_HORA).getValue());
                        listaReportes.add(map);
                    }
                }
                listaReportesUsuario.setAdapter(getAdapter(MainActivity.this));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                msg( "Error en la lectura " + databaseError.getCode());
            }
        });
    }

    public ListAdapter getAdapter(Context context)
    {
        ListAdapter adapter;
        String[] from = { TAG_ID, TAG_ESTADO, TAG_FECHA, TAG_HORA};
        int[] to = { R.id.ttvFolioReporte, R.id.ttvEstadoReporte, R.id.ttvFechaReporte, R.id.ttvHoraReporte};

        adapter = new SimpleAdapter(context, listaReportes, R.layout.item_list, from, to);

        return adapter;
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view)
    {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.app_bar_profile:
                finish();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                msg("Perfil");
                return true;
            case R.id.app_bar_logout:
                mAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                return true;
            case R.id.app_bar_reporte:
                startActivity(new Intent(getApplicationContext(), GenerateActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void msg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

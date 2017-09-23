package com.example.alexander.reportecalle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializar();

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
            textView = (TextView) findViewById(R.id.ttvMailUser);

            FirebaseUser user = mAuth.getCurrentUser();

            textView.setText("Bienvenido " + user.getDisplayName());
        }catch (ExceptionInInitializerError error) {
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

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

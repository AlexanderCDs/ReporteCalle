package com.example.alexander.reportecalle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText edtName, edtEmail, edtPass, edtPassConfirmation;
    private Button btnCancelar, btnGuardar;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        inicializar();

        btnCancelar.setOnClickListener(this);
        btnGuardar.setOnClickListener(this);

        //Accion de la flecha de regreso
        //android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void inicializar()
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

        edtName = (EditText) findViewById(R.id.edtFirstName);
        edtEmail = (EditText) findViewById(R.id.edtEmailProfile);
        edtPass = (EditText) findViewById(R.id.edtPasswordProfile);
        edtPassConfirmation = (EditText) findViewById(R.id.edtPasswordProfileConfirmation);

        btnGuardar = (Button) findViewById(R.id.btnSaveProfile);
        btnCancelar= (Button) findViewById(R.id.btnCancelarProfile);

        FirebaseUser user = mAuth.getCurrentUser();
        edtName.setText(user.getDisplayName());
        edtEmail.setText(user.getEmail());

        pDialog = new ProgressDialog(this);
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
    public void onClick(View view) {
        if (view == btnGuardar)
        {
            final String name = edtName.getText().toString();
            final String email = edtEmail.getText().toString();
            final String passNew = edtPass.getText().toString();
            final String passNewConf = edtPassConfirmation.getText().toString();
            final FirebaseUser user = mAuth.getInstance().getCurrentUser();
            pDialog.setMessage(getResources().getString(R.string.txt_actualizando));
            pDialog.show();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        msg(getResources().getString(R.string.txt_nombre) + " " +
                                getResources().getString(R.string.txt_update));

                        if(!passNew.equals("") && !passNewConf.equals("") && !passNew.equals(null) && !passNewConf.equals(null) )
                        {
                            if (passNew.equals(passNewConf))
                            {
                                user.updatePassword(passNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AuthCredential credential = EmailAuthProvider.getCredential(email, passNew);
                                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    msg(getResources().getString(R.string.txt_pass) + " " +
                                                            getResources().getString(R.string.txt_update));
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else
                            {
                                msg(getResources().getString(R.string.txt_msg_confirmacion));
                            }
                        }
                        pDialog.dismiss();
                    }
                }
            });
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        if (view == btnCancelar)
        {
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
    public void msg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

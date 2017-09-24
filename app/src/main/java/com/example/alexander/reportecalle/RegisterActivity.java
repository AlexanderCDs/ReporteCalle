package com.example.alexander.reportecalle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText edtMail, edtPass;
    private Button btnCrear, btnCancelar;

    private static final String TAG = "MyActivity";

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inicializar();

        btnCrear.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);

    }

    public void inicializar()
    {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    finish();
                    Intent next = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(next);
                }
                else
                {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        edtMail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtPassword);
        btnCrear = (Button) findViewById(R.id.btnCrearCuenta);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);
        pDialog = new ProgressDialog(this);
    }

    public void verificationEmailPass(String email, String password)
    {
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
        {
            msg(getResources().getString(R.string.txt_por_favor_ingrese) + " un " +
                    getResources().getString(R.string.txt_correo_electronico_contrasenia));
        }
        else
        {
            if(TextUtils.isEmpty(email))
            {
                msg(getResources().getString(R.string.txt_por_favor_ingrese) + " un " +
                        getResources().getString(R.string.txt_mail).toLowerCase());
            }
            else
            {
                if(TextUtils.isEmpty(password))
                {
                    msg(getResources().getString(R.string.txt_por_favor_ingrese) + " una " +
                            getResources().getString(R.string.txt_pass).toLowerCase());
                }
            }
        }
    }

    public void msg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void createAccount()
    {
        try
        {
            String email = edtMail.getText().toString().trim();
            String password = edtPass.getText().toString();
            verificationEmailPass(email, password);
            pDialog.setMessage("Registrando cuenta, por favor espere...");
            pDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                    if (task.isSuccessful())
                    {
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                    {
                        msg("Registro denegado");
                    }
                    pDialog.dismiss();
                }
            });
        }
        catch (Exception e)
        {
            pDialog.dismiss();
            msg(getResources().getString(R.string.txt_llene_campos_corretos));
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
    public void onClick(View view) {
        if (view == btnCrear)
        {
            createAccount();
        }
        if (view == btnCancelar)
        {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }
}

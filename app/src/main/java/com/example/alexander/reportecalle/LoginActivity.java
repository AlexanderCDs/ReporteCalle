package com.example.alexander.reportecalle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText edtMail, edtPass;
    private Button btnLogin;
    private TextView ttvCrear;

    private static final String TAG = "MyActivity";

    private ProgressDialog pDialog;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializar();

        btnLogin.setOnClickListener(this);
        ttvCrear.setOnClickListener(this);

    }



    public void inicializar()
    {
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            finish();
            Intent next = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(next);
        }

        edtMail = (EditText) findViewById(R.id.edtMail);
        edtPass = (EditText) findViewById(R.id.edtPass);
        ttvCrear = (TextView) findViewById(R.id.ttvRegistro);
        btnLogin = (Button) findViewById(R.id.btnIniciarSesion);
        pDialog = new ProgressDialog(this);
        dialog = new Dialog(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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

    public void signIn()
    {
        try
        {
            String email = edtMail.getText().toString().trim();
            String password = edtPass.getText().toString();
            verificationEmailPass(email, password);
            pDialog.setMessage(getResources().getString(R.string.txt_iniciando_sesion));
            pDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pDialog.dismiss();
                    if (task.isSuccessful())
                    {
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                    {
                        msg(getResources().getString(R.string.txt_correo_contrasenia_invalida));
                    }
                }
            });
        }
        catch (Exception e)
        {
            pDialog.dismiss();
            msg(getResources().getString(R.string.txt_llene_campos_corretos));//Cambiar el msg por uno adecuado
        }

    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void msg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if (view == btnLogin)
        {
            if (isNetworkAvailable(getApplicationContext()))
            {
                signIn();
            }
            else
            {
                msg(getResources().getString(R.string.txt_sin_conexion));
            }
        }
        if (view == ttvCrear)
        {
            finish();
            Intent next = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(next);
            //createAccount();
        }
    }
}


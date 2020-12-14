package com.example.recipefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.recipefirebase.ui.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /*----- XML Variables -----*/
    private TextView errorMessage;
    private EditText email;
    private EditText password;

    /*----- Variables -----*/
    private LoadingDialog loadingDialog;

    /*----- Database Variables -----*/
    private FirebaseAuth auth;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*---------- Initializing Database Instance ----------*/
        auth = FirebaseAuth.getInstance();

        /*---------- Checking Internet Connection ----------*/
        if (isConnected(this)) {
            showCustomDialog();
        } else {/*---------- Checking If User Is Already Logged In ----------*/
            FirebaseUser mFirebaseUser = auth.getCurrentUser();
            if (mFirebaseUser != null) {
                Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        }

        /*---------- Variables ----------*/
        loadingDialog = new LoadingDialog(this);

        /*---------- Hooks ----------*/
        Button loginButton = findViewById(R.id.loginButton);
        errorMessage = findViewById(R.id.errorMessageTextView);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);

        /*---------- Click Listeners ----------*/
        loginButton.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        /*---------- Checking Internet Connection ----------*/
        if (isConnected(this)) {
            showCustomDialog();
        } else {/*---------- Checking If User Is Already Logged In ----------*/
            FirebaseUser mFirebaseUser = auth.getCurrentUser();
            if (mFirebaseUser != null) {
                Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.loginButton) {
            loadingDialog.startLoadingDialog();
            loginUser(email.getText().toString(), password.getText().toString());
        }

    }//end of onClick

    /*---------- Connection User To Firebase ----------*/
    private void loginUser(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setVisibility(View.VISIBLE);
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    loadingDialog.dismissDialog();
                    Intent mainScreenIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(mainScreenIntent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismissDialog();
                    errorMessage.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    /*---------- Checking Internet Access ----------*/
    private boolean isConnected(LoginActivity loginActivity) {

        ConnectivityManager connectivityManager = (ConnectivityManager) loginActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return (wifiConn == null || !wifiConn.isConnected()) && (mobileConn == null || !mobileConn.isConnected());
        }
        return true;
    }

    /*---------- Asking User To Connect To The Internet Dialog ----------*/
    private void showCustomDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setMessage("Δεν υπάρχει σύνδεση στο διαδίκτυο")
                .setCancelable(false)
                .setPositiveButton("Συνδεση", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Εξοδος", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
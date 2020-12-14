package com.example.recipefirebase.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.recipefirebase.R;

public class LoadingDialog {

    /*----- Variables -----*/
    private Context context;
    private AlertDialog dialog;


    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_dialog, null);

        builder.setView(view);
        builder.setCancelable(false);


        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}

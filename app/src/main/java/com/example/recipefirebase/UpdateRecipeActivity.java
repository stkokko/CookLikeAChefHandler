package com.example.recipefirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.recipefirebase.adapter.UpdateRecyclerViewAdapter;
import com.example.recipefirebase.model.Recipe;

import java.util.ArrayList;

public class UpdateRecipeActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    /*----- Variables -----*/
    private UpdateRecyclerViewAdapter updateRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        /*----- Hooks -----*/
        RecyclerView updateRecyclerView = findViewById(R.id.update_recyclerView);
        ImageView returnIcon = findViewById(R.id.return_search);
        EditText searchEditText = findViewById(R.id.searchEditText);

        /*----- Bundle-Getting Recipes From Home -----*/
        Bundle bundle = getIntent().getExtras();
        ArrayList<Recipe> recipes;
        if (bundle != null) {
            recipes = (ArrayList<Recipe>) bundle.getSerializable("RecipeList");
        } else {
            recipes = new ArrayList<>();
        }

        /*----- Setting Up The Adapter -----*/
        updateRecyclerViewAdapter = new UpdateRecyclerViewAdapter(this, recipes);
        updateRecyclerView.setHasFixedSize(true);
        updateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateRecyclerView.setAdapter(updateRecyclerViewAdapter);

        /*----- Click Listener -----*/
        returnIcon.setOnClickListener(this);

        /*----- Watcher/EditText Listener -----*/
        searchEditText.addTextChangedListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageURI = data.getData();
            updateRecyclerViewAdapter.setImageUri(imageURI);
            Toast.makeText(this, R.string.select_image_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.return_search) {
            finish();
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateRecyclerViewAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
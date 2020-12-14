package com.example.recipefirebase;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.recipefirebase.adapter.DeleteRecyclerViewAdapter;
import com.example.recipefirebase.model.Recipe;


import java.util.ArrayList;
import java.util.List;

public class RemoveRecipeActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    /*----- Variables -----*/
    private DeleteRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_recipe);

        /*---------- Variables ----------*/
        List<Recipe> recipeList;

        /*----- Hooks -----*/
        ImageView return_icon = findViewById(R.id.return_search);
        EditText searchEditText = findViewById(R.id.searchEditText);
        RecyclerView recyclerView = findViewById(R.id.delete_recyclerView);


        /*---------- Bundle ----------*/
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recipeList = (ArrayList<Recipe>) bundle.getSerializable("RecipeList");
        } else {
            recipeList = new ArrayList<>();
        }

        /*---------- Setting Up Recycler View ----------*/
        adapter = new DeleteRecyclerViewAdapter(RemoveRecipeActivity.this, recipeList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RemoveRecipeActivity.this));
        recyclerView.setAdapter(adapter);

        /*---------- Click Listeners ----------*/
        return_icon.setOnClickListener(this);

        /*---------- Watcher/EditText Click Listener ----------*/
        searchEditText.addTextChangedListener(this);
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

        adapter.getFilter().filter(s);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
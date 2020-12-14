package com.example.recipefirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.recipefirebase.data.HttpRecipeAsyncResponse;
import com.example.recipefirebase.data.RecipeBankFirebase;
import com.example.recipefirebase.model.Recipe;
import com.example.recipefirebase.ui.LoadingDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;



import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_ADD_ACTIVITY = 100;

    /*----- Variables -----*/
    private List<String> recipeNameList;
    private List<Recipe> recipesList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        /*---------- Variables ----------*/
        recipeNameList = new ArrayList<>();
        final LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDialog();

        /*---------- Hooks ----------*/
        Button add = findViewById(R.id.add_button);
        Button remove = findViewById(R.id.remove_button);
        Button update = findViewById(R.id.update_button);
        Button logOut = findViewById(R.id.log_out_button);



        /*---------- Setting Up Recipe List(Retrieving Recipes From Database) ----------*/
        recipesList = new RecipeBankFirebase().getRecipes(new HttpRecipeAsyncResponse() {

            @Override
            public void precessFinishedRecipeList(ArrayList<Recipe> recipes) {

                for (Recipe recipe : recipes) {
                    recipeNameList.add(recipe.getName().toLowerCase());
                }

                loadingDialog.dismissDialog();
            }

            @Override
            public void processFinishedRecipeStorageUrl(String url) {

            }

        });

        /*---------- Click Listeners ----------*/
        add.setOnClickListener(this);
        remove.setOnClickListener(this);
        update.setOnClickListener(this);
        logOut.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        final LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDialog();

        /*---------- Getting Recipes Every Time We Are at Home Activity ----------*/
        recipesList = new RecipeBankFirebase().getRecipes(new HttpRecipeAsyncResponse() {

            @Override
            public void precessFinishedRecipeList(ArrayList<Recipe> recipes) {

                recipeNameList = new ArrayList<>();
                for (Recipe recipe : recipes) {
                    recipeNameList.add(recipe.getName().toLowerCase());
                }

                loadingDialog.dismissDialog();
            }

            @Override
            public void processFinishedRecipeStorageUrl(String url) {

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_ACTIVITY && resultCode == RESULT_OK) {
            Snackbar.make(findViewById(android.R.id.content), R.string.add_recipe_success, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.add_button) {
            Intent addRecipeIntent = new Intent(HomeActivity.this, AddRecipeActivity.class);
            addRecipeIntent.putExtra("RecipeNameList", (ArrayList<String>) recipeNameList);
            startActivityForResult(addRecipeIntent, REQUEST_CODE_ADD_ACTIVITY);
        } else if (v.getId() == R.id.remove_button) {
            Intent removeRecipeIntent = new Intent(HomeActivity.this, RemoveRecipeActivity.class);
            removeRecipeIntent.putExtra("RecipeList", (ArrayList<Recipe>) recipesList);
            startActivity(removeRecipeIntent);
        } else if (v.getId() == R.id.update_button) {
            Intent updateRecipeIntent = new Intent(HomeActivity.this, UpdateRecipeActivity.class);
            updateRecipeIntent.putExtra("RecipeList", (ArrayList<Recipe>) recipesList);
            startActivity(updateRecipeIntent);
        } else {
            FirebaseAuth.getInstance().signOut();
            Intent logInIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(logInIntent);
            finish();
        }

    }

}
package com.example.recipefirebase.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.recipefirebase.model.Ingredient;
import com.example.recipefirebase.model.Recipe;
import com.example.recipefirebase.model.RecipeCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class RecipeBankFirebase {

    /*----- Variables -----*/
    private ArrayList<Recipe> recipeArrayList;
    private String url;

    /*----- Getting Recipes From Firebase -----*/
    public List<Recipe> getRecipes(final HttpRecipeAsyncResponse callBack) {

        recipeArrayList = new ArrayList<>();
        /*----- Database Variables -----*/
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes");

        /*----- Event Listener For A Single Record -----*/
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsLanguage : snapshot.getChildren()) { /*----- For Every Language Record -----*/

                    Log.d("LANGUAGE", "onDataChange: " + dsLanguage.getKey());

                    for (DataSnapshot ds : dsLanguage.getChildren()) { /*----- For Every Recipe Record -----*/
                        Recipe recipe = new Recipe();
                        recipe.setName(ds.child("name").getValue(String.class));

                        ArrayList<Ingredient> ingredients = new ArrayList<>();
                        int counter = 0;
                        Ingredient ingredient = new Ingredient();
                        for (DataSnapshot ingredientDS : ds.child("ingredients").getChildren()) { /*----- Separating For Every Ingredient, Its Name And Quantity -----*/

                            /*----- When Counter Is Even, Ingredient Name Is Added  -----*/
                            if (counter % 2 == 0) {
                                ingredient.setName(ingredientDS.getValue(String.class));
                                counter++;
                                continue;
                            } else ingredient.setQuantity(ingredientDS.getValue(String.class));

                            /*----- When Counter Is Odd, Ingredient Quantity Is Added  -----*/
                            ingredients.add(ingredient);
                            counter++;
                            ingredient = new Ingredient();

                        }//end for ingredients
                        recipe.setIngredients(ingredients);

                        RecipeCategory recipeCategory = new RecipeCategory();
                        recipeCategory.setName(ds.child("category").getValue(String.class));
                        recipe.setCategory(recipeCategory);
                        recipe.setImage(ds.child("imageURL").getValue(String.class));
                        recipe.setDescription(ds.child("steps").getValue(String.class));
                        recipe.setLanguage(ds.child("language").getValue(String.class));
                        recipeArrayList.add(recipe);


                    }
                }
                if (callBack != null) callBack.precessFinishedRecipeList(recipeArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        return recipeArrayList;


    }

    /*----- Uploading Picture To Firebase -----*/
    public void uploadPicture(String name, Uri imageURI, final HttpRecipeAsyncResponse callBack) {

        /*----- Variables -----*/
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        final String key = name.substring(0, 1).toUpperCase() + name.substring(1).trim();
        final StorageReference riversRef = storageReference.child("images/" + key);

        /*----- Uploading Image To Firebase Storage  -----*/
        riversRef.putFile(imageURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                if (callBack != null) callBack.processFinishedRecipeStorageUrl(url);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                url = "";
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        url = "";
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    }
                });
    }

}

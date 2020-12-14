package com.example.recipefirebase.data;

import com.example.recipefirebase.model.Recipe;

import java.util.ArrayList;

public interface HttpRecipeAsyncResponse {
    void precessFinishedRecipeList(ArrayList<Recipe> recipes);

    void processFinishedRecipeStorageUrl(String url);

}

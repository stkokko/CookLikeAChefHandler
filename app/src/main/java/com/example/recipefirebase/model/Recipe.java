package com.example.recipefirebase.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {

    /*----- Variables -----*/
    private int id;
    private String name;
    private RecipeCategory category;
    private String image;
    private ArrayList<Ingredient> ingredients;
    private String description;
    private String language;


    public Recipe() {

    }

    public Recipe(String name, RecipeCategory category, String image, ArrayList<Ingredient> ingredients, String description, String language) {
        this.name = name;
        this.category = category;
        this.image = image;
        this.ingredients = ingredients;
        this.description = description;
        this.language = language;
    }

    public Recipe(int id, String name, RecipeCategory category, String image, ArrayList<Ingredient> ingredients, String description, String language) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.image = image;
        this.ingredients = ingredients;
        this.description = description;
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecipeCategory getCategory() {
        return category;
    }

    public void setCategory(RecipeCategory category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", image='" + image + '\'' +
                ", ingredients=" + ingredients +
                ", description='" + description +
                '}';
    }
}

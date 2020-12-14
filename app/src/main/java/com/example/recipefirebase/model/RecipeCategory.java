package com.example.recipefirebase.model;

import java.io.Serializable;

public class RecipeCategory implements Serializable {

    /*----- Variables -----*/
    private String name;

    public RecipeCategory() {
    }

    public RecipeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.example.recipefirebase.model;

import java.io.Serializable;

public class Ingredient implements Serializable {

    /*----- Variables -----*/
    private String quantity;
    private String name;

    public Ingredient() {

    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

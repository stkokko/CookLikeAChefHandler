package com.example.recipefirebase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipefirebase.R;

import java.util.ArrayList;


public class IngredientsRecyclerViewAdapter extends RecyclerView.Adapter<IngredientsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> ingredientArrayList;
    private Context context;


    public IngredientsRecyclerViewAdapter(ArrayList<String> ingredientArrayList, Context context) {
        this.ingredientArrayList = ingredientArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public IngredientsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.ingredient_row, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsRecyclerViewAdapter.ViewHolder holder, int position) {

        holder.ingredientTextView.setText(ingredientArrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return ingredientArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView ingredientTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientTextView = itemView.findViewById(R.id.ingredient_name_TextView);
        }


    }


}

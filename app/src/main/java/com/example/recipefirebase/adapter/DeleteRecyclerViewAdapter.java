package com.example.recipefirebase.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.recipefirebase.R;
import com.example.recipefirebase.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteRecyclerViewAdapter extends RecyclerView.Adapter<DeleteRecyclerViewAdapter.ViewHolder> implements Filterable {

    /*----- Variables -----*/
    private Context context;
    private List<Recipe> recipeList;
    private List<Recipe> recipesAll;
    RequestOptions options;

    /*----- PopUp -----*/
    private AlertDialog dialog;


    public DeleteRecyclerViewAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        this.recipesAll = new ArrayList<>(recipeList);

        options = new RequestOptions().centerCrop().placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background);
    }

    @NonNull
    @Override
    public DeleteRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*---------- Setting Up Recycler View Row ----------*/
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteRecyclerViewAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.recipeName.setText(recipe.getName());
        Glide.with(context).load(recipe.getImage()).apply(options).into(holder.recipeImageView);
        holder.recipeImageView.setAlpha(0.5f);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    /*---------- Search Filter ----------*/
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Recipe> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(recipesAll);
            } else {
                for (Recipe recipe : recipesAll) {
                    if (recipe.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(recipe);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;


            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            recipeList.clear();
            recipeList.addAll((Collection<? extends Recipe>) results.values);
            notifyDataSetChanged();

        }
    };

    /*---------- Deleting Recipe ----------*/
    private void deleteItem(final int adapterPosition, View v) {

        if (!checkDeleteAction(adapterPosition,v)) return;

        /*----- Pop Up -----*/
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirmation_popup, null);

        /*---------- Hooks ----------*/
        Button noButton = view.findViewById(R.id.conf_no_button);
        Button yesButton = view.findViewById(R.id.conf_yes_button);

        /*---------- Initializing Pop Up ----------*/
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

        /*---------- On Device Button Back, closing dialog ----------*/
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });

        /*---------- Click Listeners ----------*/
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference mDatabase;
                if (recipeList.get(adapterPosition).getLanguage().equalsIgnoreCase("gr"))
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes").child("Greek");
                else
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes").child("English");

                String recipeId = recipeList.get(adapterPosition).getName();
                String imageURL = recipeList.get(adapterPosition).getImage();
                mDatabase.child(recipeId).removeValue();
                dialog.dismiss();
                recipeList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);

                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

                storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
            }
        });

    }

    public boolean checkDeleteAction(int adapterPosition, View view) {
        int brunchCounter = 0;
        int saladsCounter = 0;
        int burgersCounter = 0;
        int dessertsCounter = 0;
        int mainDishesCounter = 0;

        for (Recipe recipe : recipeList) {
            switch (recipe.getCategory().getName()) {
                case "Brunch":
                    brunchCounter++;
                    break;
                case "Desserts":
                    dessertsCounter++;
                    break;
                case "Main Dishes":
                    mainDishesCounter++;
                    break;
                case "Burgers":
                    burgersCounter++;
                    break;
                case "Salads":
                    saladsCounter++;
                    break;
            }
        }

        switch (recipeList.get(adapterPosition).getCategory().getName()) {

            case "Brunch":
                if (brunchCounter == 4) {
                    Snackbar.make(view, "Αδύνατη διαγραφή. Ελάχιστο πλήθος 4.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                break;
            case "Desserts":
                if (dessertsCounter == 4) {
                    Snackbar.make(view, "Αδύνατη διαγραφή. Ελάχιστο πλήθος 4.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                break;
            case "Main Dishes":
                if (mainDishesCounter == 4) {
                    Snackbar.make(view, "Αδύνατη διαγραφή. Ελάχιστο πλήθος 4.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                break;
            case "Burgers":
                if (burgersCounter == 4) {
                    Snackbar.make(view, "Αδύνατη διαγραφή. Ελάχιστο πλήθος 4.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                break;
            case "Salads":
                if (saladsCounter == 4) {
                    Snackbar.make(view, "Αδύνατη διαγραφή. Ελάχιστο πλήθος 4.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                break;

        }

        return true;
    }

    /*---------- ViewHolder Class ----------*/
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView recipeName;
        public ImageView recipeImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipe_title_TextView);
            recipeImageView = itemView.findViewById(R.id.recipe_imageView);
            recipeImageView.setOnLongClickListener(this);
        }


        @Override
        public boolean onLongClick(View v) {
            deleteItem(getAdapterPosition(), v);
            return false;
        }
    }//end of ViewHolder class

}

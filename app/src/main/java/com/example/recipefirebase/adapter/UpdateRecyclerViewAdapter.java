package com.example.recipefirebase.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.recipefirebase.R;
import com.example.recipefirebase.data.HttpRecipeAsyncResponse;
import com.example.recipefirebase.data.RecipeBankFirebase;
import com.example.recipefirebase.model.Ingredient;
import com.example.recipefirebase.model.Recipe;
import com.example.recipefirebase.model.RecipeCategory;
import com.example.recipefirebase.ui.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class UpdateRecyclerViewAdapter extends RecyclerView.Adapter<UpdateRecyclerViewAdapter.ViewHolder> implements Filterable, View.OnClickListener, TextWatcher {

    /*----- Variables -----*/
    private Context context;
    private List<Recipe> recipeList;
    private List<Recipe> recipesAll;
    private ArrayList<String> recipeIngredients;
    private ArrayList<String> recipeNames;
    private ArrayAdapter<String> adapter;
    private int counter;
    private Recipe recipeItemAdapterPosition;
    private int recipePosition;
    private LoadingDialog loadingDialog;
    private Uri uri;
    private RequestOptions options;


    /* ----- PopUp Variables -----*/
    private AlertDialog dialog;

    /* ----- XML Variables -----*/
    private EditText recipeName;
    private EditText ingredientName;
    private EditText ingredientQuantity;
    private EditText recipeSteps;
    private Button updateButton;
    private Button nextButton;
    private Button previousButton;
    private Button addButton;
    private LinearLayout recipeNameLayout;
    private LinearLayout ingredientsLayout;
    private LinearLayout ingredientsQuantityLayout;
    private LinearLayout listViewLayout;
    private LinearLayout categoryLayout;
    private LinearLayout descriptionLayout;
    private ListView ingredientsListView;
    private View popUpdateView;

    /*----- Constructor -----*/
    public UpdateRecyclerViewAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        this.recipesAll = new ArrayList<>(recipeList);
        recipeNames = new ArrayList<>();
        loadingDialog = new LoadingDialog(context);

        for (Recipe recipe : recipeList) {
            recipeNames.add(recipe.getName());
        }

        counter = 1;

        options = new RequestOptions().centerCrop().placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background);
    }


    @NonNull
    @Override
    public UpdateRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*---------- Setting Up Recycler View Row ----------*/
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_row, parent, false);
        return new UpdateRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateRecyclerViewAdapter.ViewHolder holder, int position) {
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
    public void onClick(View v) {

        if (v.getId() == R.id.next_popup_button) {

            if (recipeName.getText().toString().trim().isEmpty()) {
                Snackbar.make(v, R.string.empty_name, Snackbar.LENGTH_LONG).show();
            } else if (recipeNames.contains(recipeName.getText().toString().toLowerCase().trim()) && !recipeName.getText().toString().trim().equalsIgnoreCase(recipeItemAdapterPosition.getName())) {
                Snackbar.make(v, R.string.name_exists_message, Snackbar.LENGTH_LONG).show();
            } else if (recipeIngredients.size() == 0 && counter == 2) {

                if (ingredientName.getText().toString().trim().isEmpty() && ingredientQuantity.getText().toString().trim().isEmpty()) {
                    Snackbar.make(v, R.string.empty_ingredient, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(v, R.string.empty_ingredient_list, Snackbar.LENGTH_LONG).show();
                }

            } else {
                counter++;
                if (counter == 2) { /*---------- Ingredients Layout ----------*/

                    /*---------- Setting Up The Adapter ----------*/
                    adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, recipeIngredients);
                    ingredientsListView.setAdapter(adapter);

                    recipeNameLayout.setVisibility(View.GONE);
                    ingredientsLayout.setVisibility(View.VISIBLE);
                    ingredientsQuantityLayout.setVisibility(View.VISIBLE);
                    listViewLayout.setVisibility(View.VISIBLE);
                    previousButton.setVisibility(View.VISIBLE);
                } else if (counter == 3) { /*---------- Category Layout ----------*/
                    String category = recipeItemAdapterPosition.getCategory().getName();

                    if (category.trim().equalsIgnoreCase("Brunch")) {
                        RadioButton brunchRadioButton = popUpdateView.findViewById(R.id.brunch_radio_button);
                        brunchRadioButton.setChecked(true);
                    } else if (category.trim().equalsIgnoreCase("Salads")) {
                        RadioButton brunchRadioButton = popUpdateView.findViewById(R.id.salads_radio_button);
                        brunchRadioButton.setChecked(true);
                    } else if (category.trim().equalsIgnoreCase("Main Dishes")) {
                        RadioButton brunchRadioButton = popUpdateView.findViewById(R.id.main_dishes_radio_button);
                        brunchRadioButton.setChecked(true);
                    } else if (category.trim().equalsIgnoreCase("Burgers")) {
                        RadioButton brunchRadioButton = popUpdateView.findViewById(R.id.burgers_radio_button);
                        brunchRadioButton.setChecked(true);
                    } else if (category.trim().equalsIgnoreCase("Desserts")) {
                        RadioButton brunchRadioButton = popUpdateView.findViewById(R.id.desserts_radio_button);
                        brunchRadioButton.setChecked(true);
                    }

                    ingredientsLayout.setVisibility(View.GONE);
                    ingredientsQuantityLayout.setVisibility(View.GONE);
                    listViewLayout.setVisibility(View.GONE);

                    categoryLayout.setVisibility(View.VISIBLE);
                } else if (counter == 4) { /*---------- Steps Layout ----------*/
                    recipeSteps.setText(recipeItemAdapterPosition.getDescription());

                    categoryLayout.setVisibility(View.GONE);
                    descriptionLayout.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                    updateButton.setVisibility(View.VISIBLE);
                }//end if/else/if
            }

        } else if (v.getId() == R.id.update_popup_button) {

            if (recipeSteps.getText().toString().trim().isEmpty()) { /*---------- Checking If Recipe Steps Is Empty ----------*/
                Snackbar.make(v, R.string.empty_description, Snackbar.LENGTH_LONG).show();
            } else {

                /*---------- Start Loading Dialog ----------*/
                loadingDialog.startLoadingDialog();


                final HashMap<String, Object> recipeMap = new HashMap<>();
                final Recipe recipe = new Recipe();
                final String id;

                final DatabaseReference mDatabase;
                if (recipeList.get(recipePosition).getLanguage().equalsIgnoreCase("gr")) {
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes").child("Greek");
                    recipe.setLanguage("GR");
                } else {
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes").child("English");
                    recipe.setLanguage("EN");
                }


                if (!recipeName.getText().toString().trim().equalsIgnoreCase(recipeItemAdapterPosition.getName())) { /*---------- If We Change The Recipe Name Then Remove The Old One (Recipe) ----------*/
                    mDatabase.child(recipeList.get(recipePosition).getName()).removeValue();
                }
                id = recipeName.getText().toString().substring(0, 1).toUpperCase() + recipeName.getText().toString().substring(1).trim();
                recipe.setName(id);
                String url;

                if (uri != null) { /*---------- If Picture Is Not NULL ----------*/
                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(recipeList.get(recipePosition).getImage());

                    storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });

                    /*---------- Uploading Image On Firebase Storage ----------*/
                    new RecipeBankFirebase().uploadPicture(recipeName.getText().toString(), uri, new HttpRecipeAsyncResponse() {

                        @Override
                        public void precessFinishedRecipeList(ArrayList<Recipe> recipes) {

                        }

                        @Override
                        public void processFinishedRecipeStorageUrl(String url) {

                            if (url != null) {
                                updateRecipe(url, recipeMap, recipe, mDatabase, id);
                                dialog.dismiss();
                                loadingDialog.dismissDialog();
                            } else {
                                Snackbar.make(popUpdateView, R.string.error_message, Snackbar.LENGTH_LONG);
                            }

                        }

                    });

                    uri = null;
                } else { /*---------- If Its The Same Image ----------*/
                    url = recipeList.get(recipePosition).getImage();
                    updateRecipe(url, recipeMap, recipe, mDatabase, id);
                    dialog.dismiss(); //closing popup
                    loadingDialog.dismissDialog();
                }

            }

        } else if (v.getId() == R.id.previous_popup_button) {
            counter--;

            if (counter == 1) { /*---------- Name Layout ----------*/
                recipeNameLayout.setVisibility(View.VISIBLE);
                ingredientsLayout.setVisibility(View.GONE);
                ingredientsQuantityLayout.setVisibility(View.GONE);
                listViewLayout.setVisibility(View.GONE);
                previousButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
            } else if (counter == 2) { /*---------- Ingredients Layout ----------*/

                categoryLayout.setVisibility(View.GONE);
                ingredientsLayout.setVisibility(View.VISIBLE);
                ingredientsQuantityLayout.setVisibility(View.VISIBLE);
                listViewLayout.setVisibility(View.VISIBLE);

            } else if (counter == 3) { /*---------- Category Layout ----------*/
                descriptionLayout.setVisibility(View.GONE);
                categoryLayout.setVisibility(View.VISIBLE);
                updateButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
            }

        } else if (v.getId() == R.id.exit_popup) {
            closeDialog();
        } else if (v.getId() == R.id.select_image_button) {
            choosePicture();
        } else if (v.getId() == R.id.add_button_popup) {
            addIngredient();
        }
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (ingredientName.getText().toString().isEmpty() || ingredientQuantity.getText().toString().isEmpty()) {
            disableButton(addButton);
        } else {
            enableButton(addButton);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @SuppressLint("InflateParams")
    private void updateItem(int adapterPosition) {
        counter = 1; //Important

        /*----- Pop Up Build Set Up -----*/
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        popUpdateView = inflater.inflate(R.layout.popup_update_recipe, null);

        /*----- XML PopUp Variables-Hooks -----*/
        ImageView exitPopupImageView = popUpdateView.findViewById(R.id.exit_popup);
        recipeName = popUpdateView.findViewById(R.id.recipe_name_popup);
        ingredientName = popUpdateView.findViewById(R.id.ingredients_popup);
        ingredientQuantity = popUpdateView.findViewById(R.id.ingredients_quantity_popup);
        recipeSteps = popUpdateView.findViewById(R.id.description_popup);
        ingredientsListView = popUpdateView.findViewById(R.id.ingredients_listview);
        updateButton = popUpdateView.findViewById(R.id.update_popup_button);
        Button choosePictureButton = popUpdateView.findViewById(R.id.select_image_button);
        nextButton = popUpdateView.findViewById(R.id.next_popup_button);
        previousButton = popUpdateView.findViewById(R.id.previous_popup_button);
        addButton = popUpdateView.findViewById(R.id.add_button_popup);

        /*----- Layout Hooks -----*/
        recipeNameLayout = popUpdateView.findViewById(R.id.recipe_name_layout);
        ingredientsLayout = popUpdateView.findViewById(R.id.ingredients_layout);
        ingredientsQuantityLayout = popUpdateView.findViewById(R.id.ingredients_quantity_layout);
        listViewLayout = popUpdateView.findViewById(R.id.listview_layout);
        categoryLayout = popUpdateView.findViewById(R.id.category_layout);
        descriptionLayout = popUpdateView.findViewById(R.id.description_layout);

        /*----- Event listeners -----*/
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);
        choosePictureButton.setOnClickListener(this);
        exitPopupImageView.setOnClickListener(this);
        addButton.setOnClickListener(this);
        ingredientName.addTextChangedListener(this);
        ingredientQuantity.addTextChangedListener(this);

        /*----- Disable Button -----*/
        disableButton(addButton);

        /*----- Getting Selected Recipe -----*/
        recipeItemAdapterPosition = recipeList.get(adapterPosition);
        recipePosition = adapterPosition;

        /*----- Set Recipe Name PopUp -----*/
        recipeName.setText(recipeItemAdapterPosition.getName());

        /*----- Initializing PopUp -----*/
        builder.setView(popUpdateView);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

        /*---------- On Device Button Back, closing dialog ----------*/
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    closeDialog();
                    return true;
                } else {
                    return false;
                }
            }
        });

        /*---------- OnLongClickListener PopUp Dialog For Deleting Ingredient From ListView  ----------*/
        ingredientsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builderConfirm = new AlertDialog.Builder(view.getContext());
                LayoutInflater inflaterConfirm = LayoutInflater.from(view.getContext());
                final AlertDialog dialogConfirm;

                View confirmationView = inflaterConfirm.inflate(R.layout.confirmation_popup, null);

                Button noButton = confirmationView.findViewById(R.id.conf_no_button);
                Button yesButton = confirmationView.findViewById(R.id.conf_yes_button);

                builderConfirm.setView(confirmationView);
                builderConfirm.setCancelable(false);
                dialogConfirm = builderConfirm.create();
                dialogConfirm.show();

                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogConfirm.dismiss();

                    }
                });

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogConfirm.dismiss();
                        recipeIngredients.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });

                return true;
            }
        });

    }

    /*---------- Update Recipe Reference On Firebase ----------*/
    public void updateRecipe(String url, HashMap<String, Object> recipeMap, Recipe recipe, DatabaseReference mDatabase, String id) {
        recipeMap.put("name", id);

        HashMap<String, Object> ingredients = new HashMap<>();
        ArrayList<Ingredient> newRecipeIngredientsArrayList = new ArrayList<>();
        for (int i = 0; i < recipeIngredients.size(); i++) {
            Ingredient ingredient = new Ingredient();
            /*---------- We Have To Obtain Both Ingredient Name And Quantity From One String Which Hold Them Both ----------*/
            int openParenthesis = recipeIngredients.get(i).indexOf("(");
            int closeParenthesis = recipeIngredients.get(i).indexOf(")");
            String ingredientName = recipeIngredients.get(i).substring(0, openParenthesis); /*---------- Obtaining Name ----------*/
            String ingredientQuantity = recipeIngredients.get(i).substring(openParenthesis + 1, closeParenthesis); /*---------- Obtaining Quantity ----------*/
            ingredients.put(i + "_name", ingredientName.trim());
            ingredients.put(i + "_quantity", ingredientQuantity.trim());
            ingredient.setName(ingredientName.trim());
            ingredient.setQuantity(ingredientQuantity.trim());
            newRecipeIngredientsArrayList.add(ingredient);
        }

        recipeMap.put("ingredients", ingredients);
        recipe.setIngredients(newRecipeIngredientsArrayList);

        RadioGroup recipeCategoryRadioGroup = popUpdateView.findViewById(R.id.category_radio_group);
        RadioButton radioButton = popUpdateView.findViewById(recipeCategoryRadioGroup.getCheckedRadioButtonId());
        String category = radioButton.getText().toString().trim();
        RecipeCategory recipeCategory = new RecipeCategory();
        recipeCategory.setName(category);


        recipeMap.put("category", category);
        recipe.setCategory(recipeCategory);
        recipeMap.put("steps", recipeSteps.getText().toString().trim());
        recipe.setDescription(recipeSteps.getText().toString().trim());
        recipeMap.put("imageURL", url);
        recipeMap.put("language", recipe.getLanguage());
        recipeMap.put("timestamp", ServerValue.TIMESTAMP);
        recipe.setImage(url);


        mDatabase.child(id).updateChildren(recipeMap);

        /*---------- Updating Each And Every List That Its Related To Recipe ----------*/
        recipeList.remove(recipePosition);
        recipeNames.remove(recipePosition);
        recipeList.add(recipe);
        recipeNames.add(recipe.getName());
        notifyItemChanged(recipePosition);
        notifyItemRangeChanged(recipePosition, recipeList.size());
    }

    /*---------- Adding Ingredient to ListView ----------*/
    private void addIngredient() {
        if (recipeIngredients.contains(ingredientName.getText().toString() + " (" + ingredientQuantity.getText().toString() + ")")) {
            Snackbar.make(popUpdateView, R.string.ingredient_exists, Snackbar.LENGTH_LONG).show();
            return;
        }


        recipeIngredients.add(ingredientName.getText().toString() + " (" + ingredientQuantity.getText().toString() + ")");
        adapter.notifyDataSetChanged();
    }

    /*---------- Opens Select Image Activity ----------*/
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(intent, 1);

    }

    public void closeDialog() {
        dialog.dismiss();
        counter = 1;
    }

    public void setImageUri(Uri uri) {
        this.uri = uri;
    }

    /*---------- Disabling Buttons ----------*/
    private void disableButton(Button button) {
        button.setClickable(false);
        button.setBackgroundColor(0x66FF0D0D);
    }

    /*---------- Enabling Buttons ----------*/
    private void enableButton(Button button) {
        button.setClickable(true);
        button.setBackgroundColor(0xFFFF0D0D);
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
            recipeIngredients = new ArrayList<>();

            for (Ingredient ingredient : recipeList.get(getAdapterPosition()).getIngredients()) {
                recipeIngredients.add(ingredient.getName() + " (" + ingredient.getQuantity() + ")");
            }


            updateItem(getAdapterPosition());
            return true;
        }
    }//end of ViewHolder class

}

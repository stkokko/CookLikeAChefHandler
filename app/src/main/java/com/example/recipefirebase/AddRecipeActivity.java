package com.example.recipefirebase;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.example.recipefirebase.adapter.IngredientsRecyclerViewAdapter;
import com.example.recipefirebase.data.HttpRecipeAsyncResponse;
import com.example.recipefirebase.data.RecipeBankFirebase;
import com.example.recipefirebase.model.Recipe;
import com.example.recipefirebase.ui.LoadingDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AddRecipeActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    /*----- XML Variables -----*/
    private EditText name;
    private EditText ingredient;
    private EditText quantity;
    private EditText steps;
    private RadioButton category;
    private RadioGroup radioGroup;
    private Button addIngredientButton;
    private Button resumeRecipeButton;
    private Button nextButton;
    private Button previousButton;
    private LinearLayout linearLayoutName;
    private LinearLayout linearLayoutIngredient;
    private LinearLayout linearLayoutListView;
    private LinearLayout linearLayoutCategory;
    private LinearLayout linearLayoutSteps;
    private RecyclerView ingredientsRecyclerView;

    /*----- XML PopUp Views -----*/
    private View popupView;
    private Button popupSaveButton;
    private Button imageButton;

    /*----- Variables -----*/
    private IngredientsRecyclerViewAdapter ingredientsRecyclerViewAdapter;
    private ArrayList<String> ingredientsArrayList;
    private ArrayList<String> ingredientQuantityList;
    private ArrayList<String> recipeIngredients;
    private int counter;
    private AlertDialog dialog;
    private Uri imageURI;
    private List<String> recipeNameList;
    private LoadingDialog loadingDialog;
    private String language;
    private String deletedIngredient;

    /*----- Database Variables -----*/
    private DatabaseReference mDatabase;

    /*----- Swipe Variables -----*/
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();
            deletedIngredient = ingredientsArrayList.get(position);
            ingredientsArrayList.remove(position);
            ingredientsRecyclerViewAdapter.notifyDataSetChanged();
            Snackbar.make(ingredientsRecyclerView, deletedIngredient + " deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ingredientsArrayList.add(deletedIngredient);
                            enableButton(nextButton);
                            ingredientsRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    }).show();

            if (ingredientsArrayList.size() == 0) disableButton(nextButton);

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightBackgroundColor(Color.RED)
                    .create()
                    .decorate();


            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        /*---------- Variables Setup ----------*/
        counter = 1;
        loadingDialog = new LoadingDialog(AddRecipeActivity.this);
        ingredientsArrayList = new ArrayList<>();
        recipeIngredients = new ArrayList<>();
        ingredientQuantityList = new ArrayList<>();


        /*---------- Hooks ----------*/
        name = findViewById(R.id.recipe_name);
        ingredient = findViewById(R.id.ingredient_name);
        quantity = findViewById(R.id.quantity_editText);
        radioGroup = findViewById(R.id.category_radio_group);
        steps = findViewById(R.id.steps);
        addIngredientButton = findViewById(R.id.add_ingredient_button);
        resumeRecipeButton = findViewById(R.id.resume_recipe_button);
        nextButton = findViewById(R.id.next_button);
        previousButton = findViewById(R.id.previous_button);
        ingredientsRecyclerView = findViewById(R.id.ingredients_recyclerView);
        linearLayoutName = findViewById(R.id.linear_layout_name);
        linearLayoutIngredient = findViewById(R.id.linear_layout_ingredient);
        linearLayoutListView = findViewById(R.id.linear_layout_listview);
        linearLayoutCategory = findViewById(R.id.linear_layout_category);
        linearLayoutSteps = findViewById(R.id.linear_layout_steps);

        /*---------- Recycler View Set Up ----------*/
        ingredientsRecyclerViewAdapter = new IngredientsRecyclerViewAdapter(recipeIngredients, this);
        ingredientsRecyclerView.setHasFixedSize(true);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setAdapter(ingredientsRecyclerViewAdapter);

        ImageView returnIcon = findViewById(R.id.return_icon);


        /*---------- Bundle ----------*/
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recipeNameList = bundle.getStringArrayList("RecipeNameList");
        }

        /*---------- Click Listeners ----------*/
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        addIngredientButton.setOnClickListener(this);
        //deleteIngredientButton.setOnClickListener(this);
        resumeRecipeButton.setOnClickListener(this);
        returnIcon.setOnClickListener(this);

        /*---------- Watch/EditText Listeners ----------*/
        ingredient.addTextChangedListener(this);
        quantity.addTextChangedListener(this);

        /*---------- Firebase Instance  Setup ----------*/
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*---------- Swipe Recycler View Set Up ----------*/
        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleCallback);
        touchHelper.attachToRecyclerView(ingredientsRecyclerView);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            enableButton(popupSaveButton);
            Snackbar.make(popupView, R.string.select_image_success, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.next_button) {

            if (name.getText().toString().isEmpty()) {
                Snackbar.make(v, R.string.empty_name, Snackbar.LENGTH_LONG).show();
            } else if (recipeNameList.contains(name.getText().toString().toLowerCase().trim())) {
                Snackbar.make(v, R.string.name_exists, Snackbar.LENGTH_LONG).show();
            } else if (recipeIngredients.size() == 0 && counter == 2) {
                Snackbar.make(v, R.string.empty_ingredient_list, Snackbar.LENGTH_LONG).show();
            } else if (radioGroup.getCheckedRadioButtonId() == -1 && counter == 3) {
                Snackbar.make(v, R.string.empty_category, Snackbar.LENGTH_LONG).show();
            } else {
                counter++;
                if (counter == 2) { /*---------- Ingredients Layout ----------*/

                    linearLayoutName.setVisibility(View.GONE);
                    linearLayoutIngredient.setVisibility(View.VISIBLE);
                    linearLayoutListView.setVisibility(View.VISIBLE);
                    previousButton.setVisibility(View.VISIBLE);
                } else if (counter == 3) { /*---------- Category Layout ----------*/
                    linearLayoutIngredient.setVisibility(View.GONE);
                    linearLayoutListView.setVisibility(View.GONE);

                    linearLayoutCategory.setVisibility(View.VISIBLE);
                } else if (counter == 4) { /*---------- Steps Layout ----------*/

                    int selectedRadioButton = radioGroup.getCheckedRadioButtonId();
                    category = findViewById(selectedRadioButton);

                    linearLayoutCategory.setVisibility(View.GONE);
                    linearLayoutSteps.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                    resumeRecipeButton.setVisibility(View.VISIBLE);
                }//end if/else/if
            }

        } else if (v.getId() == R.id.previous_button) {

            counter--;

            if (counter == 1) { /*---------- Name Layout ----------*/
                linearLayoutName.setVisibility(View.VISIBLE);
                linearLayoutIngredient.setVisibility(View.GONE);
                linearLayoutListView.setVisibility(View.GONE);
                previousButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
            } else if (counter == 2) { /*---------- Ingredients Layout ----------*/

                linearLayoutCategory.setVisibility(View.GONE);
                linearLayoutIngredient.setVisibility(View.VISIBLE);
                linearLayoutListView.setVisibility(View.VISIBLE);
            } else if (counter == 3) { /*---------- Category Layout ----------*/
                linearLayoutSteps.setVisibility(View.GONE);
                linearLayoutCategory.setVisibility(View.VISIBLE);
                resumeRecipeButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
            }

        } else if (v.getId() == R.id.add_ingredient_button) {
            /*---------- Adding Ingredient To ListView ----------*/
            addIngredient(v);

        } else if (v.getId() == R.id.resume_recipe_button) {

            /*---------- Recipe Sum up via pop up ----------*/
            if (steps.getText().toString().isEmpty() && counter == 4) { /*---------- Checking If Description is empty ----------*/
                Snackbar.make(v, R.string.empty_description, Snackbar.LENGTH_LONG).show();
            } else {
                /*---------- Creating/Showing Summed Up Recipe ----------*/
                createPopupDialog();
            }
        } else if (v.getId() == R.id.return_icon) {
            finish();
        } else if (v.getId() == R.id.save_popup_button) {
            /*---------- Saving Recipe ----------*/
            loadingDialog.startLoadingDialog();
            saveRecipe();
        } else if (v.getId() == R.id.exit_popup) {
            dialog.dismiss();
        } else if (v.getId() == R.id.select_image_button) {
            /*---------- Selecting Picture From Device ----------*/
            choosePicture();
        } else if (v.getId() == R.id.greekRadioButton || v.getId() == R.id.englishRadioButton) {
            imageButton.setBackgroundColor(Color.DKGRAY);
            imageButton.setClickable(true);
            language = v.getId() == R.id.greekRadioButton ? "GR" : "EN";
        }

    }//end of onClick

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (ingredient.getText().toString().isEmpty() || quantity.getText().toString().isEmpty()) {
            disableButton(addIngredientButton);
        } else {
            enableButton(addIngredientButton);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    /*---------- Opens Select Image Activity ----------*/
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }


    /*---------- Saving Recipe on Firebase ----------*/
    private void saveRecipe() {

        new RecipeBankFirebase().uploadPicture(name.getText().toString(), imageURI, new HttpRecipeAsyncResponse() {

            @Override
            public void precessFinishedRecipeList(ArrayList<Recipe> recipes) {

            }

            @Override
            public void processFinishedRecipeStorageUrl(String url) {

                /*---------- First We Save Image to Firebase and get image url
                             and then uploading the rest of the recipe ----------*/
                if (url != null) {
                    HashMap<String, Object> recipeMap = new HashMap<>();
                    String id = name.getText().toString().substring(0, 1).toUpperCase() + name.getText().toString().substring(1).trim();
                    recipeMap.put("name", id);

                    HashMap<String, Object> ingredients = new HashMap<>();
                    for (int i = 0; i < ingredientsArrayList.size(); i++) {
                        ingredients.put(i + "_name", ingredientsArrayList.get(i).trim());
                        ingredients.put(i + "_quantity", ingredientQuantityList.get(i).trim());
                    }

                    recipeMap.put("ingredients", ingredients);
                    recipeMap.put("category", category.getText().toString().trim());
                    recipeMap.put("steps", steps.getText().toString().trim());
                    recipeMap.put("imageURL", url);
                    recipeMap.put("language", language);
                    recipeMap.put("timestamp", ServerValue.TIMESTAMP);


                    if (language.equals("GR")) {
                        mDatabase.child("Recipes").child("Greek").child(id).updateChildren(recipeMap);
                    } else {
                        mDatabase.child("Recipes").child("English").child(id).updateChildren(recipeMap);
                    }
                    dialog.dismiss();


                    /*---------- Closing Activity after 2500(2.5sec) millisecond ----------*/
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismissDialog();
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }, 2500);
                } else {
                    Snackbar.make(popupView, R.string.error_message, Snackbar.LENGTH_LONG);
                }

            }

        });

    }

    /*---------- Creating Summed Up Recipe PopUp ----------*/
    @SuppressLint("InflateParams")
    private void createPopupDialog() {

        /*---------- PopUp SetUp ----------*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        popupView = getLayoutInflater().inflate(R.layout.popup_add_recipe, null);

        /*---------- PopUp Hooks ----------*/
        TextView popupName = popupView.findViewById(R.id.recipe_name_popup);
        TextView popupIngredients = popupView.findViewById(R.id.ingredients_popup);
        TextView popupCategory = popupView.findViewById(R.id.category_popup);
        TextView popupDescription = popupView.findViewById(R.id.description_popup);
        ImageView exitPopup = popupView.findViewById(R.id.exit_popup);
        RadioGroup languageRadioGroup = popupView.findViewById(R.id.languageRadioGroup);
        RadioButton greekLanguageRadioButton = popupView.findViewById(R.id.greekRadioButton);
        RadioButton englishLanguageRadioButton = popupView.findViewById(R.id.englishRadioButton);

        popupSaveButton = popupView.findViewById(R.id.save_popup_button);
        imageButton = popupView.findViewById(R.id.select_image_button);

        /*---------- Making EditTexts Scrollable ----------*/
        popupIngredients.setMovementMethod(new ScrollingMovementMethod());
        popupDescription.setMovementMethod(new ScrollingMovementMethod());

        /*---------- Initializing PopUp Hooks ----------*/
        popupName.setText(name.getText().toString());
        StringBuilder ingredientsList = new StringBuilder();
        for (int i = 0; i < ingredientsArrayList.size(); i++) {
            if (i != ingredientsArrayList.size() - 1)
                ingredientsList.append(i + 1).append(") ").append(ingredientsArrayList.get(i)).append(" (").append(ingredientQuantityList.get(i)).append("),\n");
            else
                ingredientsList.append(i + 1).append(") ").append(ingredientsArrayList.get(i)).append(" (").append(ingredientQuantityList.get(i)).append(")");
        }
        popupIngredients.setText(ingredientsList.toString());
        popupCategory.setText(category.getText().toString());
        popupDescription.setText(steps.getText().toString());

        /*---------- PopUp Hook Click Listeners ----------*/
        imageButton.setOnClickListener(this);
        popupSaveButton.setOnClickListener(this);
        exitPopup.setOnClickListener(this);
        greekLanguageRadioButton.setOnClickListener(this);
        englishLanguageRadioButton.setOnClickListener(this);


        if (languageRadioGroup.getCheckedRadioButtonId() != -1) {
            /*---------- Enabling Buttons ----------*/
            imageButton.setBackgroundColor(Color.DKGRAY);
            imageButton.setClickable(true);
        }




        /*---------- Disabling Buttons ----------*/
        disableButton(popupSaveButton);
        imageButton.setClickable(false);


        /*---------- Initializing Pop Up ----------*/
        builder.setView(popupView);
        builder.setCancelable(false);
        dialog = builder.create(); //creating our dialog object
        dialog.show(); // important step

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

    }

    /*---------- Adding Ingredient to ListView ----------*/
    private void addIngredient(View v) {

        //ingredientsListView.setAdapter(adapterSimpleList);
        //disableButton(deleteIngredientButton);
        //enableButton(addIngredientButton);
        if (ingredientsArrayList.contains(ingredient.getText().toString())) {
            Snackbar.make(v, R.string.ingredient_exists, Snackbar.LENGTH_LONG).show();
            return;
        }

        recipeIngredients.add(ingredient.getText().toString() + " (" + quantity.getText().toString() + ")");
        ingredientsArrayList.add(ingredient.getText().toString());
        ingredientQuantityList.add(quantity.getText().toString());
        ingredientsRecyclerViewAdapter.notifyDataSetChanged();
        //adapterSimpleList.notifyDataSetChanged();
        enableButton(nextButton);
        ingredient.setText("");
        quantity.setText("");

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

}
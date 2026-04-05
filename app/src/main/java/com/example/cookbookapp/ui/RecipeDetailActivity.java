package com.example.cookbookapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cookbookapp.data.AppDatabase;
import com.example.cookbookapp.data.RecipeWithInstructions;
import com.example.cookbookapp.databinding.ActivityRecipeDetailBinding;
import com.example.cookbookapp.models.Recipe;

import java.util.concurrent.Executors;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_RECIPE_NAME = "recipe_name";
    public static final String EXTRA_RECIPE_CATEGORY = "recipe_category";
    public static final String EXTRA_RECIPE_INSTRUCTIONS = "recipe_instructions";
    public static final String EXTRA_RECIPE_IMAGE = "recipe_image";

    private ActivityRecipeDetailBinding binding;
    private AppDatabase db;
    private String recipeId;
    private String name, category, instructions, image;
    private boolean isLocal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }

        db = AppDatabase.getInstance(this);

        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        name = getIntent().getStringExtra(EXTRA_RECIPE_NAME);
        category = getIntent().getStringExtra(EXTRA_RECIPE_CATEGORY);
        instructions = getIntent().getStringExtra(EXTRA_RECIPE_INSTRUCTIONS);
        image = getIntent().getStringExtra(EXTRA_RECIPE_IMAGE);

        displayData();
        checkIfSaved();

        binding.btnSave.setOnClickListener(v -> saveToLocal());
        binding.btnDelete.setOnClickListener(v -> deleteFromLocal());
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            intent.putExtra(AddRecipeActivity.EXTRA_RECIPE_ID, recipeId);
            startActivity(intent);
            finish(); // Close detail to refresh after edit
        });
    }

    private void displayData() {
        binding.tvTitle.setText(name);
        binding.tvCategory.setText(category);
        binding.tvInstructions.setText(instructions);

        Glide.with(this)
                .load(image)
                .into(binding.ivRecipe);
    }

    private void checkIfSaved() {
        Executors.newSingleThreadExecutor().execute(() -> {
            RecipeWithInstructions ri = db.recipeDao().getRecipeById(recipeId);
            runOnUiThread(() -> {
                if (ri != null) {
                    isLocal = true;
                    binding.btnSave.setVisibility(View.GONE);
                    binding.btnEdit.setVisibility(View.VISIBLE);
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    // Update data from local DB in case it was edited
                    name = ri.getRecipe().getName();
                    category = ri.getRecipe().getCategory();
                    instructions = ri.getRecipe().getDescription();
                    image = ri.getRecipe().getImageUrl();
                    displayData();
                } else {
                    isLocal = false;
                    binding.btnSave.setVisibility(View.VISIBLE);
                    binding.btnEdit.setVisibility(View.GONE);
                    binding.btnDelete.setVisibility(View.GONE);
                }
            });
        });
    }

    private void saveToLocal() {
        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name(name)
                .description(instructions)
                .category(category)
                .imageUrl(image)
                .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            db.recipeDao().insertRecipe(recipe);
            runOnUiThread(() -> {
                Toast.makeText(this, "Saved to Library", Toast.LENGTH_SHORT).show();
                checkIfSaved();
            });
        });
    }

    private void deleteFromLocal() {
        Recipe recipe = Recipe.builder().id(recipeId).build();
        Executors.newSingleThreadExecutor().execute(() -> {
            db.recipeDao().deleteRecipe(recipe);
            runOnUiThread(() -> {
                Toast.makeText(this, "Deleted from Library", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.example.cookbookapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cookbookapp.data.AppDatabase;
import com.example.cookbookapp.data.RecipeWithInstructions;
import com.example.cookbookapp.databinding.ActivityAddRecipeBinding;
import com.example.cookbookapp.models.Recipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AddRecipeActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private ActivityAddRecipeBinding binding;
    private AppDatabase db;
    private String selectedImagePath = "";
    private String existingRecipeId = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageLocally(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        existingRecipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (existingRecipeId != null) {
            loadExistingRecipe();
            setTitle("Edit Recipe");
        } else {
            setTitle("Add Recipe");
        }

        binding.btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        binding.btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void loadExistingRecipe() {
        Executors.newSingleThreadExecutor().execute(() -> {
            RecipeWithInstructions ri = db.recipeDao().getRecipeById(existingRecipeId);
            if (ri != null) {
                runOnUiThread(() -> {
                    Recipe r = ri.getRecipe();
                    binding.etName.setText(r.getName());
                    binding.etCategory.setText(r.getCategory());
                    binding.etImageUrl.setText(r.getImageUrl());
                    binding.etDescription.setText(r.getDescription());
                    selectedImagePath = r.getImageUrl();
                    if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                        Glide.with(this).load(selectedImagePath).into(binding.ivPreview);
                    }
                });
            }
        });
    }

    private void saveImageLocally(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            String fileName = "recipe_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            OutputStream out = new FileOutputStream(file);
            
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();

            selectedImagePath = file.getAbsolutePath();
            Glide.with(this).load(file).into(binding.ivPreview);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecipe() {
        String name = binding.etName.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();
        String imageUrl = binding.etImageUrl.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = (existingRecipeId != null) ? existingRecipeId : UUID.randomUUID().toString();
        // If selectedImagePath was updated via gallery, use it. Otherwise use the text field URL/Path
        String finalImage = selectedImagePath; 
        if (binding.etImageUrl.getText().length() > 0 && (selectedImagePath == null || !selectedImagePath.equals(imageUrl))) {
            finalImage = imageUrl;
        }

        Recipe recipe = Recipe.builder()
                .id(id)
                .name(name)
                .category(category)
                .imageUrl(finalImage)
                .description(description)
                .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            db.recipeDao().insertRecipe(recipe); // REPLACE strategy handles update
            runOnUiThread(() -> {
                Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show();
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

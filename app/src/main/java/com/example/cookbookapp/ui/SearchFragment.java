package com.example.cookbookapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookbookapp.api.Meal;
import com.example.cookbookapp.api.MealsResponse;
import com.example.cookbookapp.api.TheMealDbApi;
import com.example.cookbookapp.data.AppDatabase;
import com.example.cookbookapp.databinding.FragmentSearchBinding;
import com.example.cookbookapp.models.Recipe;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private FragmentSearchBinding binding;
    private RecipeAdapter adapter;
    private TheMealDbApi api;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        initRetrofit();
        setupRecyclerView();
        setupSearch();

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(TheMealDbApi.class);
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchRecipes(String query) {
        if (query.isEmpty()) {
            adapter.setMeals(null);
            return;
        }

        api.searchMeals(query).enqueue(new Callback<MealsResponse>() {
            @Override
            public void onResponse(Call<MealsResponse> call, Response<MealsResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    adapter.setMeals(response.body().getMeals());
                }
            }

            @Override
            public void onFailure(Call<MealsResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSaveClick(Meal meal) {
        Recipe recipe = Recipe.builder()
                .id(meal.getIdMeal())
                .name(meal.getStrMeal())
                .description(meal.getStrInstructions())
                .category(meal.getStrCategory())
                .imageUrl(meal.getStrMealThumb())
                .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            db.recipeDao().insertRecipe(recipe);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "Saved " + meal.getStrMeal(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onItemClick(Meal meal) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, meal.getIdMeal());
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_NAME, meal.getStrMeal());
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_CATEGORY, meal.getStrCategory());
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INSTRUCTIONS, meal.getStrInstructions());
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_IMAGE, meal.getStrMealThumb());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

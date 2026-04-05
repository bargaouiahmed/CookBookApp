package com.example.cookbookapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookbookapp.api.Meal;
import com.example.cookbookapp.data.AppDatabase;
import com.example.cookbookapp.data.RecipeWithInstructions;
import com.example.cookbookapp.databinding.FragmentLibraryBinding;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private FragmentLibraryBinding binding;
    private RecipeAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        setupRecyclerView();
        observeLibrary();
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(this);
        binding.recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewLibrary.setAdapter(adapter);
    }

    private void observeLibrary() {
        db.recipeDao().getRecipesWithInstructions().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes == null || recipes.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.setMeals(new ArrayList<>());
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                List<Meal> meals = new ArrayList<>();
                for (RecipeWithInstructions r : recipes) {
                    Meal m = new Meal();
                    m.setIdMeal(r.getRecipe().getId());
                    m.setStrMeal(r.getRecipe().getName());
                    m.setStrCategory(r.getRecipe().getCategory());
                    m.setStrInstructions(r.getRecipe().getDescription());
                    m.setStrMealThumb(r.getRecipe().getImageUrl());
                    meals.add(m);
                }
                adapter.setMeals(meals);
            }
        });
    }

    @Override
    public void onSaveClick(Meal meal) {
        // Option to unsave/delete from list directly could go here
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

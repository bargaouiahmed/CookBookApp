package com.example.cookbookapp.api;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Meal {
    @SerializedName("idMeal")
    private String idMeal;
    @SerializedName("strMeal")
    private String strMeal;
    @SerializedName("strCategory")
    private String strCategory;
    @SerializedName("strInstructions")
    private String strInstructions;
    @SerializedName("strMealThumb")
    private String strMealThumb;
}

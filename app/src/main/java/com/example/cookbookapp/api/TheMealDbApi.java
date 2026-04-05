package com.example.cookbookapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TheMealDbApi {
    @GET("search.php")
    Call<MealsResponse> searchMeals(@Query("s") String query);
}

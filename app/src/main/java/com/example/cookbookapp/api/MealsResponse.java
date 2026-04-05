package com.example.cookbookapp.api;

import java.util.List;
import lombok.Data;

@Data
public class MealsResponse {
    private List<Meal> meals;
}

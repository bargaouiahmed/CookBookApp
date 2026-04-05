package com.example.cookbookapp.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.cookbookapp.models.Instruction;
import com.example.cookbookapp.models.Recipe;

import java.util.List;

import lombok.Data;

@Data
public class RecipeWithInstructions {
    @Embedded
    private Recipe recipe;
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    private List<Instruction> instructions;
}

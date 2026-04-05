package com.example.cookbookapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(
    tableName = "instructions",
    foreignKeys = @ForeignKey(
        entity = Recipe.class,
        parentColumns = "id",
        childColumns = "recipeId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("recipeId")}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instruction {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String recipeId;
    private int stepNumber;
    private String instructionText;
}

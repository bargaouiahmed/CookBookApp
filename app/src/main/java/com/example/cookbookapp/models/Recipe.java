package com.example.cookbookapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Recipe {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;

    private String description;
    private String category;
    private String imageUrl;
}

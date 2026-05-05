package com.example.cookbookapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.cookbookapp.models.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Insert
    void insertUser(User user);
}

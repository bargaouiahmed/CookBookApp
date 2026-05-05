package com.example.cookbookapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookbookapp.MainActivity;
import com.example.cookbookapp.data.AppDatabase;
import com.example.cookbookapp.databinding.ActivityLoginBinding;
import com.example.cookbookapp.models.User;
import com.example.cookbookapp.utils.HashUtils;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);

        checkUserCount();

        binding.btnLogin.setOnClickListener(v -> login());
        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void checkUserCount() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = db.userDao().getUserCount();
            runOnUiThread(() -> {
                if (count > 0) {
                    binding.btnRegister.setVisibility(View.GONE);
                } else {
                    binding.btnLogin.setVisibility(View.GONE);
                }
            });
        });
    }

    private void login() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().getUserByUsername(username);
            String hashedPassword = HashUtils.hashPassword(password);

            runOnUiThread(() -> {
                if (user != null && user.getPassword().equals(hashedPassword)) {
                    navigateToMain();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void register() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            String hashedPassword = HashUtils.hashPassword(password);
            User newUser = new User(username, hashedPassword);
            db.userDao().insertUser(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                checkUserCount(); // Refresh visibility
                binding.btnLogin.setVisibility(View.VISIBLE);
                binding.btnRegister.setVisibility(View.GONE);
            });
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.register.setOnClickListener(view -> {
            // start register activity
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        binding.login.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }
}
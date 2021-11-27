package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.databinding.ActivityAccountBinding;


public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAccountBinding binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.register.setOnClickListener(view -> {
            // start register activity
            startActivity(new Intent(AccountActivity.this, RegisterActivity.class));
        });

        // start login activity
        binding.login.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, LoginActivity.class)));
    }
}
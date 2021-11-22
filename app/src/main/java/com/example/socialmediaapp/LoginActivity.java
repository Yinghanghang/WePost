package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        binding.loginButton.setOnClickListener(view -> {
            String emailString = binding.email.getText().toString();
            String passwordString = binding.password.getText().toString();

            //validate the email format
            if (!Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
                binding.email.setError("Invalid Email");
                binding.email.setFocusable(true);
            } else {
                doLogin(emailString, passwordString);
            }
        });

        binding.noAccountText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void doLogin(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login succeeded.",
                                Toast.LENGTH_SHORT).show();
                        // Sign in success, redirect to start activity
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        progressDialog.dismiss();
                        // Sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Login failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            //get and show the error message
            Toast.makeText(LoginActivity.this, "" + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go to previous activity
        return super.onSupportNavigateUp();
    }
}
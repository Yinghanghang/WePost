package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        username = binding.rUsername;

        binding.rRegister.setOnClickListener(v -> {
            String str_username = username.getText().toString();
            String str_email = binding.rEmail.getText().toString();
            String str_password = binding.rPassword.getText().toString();
            String str_confirm_password = binding.rConfirmPassword.getText().toString();

            //validate the input
            if (TextUtils.isEmpty(str_username)) {
                username.setError("Username is required");
                username.setFocusable(true);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
                binding.rEmail.setError("Invalid Email");
                binding.rEmail.setFocusable(true);
            } else if (str_password.length() < 6) {
                binding.rPassword.setError("Password length at least 6 characters");
                binding.rPassword.setFocusable(true);
            } else if (!str_password.equals(str_confirm_password)) {
                binding.rConfirmPassword.setError("Passwords do not match");
                binding.rConfirmPassword.setFocusable(true);
            } else {
                // register the user
                register(str_email, str_password);
            }
        });

        // if have an account, go to login activity
        binding.rHaveAccount.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void register(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, dismiss dialog and start register activity
                        progressDialog.dismiss();
                        FirebaseUser user = auth.getCurrentUser();
                        HashMap<String, Object> hashMap = new HashMap<>();

                        String userEmail = user.getEmail();
                        String userId = user.getUid();

                        hashMap.put("userEmail", userEmail);
                        hashMap.put("userID", userId);
                        hashMap.put("userName", username.getText().toString());
                        hashMap.put("userImage", "");

                        // create an instance of firebase database
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        // a location to store the data
                        DatabaseReference reference = database.getReference("users");
                        // put the data that is stored in a hashmap into the database
                        reference.child(userId).setValue(hashMap);

                        Toast.makeText(RegisterActivity.this, "Registration succeeded.", Toast.LENGTH_SHORT).show();
                        // redirect to HomeActivity
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // Sign in fails, notify the user.
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            // show error message
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // go to previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
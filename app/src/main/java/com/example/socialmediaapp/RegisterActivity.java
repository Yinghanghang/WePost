package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    private EditText username, email, password, confirm_password;
    private Button register;
    private TextView have_account;
    private ProgressDialog progressDialog;
    // declare an instance of the FirebaseAuth object:
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        username = binding.rUsername;
        email = binding.rEmail;
        password = binding.rPassword;
        confirm_password = binding.rConfirmPassword;
        register = binding.rRegister;
        have_account = binding.rHaveAccount;

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");

        register.setOnClickListener(v -> {
            String str_username = username.getText().toString();
            String str_email = email.getText().toString();
            String str_password = password.getText().toString();
            String str_confirm_password = confirm_password.getText().toString();

            //validate
            if(TextUtils.isEmpty(str_username)) {
                username.setError("Username is required");
                username.setFocusable(true);
            } else if(!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()){
                email.setError("Invalid Email");
                email.setFocusable(true);
            } else if(str_password.length() < 6) {
                password.setError("Password length at least 6 characters");
                password.setFocusable(true);
            } else if(!str_password.equals(str_confirm_password)){
                confirm_password.setError("Passwords do not match");
                confirm_password.setFocusable(true);
            } else {
                register(str_email, str_password);
            }
        });

        have_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void register(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, dismiss dialog and start register activity
                        progressDialog.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();
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
                        startActivity(new Intent(RegisterActivity.this, StartActivity.class));
                        finish();
                    } else {
                        // Sign in fails, notify the user.
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // show error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed(); // go to previous activity
        return super.onSupportNavigateUp();
    }
}
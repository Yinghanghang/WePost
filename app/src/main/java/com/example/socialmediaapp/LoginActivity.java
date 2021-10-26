package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button login;
    private TextView not_have_account;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        email = findViewById(R.id.l_email);
        password = findViewById(R.id.l_password);
        login = findViewById(R.id.l_login);
        not_have_account = findViewById(R.id.l_not_have_account);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging In...");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                //validate the email format
                if(!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()){
                    email.setError("Invalid Email");
                    email.setFocusable(true);
                } else {
                    login(str_email, str_password);
                }
            }
        });

        not_have_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login(String str_email, String str_password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(str_email, str_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login succeeded.",
                                    Toast.LENGTH_SHORT).show();

//                            FirebaseUser user = mAuth.getCurrentUser();
//                            HashMap<Object, String> hashMap = new HashMap<>();
//
//                            String userEmail = user.getEmail();
//                            String userId = user.getUid();
//                            hashMap.put("userEmail", userEmail);
//                            hashMap.put("userID", userId);
//                            hashMap.put("userName", "");
//                            hashMap.put("userImage", "");
//
//                            // create an instance of firebase database
//                            FirebaseDatabase database = FirebaseDatabase.getInstance();
//                            // a location to store the data
//                            DatabaseReference reference = database.getReference("users");
//                            // put the data that is stored in a hashmap into the database
//                            reference.child(userId).setValue(hashMap);

                            // Sign in success, redirect to start activity
                            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            // Sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //get and show the error message
                Toast.makeText(LoginActivity.this, "" + e.getMessage(),
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
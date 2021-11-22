package com.example.socialmediaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.socialmediaapp.Fragment.ContactsFragment;
import com.example.socialmediaapp.Fragment.HomeFragment;
import com.example.socialmediaapp.Fragment.ProfileFragment;
import com.example.socialmediaapp.databinding.ActivityStartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStartBinding binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        refreshIfLogout();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String publisher = extras.getString("publisherid");
            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            // home fragment default
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_contacts:
                    selectedFragment = new ContactsFragment();
                    break;
                case R.id.nav_profile:
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();
                    selectedFragment = new ProfileFragment();
                    break;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
            }

            return true;
        });
    }

    private void refreshIfLogout() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        } else {
            //stay in current page
        }
    }
}
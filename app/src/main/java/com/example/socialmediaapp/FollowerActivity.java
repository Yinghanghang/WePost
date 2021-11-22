package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.socialmediaapp.Adapter.UserAdapter;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.databinding.ActivityFollowerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowerActivity extends AppCompatActivity {
    private String userId;
    private List<String> idList;
    private List<User> userList;
    private UserAdapter userAdapter;
    private final ValueEventListener refreshIdListListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            idList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                idList.add(snapshot.getKey());
            }
            loadUsers();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFollowerBinding binding = ActivityFollowerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, false);
        binding.recyclerView.setAdapter(userAdapter);

        idList = new ArrayList<>();
        switch (title) {
            case "Likes":
                getLikes();
                break;
            case "Following":
                getFollowing();
                break;
            case "Followers":
                getFollowers();
                break;
        }
    }

    // back button on action bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : idList) {
                        if (user.getUserID().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("follow")
                .child(userId).child("followers");
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("follow")
                .child(userId).child("following");
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes")
                .child(userId);
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }
}
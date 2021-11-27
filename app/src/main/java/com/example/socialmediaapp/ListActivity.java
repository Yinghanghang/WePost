package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.socialmediaapp.Adapter.UserAdapter;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.databinding.ActivityListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private String id;
    private List<String> idList;
    private List<User> userList;
    private UserAdapter userAdapter;

    private final ValueEventListener refreshIdListListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            idList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                idList.add(snapshot.getKey()); // get the user id
            }
            // get the users list from user id list
            loadUsers();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityListBinding binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get data from PostDetailActivity or PostAdapter or ProfileFragment
        Intent intent = getIntent();
        id = intent.getStringExtra("id"); // either postid or userid
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
            case "Likes":   // from PostAdapter or PostDetailActivity
                getLikes();
                break;
            case "Following":  // from ProfileFragment
                getFollowing();
                break;
            case "Followers":   // from ProfileFragment
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
                .child(id).child("followers");
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("follow")
                .child(id).child("following");
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes")
                .child(id);
        reference.addListenerForSingleValueEvent(refreshIdListListener);
    }
}
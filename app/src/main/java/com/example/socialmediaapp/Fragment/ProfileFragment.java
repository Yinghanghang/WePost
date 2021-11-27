package com.example.socialmediaapp.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Adapter.MyPostAdapter;
import com.example.socialmediaapp.EditProfileActivity;
import com.example.socialmediaapp.ListActivity;
import com.example.socialmediaapp.AccountActivity;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String profileId;

    private MyPostAdapter myPostAdapter;
    private List<Post> postList;
    private FragmentProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // get data from UserAdapter/HomeActivity
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        profileId = prefs.getString("profileid", "none");

        binding.recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        binding.recyclerView.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        myPostAdapter = new MyPostAdapter(getContext(), postList);
        binding.recyclerView.setAdapter(myPostAdapter);

        getUsers();
        getFollowers();
        refreshPostList();

        if (profileId.equals(firebaseUser.getUid())) {
            binding.pEditProfile.setText("Edit Profile");
        } else {
            getFollowing();
        }

        binding.pEditProfile.setOnClickListener(v -> {
            String button = binding.pEditProfile.getText().toString();
            switch (button) {
                case "Edit Profile":
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                    break;
                case "follow":
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    break;
                case "following":
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                    break;
            }
        });

        binding.followers.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "Followers");
            startActivity(intent);
        });

        binding.following.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "Following");
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getUserImage()).into(binding.pImage);
                binding.pUsername.setText(user.getUserName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    binding.pEditProfile.setText("following");
                } else {
                    binding.pEditProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference_followers = FirebaseDatabase.getInstance().getReference("follow").child(profileId).child("followers");
        reference_followers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                binding.followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference reference_following = FirebaseDatabase.getInstance().getReference("follow").child(profileId).child("following");
        reference_following.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                binding.following.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void refreshPostList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPostAuthor().equals(profileId)) {
                        postList.add(post);
                    }
                }
                binding.posts.setText("" + postList.size());
                myPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getActivity(), AccountActivity.class));
            getActivity().finish();
        } else {
            //stay in current page
        }
    }
}
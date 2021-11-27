package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Adapter.CommentAdapter;
import com.example.socialmediaapp.Model.Comment;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.databinding.ActivityPostDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    String postId, publisherId;

    private ActivityPostDetailBinding binding;

    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must be executed first
        redirectIfLogout();

        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get data from PostAdapter
        postId = getIntent().getStringExtra("postid");
        publisherId = getIntent().getStringExtra("publisherid");

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments, postId);
        recyclerView.setAdapter(commentAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
            intent.putExtra("publisherid", publisherId);
            startActivity(intent);
        });

        binding.username.setOnClickListener(view -> {
            Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
            intent.putExtra("publisherid", publisherId);
            startActivity(intent);
        });

        binding.more.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, view);
            popupMenu.inflate(R.menu.post_menu);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.edit:
                        Intent intent = new Intent(PostDetailActivity.this, PostActivity.class);
                        intent.putExtra("key", "editPost");
                        intent.putExtra("editPostId", postId);
                        startActivity(intent);
                        return true;
                    case R.id.delete:
                        FirebaseDatabase.getInstance().getReference("posts")
                                .child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                // after delete post, redirect to HomeActivity
                                startActivity(new Intent(PostDetailActivity.this, HomeActivity.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        return true;
                    default:
                        return false;
                }
            });

            if (!publisherId.equals(currentUser)) {
                popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
            }
            popupMenu.show();
        });

        binding.sendBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.commentEt.getText())) {
                Toast.makeText(PostDetailActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            } else {
                postComment();
            }
        });

        binding.like.setOnClickListener(v -> {
            if (binding.like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("likes").child(postId)
                        .child(currentUser).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("likes").child(postId)
                        .child(currentUser).removeValue();
            }
        });

        // get the likes list
        binding.likes.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, ListActivity.class);
            intent.putExtra("id", postId);
            intent.putExtra("title", "Likes");
            startActivity(intent);
        });

        loadAuthorInfo();
        loadPostInfo();
        loadLiked();
        loadLikes();
        loadUserImage();
        loadComments();
    }

    // get the data updated after edit
    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            redirectIfLogout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void postComment() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postId);

        // generate an id for each comment
        String commentId = reference.push().getKey();

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", binding.commentEt.getText().toString());
        hashMap.put("publisher", currentUser);
        hashMap.put("commentid", commentId);
        hashMap.put("commentTime", System.currentTimeMillis());

        reference.child(commentId).setValue(hashMap);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        // reset the comment to empty
        binding.commentEt.setText("");
    }

    private void loadUserImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(currentUser);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getUserImage()).into(binding.imageAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    comments.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);

                if (TextUtils.isEmpty(post.getPostCaption())) {
                    binding.caption.setVisibility(View.GONE);
                } else {
                    binding.caption.setVisibility(View.VISIBLE);
                    binding.caption.setText(post.getPostCaption());
                }

                binding.time.setText(formatPostTime(post.getPostTime()));

                if (post.getPostImage().equals("noImage")) {
                    // hide post image view
                    binding.postImage.setVisibility(View.GONE);
                } else {
                    binding.postImage.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext()).load(post.getPostImage()).into(binding.postImage);
                }

                if (TextUtils.isEmpty(post.getPostLocation())) {
                    binding.location.setVisibility(View.GONE);
                } else {
                    binding.location.setVisibility(View.VISIBLE);
                    binding.location.setText(post.getPostLocation());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadAuthorInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(publisherId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                binding.username.setText(user.getUserName());

                Glide.with(getApplicationContext())
                        .load(user.getUserImage())
                        .placeholder(R.drawable.ic_add_image)
                        .error(R.drawable.ic_add_image)
                        .into(binding.imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadLiked() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUser).exists()) {
                    binding.like.setImageResource(R.drawable.ic_liked);
                    binding.like.setTag("liked");
                } else {
                    binding.like.setImageResource(R.drawable.ic_like);
                    binding.like.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                binding.likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String formatPostTime(long postTime) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(postTime);
        return DateFormat.format("MM/dd/yyyy hh:mm aa", calendar).toString();
    }

    private void redirectIfLogout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, AccountActivity.class));
            finish();
        } else {
            //stay in current page
        }
    }
}
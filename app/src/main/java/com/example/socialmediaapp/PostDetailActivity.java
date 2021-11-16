package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Fragment.ProfileFragment;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private FirebaseUser user;
    String postid, publisherid;

    ImageView image_profile, post_image, more, like;
    TextView username, time, description, likes;
    LinearLayout profileLayout;

    EditText comment;
    ImageButton send;
    ImageView image_avatar;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        image_profile = findViewById(R.id.image_profile);
        post_image = findViewById(R.id.post_image);
        more = findViewById(R.id.more);
        like = findViewById(R.id.like);
        username = findViewById(R.id.username);
        time = findViewById(R.id.time);
        description = findViewById(R.id.caption);
        likes = findViewById(R.id.likes);
        comment = findViewById(R.id.commentEt);
        send = findViewById(R.id.sendBtn);
        image_avatar = findViewById(R.id.image_avatar);

        user = FirebaseAuth.getInstance().getCurrentUser();

        checkUserStatus();

        loadAuthorInfo();
        loadPostInfo();
        isLiked();
        numberOfLikes();

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PostDetailActivity.this, StartActivity.class);
                intent.putExtra("publisherid", publisherid);
                startActivity(intent);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PostDetailActivity.this, StartActivity.class);
                intent.putExtra("publisherid", publisherid);
                startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, view);
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.edit:
                                //editPost(post.getPostID());
                                Intent intent = new Intent(PostDetailActivity.this, PostActivity.class);
                                intent.putExtra("key", "editPost");
                                intent.putExtra("editPostId", postid);
                                startActivity(intent);
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("posts")
                                        .child(postid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        snapshot.getRef().removeValue();
                                        Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                                return true;
                            default:
                                return false;
                        }
                    }
                });

                if (!publisherid.equals(user.getUid())){
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getText().toString().equals("")){
                    Toast.makeText(PostDetailActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postid)
                            .child(user.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postid)
                            .child(user.getUid()).removeValue();
                }
            }
        });

        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, FollowerActivity.class);
                intent.putExtra("id", postid);
                intent.putExtra("title", "Likes");
                startActivity(intent);
            }
        });

    }

    private void addComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);

        String commentid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", comment.getText().toString());
        hashMap.put("publisher", user.getUid());
        hashMap.put("commentid", commentid);
        hashMap.put("commentTime", System.currentTimeMillis());

        reference.child(commentid).setValue(hashMap);
        dismissProgressDialog();
        comment.setText("");
    }

    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);

                if(post.getPostCaption().equals("")) {
                    description.setVisibility(View.GONE);
                } else {
                    description.setVisibility(View.VISIBLE);
                    description.setText(post.getPostCaption());
                }

                time.setText(formatPostTime(post.getPostTime()));

                if(post.getPostImage().equals("noImage")) {
                    // hide post image view
                    post_image.setVisibility(View.GONE);
                } else {
                    post_image.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext()).load(post.getPostImage()).into(post_image);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadAuthorInfo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(publisherid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUserName());

                try {
                    Glide.with(getApplicationContext()).load(user.getUserImage()).into(image_profile);
                    Glide.with(getApplicationContext()).load(user.getUserImage()).into(image_avatar);

                } catch (Exception e) {
                    Glide.with(getApplicationContext()).load(R.drawable.ic_add_image).into(image_profile);
                    Glide.with(getApplicationContext()).load(R.drawable.ic_add_image).into(image_avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(){

        user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user.getUid()).exists()){
                    like.setImageResource(R.drawable.ic_liked);
                    like.setTag("liked");
                } else{
                    like.setImageResource(R.drawable.ic_like);
                    like.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void numberOfLikes(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount()+" likes");
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

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            //stay in current page
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void dismissProgressDialog() {
        if ( progressDialog!=null &&  progressDialog.isShowing()){
            progressDialog.dismiss();
        }
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
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
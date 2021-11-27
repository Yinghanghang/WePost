package com.example.socialmediaapp.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.ListActivity;
import com.example.socialmediaapp.Fragment.ProfileFragment;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.PostActivity;
import com.example.socialmediaapp.PostDetailActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.PostItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final Context context;
    private final List<Post> posts;
    private final String currentUser;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.post_item, parent, false));
    }

    private String formatPostTime(long postTime) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(postTime);
        return DateFormat.format("MM/dd/yyyy hh:mm aa", calendar).toString();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        PostItemBinding binding = PostItemBinding.bind(holder.itemView);

        // hide the image if no image exists
        if (post.getPostImage().equals("noImage")) {
            // hide post image view
            binding.postImage.setVisibility(View.GONE);
        } else {
            binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPostImage()).into(binding.postImage);
        }

        // hide the description if no description exists
        if (post.getPostCaption().equals("")) {
            binding.caption.setVisibility(View.GONE);
        } else {
            binding.caption.setVisibility(View.VISIBLE);
            binding.caption.setText(post.getPostCaption());
        }

        if (TextUtils.isEmpty(post.getPostLocation())) {
            binding.location.setVisibility(View.GONE);
        } else {
            binding.location.setVisibility(View.VISIBLE);
            binding.location.setText(post.getPostLocation());
        }

        // set the author information
        getAuthor(binding.imageProfile, binding.username, post.getPostAuthor());
        // set the post time
        binding.time.setText(formatPostTime(post.getPostTime()));

        // set the like button
        getLiked(post.getPostID(), binding.like);
        // get number of likes
        getLikes(post.getPostID(), binding.likes);
        // get all the comments
        getComments(post.getPostID(), binding.comments);

        // set the click events
        binding.imageProfile.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", post.getPostAuthor());
            editor.apply();

            // go to user profile if click the profile image
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        });

        binding.username.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", post.getPostAuthor());
            editor.apply();

            // go to user profile if click the username
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        });

        binding.postImage.setOnClickListener(view -> {
            // go to post detail page if click post image
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);

        });

        binding.caption.setOnClickListener(view -> {
            // go to post detail page if click post description
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);
        });

        binding.more.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.post_menu);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.edit:
                        // start PostActivity while passing the postid
                        Intent intent = new Intent(context, PostActivity.class);
                        intent.putExtra("key", "editPost");
                        intent.putExtra("editPostId", post.getPostID());
                        context.startActivity(intent);
                        return true;
                    case R.id.delete:
                        FirebaseDatabase.getInstance().getReference("posts")
                                .child(post.getPostID()).removeValue();
                        return true;
                    default:
                        return false;
                }
            });

            // hide edit/delete option if posts are not created by current user
            if (!post.getPostAuthor().equals(currentUser)) {
                popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
            }
            popupMenu.show();
        });

        binding.like.setOnClickListener(view -> {
            if (binding.like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostID())
                        .child(currentUser).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostID())
                        .child(currentUser).removeValue();
            }
        });

        binding.comment.setOnClickListener(view -> {
            //start PostDetailActivity by passing postid and publisherid
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);

        });

        binding.comments.setOnClickListener(view -> {
            //start PostDetailActivity by passing postid and publisherid
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);
        });

        binding.likes.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListActivity.class);
            intent.putExtra("id", post.getPostID());
            intent.putExtra("title", "Likes");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void getAuthor(ImageView imageProfile, TextView username, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("userName").getValue().toString();
                String userImage = dataSnapshot.child("userImage").getValue().toString();
                username.setText(userName);

                Glide.with(context)
                        .load(userImage)
                        .placeholder(R.drawable.ic_add_image)
                        .error(R.drawable.ic_add_image)
                        .into(imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComments(String postId, TextView comments) {
        // get number of comments under a post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments.setText("View All " + dataSnapshot.getChildrenCount() + " comments");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLiked(String postId, ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUser).exists()) {
                    // if current user already liked the post
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLikes(String postId, TextView likes) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get number of likes of a post
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;
    private FirebaseUser user;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image);

        if(post.getPostCaption().equals("")) {
            holder.caption.setVisibility(View.GONE);
        } else {
            holder.caption.setVisibility(View.VISIBLE);
            holder.caption.setText(post.getPostCaption());
        }
        authorInfo(holder.image_profile, holder.username,  post.getPostAuthor());

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image, like, comment;
        public TextView username, likes, caption, comments;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            caption = itemView.findViewById(R.id.caption);
            comments = itemView.findViewById(R.id.comments);
        }
    }

    private void authorInfo(ImageView image_profile, TextView username, String userid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String str_name = dataSnapshot.child("userName").getValue().toString();
                String str_image = dataSnapshot.child("userImage").getValue().toString();
                username.setText(str_name);

                try {
                    Picasso.get().load(str_image).into(image_profile);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_add_image).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.PostDetailActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.MypostItemBinding;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> posts;

    public MyPostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.mypost_item, parent, false));
    }

    private String formatPostTime(long postTime) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(postTime);
        return DateFormat.format("MM/dd/yyyy", calendar).toString();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Post post = posts.get(position);
        final MypostItemBinding binding = MypostItemBinding.bind(holder.itemView);

        binding.time.setText(formatPostTime(post.getPostTime()));

        if (post.getPostImage().equals("noImage")) {
            // hide post image view
            binding.postImage.setVisibility(View.GONE);
        } else {
            binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPostImage()).into(binding.postImage);
        }

        if (post.getPostCaption().equals("")) {
            binding.caption.setVisibility(View.GONE);
        } else {
            binding.caption.setVisibility(View.VISIBLE);
            binding.caption.setText(post.getPostCaption());
        }

        binding.postImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);
        });


        binding.caption.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostID());
            intent.putExtra("publisherid", post.getPostAuthor());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
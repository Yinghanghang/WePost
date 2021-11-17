package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.PostDetailActivity;
import com.example.socialmediaapp.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ViewHolder> {

    private Context context;
    private List<Post> postList;

    public MyPostAdapter(Context context, List<Post> posts){
            this.context = context;
            postList = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.mypost_item, parent, false);
            return new ViewHolder(view);
    }

    private String formatPostTime(long postTime) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(postTime);
        return DateFormat.format("MM/dd/yyyy", calendar).toString();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Post post = postList.get(position);

        holder.time.setText(formatPostTime(post.getPostTime()));

        if(post.getPostImage().equals("noImage")) {
            // hide post image view
            holder.post_image.setVisibility(View.GONE);
        } else {
            holder.post_image.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPostImage()).into(holder.post_image);
        }

        if(post.getPostCaption().equals("")) {
            holder.caption.setVisibility(View.GONE);
        } else {
            holder.caption.setVisibility(View.VISIBLE);
            holder.caption.setText(post.getPostCaption());
        }

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postid", post.getPostID());
                intent.putExtra("publisherid", post.getPostAuthor());
                context.startActivity(intent);
            }
        });


        holder.caption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postid", post.getPostID());
                intent.putExtra("publisherid", post.getPostAuthor());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image;
        public TextView time, caption;

        public ViewHolder(View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_image);
            time = itemView.findViewById(R.id.time);
            caption = itemView.findViewById(R.id.caption);
        }
    }
}
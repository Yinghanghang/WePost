package com.example.socialmediaapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.HomeActivity;
import com.example.socialmediaapp.Model.Comment;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.CommentItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final Context context;
    private final List<Comment> comments;
    private final String postId;
    private final String currentUser;

    public CommentAdapter(Context context, List<Comment> comments, String postId) {
        this.context = context;
        this.comments = comments;
        this.postId = postId;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get data
        Comment comment = comments.get(position);
        CommentItemBinding binding = CommentItemBinding.bind(holder.itemView);

        binding.comment.setText(comment.getComment());
        binding.time.setText(formatPostTime(comment.getCommentTime()));
        getPublisherInfo(binding.imageProfile, binding.username, comment.getPublisher());

        binding.username.setOnClickListener(view -> {
            // go to HomeActivity while sending publisherid information
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            context.startActivity(intent);
        });

        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(view -> {
            // can only remove your own comments
            if (comment.getPublisher().equals(currentUser)) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Do you want to delete this comment?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            FirebaseDatabase.getInstance().getReference("comments")
                                    .child(postId).child(comment.getCommentid())
                                    .removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            dialog.dismiss();
                        });
                alertDialog.show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private void getPublisherInfo(ImageView imageView, TextView username, String publisherId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getUserImage()).into(imageView);
                username.setText(user.getUserName());
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

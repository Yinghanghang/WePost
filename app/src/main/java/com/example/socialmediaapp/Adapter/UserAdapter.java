package com.example.socialmediaapp.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Fragment.ProfileFragment;
import com.example.socialmediaapp.HomeActivity;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.UserItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context context;
    private final List<User> users;
    private final boolean isFragment;
    private final String currentUser;

    public UserAdapter(Context context, List<User> users, boolean isFragment) {
        this.context = context;
        this.users = users;
        this.isFragment = isFragment;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        UserItemBinding binding = UserItemBinding.bind(holder.itemView);

        binding.follow.setVisibility(View.VISIBLE);
        loadFollowing(user.getUserID(), binding.follow);

        binding.username.setText(user.getUserName());
        Glide.with(context).load(user.getUserImage()).into(binding.imageProfile);

        if (user.getUserID().equals(currentUser)) {
            binding.follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (isFragment) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", user.getUserID());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            } else {
                // can't jump from an activity(FollowerActivity) to a fragment(ProfileFragment)
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("publisherid", user.getUserID());
                context.startActivity(intent);
            }
        });

        binding.follow.setOnClickListener(view -> {
            if (binding.follow.getText().toString().equals("follow")) {
                FirebaseDatabase.getInstance().getReference().child("follow").child(currentUser)
                        .child("following").child(user.getUserID()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getUserID())
                        .child("followers").child(currentUser).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("follow").child(currentUser)
                        .child("following").child(user.getUserID()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getUserID())
                        .child("followers").child(currentUser).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void loadFollowing(final String userid, final Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(currentUser).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
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

package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;  // set by crop activity
    private StorageReference storageRef;

    private ActivityPostBinding binding;
    private ProgressDialog progressDialog;

    private String editCaption, editImage;  // set if editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");

        progressDialog = new ProgressDialog(this);

        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        //get data through intent from previous activities' adapter
        Intent intent = getIntent();
        String isUpdatedKey = intent.getStringExtra("key");
        String editPostId = intent.getStringExtra("editPostId");

        // check if we came here to update post or add post
        if ("editPost".equals(isUpdatedKey)) {
            //update post
            actionBar.setTitle("Edit Post");
            binding.post.setText("Update");
            loadPostData(editPostId);
        } else {
            // add new post
        }

        binding.pCancel.setOnClickListener(view -> {
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        });

        binding.post.setOnClickListener(view -> {
            if ("editPost".equals(isUpdatedKey)) {
                progressDialog.setMessage("Updating Post...");
            } else {
                progressDialog.setMessage("Publishing post");
            }
            progressDialog.show();
            if (imageUri != null) {
                // Updated by crop image activity, either from adding new post or editing post
                doUploadImage(editPostId, imageUri);
            } else {
                // editing a post without changing the image
                updateDatabase(editPostId, editImage);
            }
        });

        binding.pImageAdded.setOnClickListener(view -> CropImage.activity()
                .setAspectRatio(1, 1)
                .start(PostActivity.this));
    }

    private void loadPostData(String editPostId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts").child(editPostId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);

                //get old data
                editCaption = post.getPostCaption();
                editImage = post.getPostImage();

                // set data to views
                binding.pCaption.setText(editCaption);

                if (!editImage.equals("noImage")) {
                    Glide.with(getApplicationContext()).load(editImage).into(binding.pImageAdded);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void doUploadImage(String editPostId, Uri imageUri) {
        // post with image
        StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));

        fileReference.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateDatabase(editPostId, task.getResult().toString());
            } else {
                Toast.makeText(PostActivity.this, "Posting Failed", Toast.LENGTH_SHORT).show();
            }
            dismissProgressDialog();
        }).addOnFailureListener(e -> {
            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            dismissProgressDialog();
        });
    }

    private void updateDatabase(String editPostId, String downloadUri) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        boolean isNewPost = editPostId == null;
        if (isNewPost) {
            // push(): add a new child at the path stored within the database reference
            editPostId = databaseReference.push().getKey();
        }
        if (downloadUri == null) {
            downloadUri = "noImage";
        }

        Map<String, Object> postData = new HashMap<>();
        postData.put("postID", editPostId);
        postData.put("postImage", downloadUri);
        postData.put("postCaption", binding.pCaption.getText().toString());
        postData.put("postAuthor", FirebaseAuth.getInstance().getCurrentUser().getUid());
        postData.put("postTime", System.currentTimeMillis());

        if (!isNewPost) {
            databaseReference.child(editPostId).updateChildren(postData);
            Toast.makeText(PostActivity.this, "Post updated", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child(editPostId).setValue(postData);
            Toast.makeText(PostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            binding.pImageAdded.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Crop activity failed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }
}
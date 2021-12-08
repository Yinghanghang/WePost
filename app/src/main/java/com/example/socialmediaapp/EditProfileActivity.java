package com.example.socialmediaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private StorageReference storageRef;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Profile");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String nameString = dataSnapshot.child("userName").getValue().toString();
                String imageString = dataSnapshot.child("userImage").getValue().toString();
                binding.eUsername.setText(nameString);

                Glide.with(getApplicationContext())
                        .load(imageString)
                        .error(R.drawable.ic_add_image)
                        .placeholder(R.drawable.ic_add_image)
                        .into(binding.ePhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.eCancel.setOnClickListener(v -> finish());

        binding.eChangePhoto.setOnClickListener(v -> CropImage.activity().setAspectRatio(1, 1).start(EditProfileActivity.this));

        binding.ePhoto.setOnClickListener(v -> CropImage.activity().setAspectRatio(1, 1).start(EditProfileActivity.this));

        binding.eSave.setOnClickListener(v -> {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("userName", binding.eUsername.getText().toString());
            databaseReference.updateChildren(profileData);
            uploadImage();
            finish();
        });

        pd = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.getUri();
                binding.ePhoto.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Crop activity failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // cancel Handler's jobs before leaving the activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void uploadImage() {
        pd.setMessage("Uploading image");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            // putFile() takes a File and returns an UploadTask which you can use to manage and monitor the status of the upload.
            fileReference.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String myUrl = downloadUri.toString();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("userImage", "" + myUrl);
                    databaseReference.updateChildren(hashMap);
                    dismissProgressDialog();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Edit profile failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No Image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
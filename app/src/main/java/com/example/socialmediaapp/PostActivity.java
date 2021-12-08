package com.example.socialmediaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.databinding.ActivityPostBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;  // set by crop activity
    private StorageReference storageRef;

    private ActivityPostBinding binding;
    private ProgressDialog progressDialog;
    private Location lastKnownLocation;

    private String editCaption, editImage;  // set if editing the post

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");

        progressDialog = new ProgressDialog(this);

        // location of stored images on cloud
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        //get data through intent from previous activities' adapter (when choose "edit" menu)
        Intent intent = getIntent();
        String isUpdatedKey = intent.getStringExtra("key");
        String editPostId = intent.getStringExtra("editPostId");

        // check if we came here to update post or add post
        if ("editPost".equals(isUpdatedKey)) {
            //update post
            actionBar.setTitle("Edit Post");
            binding.post.setText("UPDATE");
            // load the original data from the post
            loadPostData(editPostId);
        } else {
            // add new post
        }

        binding.pCancel.setOnClickListener(view -> {
            // if click cancel, go to HomeActivity
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
                // Adding a new post without adding an image or editing a post without changing the image
                // editImage == null if it is a new post; editImage == "noImage" if original post has no image
                updateDatabase(editPostId, editImage);
            }
        });

        binding.pImageAdded.setOnClickListener(view -> CropImage.activity()
                .setAspectRatio(1, 1)
                .start(PostActivity.this));

        binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!checkPermission()) {
                    requestPermission();
                }
                // notify the system to update current location
                updatePostLocation();
            }
        });
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
        /* ContentResolver.getType(): get the MIME type corresponding to a content URI
        getExtensionFromMimeType(String mimeType): return the registered extension for the given MIME type.
        */
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
            // show error message
            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            dismissProgressDialog();
        });
    }

    private void updateDatabase(String editPostId, String downloadUri) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        boolean isNewPost = editPostId == null;
        if (isNewPost) {
            // generate a new key by push(): add a new child at the path stored within the database reference
            editPostId = databaseReference.push().getKey();
        }
        if (downloadUri == null) {
            downloadUri = "noImage";
        }

        if (downloadUri.equals("noImage") && binding.pCaption.getText().toString().equals("")) {
            Toast.makeText(PostActivity.this, "Can't send empty post", Toast.LENGTH_SHORT).show();
            dismissProgressDialog();
            return;
        }

        Map<String, Object> postData = new HashMap<>();
        postData.put("postID", editPostId);
        postData.put("postImage", downloadUri);
        postData.put("postCaption", binding.pCaption.getText().toString());
        postData.put("postAuthor", FirebaseAuth.getInstance().getCurrentUser().getUid());
        postData.put("postTime", System.currentTimeMillis());
        if (binding.checkBox.isChecked()) {
            String postLocation = getPostLocation();
            if (postLocation == null) {
                Toast.makeText(this, "Couldn't get current location", Toast.LENGTH_SHORT).show();
            }
            postData.put("postLocation", postLocation);
        } else {
            postData.put("postLocation", null);
        }

        if (!isNewPost) {
            databaseReference.child(editPostId).updateChildren(postData);
            Toast.makeText(PostActivity.this, "Post updated", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child(editPostId).setValue(postData);
            Toast.makeText(PostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
        }
        // go to previous activity
        finish();
    }

    private boolean checkPermission() {
        int result1 = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result1 == PackageManager.PERMISSION_GRANTED
                || result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        // must call updatedPostLocation() here because when user set permission the first time, the updatePostLocation() in binding.checkbox can return null
                        updatePostLocation();
                        break;
                    }
                }
            }
        }
    }

    private String getPostLocation() {
        if (lastKnownLocation == null) {
            //Log.e("tao", "last known location is null");
            return null;
        }
        // change the location to city and state name
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                return String.format("%s, %s", addresses.get(0).getLocality(), addresses.get(0).getAdminArea());
            }
        } catch (IOException e) {
            //Log.e("location", e.getMessage(), e);
        }
        return null;
        //return String.format("%.2f, %.2f", lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
    }

    private void updatePostLocation() {
        //Log.i("tao", "updatePostLocation");
        if (!checkPermission()) {
            Log.e("address", "No location permissions");
            return;
        }
        FusedLocationProviderClient provider =
                LocationServices.getFusedLocationProviderClient(this);
        provider.getLastLocation().addOnSuccessListener(location -> {
            //Log.i("tao", "onSuccessListener");
            lastKnownLocation = location;
        });
        // second method to request the location in case the above location is null since we don't know when successListener is called
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        provider.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i("Pitao", "onLocationResult");
                lastKnownLocation = locationResult.getLastLocation();
            }
        }, getMainLooper());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // get the image uri from crop image activity
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
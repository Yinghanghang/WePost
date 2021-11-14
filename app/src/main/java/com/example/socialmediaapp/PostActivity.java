package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    String myUrl = "";
    private StorageTask uploadTask;
    StorageReference storageRef;

    ImageView photo;
    TextView post, cancel;
    EditText caption;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");
        progressDialog = new ProgressDialog(this);

        cancel = findViewById(R.id.p_cancel);
        photo = findViewById(R.id.p_image_added);
        post = findViewById(R.id.p_post);
        caption = findViewById(R.id.p_caption);

        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, StartActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
//                finish();
                dismissProgressDialog();
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadData(){
        progressDialog.setMessage("Publishing post");
        progressDialog.show();

        if (imageUri != null){
            // post with image
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");

                        // push(): add a new child at the path stored within the database reference
                        String postid = databaseReference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postID", postid);
                        hashMap.put("postImage", myUrl);
                        hashMap.put("postCaption", caption.getText().toString());
                        hashMap.put("postAuthor", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("postTime", System.currentTimeMillis());

                        databaseReference.child(postid).setValue(hashMap);
                        Toast.makeText(PostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Posting Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // post without image
            String description = caption.getText().toString();
            if(TextUtils.isEmpty(description)) {
                Toast.makeText(PostActivity.this, "Please enter description", Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
                String postid = databaseReference.push().getKey();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("postID", postid);
                hashMap.put("postImage", "noImage");
                hashMap.put("postCaption", caption.getText().toString());
                hashMap.put("postAuthor", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("postTime", System.currentTimeMillis());

                databaseReference.child(postid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            photo.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Crop activity failed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, StartActivity.class));
            finish();
        }
    }

    private void dismissProgressDialog() {
        if ( progressDialog!=null &&  progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dismissProgressDialog();
    }
}
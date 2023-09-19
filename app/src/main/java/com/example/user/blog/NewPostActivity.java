package com.example.user.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.google.android.gms.ads.AdView;
import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private ImageView newPostImage;
    private EditText newPostDesc;

    private Uri postImageUri = null;

    private ProgressBar newPostProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private String currentUserId;
    private String downloadUrl;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        final Toolbar newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostProgress = findViewById(R.id.spin_kit);
        DoubleBounce cubeGrid = new DoubleBounce();
        newPostProgress.setIndeterminateDrawable(cubeGrid);

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        final Button newPostBtn = findViewById(R.id.post_btn);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null){

                    newPostProgress.setVisibility(View.VISIBLE);

                    newPostBtn.setClickable(false);
                    newPostToolbar.setClickable(false);
                    newPostDesc.setClickable(false);
                    newPostImage.setClickable(false);

                    final String randomName = UUID.randomUUID().toString();

                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageData = byteArrayOutputStream.toByteArray();

                    UploadTask filePath = storageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
                    filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                File newThumbFile = new File(postImageUri.getPath());
                                try {

                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(1)
                                            .compressToBitmap(newThumbFile);

                                } catch (IOException e) {
                                    Log.i("Result", "Error compressing image : "+e.toString());
                                }

                                ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream1);
                                byte[] thumbData = byteArrayOutputStream1.toByteArray();
/////
                                final StorageReference filepath = storageReference.child("post_images/thumbs").child(randomName + ".jpg");
                                filepath.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                downloadUrl = uri.toString();
                                                Log.i("Result", "Post image saved in firebase storage");
                                                Log.i("Result", downloadUrl);
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {

                                                final Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url", downloadUrl);
                                                postMap.put("image_thumb", downloadUrl);
                                                postMap.put("desc", desc);
                                                postMap.put("user_id", currentUserId);
                                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                                Log.i("Result", postMap.toString());

                                                firebaseFirestore.collection("Posts").add(postMap);
                                                Log.i("Result", "Post added to FirebaseFirestore");

                                                newPostBtn.setClickable(true);
                                                newPostToolbar.setClickable(true);
                                                newPostDesc.setClickable(true);
                                                newPostImage.setClickable(true);
                                                newPostProgress.setVisibility(View.INVISIBLE);

                                                startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        });
                                    }
                                });
/*

                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                        .child(randomName + ".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        download_url = storageReference.child("post_images").child(randomName + ".jpg").getDownloadUrl();

                                        final Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", download_url);
                                        postMap.put("image_thumb", download_url.toString());
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                                    finish();

                                                } else {
                                                    Toast.makeText(NewPostActivity.this, "Could not add post please try again later",Toast.LENGTH_SHORT).show();
                                                }
                                                newPostBtn.setClickable(true);
                                                newPostToolbar.setClickable(true);
                                                newPostDesc.setClickable(true);
                                                newPostImage.setClickable(true);
                                                newPostProgress.setVisibility(View.INVISIBLE);

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                    }
                                });
*/
                            } else {
                                newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                }else{
                    Toast.makeText(NewPostActivity.this, "image and description required", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Log.i("Result", "Error");
            }
        }
    }
    @Override
    public void onBackPressed() {
        Log.i("Result", "Back button pressed, opening Main Activity");
        System.exit(0);
    }
}

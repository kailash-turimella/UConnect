package com.example.user.blog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import static android.widget.Toast.LENGTH_SHORT;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;

    private String userId;
    private String pin;

    private InterstitialAd mInterstitialAd;

    private EditText setupName, setupBio;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private byte[] thumbData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setupBio = findViewById(R.id.setup_bio);
        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progress);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);
        TextView additionalSettingsText = findViewById(R.id.additional_settings_btn);

        additionalSettingsText.setVisibility(View.VISIBLE);

        MobileAds.initialize(this, "ca-app-pub-5850772059044802~1045057353");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        additionalSettingsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mInterstitialAd.isLoaded()) {

                    startActivity(new Intent(SetupActivity.this, FingerprintActivity.class).putExtra("a", 1));


//                    mInterstitialAd.show();


                } else {
                    startActivity(new Intent(SetupActivity.this, FingerprintActivity.class).putExtra("a", 1));
                    Toast.makeText(SetupActivity.this, "The interstitial wasn't loaded", LENGTH_SHORT).show();
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

                Toast.makeText(SetupActivity.this, "failed to load the ad", Toast.LENGTH_LONG).show();
                startActivity(new Intent(SetupActivity.this, FingerprintActivity.class).putExtra("a", 1));

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {

                startActivity(new Intent(SetupActivity.this, FingerprintActivity.class).putExtra("a", 1));

            }
        });


        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(Objects.requireNonNull(task.getResult()).exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String bio = task.getResult().getString("bio");
                        pin = task.getResult().getString("pin");
                        assert pin != null;
                        if(pin.equals("")){
                            Map<String, String> userMap = new HashMap<>();
                            assert name != null;
                            userMap.put("name", name);
                            assert bio != null;
                            userMap.put("bio", bio);
                            assert image != null;
                            userMap.put("image", image);
                            userMap.put("pin","0000");

                            databaseReference.child("Users").child(userId).child("name").setValue(name);
                            databaseReference.child("Users").child(userId).child("bio").setValue(bio);
                            databaseReference.child("Users").child(userId).child("image").setValue(image);
                            databaseReference.child("Users").child(userId).child("pin").setValue("0000");

                            firebaseFirestore.collection("Users").document(userId).set(userMap);
                            Toast.makeText(SetupActivity.this, "You're default 4-digit PIN is 0000. You can change it anytime by going to additional setting and entering a new PIN", Toast.LENGTH_LONG).show();
                        }
                        mainImageURI = Uri.parse(image);

                        setupName.setText(name);
                        setupBio.setText(bio);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);

                    }

                } else {

                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("Result", "SetUp Button Clicked");
                final String userName = setupName.getText().toString();
                final String bio = setupBio.getText().toString();

                if (!TextUtils.isEmpty(userName) && mainImageURI != null && !TextUtils.isEmpty(bio)) {

                        Log.i("Result", "None of the fields are empty");

                        setupProgress.setVisibility(View.VISIBLE);
                        userId = firebaseAuth.getCurrentUser().getUid();

                        try {

                            Log.i("Result", "Trying to compress image");

                            Bitmap bitmap = ((BitmapDrawable) setupImage.getDrawable()).getBitmap();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            thumbData = byteArrayOutputStream.toByteArray();

                            Log.i("Result", "Image compressed successfully");

                        } catch (Exception e) {
                            Toast.makeText(SetupActivity.this, "Error compressing image  : "+e.toString(), LENGTH_SHORT).show();
                            Log.i("Result", "Couldn't compress image  : " + e.toString());
                        }

                        try {

                            final StorageReference filepath = storageReference.child("profile_images").child(userId + ".jpg");
                            filepath.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        mainImageURI = uri;
                                                        Log.i("Result", "Profile image saved in firebase storage");
                                                        Log.i("Result", mainImageURI.toString());
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(SetupActivity.this, "Image error", LENGTH_SHORT).show();
                                                Log.i("Result", "Image error");
                                            }
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (pin == null || pin.equals("") || pin.equals("null")) {
                                                Toast.makeText(SetupActivity.this, "You're default 4-digit PIN is 0000. You can change it anytime by going to additional setting and entering a new PIN", Toast.LENGTH_LONG).show();
                                                pin = "0000";
                                            }

                                            Map<String, String> userMap = new HashMap<>();
                                            userMap.put("name", userName);
                                            userMap.put("bio", bio);
                                            userMap.put("image", mainImageURI.toString());
                                            userMap.put("pin", pin);

                                            databaseReference.child("Users").child(userId).child("name").setValue(userName);
                                            databaseReference.child("Users").child(userId).child("bio").setValue(bio);
                                            databaseReference.child("Users").child(userId).child("image").setValue(mainImageURI.toString());
                                            databaseReference.child("Users").child(userId).child("pin").setValue(pin);
                                            Log.i("Result", "Profile added to database");
                                            Log.i("Result", userMap.toString());

                                            firebaseFirestore.collection("Users").document(userId).set(userMap);
                                            Log.i("Result", "Profile added to FirebaseFirestore");

                                            startActivity(new Intent(SetupActivity.this, MainActivity.class));
                                            finish();

                                        }
                                    });
                                }
                            });
                        }catch(Exception e){
                            Log.i("Result", "Error while uploading data to firebase firestore  : "+e.toString());
                            Toast.makeText(SetupActivity.this, "Error while uploading data to firebase", LENGTH_SHORT).show();
                            setupProgress.setVisibility(View.INVISIBLE);
                        }
/*
                    try {

                        setupProgress.setVisibility(View.VISIBLE);

                        if (isChanged) {
                            user_id = firebaseAuth.getCurrentUser().getUid();

                            File newImageFile = new File(mainImageURI.getPath());
                            try {
                                compressedImageFile = new Compressor(SetupActivity.this)
                                        .setMaxHeight(125)
                                        .setMaxWidth(125)
                                        .setQuality(50)
                                        .compressToBitmap(newImageFile);

                            } catch (IOException e) {
                                Toast.makeText(SetupActivity.this, "Error compressing image", LENGTH_SHORT).show();
                            }
                            ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, bAOS);
                            byte[] thumbData = bAOS.toByteArray();

                            UploadTask image_path = storageReference.child("profile_images").child(user_id + ".jpg").putBytes(thumbData);
                            
                            image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {
                                        storeFirestore(task, user_name, bio);
                                    } else {

                                        String error = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();

                                        setupProgress.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            storeFirestore(null, user_name, bio);
                        }
                    }catch (Exception e){
                        Log.i("Error",e.toString());
                    }
*/
                }else{
                    Toast.makeText(SetupActivity.this, "All fields are required", LENGTH_SHORT).show();
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        BringImagePicker();
                    }
                } else {

                    BringImagePicker();
                }
            }

        });
    }
/*
    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name, String bio) {

        Uri download_uri;

        if (task != null) {
            download_uri = task.getResult().getMetadata().getReference().getDownloadUrl().getResult();
        } else {
            download_uri = mainImageURI;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("bio", bio);
        userMap.put("image", download_uri.toString());
        userMap.put("pin", pin);

        try {
            firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Toast.makeText(SetupActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();
                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    } else {

                        String error = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                    }

                    setupProgress.setVisibility(View.INVISIBLE);

                }
            });
        }catch(Exception e){
            Log.i("Result", e.toString());
        }
    }
*/
    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);

//                boolean isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Toast.makeText(SetupActivity.this, "Couldn't crop image", LENGTH_SHORT).show();
                Log.i("Result", "Error while trying to crop image");

            }
        }

    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SetupActivity.this, MainActivity.class));
    }
}

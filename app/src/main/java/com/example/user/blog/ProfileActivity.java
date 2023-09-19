package com.example.user.blog;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextView username, status;
    private ImageView dp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.profile_username);
        dp = findViewById(R.id.profile_dp);
        status = findViewById(R.id.profile_status);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        final String uid = getIntent().getStringExtra("uid");

        firebaseFirestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(Objects.requireNonNull(task.getResult()).exists()){

                        String name = task.getResult().getString("name");
                        String bio1  = task.getResult().getString("bio");
                        String image = task.getResult().getString("image");

                        username.setText(name);
                        status.setText(bio1);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                            Glide.with(ProfileActivity.this)
                                    .setDefaultRequestOptions(placeholderRequest)
                                    .load(image)
                                    .into(dp);
                    }

                } else {
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Log.i("Result", "Error : "+error);
                }
            }
        });
        username.setVisibility(View.VISIBLE);
        dp.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
    }
}

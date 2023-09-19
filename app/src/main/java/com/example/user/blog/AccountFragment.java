package com.example.user.blog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AccountFragment extends Fragment {

//    private HomeFragment homeFragment;
//    private NotificationFragment notificationFragment;
//    private AccountFragment accountFragment;

    private TextView username, bio, uid;
    private ImageView dp;

    private String userId;

    private FirebaseFirestore firebaseFirestore;

    public AccountFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        username = view.findViewById(R.id.textView);
        bio = view.findViewById(R.id.textView2);
        dp = view.findViewById(R.id.dp);
        uid = view.findViewById(R.id.uid);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(Objects.requireNonNull(task.getResult()).exists()){

                        String name = task.getResult().getString("name");
                        String bio1  = task.getResult().getString("bio");
                        String image = task.getResult().getString("image");

                        username.setText(name);
                        bio.setText(bio1);
                        uid.setText(userId);
                        Log.i("Result", "Data retrieved and displayed successfully");

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        if (getActivity() != null) {
                            Glide.with(getActivity())
                                    .setDefaultRequestOptions(placeholderRequest)
                                    .load(image)
                                    .into(dp);
                        }
                    }
                } else {
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Log.i("Result", "Failed to retrieve data from firebase firestore : "+error);
                }
            }
        });
    }
}

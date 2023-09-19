package com.example.user.blog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.Objects;


public class NotificationFragment extends Fragment {


    public NotificationFragment() {

    }

    private EditText mSearchField;
    private ImageView dp;
    private TextView username, status;
    private Button viewUser;
    private FirebaseFirestore mUserDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        dp = view.findViewById(R.id.search_users_image);
        status = view.findViewById(R.id.search_users_bio);
        username = view.findViewById(R.id.search_users_username);
        viewUser = view.findViewById(R.id.view_user);

        ImageButton mSearchBtn = view.findViewById(R.id.search_btn);
        mSearchField = view.findViewById(R.id.search_field);

        mUserDatabase = FirebaseFirestore.getInstance();

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchText = mSearchField.getText().toString();
                firebaseUserSearch(searchText);

            }
        });
        return view;
    }
    private void firebaseUserSearch(final String searchText) {

        mUserDatabase.collection("Users").document(searchText).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (Objects.requireNonNull(task.getResult()).exists()) {

                    String name = task.getResult().getString("name");
                    String bio1 = task.getResult().getString("bio");
                    String image = task.getResult().getString("image");

                    Picasso.with(getContext()).load(image).into(dp);

                    username.setText(name);
                    status.setText(bio1);

                    status.setVisibility(View.VISIBLE);
                    username.setVisibility(View.VISIBLE);
                    dp.setVisibility(View.VISIBLE);

                    viewUser.setVisibility(View.VISIBLE);
                    viewUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v)
                        {
                            startActivity(new Intent(getContext(), ProfileActivity.class).putExtra("uid", searchText));
                        }
                    });

                } else {

                    username.setText("No User Found");
                    username.setVisibility(View.VISIBLE);
                }

            }
        });
    }
}

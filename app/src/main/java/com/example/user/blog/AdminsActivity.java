package com.example.user.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdminsActivity extends AppCompatActivity {

    private EditText pin, confirmPin;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);

        final Button setPin = findViewById(R.id.setPassword);
        pin = findViewById(R.id.messPassword);
        confirmPin = findViewById(R.id.messConfirmPass);
        progressBar = findViewById(R.id.resetPinProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("Result", "Change PIN button clicked");

                final String sPin = pin.getText().toString();
                String sConfirmPin = confirmPin.getText().toString();

                if(sPin.equals("") || sConfirmPin.equals("")){
                    Log.i("Result", "PIN not entered");
                    Toast.makeText(AdminsActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                }
                else if(!sPin.equals(sConfirmPin)){
                    Log.i("Result", "PINs do not match");
                    Toast.makeText(AdminsActivity.this, "PINs do not match", Toast.LENGTH_SHORT).show();
                }
                else if(sPin.length() != 4){
                    Log.i("Result", "the PIN entered is not a 4-digit PIN");
                    Toast.makeText(AdminsActivity.this, "Your PIN needs to be 4 digits long", Toast.LENGTH_SHORT).show();
                }
                else{

                    Log.i("Result", "Changing PIN");

                    progressBar.setVisibility(View.VISIBLE);
                    pin.setClickable(false);
                    confirmPin.setClickable(false);
                    setPin.setClickable(false);

                    final String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

                    firebaseFirestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.isSuccessful()){

                                if(Objects.requireNonNull(task.getResult()).exists()){

                                    Log.i("Result", "Creating Map");

                                    String userName = task.getResult().getString("name");
                                    String bio = task.getResult().getString("bio");
                                    String downloadUri = task.getResult().getString("image");

                                    Map<String, String> userMap = new HashMap<>();
                                    assert userName != null;
                                    userMap.put("name", userName);
                                    assert bio != null;
                                    userMap.put("bio", bio);
                                    assert downloadUri != null;
                                    userMap.put("image", downloadUri);
                                    userMap.put("pin", sPin);

                                    Log.i("Result", "Uploading Map");
                                    firebaseFirestore.collection("Users").document(uid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.i("Result", "Map Uploaded successfully");
                                                Toast.makeText(AdminsActivity.this, "You have successfully changed your PIN", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                pin.setClickable(true);
                                                confirmPin.setClickable(true);
                                                setPin.setClickable(true);
                                                startActivity(new Intent(AdminsActivity.this, MainActivity.class));
                                            }
                                        }
                                    });

                                }

                            } else {

                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                Log.i("Result", "FIRESTORE Retrieve Error : "+error);
                                Toast.makeText(AdminsActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }
            }
        });

    }
    @Override
    public void onBackPressed() {
        Log.i("Result", "Back button pressed, opening Setup Activity");
        startActivity(new Intent(AdminsActivity.this, SetupActivity.class));
    }
}

package com.example.user.blog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private InterstitialAd mInterstitialAd;

    private ClipboardManager clipboardManager;

    private String currentUserId;

//    private HomeFragment homeFragment;
//    private NotificationFragment notificationFragment;
//    private AccountFragment accountFragment;

    private float acelVal;
    private float acelLast;
    private float shake;

    private Fragment fragment = new HomeFragment();

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commitNow();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        final ImageView copyUid = findViewById(R.id.copy_uid);

        Objects.requireNonNull(getSupportActionBar()).setTitle("U Connect");

        MobileAds.initialize(this, "ca-app-pub-5850772059044802~1045057353");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        if(mAuth.getCurrentUser() != null) {

            BottomNavigationView mainBottomNav = findViewById(R.id.mainBottomNav);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    fragment = null;

                    switch (item.getItemId()) {

                        case R.id.bottom_action_home:
                            fragment = new HomeFragment();
                            copyUid.setVisibility(View.INVISIBLE);
                            break;

                        case R.id.bottom_action_account:
                            fragment = new AccountFragment();
                            copyUid.setVisibility(View.VISIBLE);
                            break;

                        case R.id.bottom_action_notif:
                            fragment = new NotificationFragment();
                            copyUid.setVisibility(View.INVISIBLE);
                            break;

                    }

                    return loadFragment(fragment);

                }
            });

            loadFragment(new HomeFragment());

            copyUid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("uid", currentUserId);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(MainActivity.this, "uid copied", Toast.LENGTH_SHORT).show();

                }
            });

            SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

            acelVal = SensorManager.GRAVITY_EARTH;
            acelLast = SensorManager.GRAVITY_EARTH;
            shake = 0.00f;

            findViewById(R.id.add_post_btn);
            FloatingActionButton addPostBtn;
            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mInterstitialAd.isLoaded()) {

                        startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                        Log.i("Result", "Ad loaded but not show");
//                        mInterstitialAd.show();



                    } else {
                        startActivity(new Intent(MainActivity.this, NewPostActivity.class));
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

                    Toast.makeText(MainActivity.this, "failed to load the ad", Toast.LENGTH_LONG).show();
                    Log.i("Result", "Couldn't load ad");
                    startActivity(new Intent(MainActivity.this, NewPostActivity.class));

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
                    // Code to be executed when the interstitial ad is closed.
                    startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                }
            });


        }else{

            sendToLogin();

        }


    }
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt(x * x + y*y + z*z);
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if(shake > 12){

                Log.i("Result", "Opening Fingerprint Activity");
                startActivity(new Intent(MainActivity.this, FingerprintActivity.class).putExtra("a", 2));
                finish();

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){

            sendToLogin();

        } else {

            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!Objects.requireNonNull(task.getResult()).exists()){

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {

                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


                    }

                }
            });

        }

    }
    @Override
    public void onResume(){
        super.onResume();
        Fragment fragment = new HomeFragment();
        loadFragment(fragment);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_logout_btn:

                Log.i("Result", "Logout");
                mAuth.signOut();
                sendToLogin();
                return true;

            case R.id.action_settings_btn:

                Log.i("Result", "Opening Setup Activity");
                startActivity(new Intent(MainActivity.this, SetupActivity.class));
                return true;

            case R.id.reload:

                Log.i("Result", "Reload button clicked\n"+fragment.toString());
//                Fragment fragment = new HomeFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commitNow();
//                loadFragment(fragment);

            default:
                return false;
        }
    }

    private void sendToLogin() {

        Log.i("Result", "Opening Login Activity");
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    /*
        public static class UsersViewHolder extends RecyclerView.ViewHolder {
            View mView;
            public UsersViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
            }
            void setDetails(Context ctx, String userName, String userStatus, String userImage){

                TextView user_name = mView.findViewById(R.id.all_users_username);
                TextView user_status = mView.findViewById(R.id.all_users_bio);
                ImageView user_image = mView.findViewById(R.id.all_users_image);

                user_name.setText(userName);
                user_status.setText(userStatus);

                Glide.with(ctx).load(userImage).into(user_image);
            }
        }
    */
    @Override
    public void onBackPressed() {
        Log.i("Result", "Back button pressed");
    }
}


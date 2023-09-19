package com.example.user.blog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerprintActivity extends AppCompatActivity {

    private EditText enteredPin;
    private Button checkPin;

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";

    private String userPin;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

//        TextView mHeadingLabel = findViewById(R.id.headingLabel);
//        ImageView mFingerprintImage = findViewById(R.id.fingerprintImage);
        TextView mParaLabel = findViewById(R.id.paraLabel);
        Button button = findViewById(R.id.button);
        enteredPin = findViewById(R.id.pin);
        checkPin = findViewById(R.id.check_pin);
//        userPin = findViewById(R.id.user_pin);
        TextView setPin = findViewById(R.id.set_pin);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Check 1: Android version should be greater or equal to Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Have permission to use fingerprint scanner in the app
        // Check 4: Lock screen is secured with at least 1 type of lock
        // Check 5: At least 1 Fingerprint is registered

        final int mode = getIntent().getIntExtra("a", 0);

        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        setPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Result", "Opening Setup Activity");
                startActivity(new Intent(FingerprintActivity.this, SetupActivity.class));
                finish();
            }
        });

        firebaseFirestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(Objects.requireNonNull(task.getResult()).exists()){

                        userPin = task.getResult().getString("pin");
                        Log.i("Result", "PIN retrieved successfully : "+userPin);
                        enteredPin.setVisibility(View.VISIBLE);
                        checkPin.setVisibility(View.VISIBLE);

                    }else{
                        Log.i("Result", "PIN not found");
                    }
                } else {

                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Log.i("Result", "Failed to get pin from firebase firestore");
                    Toast.makeText(FingerprintActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }
            }
        });
        if(mode == 1){

            setPin.setVisibility(View.INVISIBLE);
            //               Additional settings
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Result", "Opening Additional Setting");
                    startActivity(new Intent(FingerprintActivity.this, AdminsActivity.class));
                    finish();
                }
            });
            checkPin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(enteredPin.length() == 4) {

                        final int intPin = Integer.parseInt(userPin);
                        final int intEnteredPin = Integer.parseInt(enteredPin.getText().toString());

                        if (intPin == intEnteredPin) {
                            Log.i("Result", "Correct password entered, opening Admins Activity");
                            Toast.makeText(FingerprintActivity.this, "Correct password entered", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FingerprintActivity.this, AdminsActivity.class));
                            finish();
                        } else {
                            Log.i("Result", "Incorrect PIN entered");
                            Toast.makeText(FingerprintActivity.this, "Incorrect password entered", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.i("Result", "Invalid input " + enteredPin.length()+" digit PIN entered");
                        Toast.makeText(FingerprintActivity.this, "Enter your 4-digit PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }



        else if(mode == 2){

            //                    Messages
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FingerprintActivity.this, MessagesActivity.class));
                    finish();
                }
            });
            checkPin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(enteredPin.length() == 4) {

                        final int intPin = Integer.parseInt(userPin);
                        final int intEnteredPin = Integer.parseInt(enteredPin.getText().toString());

                        if (intPin == intEnteredPin) {
                            Log.i("Result", "Correct password entered, opening Messages Page Activity");
                            Toast.makeText(FingerprintActivity.this, "Correct password entered", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FingerprintActivity.this, MessagesActivity.class));
                            finish();
                        } else {
                            Log.i("Result", "Incorrect PIN entered");
                            Toast.makeText(FingerprintActivity.this, "Incorrect password entered", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.i("Result", "Invalid input : " + enteredPin);
                        Toast.makeText(FingerprintActivity.this, "Enter your 4-digit PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if (!fingerprintManager.isHardwareDetected()) {

                mParaLabel.setText("Fingerprint Scanner not detected in Device");

            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {

                mParaLabel.setText("Permission not granted to use Fingerprint Scanner");

            } else if (!keyguardManager.isKeyguardSecure()) {

                mParaLabel.setText("Add Lock to your Phone in Settings");

            } else if (!fingerprintManager.hasEnrolledFingerprints()) {

                mParaLabel.setText("You should add at least 1 Fingerprint to use this Feature");

            } else {

                mParaLabel.setText("Place your Finger on Scanner to get Further Access.");

                generateKey();

                if (cipherInit()) {

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                }
            }

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

    @Override
    public void onBackPressed() {
        Log.i("Result", "Back button pressed, opening Main Activity");
        startActivity(new Intent(FingerprintActivity.this, MainActivity.class));
    }
}

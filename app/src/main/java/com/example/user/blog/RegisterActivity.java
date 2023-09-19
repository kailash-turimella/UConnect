package com.example.user.blog;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText regPassField, regConfirmPassField, regEmailField;
    private ProgressBar regProgress;
    private FirebaseAuth mAuth;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        regEmailField = findViewById(R.id.reg_email);
        regPassField = findViewById(R.id.reg_pass);
        regConfirmPassField = findViewById(R.id.reg_confirm_pass);
        Button regBtn = findViewById(R.id.reg_btn);
        Button regLoginBtn = findViewById(R.id.reg_login_btn);
        regProgress = findViewById(R.id.reg_progress);
        CheckBox pass = findViewById(R.id.see_reg_pass);
        CheckBox confirmPass = findViewById(R.id.see_reg_confirm_pass);

        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
        pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    regPassField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    regPassField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });

        confirmPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    regConfirmPassField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    regConfirmPassField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = regEmailField.getText().toString();
                final String pass = regPassField.getText().toString();
                String confirmPass = regConfirmPassField.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){

                    if(pass.equals(confirmPass)){

                        regProgress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    String uid = mAuth.getUid();
                                    databaseReference = FirebaseDatabase.getInstance().getReference();
                                    assert uid != null;
                                    databaseReference.child("Users").child(uid).child("password").setValue(pass);
                                    databaseReference.child("Users").child(uid).child("email").setValue(email);

                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                } else {

                                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();

                                }
                                regProgress.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {

                        Toast.makeText(RegisterActivity.this, "Your passwords are supposed to match.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }
    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}

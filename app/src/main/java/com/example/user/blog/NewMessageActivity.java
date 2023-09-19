package com.example.user.blog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class NewMessageActivity extends AppCompatActivity {

    private ImageView messageImg;
    private EditText messageEt;
    private Button sendMessage;
    private String imageName = UUID.randomUUID().toString() + ".jpg";
    private String downloadUrl = "...";
    private String message;
    private ProgressBar spinKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        messageImg = findViewById(R.id.message_image);
        messageEt = findViewById(R.id.message_et);
        sendMessage = findViewById(R.id.send_message_btn);

        spinKit = findViewById(R.id.new_message_spinkit);
        DoubleBounce cubeGrid = new DoubleBounce();
        spinKit.setIndeterminateDrawable(cubeGrid);
    }
    public void getPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void chooseImage(View view){
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            getPhoto();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImage = data.getData();
        if(requestCode == 1 && resultCode == RESULT_OK){
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                messageImg.setImageBitmap(bitmap);
            }catch(Exception e){
                Log.i("Error", "While loading image"+e.toString());
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if(requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }
    public void sendMessage(View view){
        message = messageEt.getText().toString();
        Log.i("Result", "Send button clicked");
        if(!message.equals("")) {
            Log.i("Result", "Message : "+message);
            messageImg.setClickable(false);
            messageEt.setClickable(false);
            sendMessage.setClickable(false);
            spinKit.setVisibility(View.VISIBLE);
            // Get the data from an ImageView as bytes
            messageImg.setDrawingCacheEnabled(true);
            messageImg.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) messageImg.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            // Converts image view into a bitmap and then a jpg

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("messages").child(imageName).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewMessageActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    Log.i("Result", "Could not add image to storage : " + e.toString());
                    spinKit.setVisibility(View.INVISIBLE);
                    messageImg.setClickable(true);
                    messageEt.setClickable(true);
                    sendMessage.setClickable(true);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Log.i("Result", "Image added to firebase storage");
                    spinKit.setVisibility(View.INVISIBLE);
                    messageImg.setClickable(true);
                    messageEt.setClickable(true);
                    sendMessage.setClickable(true);
                    FirebaseStorage.getInstance().getReference().child("messages").child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            Intent i = new Intent(NewMessageActivity.this, ChooseUsersActivity.class);
                            i.putExtra("imageURL", downloadUrl);
                            i.putExtra("imageName", imageName);
                            i.putExtra("message", message);

                            Log.i("Result", "DownloadUrl = " + downloadUrl);

                            startActivity(i);
                            Log.i("Result", "Opening ChooseUsersActivity");
                        }
                    });
                }
            });
        }else{
            Toast.makeText(NewMessageActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            Log.i("Result","Message is empty");
        }
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(NewMessageActivity.this, MessagesActivity.class));
    }
}

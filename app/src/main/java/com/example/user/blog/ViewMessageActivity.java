package com.example.user.blog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class ViewMessageActivity extends AppCompatActivity {

    private ImageView messageImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);

        TextView messageTextView = findViewById(R.id.viewMessageMessage);
        messageImageView = findViewById(R.id.viewMessageImageView);

        messageTextView.setText(getIntent().getStringExtra("message"));
        download(messageImageView);
    }
    public void download(View view ){

        ImageDownloader task = new ImageDownloader();
        Bitmap image;
        try {
            // Image address
            image = task.execute(getIntent().getStringExtra("imageURL")).get();
            messageImageView.setImageBitmap(image);
        } catch (Exception e) {
            Log.i("Result",e.toString());
            e.printStackTrace();
        }
    }
    public static class ImageDownloader extends AsyncTask<String,Void,Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.i("Result",e.toString());
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Snaps")
                .child(getIntent().getStringExtra("imageKey"))
                .removeValue();

        FirebaseStorage.getInstance().getReference()
                .child("messages")
                .child(getIntent().getStringExtra("imageName"))
                .delete();
    }
}
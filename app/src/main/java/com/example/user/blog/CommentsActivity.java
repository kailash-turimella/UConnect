package com.example.user.blog;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity {

    private EditText commentField;
    private TextView commentCount;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    private FirebaseFirestore firebaseFirestore;

    private String blogPostId;
    private String currentUserId;

    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        blogPostId = getIntent().getStringExtra("blog_post_id");

        commentField = findViewById(R.id.comment_field);
        ImageView commentPostBtn = findViewById(R.id.comment_post_btn);
        RecyclerView commentList = findViewById(R.id.comment_list);
        commentCount = findViewById(R.id.blog_comment_count);

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(new LinearLayoutManager(this));
        commentList.setAdapter(commentsRecyclerAdapter);


        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                    commentsRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });

        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("Result", "Post button clicked");

                final String commentMessage = commentField.getText().toString();

                if(!commentMessage.equals(" ")) {
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMessage);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    commentField.setText("");

                    firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (!task.isSuccessful()) {
                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                Log.i("Result", "Error Posting Comment" + error);
                                Toast.makeText(CommentsActivity.this, "Error Posting Comment : " + error, Toast.LENGTH_SHORT).show();
                                commentCount.setText(count + " Comments");
                                count = count + 1;
                                commentField.setText(commentMessage);
                            } else {
                                Log.i("Result", "Comment posted");
                            }
                        }
                    });
                }else{
                    Log.i("Result", "User didn't enter anything");
                    Toast.makeText(CommentsActivity.this, "Enter something", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Log.i("Result", "Back button pressed, opening Main Activity");
        System.exit(0);
    }
}
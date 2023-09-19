package com.example.user.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> emails = new ArrayList<>();
    private ArrayList<DataSnapshot> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar messagesToolbar = findViewById(R.id.messages_toolbar);
        setSupportActionBar(messagesToolbar);
        messagesToolbar.setTitle("Your Messages");

        ListView messagesListView = findViewById(R.id.messagesListView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emails);
        messagesListView.setAdapter(adapter);
        Log.i("Result", "Loading Messages");

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                        .getUid()).child("Snaps").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                emails.add(Objects.requireNonNull(dataSnapshot.child("from").getValue()).toString());
                messages.add(dataSnapshot);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int index = 0;
                for(DataSnapshot message:messages){
                    if(Objects.equals(message.getKey(), dataSnapshot.getKey())){
                        messages.remove(index);
                        emails.remove(index);
                    }
                    index += 1;
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DataSnapshot dataSnapshot = messages.get(i);

                Intent intent = new Intent(MessagesActivity.this, ViewMessageActivity.class);
                intent.putExtra("imageName", Objects.requireNonNull(dataSnapshot.child("imageName").getValue()).toString());
                intent.putExtra("imageURL",Objects.requireNonNull(dataSnapshot.child("imageURL").getValue()).toString());
                intent.putExtra("message",Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString());
                intent.putExtra("imageKey",dataSnapshot.getKey());
                startActivity(intent);
            }
        });
    }
    public void newMessage(View view){
        Log.i("Result", "New Message Clicked");
        startActivity(new Intent(MessagesActivity.this, NewMessageActivity.class));
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(MessagesActivity.this, MainActivity.class));
        Log.i("Result", "Back button pressed, opening MainActivity");
    }
}

package com.example.user.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChooseUsersActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();

    private DatabaseReference databaseReference;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_users);

        ListView usersList = findViewById(R.id.chooseUserListView);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        usersList.setAdapter(adapter);

        databaseReference.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                name = Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();
                names.add(name);
                keys.add(dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Map<String, String> snapMap = new HashMap<>();
                snapMap.put("from", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()));
                snapMap.put("imageName", getIntent().getStringExtra("imageName"));
                snapMap.put("imageURL", getIntent().getStringExtra("imageURL"));
                snapMap.put("message", getIntent().getStringExtra("message"));

                Log.i("Result", "\n\n"+snapMap.toString()+"\n\n");

                databaseReference.child("Users").child(keys.get(i)).child("Snaps").push().setValue(snapMap);
                Log.i("Result", "Added snapMap to database");

                startActivity(new Intent(ChooseUsersActivity.this, MessagesActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                Log.i("Result", "Going back to MessagesActivity");
            }
        });
    }
}
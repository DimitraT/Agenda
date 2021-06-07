package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Contacts extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();
    ListView list;
    ArrayList<String> listData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        list = findViewById(R.id.list);
        populate();
    }
    public void addContact(View view){
        Intent intent = new Intent(this,AddContact.class);
        intent.putExtra("lista",listData);
        startActivity(intent);
    }

    public void populate(){
        fbHelper.myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //pairnoume tous contacts tou sygkekrimenou xristi
                String contact = snapshot.child(fbHelper.currentUser.getUid()).child("contacts").getValue().toString();
                if (contact.equals(" ")){//an einai " " tote den yparxoun epafes
                    listData.clear();
                    listData.add(getString(R.string.contactsAddContactsTotheList));
                    ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listData);
                    list.setAdapter(adapter);
                    return;
                }
                else{
                    listData.clear();
                    fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("contacts").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            for(DataSnapshot s:snap.getChildren()){
                               listData.add(s.getValue().toString());//pros8ese ka8e paidi sthn lista listData kai emfanise ta
                            }
                            ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listData);
                            list.setAdapter(adapter);
                            return;
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            throw error.toException();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
    }

}
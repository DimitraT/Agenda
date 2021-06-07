package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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


public class Notifications extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();

    ListView notificationsList;
    ArrayList<String> data = new ArrayList<>();
    String NotiTitle, creator,title,hour,eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsList = findViewById(R.id.notificationsList);
        populateNotifications();
    }

    public void populateNotifications() {
        fbHelper.myRef = fbHelper.database.getReference("Users/" + fbHelper.currentUser.getUid() + "/events");
        fbHelper.myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int events = (int) snapshot.getChildrenCount();
                if (events != 0) {
                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                        eventTitle = (String) childDataSnapshot.child("state").getValue();
                        if (eventTitle.equals("invited") ){
                            creator = childDataSnapshot.child("creator").getValue().toString();
                            title = childDataSnapshot.child("title").getValue().toString();
                            hour = childDataSnapshot.child("startDate").getValue().toString();
                            data.add(getString(R.string.notificationsNewEvent)+"\n" +title+ "\n"+ getString(R.string.notifiactionsCreatedBy) +creator +"\n"+ getString(R.string.notifiactionsHours) + " "+ hour);
                        }
                    }
                }
                fbHelper.myRef = fbHelper.database.getReference("Users/"+fbHelper.currentUser.getUid()+"/notifications");
                fbHelper.myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int notis = (int) snapshot.getChildrenCount();
                        if(notis!=0){
                            for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                NotiTitle  = (String)childDataSnapshot.getValue();
                                data.add(NotiTitle);
                            }
                        }
                        ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.noti_list, data);
                        notificationsList.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
    }
}
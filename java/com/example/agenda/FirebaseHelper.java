package com.example.agenda;

import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;

public class FirebaseHelper {

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference myRef;

    public FirebaseHelper(){
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myRef = database.getReference("Users");
        currentUser = mAuth.getCurrentUser();
    }
//exei ola ta events olwn twn hmerwn tou current user
    public void readEvents(CompactCalendarView compactCalendarView){
        myRef = database.getReference("Users/"+currentUser.getUid()+"/events");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int events = (int) snapshot.getChildrenCount();
                if (events != 0){
                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                        String startDate = (String) childDataSnapshot.child("startDate").getValue();
                        try {
                            long epoch = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(startDate).getTime();
                            Event event = new Event(Color.GREEN, epoch);
                            compactCalendarView.addEvent(event);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}

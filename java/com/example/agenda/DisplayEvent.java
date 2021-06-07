package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DisplayEvent extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();

    //key = exei to eventID
    String key,creator,eventId,currentEmail,keyParticipant,title,creatorid;
    TextView eventTitle,eventDescription,eventLocation,eventParticipants,txtstart,txtend,startdate,enddate;
    Switch switch1;
    ArrayList<String> participants = new ArrayList<>();
    ArrayList<String> partWithCreator = new ArrayList<>();
    Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_event);

        currentEmail = fbHelper.currentUser.getEmail();

        switch1 = findViewById(R.id.switch1);
        startdate = findViewById(R.id.startDate);
        enddate = findViewById(R.id.endDate);
        txtstart = findViewById(R.id.txtstart);
        txtend = findViewById(R.id.txtend);
        eventParticipants = findViewById(R.id.eventParticipants);
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventLocation = findViewById(R.id.eventLocation);

        button4 = findViewById(R.id.button4);
        key = getIntent().getStringExtra("key");
        populate();
    }

    //kanoume populate ta stoixeia tou event pou exei pathsei na dei o xrhsths
    public void populate(){
        if(key !=null){
            fbHelper.myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String k = snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("title").getValue(String.class);
                    eventTitle.setText(k);
                    k = snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("description").getValue(String.class);
                    eventDescription.setText(k);
                    k = snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("location").getValue(String.class);
                    eventLocation.setText(k);
                    k =  snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("startDate").getValue(String.class);
                    txtstart.setText(k);
                    k =  snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("endDate").getValue(String.class);
                    txtend.setText(k);
                    boolean t =  snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("allDay").getValue(boolean.class);
                    switch1.setChecked(t);
                    creator = snapshot.child(fbHelper.currentUser.getUid()).child("events").child(key).child("creator").getValue(String.class);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }

            });

            //emfanish twn summetexontwn
            fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String participant =" ";
                    int part = (int) snapshot.getChildrenCount();
                    if (part != 0){
                        for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                            String k =  childDataSnapshot.getValue().toString();
                            participant = participant + " " + k;
                        }
                    }
                    eventParticipants.setText(participant+ " " + creator);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    // edw mpainoume mesa sto event pou kalesan ton current user kai pairnoume ton creator ton eventId kai ton titlo
    //kai mesa sth lista participants kai partWithCreator mpainoun oi participants tou event enw sth 2h bazoume kai ton creator.
    //kai psaxnw me basi to email tou creator prokeimenou na brw to Uid tou
    public void onStart(){
        super.onStart();
        fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                creator = (String) snapshot.child("creator").getValue();
                eventId = (String) snapshot.child("eventId").getValue();
                title = (String) snapshot.child("title").getValue();
                fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int part = (int) snapshot.getChildrenCount();
                        if (part != 0) {
                            for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                String k = childDataSnapshot.getValue().toString();
                                if(!k.equals(currentEmail)) {
                                    participants.add(k);
                                    partWithCreator.add(k);
                                }
                            }
                        }
                        partWithCreator.add(creator);

                        Query query2 = fbHelper.myRef.orderByChild("email").equalTo(creator);
                        query2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot child : snapshot.getChildren()){
                                    creatorid = child.getKey();
                                }
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //otan o xrhsths pathsei to koumpi deny tote gia ka8e participant+creator briskw to Uid tou kai mpainw sta event tou kai psaxnw
    //to sygkekrimeno event kai kanw update tous participants diagrafontas tous palious kai bazontas tous ypoloipous.
    public void deny(View view){
        for (int i=0; i<partWithCreator.size(); i++)
        {
            Query query1 = fbHelper.myRef.orderByChild("email").equalTo(partWithCreator.get(i));
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        //pairnei to id tou ka8e participant
                        keyParticipant = child.getKey();
                    }
                    DatabaseReference ref = fbHelper.database.getReference("Users/"+keyParticipant+"/events");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String l = data.getKey();
                                    String state = (String) data.child("state").getValue();
                                    if (state.equals("invited")){
                                        String NotiTitle = (String) data.child("eventId").getValue();
                                        if (NotiTitle.equals(eventId)) {
                                            ref.child(l).child("participants").removeValue();
                                            ref.child(l).child("participants").setValue(participants);
                                        }
                                    }else{
                                        if(eventId.equals(l)){
                                            ref.child(l).child("participants").removeValue();
                                            ref.child(l).child("participants").setValue(participants);
                                        }
                                    }
                                }
                            }
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
            //diagrafi apo ton xristi to sygkekrimeno event
            fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).removeValue();

            //stelnei eidopoihsh ston creator tou event oti den a paei
            String acceptNoti = getString(R.string.displayEventDenyInvitation) + '\n' + getString(R.string.displayEventFromTheUser) + currentEmail + getString(R.string.displayEventInTheEventWithTitle) + title ;
            fbHelper.myRef.child(creatorid).child("notifications").push().setValue(acceptNoti);
            Toast.makeText(this,R.string.displayEventToastNotitoCreatorDeny,Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
    }

//koumpi apodoxis prosklisis
    public void accept(View view){
        button4.setEnabled(false);
        String acceptNoti = getString(R.string.displayEventAcceptInvitation) + "\n" + getString(R.string.displayEventFromTheUser) + currentEmail + getString(R.string.displayEventInTheEventWithTitle) + title ;
        fbHelper.myRef.child(creatorid).child("notifications").push().setValue(acceptNoti);
        Toast.makeText(this,R.string.displayEventToastNotiToCreatorAccept, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item1: // selida contacts
                Intent intent = new Intent(this,Chat.class);
                intent.putExtra("key",key);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

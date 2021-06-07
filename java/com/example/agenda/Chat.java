package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();

    String key,myMessage,creator,current,eventId,keyParticipant;
    ListView listChat;
    ArrayList<String> comments = new ArrayList<>();
    ArrayList<String> participants = new ArrayList<>();
    EditText message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        current = fbHelper.mAuth.getCurrentUser().getEmail();

        key = getIntent().getStringExtra("key");
        listChat = findViewById(R.id.chatList);
        message = findViewById(R.id.message);
        populate();
    }

    //mpainoume mesa sto sygkekrimeno event kai painoume tous participants kai ton creator
    public void onStart(){
        super.onStart();
        fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                creator = (String)snapshot.child("creator").getValue();
                eventId = (String)snapshot.child("eventId").getValue();
                fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int part = (int) snapshot.getChildrenCount();
                        if (part != 0){
                            for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                String k  =  childDataSnapshot.getValue().toString();
                                participants.add(k);
                            }
                        }
                        participants.add(creator);
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

    //kanoume populate ta comments tou sygkekrimenou event
    public void populate() {
        if (key != null) {
            fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int comment = (int) snapshot.getChildrenCount();
                        if (comment != 0) {
                            for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                String comm = (String) childDataSnapshot.getValue();
                                comments.add(comm);
                            }
                            ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.chat_list, comments);
                            listChat.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }

            });
        }

    }

    //stelnoume to mnm se olous tous participants kai ston creator
    public void sendComment(View view){
        myMessage = current +"\n"+ message.getText().toString();//exei to mnm se morfi email: message

            for (int i=0; i<participants.size(); i++)//gia ka8a participant
            {
                Query query1 = fbHelper.myRef.orderByChild("email").equalTo(participants.get(i));//bres ton me basi to email
                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            //pairnei to id tou ka8e participant
                            keyParticipant = child.getKey();
                        }
                        DatabaseReference ref = fbHelper.database.getReference("Users/"+keyParticipant+"/events");//phgaine sta event tou
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        String l = data.getKey();//pare to kleidi tou event
                                        String state = (String) data.child("state").getValue();
                                        if (state.equals("invited")){//an to state einai invited bale to mnm
                                            String NotiTitle = (String) data.child("eventId").getValue();
                                            if (NotiTitle.equals(eventId)) {
                                                ref.child(l).child("comments").push().setValue(myMessage);
                                            }
                                        }else{
                                            if(eventId.equals(l))
                                                ref.child(l).child("comments").push().setValue(myMessage);
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
                }

        message.setText(" ");
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }

    public void speak(View view){
        //pairno to permission gia thn ixografisi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //an den to exw zitao to permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},235);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent,2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2 && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            message.setText(matches.get(0));//grapse sto editText to mnm pou ekfwnh8hke


        }
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
    }
}
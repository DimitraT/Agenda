package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class AddEvent extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    SharedPreferences preferences;

    LinearLayout createlayout,editlayout;

    Switch switch1;
    TextView startdate,enddate,txtstart,txtend,eventTitle,eventDescription,eventLocation,eventParticipants;
    String finalstart,finalend,time ,date, key=null, creator,key1;

    // listdata = exei mesa tis epafes tou xristi pou tis trabaei apo tin firebase
    ArrayList<String> listData = new ArrayList<>();

    //selected = i lista me tis epilegmenes epafes (tis epafes pou 8elei na pros8esei (0,1)
    ArrayList<Integer> selected = new ArrayList<>();

    // participants = lista pou periexei ta onomata tn epilegmenon atomon
    ArrayList<String> participants = new ArrayList<>();

    //exei tous participants tou arxikou event prin tous peira3ei sto edit
    ArrayList<String> temp = new ArrayList<>();

    Boolean start;
    boolean []check;
    boolean allday;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        key = getIntent().getStringExtra("key");

        switch1 = findViewById(R.id.switch1);
        startdate = findViewById(R.id.startDate);
        enddate = findViewById(R.id.endDate);
        txtstart = findViewById(R.id.txtstart);
        txtend = findViewById(R.id.txtend);
        eventParticipants = findViewById(R.id.eventParticipants);
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventLocation = findViewById(R.id.eventLocation);
        createlayout = findViewById(R.id.createLayout);
        editlayout = findViewById(R.id.editLayout);

        //pairnoume to email tou current user kai ton 8etoume ws creator sta event
        fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                creator = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sday = preferences.getString("selectedDay", " ");//hmera
        String startTime = preferences.getString("timeStart"," ");//00:00
        String endTime = preferences.getString("timeEnd"," ");//23:59
        txtstart.setText(sday + " "+ startTime);
        txtend.setText(sday+ " " + endTime);
        finalstart = sday + " "+ startTime;
        finalend = sday+ " " + endTime;

//SELIDA EDIT-------------------------------------------
        //pairnoume to kleidi tou event apo thn homePage. An den einai null tote to event yparxei
        if(key !=null){
            createlayout.setVisibility(View.GONE);
            editlayout.setVisibility(View.VISIBLE);
            fbHelper.myRef.addListenerForSingleValueEvent(new ValueEventListener() {//fortwnw ta stoixeia tou event gia na kanw edit
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
                    if (t==true){
                        startdate.setEnabled(false);
                        enddate.setEnabled(false);
                        txtstart.setEnabled(false);
                        txtend.setEnabled(false);
                        startdate.setTextColor(Color.GRAY);
                        enddate.setTextColor(Color.GRAY);
                        txtstart.setTextColor(Color.GRAY);
                        txtend.setTextColor(Color.GRAY);
                    }
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
                            temp.add(k);
                            participants.add(k);
                        }
                    }
                    eventParticipants.setText(participant);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }else {
            createlayout.setVisibility(View.VISIBLE);
            editlayout.setVisibility(View.GONE);
        }


//emfanish pop up gia epilogi symmetexontwn kai stis 2 selides
        fbHelper.myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String contact = snapshot.child(fbHelper.currentUser.getUid()).child("contacts").getValue().toString();
                if (contact.equals(" ")) {
                    listData.clear();
                    listData.add(getString(R.string.addEventNoContacts));
                    return;
                } else {
                    listData.clear();
                    fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("contacts").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            for (DataSnapshot s : snap.getChildren()) {
                                listData.add(s.getValue().toString());
                            }
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

//koumpi SAVE meta apo epe3ergasia
    public void save(View view) throws ParseException{
        // elegxos imera enarksis < imera liksis
        if (!(sdf.parse(txtstart.getText().toString()).before(sdf.parse(txtend.getText().toString())))) {
            txtend.setText(txtstart.getText().toString());
            Toast.makeText(this, R.string.addEventDateCheck, Toast.LENGTH_LONG).show();
            return;
        }
        else{
            //Bhmata: 1.diagrafh olou tou event apo ka8e participant
            //        2.3anadhmiourgia tou event se ka8e participant + kombos eventID
            deleteEventfromParticipant();

            //ftiaxnei antikeimeno gia ton creator
            UserInfo userInfo = new UserInfo(creator,eventTitle.getText().toString(),txtstart.getText().toString(),txtend.getText().toString(),eventDescription.getText().toString(),eventLocation.getText().toString(),allday,participants,"creator");
            DatabaseReference partRef = fbHelper.database.getReference("Users/"+ fbHelper.currentUser.getUid()+"/events");
            partRef.child(key).setValue(userInfo);//ta ekxwrei ston creator
            partRef.child(key).child("eventId").setValue(key);

            //Update twn allagwn kai stous participants
            for(int i=0; i<participants.size();i++){
                //pros8etei to notification stous participants
                Query query = fbHelper.myRef.orderByChild("email").equalTo(participants.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            //pairnei to id tou ka8e participant
                            key1 = child.getKey();
                            DatabaseReference partRef = fbHelper.database.getReference("Users/" + key1 + "/events");
                            partRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String myKey = partRef.push().getKey();
                                        UserInfo userInfo = new UserInfo(creator,eventTitle.getText().toString(),txtstart.getText().toString(),txtend.getText().toString(),eventDescription.getText().toString(),eventLocation.getText().toString(),allday,participants,"invited");
                                        partRef.child(myKey).setValue(userInfo);
                                        //ekxwroue se ka8e eventID to id tou event
                                        partRef.child(myKey).child("eventId").setValue(key);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            Toast.makeText(this,R.string.addEventSuccess,Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
        }
    }

    public void deleteEventfromParticipant(){
        int to;
        if(temp.size()>participants.size()){
            to = temp.size();
        }
        for(int j=0; j<temp.size(); j++){//gia ka8e participant
            Query query = fbHelper.myRef.orderByChild("email").equalTo(temp.get(j));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        //pairnei to id tou ka8e participant
                        key1 = child.getKey();
                        DatabaseReference ref = fbHelper.database.getReference("Users/"+key1+"/events");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        int children = (int)snapshot.getChildrenCount();
                                        String l = data.getKey();
                                        String state = (String) data.child("state").getValue();
                                        if (state.equals("invited")){
                                            String NotiTitle = (String) data.child("eventId").getValue();
                                            if (NotiTitle.equals(key)) {
                                                ref.child(l).removeValue();//kane remove olo to event
                                                if (children==1){
                                                    DatabaseReference newRef = fbHelper.database.getReference("Users/"+key1);
                                                    newRef.child("events").setValue(" ");
                                                }

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

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

//koumpi delete
    public void delete(View view){
        fbHelper.myRef.child(fbHelper.currentUser.getUid()).child("events").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deleteEventfromParticipant();//diagrafh tou event apo ka8e participant
                snapshot.getRef().removeValue();//diagrafh apo ton creator
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent intent = new Intent(getApplicationContext(),Home.class);
        startActivity(intent);
    }

//koumpi cancel einai idio kai stis 2 selides --------------------------------------------------------------------------------------
    public void cancel(View view){
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }


// patima switch----------------------------------
    public void clicked(View view){
        if(switch1.isChecked()){
            startdate.setEnabled(false);
            enddate.setEnabled(false);
            txtstart.setEnabled(false);
            txtend.setEnabled(false);
            startdate.setTextColor(Color.GRAY);
            enddate.setTextColor(Color.GRAY);
            txtstart.setTextColor(Color.GRAY);
            txtend.setTextColor(Color.GRAY);
            String[] separated = txtstart.getText().toString().split(" ");
            String mera = separated[0];
            txtend.setText(mera+" "+ "23:59");
            txtstart.setText(mera +" "+ "00:00");
            allday = true;
        }else{
            startdate.setEnabled(true);
            startdate.setEnabled(true);
            txtstart.setEnabled(true);
            txtend.setEnabled(true);
            startdate.setTextColor(Color.BLACK);
            enddate.setTextColor(Color.BLACK);
            txtstart.setTextColor(Color.BLACK);
            txtend.setTextColor(Color.BLACK);
            allday = false;
        }
    }

    // o xristis orizei to 'APO'
    public void StartDate(View view){
        start = true;
        initDatePicker();
        datePickerDialog.show();
    }

    // epilogi imeras kai oras
    public void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                String monthString = String.valueOf(month+1);
                String dayString = String.valueOf(day);
                if (monthString.length() == 1) {
                    monthString = "0" + monthString;
                }
                if(dayString.length()==1){
                    dayString = "0"+dayString;
                }
                date = dayString + "/" + monthString+ "/" + year;
                // an exei pati8ei to koumpi 'apo' (startdate) tote kanoume set.text to finalstart
                if (start==true){
                    finalstart = date;
                    txtstart.setText(finalstart);
                }//exei pati8ei to koumpi 'mexri'
                else{
                    finalend = date;
                    txtend.setText(finalend);
                }
            }
        };
       Calendar cal = Calendar.getInstance();
       int year = cal.get(Calendar.YEAR);
       int month = cal.get(Calendar.MONTH);
       int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,dateSetListener,year,month,day);
        initTimePicker();
        timePickerDialog.show();
    }
//epilogi oras
    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                time = i +":" + i1;
                if(start == true){
                    finalstart = date + " " + time;
                    txtstart.setText(finalstart);
                }
                else{
                    finalend = date + " " + time;
                    txtend.setText(finalend);
                }

            }
        };
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this,timeSetListener,hours,min,true);
    }
    //patima mexri
    public void EndDate(View view){
        start = false;
        initDatePicker();
        datePickerDialog.show();
    }

//SELIDA ADD EVENT----------------------------------------------
 // koumpi dimiourgia
    public void create(View view) throws ParseException {
        String title = eventTitle.getText().toString();
        // elegxos imera enarksis < imera liksis
        if ( !(sdf.parse(txtstart.getText().toString()).before(sdf.parse(txtend.getText().toString())))) {
            txtend.setText(txtstart.getText().toString());
            Toast.makeText(this, R.string.addEventDateCheck, Toast.LENGTH_LONG).show();
            return;
        }
        else{
            //klasi userinfo apo8ikeysi neou event
            if(TextUtils.isEmpty(title))
                eventTitle.setText(R.string.homeTitle);

            UserInfo userInfo = new UserInfo(creator,eventTitle.getText().toString(),txtstart.getText().toString(),txtend.getText().toString(),eventDescription.getText().toString(),eventLocation.getText().toString(),allday,participants,"creator");
            DatabaseReference partRef = fbHelper.database.getReference("Users/"+ fbHelper.currentUser.getUid()+"/events");
            String eventKey = partRef.push().getKey();//kanei push ena kainourgio kleidi
            partRef.child(eventKey).setValue(userInfo);//setValue sto kleidi pou dhmiourgh8hke
            partRef.child(eventKey).child("eventId").setValue(eventKey);

            for(int i=0; i<participants.size();i++){
                //pros8etei to notification stous participants
                Query query = fbHelper.myRef.orderByChild("email").equalTo(participants.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child: snapshot.getChildren())
                        {
                            //pairnei to id tou ka8e participant
                            key1 = child.getKey();
                        }
                        DatabaseReference partRef = fbHelper.database.getReference("Users/"+key1+"/events");
                        String myKey = partRef.push().getKey();
                        UserInfo userInfo = new UserInfo(creator,eventTitle.getText().toString(),txtstart.getText().toString(),txtend.getText().toString(),eventDescription.getText().toString(),eventLocation.getText().toString(),allday,participants,"invited");
                        partRef.child(myKey).setValue(userInfo);
                        //ekxwroue se ka8e eventID to id tou event tou creator
                        partRef.child(myKey).child("eventId").setValue(eventKey);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            Toast.makeText(this,R.string.addEventSuccess,Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
    }

// alert box gia tin epilogi simmetexonton-----------------------------------------------------------
    public void participants(View view) {
        eventParticipants.setText(" ");
        selected.clear();
        participants.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);
        builder.setTitle("Επιλογή Συμμετεχόντων");
        builder.setCancelable(false);

        String[] listArray = new String[listData.size()];
        for(int i=0; i<listArray.length;i++){
            listArray[i]= listData.get(i);
        }
        check = new boolean[listData.size()];
        builder.setMultiChoiceItems(listArray, check, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if(b){
                    selected.add(i);
                    Collections.sort(selected);
                }else{
                    selected.remove(i);
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringBuilder stringBuilder = new StringBuilder();
                for(int j=0; j<selected.size(); j++){
                    stringBuilder.append(listData.get(selected.get(j)));
                    String name = listData.get(selected.get(j));
                    participants.add(name);
                    if(j!=selected.size()-1){
                        stringBuilder.append(", ");
                    }
                }
                eventParticipants.setText(stringBuilder.toString());
            }
        });

        builder.setNegativeButton(getString(R.string.addEventSetNegativeButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    //epilogi topo8esias apo ton xarti
    public  void locationPicker(View view){

        //permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},234);
            return;
        }
        else {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                //anoigma xarti
                startActivityForResult(builder.build(AddEvent.this), 1);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                float latitude = (float)place.getLatLng().latitude;
                float longitude = (float)place.getLatLng().longitude;
                String loc = String.format(getString(R.string.addEventLongitude)+ longitude +" "+ getString(R.string.addEventLatitude) + latitude);
                eventLocation.setText(loc);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item1: //selida chat
                if(key!=null){
                    Intent intent = new Intent(this,Chat.class);
                    intent.putExtra("key",key);
                    startActivity(intent);
                    finish();
                }else
                    Toast.makeText(getApplicationContext(),R.string.addEventMessage,Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
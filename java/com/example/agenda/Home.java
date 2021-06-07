package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Home extends AppCompatActivity {

    FirebaseHelper fbHelp = new FirebaseHelper();
    CompactCalendarView compactCalendarView;
    SharedPreferences preferences;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MMMM", Locale.getDefault());
    String date,clickedDay,mera,wra;
    TextView textView,monthTV;

    //data = lista pou exei thn hmera,wra,titlo,status tou ka8e event mias meras
    ArrayList<String> data = new ArrayList<>();
    //keyList = lista pou exei to ka8e kleidi apo ka8e event pou yparxei sthn data
    ArrayList<String> keyList = new ArrayList<>();

    ListView eventlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        eventlist = findViewById(R.id.eventlist);

        monthTV = findViewById(R.id.monthTV);
        monthTV.setText(monthFormat.format(new Date()));
        textView = findViewById(R.id.textView);
        textView.setText(fbHelp.currentUser.getEmail());

        //dhmiourgia highlights gia ta event tou sto calendar
        fbHelp.readEvents(compactCalendarView);

        //ta prefernces exoun thn hmera kai wra pou epilegei o xrhsths
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        date = sdf.format(new Date());

        editor.putString("timeStart", "00:00");
        editor.putString("timeEnd", "23:59");
        editor.putString("selectedDay", date);
        editor.commit();

        //ekxwroume thn epilegmenh hmera sto clickedDay kai emfanizoume ta events kalwntas thn populateEvents.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                clickedDay  = sdf.format(dateClicked);
                date = clickedDay;
                data.clear();
                keyList.clear();
                populateEvents();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                monthTV.setText(monthFormat.format(firstDayOfNewMonth));
            }
        });
    }

    public void onStart(){
        super.onStart();
        //edw 8a emfanistoun ta events ths currentDate
        clickedDay = sdf.format(new Date());
        populateEvents();

        //click gia proboli event apo to list view
        eventlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                fbHelp.myRef = fbHelp.database.getReference("Users/"+fbHelp.currentUser.getUid()+"/events");
                fbHelp.myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String state = (String) snapshot.child(keyList.get(i)).child("state").getValue();
                        if (state.equals("creator")){
                            Intent intent = new Intent(getApplicationContext(),AddEvent.class);
                            intent.putExtra("key",keyList.get(i));
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(getApplicationContext(),DisplayEvent.class);
                            intent.putExtra("key",keyList.get(i));
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }


    public void populateEvents(){
        fbHelp.myRef = fbHelp.database.getReference("Users/"+fbHelp.currentUser.getUid()+"/events");
        fbHelp.myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int event = (int) snapshot.getChildrenCount();
                if (event != 0){
                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                        String key = childDataSnapshot.getKey();
                        String startDate = (String)childDataSnapshot.child("startDate").getValue();
                        String title = (String)childDataSnapshot.child("title").getValue();
                        String state = (String) childDataSnapshot.child("state").getValue();
                        if(title.equals(null))
                            title = getString(R.string.homeTitle);
                        String[] separated = startDate.split(" ");
                        mera = separated[0];
                        wra = separated[1].trim();
                        if (mera.equals(clickedDay)){
                            String status;
                            if(state.equals("creator"))
                                status = getString(R.string.homeStatusCreator);
                            else
                                status = getString(R.string.homeStatusInvited);
                            data.add(title+"\n"+mera +" " + wra + "\n" + status);
                            keyList.add(key);
                        }

                    }
                    ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.home_list, data);
                    eventlist.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addEvent(View view){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedDay", date);
        editor.apply();

        Intent intent = new Intent(this,AddEvent.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item3: //selida notification
                startActivity(new Intent(getApplicationContext(),Notifications.class));
                finish();
                return true;

            case R.id.item2: //selida logout
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
                return true;

            case R.id.item1: // selida contacts
                startActivity(new Intent(getApplicationContext(),Contacts.class));
                return true;

            case R.id.item4: // language
                Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(languageIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
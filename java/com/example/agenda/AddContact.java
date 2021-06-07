package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class AddContact extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();

    TextView search;
    ArrayList<String> listData = new ArrayList<>();
    boolean exist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        search = findViewById(R.id.search);

        listData = getIntent().getStringArrayListExtra("lista");


    }

    //koumpi dhmioyrgia epafhs
    public void createContact(View view){
        String emailSearch = search.getText().toString();

        if (TextUtils.isEmpty(emailSearch)) {
            Toast.makeText(this, R.string.addContactToastAddanEmail, Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = fbHelper.myRef.orderByChild("email").equalTo(emailSearch);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for(int i=0; i<listData.size();i++){// elegxos gia to an to email yparxei
                        if(listData.get(i).equals(emailSearch)){
                            exist = false;
                            break;
                        }
                        else
                            exist = true;
                    }
                    if(exist == true){
                        DatabaseReference userRef = fbHelper.database.getReference("Users/"+ fbHelper.currentUser.getUid());
                        userRef.child("contacts").push().setValue(emailSearch);
                        Toast.makeText(getApplicationContext(), R.string.addContactToastSuccess,Toast.LENGTH_SHORT).show();
                        listData.add(emailSearch);
                    }else{
                        search.setText(" ");
                        Toast.makeText(getApplicationContext(),R.string.addContactToastUserIsAlready,Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),R.string.addContactToastUserdoesnotExist,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });


    }


}
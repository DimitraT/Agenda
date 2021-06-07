package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();
    TextView mail,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mail = findViewById(R.id.emailtxt);
        pass = findViewById(R.id.passtxt);

    }

    public void login(View view){
        String email = mail.getText().toString();
        String password = pass.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this, R.string.mainToastEnterEmailAndPass, Toast.LENGTH_SHORT).show();
            return;
        }
        fbHelper.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),R.string.mainToastSuccessLogin,Toast.LENGTH_SHORT);
                    FirebaseUser user = fbHelper.mAuth.getCurrentUser();
                    updateUI(user);
                }
                else {
                    Toast.makeText(getApplicationContext(),R.string.mainToastFailLogin,Toast.LENGTH_SHORT);
                    updateUI(null);
                }
            }
        });
    }

    public void updateUI(FirebaseUser currentUser) {
        Intent profileIntent = new Intent(getApplicationContext(), Home.class);
        startActivity(profileIntent);
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = fbHelper.mAuth.getCurrentUser();
        if (currentUser!=null) {
            updateUI(currentUser);
        }
    }

    public void signup(View view){
        Intent intent = new Intent(this,SignUp.class);
        startActivity(intent);
    }
}
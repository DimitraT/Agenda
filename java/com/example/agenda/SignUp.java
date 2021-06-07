package com.example.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    FirebaseHelper fbHelper = new FirebaseHelper();
    TextView email,pass,name,birth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.emailtext);
        pass = findViewById(R.id.passwordtext);
        name = findViewById(R.id.fullnametext);
        birth = findViewById(R.id.dateofbirthtext);
    }


    public void signup(View view) {
        String mail = email.getText().toString();
        String password = pass.getText().toString();
        String fname = name.getText().toString();
        String birthdate = birth.getText().toString();
        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fname) || TextUtils.isEmpty(birthdate)) {
            Toast.makeText(this, R.string.sighUpToastFillAllTheFields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!birthdate.matches("\\d{2}-\\d{2}-\\d{4}")) {
            Toast.makeText(this, R.string.sighUpToastCorrectFormat, Toast.LENGTH_SHORT).show();
            return;
        }

        fbHelper.mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //klasi users kai ta ekxoro stin realtime
                            fbHelper.currentUser = fbHelper.mAuth.getCurrentUser();
                            UserInfo userdetails = new UserInfo(mail,fname, birthdate," "," "," ");
                            fbHelper.myRef.child(fbHelper.currentUser.getUid()).setValue(userdetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, R.string.sighUpToastSuccefullySignIn, Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(), Home.class));
                                    } else {
                                        Toast.makeText(SignUp.this, R.string.sighUpToastUnsuccefullySignIn, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignUp.this, R.string.sighUpToastSomethingWentWrong, Toast.LENGTH_LONG).show();
                        }
                    }
        });


    }

    public void goToSignin(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
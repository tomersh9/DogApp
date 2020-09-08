package com.example.dogapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes

    boolean bool = false;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bool = true;
                firebaseAuth.addAuthStateListener(authStateListener);
            }
        }, 1300);

        //fixed second at start
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (bool) {

                    final FirebaseUser user = firebaseAuth.getCurrentUser(); //get current user

                    if (user != null) {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
    }
}


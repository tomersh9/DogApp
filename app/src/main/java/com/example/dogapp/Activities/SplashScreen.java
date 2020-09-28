package com.example.dogapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes

    boolean bool = false;

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        //fixed portrait mode
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageView imageView =  findViewById(R.id.splash_bg);
        imageView.animate().scaleX(4f).scaleY(2).setDuration(1500).withEndAction(new Runnable() {
            @Override
            public void run() {
                bool = true;
                firebaseAuth.addAuthStateListener(authStateListener);
            }
        }).start();

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


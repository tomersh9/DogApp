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

import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes

    boolean bool = false;
    String userId;
    int counter;  //first time run flag

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

        readCounter();


        if (counter == 0) {

            if (firebaseAuth.getUid() != null) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseAuth.getUid());

                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            userId = user.getId();
                            authStateListener.onAuthStateChanged(firebaseAuth);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }

        //fixed portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageView imageView = findViewById(R.id.splash_bg);
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
                    if (counter == 0) {
                        if (userId != null) {
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        counter++;
                        saveCounter();
                    } else {
                        if (firebaseAuth.getUid() != null) {
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
            }
        };
    }


    private void saveCounter() {
        try {
            FileOutputStream fos = openFileOutput("counter_flag", MODE_PRIVATE);
            fos.write(counter);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCounter() {
        try {
            FileInputStream fis = openFileInput("counter_flag");
            counter = fis.read();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
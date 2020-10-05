package com.example.dogapp.Activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.dogapp.Models.ModelComment;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    //const keys
    //private final String EMAIL_CHANGE_KEY = "change_email_pref_et";
    private final String GENDER_CHANGE_KEY = "gender_list_preference";
    private final String DISPLAY_NAME_CHANGE_KEY = "name_et_preference";
    private final String ABOUT_ME_CHANGE_KEY = "about_me_et_preference";
    private final String RESET_PASSWORD_KEY = "send_pass_reset_pref";
    private final String LOCATION_CHANGE_KEY = "location_et_preference";

    //walker settings keys
    private final String EXP_CHANGE_KEY = "exp_list_preference";
    private final String RANGE_CHANGE_KEY = "range_list_preference";
    private final String SIZES_CHANGE_KEY = "sizes_multi_list_preference";
    private final String LAST_CALL_KEY = "last_call_cb_preference";
    private final String PAYMENT_CHANGE_KEY = "payment_et_preference";

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences sp;

    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    private AlertDialog progressDialog;
    private ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        sp.registerOnSharedPreferenceChangeListener(listener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_layout);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setTitle(getString(R.string.settings));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        boolean isWalker = getIntent().getBooleanExtra("isWalker", false);
        boolean lastCall = getIntent().getBooleanExtra("lastCall",false);

        progressBar = findViewById(R.id.settings_progress_bar);

        if (findViewById(R.id.settings_frag_root) != null) {
            if (savedInstanceState != null) {
                return;
            }
            if (!isWalker) {
                getSupportFragmentManager().beginTransaction().add(R.id.settings_frag_root, new PreferenceSettingsFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().add(R.id.settings_frag_root, new PreferenceWalkerSettingsFragment()).commit();
            }

        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().clear().commit();
        sp.edit().putBoolean(LAST_CALL_KEY,lastCall).commit(); //save checkbox state

        //listen to sp events
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {

                    case DISPLAY_NAME_CHANGE_KEY:
                        final String name = sharedPreferences.getString(key, "");
                        if (name.isEmpty()) {
                            Snackbar.make(findViewById(R.id.settings_layout_root), R.string.field_empty_error, Snackbar.LENGTH_SHORT).show();
                        } else {

                            progressBar.setVisibility(View.VISIBLE);

                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        updateDB("fullName",name);
                                        updatePostAndComments();
                                    } else {
                                        Snackbar.make(findViewById(R.id.settings_layout_root), getString(R.string.unable_make_change), Snackbar.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        break;

                    case GENDER_CHANGE_KEY:
                        progressBar.setVisibility(View.VISIBLE);
                        Integer i = Integer.parseInt(sharedPreferences.getString(key, "0"));
                        updateDB("gender",i);
                        break;

                    case ABOUT_ME_CHANGE_KEY:
                        String about = sharedPreferences.getString(key, "");
                        if (about.isEmpty()) {
                            Snackbar.make(findViewById(R.id.settings_layout_root), R.string.field_empty_error, Snackbar.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            updateDB("aboutMe",about);
                        }
                        break;

                    case RESET_PASSWORD_KEY:
                        final String emailToSend = sharedPreferences.getString(key, "");
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailToSend).matches() || emailToSend.isEmpty()) {
                            Snackbar.make(findViewById(R.id.settings_layout_root), R.string.invalid_email, Snackbar.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            FirebaseAuth.getInstance().sendPasswordResetEmail(emailToSend).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(findViewById(R.id.settings_layout_root), R.string.email_sent, Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(findViewById(R.id.settings_layout_root), R.string.invalid_email, Snackbar.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                        break;

                    case LOCATION_CHANGE_KEY:
                        String location = sharedPreferences.getString(key, "");
                        if (location.isEmpty()) {
                            Snackbar.make(findViewById(R.id.settings_layout_root), R.string.field_empty_error, Snackbar.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            updateDB("location",location);
                        }
                        break;

                    //*****WALKER SETTINGS*****//
                    case EXP_CHANGE_KEY:
                        progressBar.setVisibility(View.VISIBLE);
                        Integer exp = Integer.parseInt(sharedPreferences.getString(key, "0"));
                        updateDB("experience",exp);
                        break;

                    case RANGE_CHANGE_KEY:
                        progressBar.setVisibility(View.VISIBLE);
                        Integer range = Integer.parseInt(sharedPreferences.getString(key, "0"));
                        updateDB("kmRange",range);
                        break;

                    case SIZES_CHANGE_KEY:
                        progressBar.setVisibility(View.VISIBLE);
                        Set<String> set = sharedPreferences.getStringSet(key,null);
                        ArrayList<String> arrayList = new ArrayList<>(set);
                        List<Integer> valuesList = new ArrayList<>();
                        for(String s : arrayList) {
                            valuesList.add(Integer.parseInt(s));
                        }
                        updateDB("dogSizesList",valuesList);
                        break;

                    case LAST_CALL_KEY:
                        progressBar.setVisibility(View.VISIBLE);
                        Boolean lastCall = sharedPreferences.getBoolean(key,false);
                        updateDB("lastCall",lastCall);
                        break;

                    case PAYMENT_CHANGE_KEY:

                        String result = sharedPreferences.getString(key,"");
                        Integer payment;

                        if(result.isEmpty()) {
                            Snackbar.make(findViewById(R.id.settings_layout_root), R.string.field_empty_error, Snackbar.LENGTH_SHORT).show();
                            break;
                        } else if(result.matches("[0-9]+")) { //a number
                            payment = Integer.parseInt(result);
                            if(payment == 0) {
                                break;
                            } else {
                                progressBar.setVisibility(View.VISIBLE);
                                updateDB("paymentPerWalk",payment);
                            }
                        }

                        break;

                }
            }
        };

    }

    private void updateDB(String field, Object value) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(field, value);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Snackbar.make(findViewById(R.id.settings_layout_root), R.string.change_successful, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.settings_layout_root), getString(R.string.unable_make_change), Snackbar.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updatePostAndComments()
    {
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //update my posts
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        final ModelPost post = ds.getValue(ModelPost.class);
                        if (post.getuId().equals(fUser.getUid())) {
                            Map<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uName", fUser.getDisplayName());
                            postRef.child(ds.getKey()).updateChildren(hashMap);
                        }

                        //update my comments
                        postRef.child(post.getpId()).child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        ModelComment comment = dataSnapshot.getValue(ModelComment.class);
                                        if (comment.getuId().equals(fUser.getUid())) {

                                            Map<String, Object> picMap = new HashMap<>();
                                            picMap.put("uName", fUser.getDisplayName());
                                            postRef.child(post.getpId()).child("Comments").child(comment.getcId()).updateChildren(picMap);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserStatus(Boolean status) {
        if (fUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserStatus(false);
    }

    public static class PreferenceSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_preferences);
        }
    }

    public static class PreferenceWalkerSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_walker_preferences);
        }
    }

}
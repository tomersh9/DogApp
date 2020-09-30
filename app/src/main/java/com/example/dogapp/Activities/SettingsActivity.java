package com.example.dogapp.Activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    //const keys
    private final String EMAIL_CHANGE_KEY = "change_email_pref_et";
    private final String GENDER_CHANGE_KEY = "gender_list_preference";
    private final String DISPLAY_NAME_CHANGE_KEY = "name_et_preference";
    private final String ABOUT_ME_CHANGE_KEY = "about_me_et_preference";
    private final String RESET_PASSWORD_KEY = "send_pass_reset_pref";

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences sp;

    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    private AlertDialog progressDialog;

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

        if (findViewById(R.id.settings_frag_root) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction().add(R.id.settings_frag_root, new PreferenceSettingsFragment()).commit();
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //listen to sp events
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {

                    case EMAIL_CHANGE_KEY:

                        final String email = sharedPreferences.getString(key,"");

                        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(SettingsActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
                        }
                        else {
                            //buildLoaderDialog("Updating your email");
                            /*fUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        //change in database also and then prompt success dialog
                                        buildConfirmDialog("Email address changed successfuly!",email);

                                    } else {
                                        buildFailDialog("Could not change email!",email + " already exist!");
                                    }
                                    progressDialog.dismiss();
                                }
                            });*/
                        }
                        break;

                    case DISPLAY_NAME_CHANGE_KEY:
                        final String name = sharedPreferences.getString(key,"");
                        if(name.isEmpty()) {
                            Toast.makeText(SettingsActivity.this, "Invalid Name!", Toast.LENGTH_SHORT).show();
                        } else {

                            buildLoaderDialog(getString(R.string.applying_change));

                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        updateNameInDB(name);
                                    } else {
                                        buildFailDialog(getString(R.string.went_wrong),getString(R.string.try_again));
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                        break;

                    case GENDER_CHANGE_KEY:
                        buildLoaderDialog(getString(R.string.applying_change));
                        Integer i = Integer.parseInt(sharedPreferences.getString(key,"0"));
                        updateGenderInDB(i);
                        break;

                    case ABOUT_ME_CHANGE_KEY:
                        String about = sharedPreferences.getString(key,"");
                        if(about.isEmpty()) {
                            Toast.makeText(SettingsActivity.this, "Invalid change!", Toast.LENGTH_SHORT).show();
                        } else {
                            buildLoaderDialog(getString(R.string.applying_change));
                            updateAboutMeInDB(about);
                        }
                        break;

                    case RESET_PASSWORD_KEY:
                        final String emailToSend = sharedPreferences.getString(key,"");
                        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailToSend).matches() || emailToSend.isEmpty()) {
                            Toast.makeText(SettingsActivity.this, R.string.invalid_address, Toast.LENGTH_SHORT).show();
                        } else {
                            buildLoaderDialog(getString(R.string.applying_change));
                            FirebaseAuth.getInstance().sendPasswordResetEmail(emailToSend).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        buildConfirmDialog(getString(R.string.email_sent),getString(R.string.password_reset_sent_to) + " " + emailToSend);
                                    } else {
                                        buildFailDialog(getString(R.string.failed), getString(R.string.the_address) + " " + emailToSend + " " + getString(R.string.not_found));
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        }

                        break;
                }
            }
        };

    }

    private void updateAboutMeInDB(String about) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("aboutMe",about);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    buildConfirmDialog(getString(R.string.change_success),getString(R.string.about_me_changed));
                } else {
                    buildFailDialog(getString(R.string.failed),getString(R.string.went_wrong));
                }
                progressDialog.dismiss();
            }
        });
    }

    private void updateGenderInDB(final Integer i) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("gender",i);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    String[] arr = getResources().getStringArray(R.array.gender_key);
                    buildConfirmDialog(getString(R.string.change_success),getString(R.string.now_displayed_as) + " " + arr[i]);
                } else {
                    buildFailDialog(getString(R.string.failed),getString(R.string.went_wrong));
                }
                progressDialog.dismiss();
            }
        });
    }

    private void updateNameInDB(final String name) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("fullName",name);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    buildConfirmDialog(getString(R.string.dis_name_update_succ), name);
                } else {
                    buildFailDialog(getString(R.string.went_wrong),getString(R.string.try_again));
                }
                progressDialog.dismiss();
            }
        });
    }

    private void buildLoaderDialog(String body) {
        final View dialogView;
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        dialogView = getLayoutInflater().inflate(R.layout.loader_dialog, null);
        TextView bodyTv = dialogView.findViewById(R.id.loader_tv);
        bodyTv.setText(body);
        progressDialog = builder1.setView(dialogView).setCancelable(false).show();
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void buildConfirmDialog(String title, String body) {

        final AlertDialog confirmDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.success_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        confirmDialog = builder.setView(dialogView).setCancelable(false).show();
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //dialog views
        TextView titleTv = dialogView.findViewById(R.id.success_title_tv);
        titleTv.setText(title);
        TextView bodyTv = dialogView.findViewById(R.id.success_body_tv);
        bodyTv.setText(body);
        Button closeBtn = dialogView.findViewById(R.id.success_dialog_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
    }

    private void buildFailDialog(String title, String body) {
        final AlertDialog failDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.failed_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        failDialog = builder.setView(dialogView).show();
        failDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //dialog views
        TextView titleTv = dialogView.findViewById(R.id.failed_title_tv);
        titleTv.setText(title);
        TextView bodyTv = dialogView.findViewById(R.id.fail_body_tv);
        bodyTv.setText(body);
        Button closeBtn = dialogView.findViewById(R.id.fail_dialog_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failDialog.dismiss();
            }
        });
    }

    public static class PreferenceSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_preferences);
        }
    }

}

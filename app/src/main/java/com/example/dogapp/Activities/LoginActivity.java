package com.example.dogapp.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dogapp.Enteties.User;
import com.example.dogapp.Fragments.ForgotPasswordFragment;
import com.example.dogapp.Fragments.RegisterFragment;
import com.example.dogapp.Fragments.SecondRegisterFragment;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements RegisterFragment.OnRegisterFragmentListener, SecondRegisterFragment.OnSecondRegisterFragmentListener, ForgotPasswordFragment.OnForgotPasswordListener {

    RelativeLayout loginContainer;

    final String REGISTER_FRAGMENT_TAG = "register_fragment";
    final String REGISTER_FRAGMENT_2_TAG = "reg_2_frag";
    final String FORGOT_PASS_TAG = "forgot_pass_frag";

    Button loginBtn, regBtn;
    TextInputLayout emailEt, passwordEt;
    TextView forgotPassTv;
    ProgressBar progressBar;
    RelativeLayout hideLayout;

    String fullName;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //actual database
    DatabaseReference users = database.getReference("users"); //create new table named "users" and we get a reference to it

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
        setContentView(R.layout.login_page_layout);

        loginBtn = findViewById(R.id.login_btn);
        regBtn = findViewById(R.id.register_btn);
        emailEt = findViewById(R.id.email_login_input);
        passwordEt = findViewById(R.id.password_login_input);
        forgotPassTv = findViewById(R.id.forgot_pass_tv);
        progressBar = findViewById(R.id.reg_2_progress_bar);
        hideLayout = findViewById(R.id.reg_2_hide_layout);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser user = firebaseAuth.getCurrentUser(); //get current user

                if (user != null) {

                    if (fullName != null) { //sign up - update profile with full name

                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullName).build())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        fullName = null;
                                        /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);

                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Hi " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                        }
                                        finish();*/
                                    }
                                });
                    } else { //only sign in
                        /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();*/
                    }
                }
            }
        };

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, new RegisterFragment(), REGISTER_FRAGMENT_TAG).addToBackStack(null).commit();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {

                    //dialog with round edges
                    final AlertDialog alertDialog;
                    View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog = builder.setView(dialogView).setCancelable(false).show();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    //login account with firebase
                    firebaseAuth.signInWithEmailAndPassword(emailEt.getEditText().getText().toString(), passwordEt.getEditText().getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        alertDialog.dismiss();
                                    } else {
                                        alertDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, new ForgotPasswordFragment(), FORGOT_PASS_TAG).addToBackStack(null).commit();
            }
        });

    }

    private boolean validateEmail() {

        if (emailEt.getEditText().getText().toString().isEmpty()) {
            emailEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        if (passwordEt.getEditText().getText().toString().isEmpty()) {
            passwordEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            passwordEt.setError(null);
            return true;
        }
    }

    @Override
    public void onNext(String name,String email,String password) {
        SecondRegisterFragment fragment = SecondRegisterFragment.newInstance(name,email,password);
        getSupportFragmentManager().beginTransaction().add(R.id.login_container, fragment, REGISTER_FRAGMENT_2_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onBack() {
        closeFragment(REGISTER_FRAGMENT_TAG);
    }

    @Override
    public void onRegister(final String name, final String email, String password, final String date, final String gender, final String title, final String location) {
        this.fullName = name; //for the auth listener

        /*final AlertDialog loadingDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        loadingDialog = builder.setView(dialogView).setCancelable(false).show();
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));*/

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressDialog.dismiss();
                closeFragment(REGISTER_FRAGMENT_2_TAG);
                closeFragment(REGISTER_FRAGMENT_TAG);

                if (task.isSuccessful()) {
                    //push new User to database
                    User user = new User(name, date,email,gender,title, location);
                    users.child(firebaseAuth.getCurrentUser().getUid()).setValue(user);
                    buildConfirmDialog();

                } else {
                    buildFailDialog();
                }
            }
        });
    }

    private void buildConfirmDialog() {
        final AlertDialog confirmDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.success_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        confirmDialog = builder.setView(dialogView).setCancelable(false).show();
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //dialog views
        TextView body = dialogView.findViewById(R.id.success_body_tv);
        body.setText(R.string.reg_complete);
        Button closeBtn = dialogView.findViewById(R.id.success_dialog_close_btn);
        closeBtn.setText(R.string.confirm);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                confirmDialog.dismiss();
                finish();
            }
        });
    }

    private void buildFailDialog() {
        final AlertDialog failDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.failed_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        failDialog = builder.setView(dialogView).show();
        failDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //dialog views
        TextView msgTv = dialogView.findViewById(R.id.fail_body_tv);
        String wrong = getString(R.string.went_wrong);
        String body = getString(R.string.try_again);
        msgTv.setText(wrong +". "+body);
        Button closeBtn = dialogView.findViewById(R.id.fail_dialog_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackSecond() {
        closeFragment(REGISTER_FRAGMENT_2_TAG);
    }

    @Override
    public void sendEmail(final String emailToSend) {
        firebaseAuth.sendPasswordResetEmail(emailToSend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {

                    final AlertDialog confirmDialog;
                    View dialogView = getLayoutInflater().inflate(R.layout.success_dialog,null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    confirmDialog = builder.setView(dialogView).show();
                    confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView mail = dialogView.findViewById(R.id.success_body_tv);
                    mail.setText(getString(R.string.password_reset_sent_to) +" " + emailToSend);
                    Button closeBtn = dialogView.findViewById(R.id.success_dialog_close_btn);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDialog.dismiss();
                        }
                    });
                }
                else {
                    final AlertDialog failDialog;
                    View dialogView = getLayoutInflater().inflate(R.layout.failed_dialog,null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    failDialog = builder.setView(dialogView).show();
                    failDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    Button closeBtn = dialogView.findViewById(R.id.fail_dialog_close_btn);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            failDialog.dismiss();
                        }
                    });
                }
                closeFragment(FORGOT_PASS_TAG);
            }
        });
    }

    @Override
    public void onForgotBack() {
        closeFragment(FORGOT_PASS_TAG);
    }

    private void closeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack(); //remove from back stack
        }
    }
}

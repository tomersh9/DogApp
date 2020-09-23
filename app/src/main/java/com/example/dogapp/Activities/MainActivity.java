package com.example.dogapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Fragments.ChatsFragment;
import com.example.dogapp.Fragments.ExploreFragment;
import com.example.dogapp.Fragments.FollowersFragment;
import com.example.dogapp.Fragments.FollowingFragment;
import com.example.dogapp.Fragments.HomeFragment;
import com.example.dogapp.Fragments.ProfileFragment;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentListener, ChatsFragment.OnChatClickListener {

    //UI Layout
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private FrameLayout frameLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BottomNavigationView bottomNavBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;

    //drawer header views
    TextView fullNameTv, titleTv, locationTv;
    ImageView drawerProfilePic;

    //Main Fragments
    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private ChatsFragment chatsFragment;
    private ProfileFragment profileFragment;

    //firebase stuff
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //actual database
    DatabaseReference usersRef = database.getReference("users"); //create new table named "users" and we get a reference to it
    FirebaseUser fUser = firebaseAuth.getCurrentUser();
    File file;

    //Change UI from notification
    BroadcastReceiver receiver;

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //if press hamburger icon
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create channel once
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("ID", "NAME", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        //initial set up of referencing
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //references
        frameLayout = findViewById(R.id.fragment_container);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        appBarLayout = findViewById(R.id.app_bar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        bottomNavBar = findViewById(R.id.bottom_navbar);

        //hamburger
        setUpActionBar();

        //navigation initialize
        navigationView.setItemIconTintList(null);

        //setting drawer header views
        View headerView = navigationView.getHeaderView(0);
        fullNameTv = headerView.findViewById(R.id.drawer_full_name_tv);
        locationTv = headerView.findViewById(R.id.drawer_location_tv);
        titleTv = headerView.findViewById(R.id.drawer_title_tv);
        drawerProfilePic = headerView.findViewById(R.id.drawer_profile_pic);

        //assign fragments
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        chatsFragment = new ChatsFragment();
        //profileFragment = new ProfileFragment();

        //set on click listeners
        setOnClickListeners();

        //set default home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        toolbar.setTitle(R.string.home);

        //listens to events of fire base instances
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (fUser != null) {

                } else { //sign out

                }
            }
        };

        //************* UPDATE USER FIELDS RETROACTIVE TO CREATION IN DATABASE*******************//
        //***************UPDATE DRAWER UI WITH USER FIELDS**************************//
        //to prevent deleted users create real time things in the table
        //update photo url field in User class

        if(fUser!=null) {
            Map<String,Object> hashMap = new HashMap<>();
            hashMap.put("photoUri",fUser.getPhotoUrl().toString());
            hashMap.put("id",fUser.getUid());
            usersRef.child(fUser.getUid()).updateChildren(hashMap);

            fUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if(task.isSuccessful()) {
                        sendRegistrationToServer(task.getResult().getToken());
                    }
                    else {
                        Toast.makeText(MainActivity.this, "NO TOKEN", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

       /* usersRef.child(fUser.getUid()).child("photoUri").setValue(fUser.getPhotoUrl().toString());
        //update Unique ID field
        usersRef.child(fUser.getUid()).child("id").setValue(fUser.getUid());*/

        //get data of current user from firebase and update views
        usersRef.child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //update things from user data
                    User user = dataSnapshot.getValue(User.class);
                    fullNameTv.setText(user.getFullName());
                    titleTv.setText(user.getTitle());
                    locationTv.setText(user.getLocation());

                } else {
                    Toast.makeText(MainActivity.this, "DataSnapShot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fUser.getPhotoUrl() != null) {
            Glide.with(this).asBitmap().load(fUser.getPhotoUrl()).into(drawerProfilePic);
        }

        //Broadcast Receiver - update UI when user is in the app
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //add badge to the chats bottom nav item
                String msg = intent.getStringExtra("message");
            }
        };

        IntentFilter filter = new IntentFilter("action_msg_receive");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void sendRegistrationToServer(String token) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", token);

        if (firebaseUser != null) {
            reference.child(firebaseUser.getUid()).updateChildren(hashMap);
        }
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar(); //getting the ToolBar we made
        actionBar.setDisplayHomeAsUpEnabled(true); //setting home button in top left
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp); //hamburger icon
    }

    private void setOnClickListeners() {

        drawerProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment profileFragment = ProfileFragment.newInstance(fUser.getUid(), fUser.getPhotoUrl().toString());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                drawerLayout.closeDrawers();
            }
        });

        //bottom nav_bar items click events (swapping different fragments)
        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment currFragment;

                switch (item.getItemId()) {

                    case R.id.bottom_home:
                        currFragment = homeFragment;
                        toolbar.setTitle(getString(R.string.home));
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_explore:
                        currFragment = exploreFragment;
                        toolbar.setTitle(getString(R.string.explore));
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_chat:
                        currFragment = chatsFragment;
                        toolbar.setTitle(getString(R.string.chats));
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_profile:
                        profileFragment = ProfileFragment.newInstance(fUser.getUid(), fUser.getPhotoUrl().toString());
                        currFragment = profileFragment;
                        break;

                    default:
                        return false;
                }

                item.setChecked(true);
                if (navigationView.getCheckedItem() != null) {
                    navigationView.getCheckedItem().setChecked(false);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currFragment).commit();
                return true;
            }
        });

        //navigation drawer select item event
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.item_profile:
                        profileFragment = ProfileFragment.newInstance(fUser.getUid(), fUser.getPhotoUrl().toString());
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                        bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                        break;

                    case R.id.item_friends:
                        /*toolbar.setTitle(getString(R.string.friends));
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FriendsFragment()).commit();*/
                        break;

                    case R.id.item_sign_out:
                        firebaseAuth.signOut();
                        setUserStatus(getString(R.string.offline));
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);//.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;
                }

                item.setChecked(true); //highlight
                drawerLayout.closeDrawers();
                return true; //done dealing with it
            }
        });
    }

    //close drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //close fragments by tag
    private void closeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack(); //remove from back stack
        }
    }

    //*****************FRAGMENTS DATA TRANSFER**************************//

    //Profile fragment events
    @Override
    public void changeProfileToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    @Override
    public void onProfileFollowingsClick(String userID) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.following));
        FollowingFragment followingFragment = FollowingFragment.newInstance(userID);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, followingFragment).commit();
    }

    @Override
    public void onProfileFollowersClick(String userID) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.followers));
        FollowersFragment followersFragment = FollowersFragment.newInstance(userID);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, followersFragment).commit();
    }

    @Override
    public void onChatClicked(String userID) {
        //TODO Check first if is in following list, else, notice user if(userID is in list)
        Intent intent = new Intent(MainActivity.this, InChatActivity.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    //*******************UPDATE USERS STATUS************************//
    private void setUserStatus(String status) {
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        usersRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus(getString(R.string.online));
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserStatus(getString(R.string.offline));
    }
}

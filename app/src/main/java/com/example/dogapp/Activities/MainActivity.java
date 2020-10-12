package com.example.dogapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
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
import com.example.dogapp.Fragments.WalkerBoardFragment;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.example.dogapp.Services.AlarmBroadcastReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentListener, ChatsFragment.OnChatClickListener, FollowersFragment.MyFollowersFragmentListener, FollowingFragment.MyFollowingFragmentListener, WalkerBoardFragment.MyWalkerBoardFragmentListener, HomeFragment.MyHomeFragmentListener {

    //Fragments TAGs
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment_tag";
    private final String FOLLOWING_FRAGMENT_TAG = "following_tag";
    private final String FOLLOWERS_FRAGMENT_TAG = "followers_tag";
    private final String DISCOVER_FRIENDS_TAG = "discover_friends_tag";

    //settings activity request
    private final int SETTINGS_REQUEST = 1;

    //UI Layout
    public Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private FrameLayout frameLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BottomNavigationView bottomNavBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;

    //drawer header views
    private TextView fullNameTv, titleTv, locationTv;
    private ImageView drawerProfilePic;

    //Main Fragments
    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private ChatsFragment chatsFragment;
    private ProfileFragment profileFragment;

    //firebase stuff
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    private FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes
    private FirebaseDatabase database = FirebaseDatabase.getInstance(); //actual database
    private DatabaseReference usersRef = database.getReference("users"); //create new table named "users" and we get a reference to it
    private FirebaseUser fUser = firebaseAuth.getCurrentUser();
    private FirebaseMessaging firebaseMessaging;
    private DatabaseReference postRef = database.getReference("Posts");
    private boolean isAnonymous;

    //Change UI from notification
    private BroadcastReceiver receiver;
    private int badges = 0;

    //is walker
    private boolean isWalker;
    private boolean lastCall;

    //TODO check if there are problems

    /*@Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }*/

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

    public static void startAlarmBroadcastReceiver(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //subscribe to posts
        subscribeSignIn();

        //fixed portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //create channel once
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("ID", "NAME", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }


        //alarm manager
        startAlarmBroadcastReceiver(this);

        //initial set up of referencing
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPopupTheme(R.style.PopupMenuItems);

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

        isAnonymous = fUser.isAnonymous();

        //set default home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        toolbar.setTitle(R.string.home);

        //set on click listeners
        setOnClickListeners();

        if (!isAnonymous) {

            navigationView.inflateMenu(R.menu.drawer_menu);
            locationTv.setVisibility(View.VISIBLE);
            titleTv.setVisibility(View.VISIBLE);

            /*//TODO check if there are problems
            //listens to events of fire base instances
            authStateListener = new FirebaseAuth.AuthStateListener() {

                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (fUser != null) {

                    } else { //sign out

                    }
                }
            };*/

            //************* UPDATE USER FIELDS RETROACTIVE TO CREATION IN DATABASE*******************//
            //***************UPDATE DRAWER UI WITH USER FIELDS**************************//
            //to prevent deleted users create real time things in the table
            //update photo url field in User class

            if (fUser != null) {

                //update user fields
                Map<String, Object> hashMap = new HashMap<>();
                hashMap.put("photoUrl", fUser.getPhotoUrl().toString());
                hashMap.put("id", fUser.getUid());
                usersRef.child(fUser.getUid()).updateChildren(hashMap);

                //token
                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            sendRegistrationToServer(task.getResult().getToken());
                        } else {
                            Toast.makeText(MainActivity.this, R.string.no_token, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            /*Map<String,Object> hashMap = new HashMap<>();
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
            });*/
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
                        if (user.getType()) { //true walker
                            titleTv.setText(R.string.dog_walker);
                        } else { //false owner
                            titleTv.setText(R.string.dog_owner);
                        }
                        locationTv.setText(user.getLocation());

                        //to give the settings activity
                        isWalker = user.getType(); //walker vs user
                        lastCall = user.getLastCall();

                    } else {
                        Toast.makeText(MainActivity.this, R.string.data_not_exist, Toast.LENGTH_SHORT).show();
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
            if (receiver == null) {
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        //add badge to the chats bottom nav item
                        Menu menu = bottomNavBar.getMenu();
                        MenuItem menuItem = menu.findItem(R.id.bottom_chat); //chats item
                        BadgeDrawable badgeDrawable = bottomNavBar.getOrCreateBadge(menuItem.getItemId());
                        badgeDrawable.setVisible(true);
                        badgeDrawable.setNumber(++badges);
                        badgeDrawable.setBackgroundColor(getResources().getColor(R.color.red));
                    }
                };
            }

            IntentFilter filter = new IntentFilter("action_msg_receive");
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

            if (getIntent().hasExtra("pendingUserID")) {
                String otherID = getIntent().getStringExtra("pendingUserID");
                String otherUrl = getIntent().getStringExtra("pendingImgURL");
                ProfileFragment profileFragment = ProfileFragment.newInstance(otherID, otherUrl);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment, PROFILE_FRAGMENT_TAG).addToBackStack(null).commit();
            }

        } else {

            navigationView.inflateMenu(R.menu.drawer_anonymus_menu);
            drawerProfilePic.setImageResource(R.drawable.user_drawer_icon_256);
            fullNameTv.setText(R.string.guest);
            locationTv.setVisibility(View.GONE);
            titleTv.setVisibility(View.GONE);

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.item_sign_out_anonymus) {
                        firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firebaseAuth.signOut();
                                Intent signOutIntent = new Intent(MainActivity.this, LoginActivity.class);//.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(signOutIntent);
                                finish();
                            }
                        });
                    }
                    drawerLayout.closeDrawers();
                    return true;
                }
            });
        }
    }

    //unsubscribe from topic when sign out
    private void unsubscribeSignOut(final String myID) {
        firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.unsubscribeFromTopic(myID);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (myID.equals(modelPost.getuId())) {
                        firebaseMessaging.unsubscribeFromTopic(modelPost.getpId());
                        firebaseMessaging.unsubscribeFromTopic(modelPost.getpId() + "Likes");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void subscribeSignIn() {
        firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.subscribeToTopic(fUser.getUid());
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (fUser.getUid().equals(modelPost.getuId())) {
                        firebaseMessaging.subscribeToTopic(modelPost.getpId());
                        firebaseMessaging.subscribeToTopic(modelPost.getpId() + "Likes");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void deleteToken(String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens").child(userID);
        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    System.out.println("token deleted!!!!!!!!!!!!!!!!!");
                } else {
                    System.out.println("token NOT deleted!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        });
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar(); //getting the ToolBar we made
        actionBar.setDisplayHomeAsUpEnabled(true); //setting home button in top left
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp); //hamburger icon
    }

    private void setOnClickListeners() {

        drawerProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                drawerLayout.closeDrawers();
            }
        });

        //bottom nav_bar items click events (swapping different fragments)
        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

                Fragment currFragment;
                FragmentManager manager = getSupportFragmentManager();

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

                        //clean badge dot
                        BadgeDrawable badgeDrawable = bottomNavBar.getBadge(item.getItemId());
                        if (badgeDrawable != null) {
                            badges = 0; // reset count when opening chats fragment
                            badgeDrawable.setNumber(badges);
                            badgeDrawable.setVisible(false);
                        }
                        break;

                    case R.id.bottom_profile:
                        if (!isAnonymous) {
                            profileFragment = ProfileFragment.newInstance(fUser.getUid(), fUser.getPhotoUrl().toString());
                            currFragment = profileFragment;
                        } else {
                            currFragment = null;
                            item.setCheckable(false);
                            Snackbar.make(coordinatorLayout, R.string.only_reg_user, Snackbar.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        return false;
                }

                item.setChecked(true);
                if (navigationView.getCheckedItem() != null) {
                    navigationView.getCheckedItem().setChecked(false);
                }

                if (currFragment != null) {
                    clearBackStack();
                    manager.beginTransaction().replace(R.id.fragment_container, currFragment).commit();
                }
                return true;
            }
        });

        //navigation drawer select item event
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.item_profile:
                        bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                        break;

                    case R.id.item_settings:
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        intent.putExtra("isWalker", isWalker); //true for walker
                        intent.putExtra("lastCall", lastCall);
                        startActivityForResult(intent, SETTINGS_REQUEST);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case R.id.item_sign_out:
                        setUserStatus(false);
                        deleteToken(fUser.getUid());
                        unsubscribeSignOut(fUser.getUid());
                        cancelAlarmManger();
                        firebaseAuth.signOut();
                        Intent signOutIntent = new Intent(MainActivity.this, LoginActivity.class);//.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(signOutIntent);
                        finish();
                        break;
                }

                item.setChecked(true); //highlight
                drawerLayout.closeDrawers();
                return true; //done dealing with it
            }
        });
    }

    private void cancelAlarmManger() {
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 9, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void clearBackStack() {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    //change from settings activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST) {

            fullNameTv.setText(fUser.getDisplayName());

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            lastCall = sp.getBoolean("last_call_cb_preference", false);

        }
    }

    //close drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            //get current fragment after
            Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currFragment != null) {
                closeFragment(currFragment.getTag());
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFollowersFragmentBackPress() {
        closeFragment(FOLLOWERS_FRAGMENT_TAG);
    }

    @Override
    public void onFollowingFragmentBackPress() {
        closeFragment(FOLLOWING_FRAGMENT_TAG);
    }

    @Override
    public void onProfileBackPress() {
        closeFragment(PROFILE_FRAGMENT_TAG);
    }

    //close fragments by tag
    private void closeFragment(String tag) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack(); //remove from back stack


            if (tag.equals(PROFILE_FRAGMENT_TAG)) {

                //get current fragment after
                Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (currFragment != null) {

                    if (currFragment.getTag().equals(FOLLOWERS_FRAGMENT_TAG)) {
                        toolbar.setTitle(getString(R.string.followers));
                    } else if (currFragment.getTag().equals(FOLLOWING_FRAGMENT_TAG)) {
                        toolbar.setTitle(getString(R.string.following));
                    } else {

                    }
                    setSupportActionBar(toolbar);
                }
            }
        }
    }


    private Fragment getCurrentFragment() {
        String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        return currentFragment;
    }

    //*****************FRAGMENTS DATA TRANSFER**************************//


    @Override
    public void onWalkerClicked(String userID, String imgURL) {
        ProfileFragment fragment = ProfileFragment.newInstance(userID, imgURL);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, PROFILE_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    //Profile fragment events
    @Override
    public void changeProfileToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onMyPostClicked() {
        bottomNavBar.setSelectedItemId(R.id.bottom_profile);
    }

    @Override
    public void onProfileSettingsClick() {
        navigationView.setCheckedItem(R.id.item_settings);
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("isWalker", isWalker); //true for walker
        intent.putExtra("lastCall", lastCall);
        startActivityForResult(intent, SETTINGS_REQUEST);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onProfileFollowingsClick(String userID) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.following));
        FollowingFragment followingFragment = FollowingFragment.newInstance(userID);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, followingFragment, FOLLOWING_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onProfileFollowersClick(String userID) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.followers));
        FollowersFragment followersFragment = FollowersFragment.newInstance(userID);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, followersFragment, FOLLOWERS_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onChatClicked(String userID) {
        //TODO Check first if is in following list, else, notice user if(userID is in list)
        Intent intent = new Intent(MainActivity.this, InChatActivity.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    //*******************UPDATE USERS STATUS************************//
    private void setUserStatus(Boolean status) {

        if (fUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            usersRef.updateChildren(hashMap);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!fUser.isAnonymous()) {
            setUserStatus(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!fUser.isAnonymous()) {
            setUserStatus(false);
        }
    }
}

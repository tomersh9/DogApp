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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
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
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentListener, ChatsFragment.OnChatClickListener, FollowersFragment.MyFollowersFragmentListener, FollowingFragment.MyFollowingFragmentListener {

    //Fragments TAGs
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment_tag";
    private final String FOLLOWING_FRAGMENT_TAG = "following_tag";
    private final String FOLLOWERS_FRAGMENT_TAG = "followers_tag";
    private final String DISCOVER_FRIENDS_TAG = "discover_friends_tag";

    //settings activity request
    private final int SETTINGS_REQUEST = 1;

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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fixed portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //create channel once
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("ID", "NAME", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

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
        //profileFragment = new ProfileFragment();

        //set on click listeners
        setOnClickListeners();

        //set default home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        toolbar.setTitle(R.string.home);

        //TODO check if there are problems
        //listens to events of fire base instances
        /*authStateListener = new FirebaseAuth.AuthStateListener() {

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
                        Toast.makeText(MainActivity.this, "NO TOKEN", Toast.LENGTH_SHORT).show();
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
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //add badge to the chats bottom nav item
                    Menu menu = bottomNavBar.getMenu();
                    MenuItem menuItem = menu.findItem(R.id.bottom_chat); //chats item
                    BadgeDrawable badgeDrawable = bottomNavBar.getOrCreateBadge(menuItem.getItemId());
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setBackgroundColor(getResources().getColor(R.color.red));
                }
            };
        }

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
                bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                drawerLayout.closeDrawers();
            }
        });

        //bottom nav_bar items click events (swapping different fragments)
        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

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

                        //clean badge dot
                        BadgeDrawable badgeDrawable = bottomNavBar.getBadge(item.getItemId());
                        if (badgeDrawable != null) {
                            badgeDrawable.setVisible(false);
                        }

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
                        bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                        break;

                    case R.id.item_settings:
                        startActivityForResult(new Intent(MainActivity.this,SettingsActivity.class),SETTINGS_REQUEST);
                        break;

                    case R.id.item_sign_out:
                        setUserStatus(false);
                        firebaseAuth.signOut();
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

    //change from settings activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SETTINGS_REQUEST) {
            //save and update user fields in firebase
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        }
    }

    //close drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else  {
            //get current fragment after
            Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currFragment != null) {
                closeFragment(currFragment.getTag());
            }

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
                    } else if (currFragment.getTag().equals(DISCOVER_FRIENDS_TAG)) {
                        toolbar.setTitle(getString(R.string.settings));
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
        setUserStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserStatus(false);
    }
}

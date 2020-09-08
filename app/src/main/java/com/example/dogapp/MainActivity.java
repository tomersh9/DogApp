package com.example.dogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.dogapp.Fragments.ChatFragment;
import com.example.dogapp.Fragments.ExploreFragment;
import com.example.dogapp.Fragments.HomeFragment;
import com.example.dogapp.Fragments.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //UI Layout
    private Toolbar toolbar;
    private ViewPager viewPager;
    private BottomNavigationView bottomNavBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;

    //Main Fragments
    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;

    //firebase stuff
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authStateListener = new FirebaseAuth.AuthStateListener() {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        //assign material design layout
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //references
        viewPager = findViewById(R.id.view_pager);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        bottomNavBar = findViewById(R.id.bottom_navbar);
        fab = findViewById(R.id.fab);

        //hamburger
        ActionBar actionBar = getSupportActionBar(); //getting the ToolBar we made
        actionBar.setDisplayHomeAsUpEnabled(true); //setting home button in top left
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp); //hamburger icon

        //icons tint
        navigationView.setItemIconTintList(null);

        //assign fragments
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Fragment currFragment = null;

                switch (item.getItemId()) {

                    case R.id.bottom_home:
                        viewPager.setCurrentItem(0);
                        break;

                    case R.id.bottom_explore:
                        viewPager.setCurrentItem(1);
                        break;

                    case R.id.bottom_chat:
                        viewPager.setCurrentItem(2);
                        break;

                    case R.id.bottom_profile:
                        viewPager.setCurrentItem(3);
                        break;

                    default:
                        return false;
                }

                //getSupportFragmentManager().beginTransaction().replace(R.id.view_pager,currFragment).commit();
                return true;
            }
        });

        //create adapter for the fragment view pager and set fragments
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(homeFragment, "Home");
        adapter.addFragment(exploreFragment, "Explore");
        adapter.addFragment(chatFragment, "Chat");
        adapter.addFragment(profileFragment, "Profile");
        viewPager.setAdapter(adapter);


        /*tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_explore_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_chat_black_24dp);
        tabLayout.getTabAt(3).setIcon(R.drawable.person_icon_24);*/

        //navigation drawer select item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.item_sign_out:
                        firebaseAuth.signOut();
                        Toast.makeText(MainActivity.this, "NOT exist", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Hey").setMessage("msg").setIcon(R.drawable.person_icon_24)
                        .setPositiveButton("press me", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar.make(coordinatorLayout, "OK", Snackbar.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });


    }
}

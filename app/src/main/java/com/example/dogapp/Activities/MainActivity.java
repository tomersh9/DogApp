package com.example.dogapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Fragments.ChatFragment;
import com.example.dogapp.Fragments.ExploreFragment;
import com.example.dogapp.Fragments.FriendsFragment;
import com.example.dogapp.Fragments.HomeFragment;
import com.example.dogapp.Fragments.ProfileFragment;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentListener {


    //User instance
    User currUser;

    //UI Layout
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private FrameLayout frameLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BottomNavigationView bottomNavBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;

    //drawer header views
    TextView fullNameTv, titleTv, locationTv;
    ImageView drawerProfilePic;

    //Main Fragments
    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;

    //firebase stuff
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //actual database
    DatabaseReference usersRef = database.getReference("users"); //create new table named "users" and we get a reference to it
    StorageReference myStorageRef;
    FirebaseUser fUser = firebaseAuth.getCurrentUser();
    File file;

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
        fab = findViewById(R.id.fab);
        //fab.hide();

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

//        myStorageRef1 = FirebaseStorage.getInstance().getReference("Images").child(name);
//        try {
//            file = File.createTempFile(name,"");
//            myStorageRef1.getFile(file)
//                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            Toast.makeText(MainActivity.this, "OKAY", Toast.LENGTH_SHORT).show();
//                            drawerProfilePic.setImageURI(Uri.fromFile(file));
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(MainActivity.this, "NOT OKAY", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //fileDownload("hhhggg@gmail.com", drawerProfilePic);

        //File tempPic = fileDownload("deded");
        //drawerProfilePic.setImageURI(Uri.fromFile(file));

        //assign fragments
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        //set on click listeners
        setOnClickListeners();

        //set default home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        //listens to events of fire base instances
        authStateListener = new FirebaseAuth.AuthStateListener() {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {

                } else { //sign out

                }
            }
        };

        //update photo url field in User class
        usersRef.child(fUser.getUid()).child("photoUri").setValue(fUser.getPhotoUrl().toString());

        //get data of current user from firebase and update views
        usersRef.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //get current user data
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String title = dataSnapshot.child("title").getValue(String.class);

                    //update things from user data
                    fullNameTv.setText(fullName);
                    titleTv.setText(title);
                    locationTv.setText(location);
                    //Toast.makeText(MainActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                    //Glide.with(MainActivity.this).load(uri).into(drawerProfilePic);
                    //Toast.makeText(MainActivity.this, email, Toast.LENGTH_SHORT).show();
                    //fileDownload(email,drawerProfilePic);
                    //drawerProfilePic.setImageResource(R.drawable.orange_dog_100);

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
    }


    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar(); //getting the ToolBar we made
        actionBar.setDisplayHomeAsUpEnabled(true); //setting home button in top left
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp); //hamburger icon
    }

    private void setOnClickListeners() {

        //bottom nav_bar items click events (swapping different fragments)
        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment currFragment;

                switch (item.getItemId()) {

                    case R.id.bottom_home:
                        currFragment = homeFragment;
                        toolbar.setTitle(getString(R.string.home));
                        fab.show();
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_explore:
                        currFragment = exploreFragment;
                        toolbar.setTitle(getString(R.string.explore));
                        fab.show();
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_chat:
                        currFragment = chatFragment;
                        toolbar.setTitle(getString(R.string.chats));
                        fab.hide();
                        setSupportActionBar(toolbar);
                        break;

                    case R.id.bottom_profile:
                        currFragment = profileFragment;
                        fab.hide();
                        break;

                    default:
                        return false;
                }
                item.setChecked(true);
                //bottomNavBar.getMenu().setGroupCheckable(0,true,true);
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
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                        bottomNavBar.setSelectedItemId(R.id.bottom_profile);
                        break;

                    case R.id.item_friends:
                        toolbar.setTitle("Friends");
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new FriendsFragment()).commit();
                        fab.show();
                        //bottomNavBar.getMenu().setGroupCheckable(0,false,true);
                        break;

                    case R.id.item_sign_out:
                        firebaseAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

                item.setChecked(true); //highlight
                drawerLayout.closeDrawers();
                return true; //done dealing with it
            }
        });

        //fab event listener
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

    @Override
    public void changeToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    public FloatingActionButton getFab() {
        if(fab!=null) {
            return fab;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void fileDownload(String fileName, final ImageView imageView) {
        try {
            file = File.createTempFile(fileName, "");
            //myStorageRef1 = FirebaseStorage.getInstance().getReference().child("Images/"+fileName);
            myStorageRef = FirebaseStorage.getInstance().getReference("Images").child(fileName);
            myStorageRef.getFile(file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            try {
                                Bitmap bitmap1 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file)));
                                imageView.setImageBitmap(bitmap1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        //return file;
    }

/*    private void collapseAppBar() {
        // Collapse the AppBarLayout with animation
        appBarLayout.setExpanded(false, true);
    }

    private void expandAppBar() {
        appBarLayout.setExpanded(true, false);
    }

    private void lockAppBar() {
    *//* Disable the nestedScrolling to disable expanding the
     appBar with dragging the nestedScrollView below it *//*
        ViewCompat.setNestedScrollingEnabled(frameLayout, false);

    *//* But still appBar is expandable with dragging the appBar itself
    and below code disables that too
     *//*
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
    }

    private void unLockAppBar() {
        ViewCompat.setNestedScrollingEnabled(frameLayout, true);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(AppBarLayout appBarLayout) {
                    return true;
                }
            });
        }
    }*/
}

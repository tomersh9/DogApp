package com.example.dogapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//import static com.example.dogapp.Activities.MainActivity.email;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Activities.MainActivity;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    //Firebase
    private List<String> followingList = new ArrayList<>();
    private List<String> followersList = new ArrayList<>();
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following");
    private DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers");
    private String userID, imgURL; //for any user of the app
    private boolean isMe;

    //views
    private ImageView profileIv, genderIv, typeIv;
    private TextView nameTv, followingTv, followersTv, criticsTv, genderAgeTv, locationTv, typeTv, aboutMeTv;
    private LinearLayout followersLayoutBtn, followingLayoutBtn;
    private FloatingActionButton chatFab, followFab, profileFab;


    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    //Dialogs
    private AlertDialog alertDialog;

    public interface OnProfileFragmentListener {
        void changeProfileToolBar(Toolbar toolbar);

        void onProfileFollowingsClick(String userID);

        void onProfileFollowersClick(String userID);
    }

    private OnProfileFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnProfileFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnProfileFragmentListener interface");
        }
    }

    public static ProfileFragment newInstance(String userID, String imgURL) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);
        bundle.putString("imgURL", imgURL);
        fragment.setArguments(bundle);
        return fragment; //holds the bundle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        //get user who activated the fragment
        userID = getArguments().getString("userID");
        imgURL = getArguments().getString("imgURL");

        if (userID.equals(fUser.getUid())) {
            isMe = true;
        } else {
            isMe = false;
        }

        //assign views
        profileIv = rootView.findViewById(R.id.profile_frag_iv);
        nameTv = rootView.findViewById(R.id.profile_frag_name_tv);
        genderAgeTv = rootView.findViewById(R.id.profile_item_gender_age_tv);
        locationTv = rootView.findViewById(R.id.profile_item_location_tv);
        typeTv = rootView.findViewById(R.id.profile_item_type_tv);
        aboutMeTv = rootView.findViewById(R.id.profile_item_about_me_tv);
        followersTv = rootView.findViewById(R.id.followers_count_tv);
        followingTv = rootView.findViewById(R.id.following_count_tv);
        typeIv = rootView.findViewById(R.id.profile_type_iv);
        genderIv = rootView.findViewById(R.id.profile_gender_iv);
        followersLayoutBtn = rootView.findViewById(R.id.followers_layout);
        followingLayoutBtn = rootView.findViewById(R.id.following_layout);
        chatFab = rootView.findViewById(R.id.chat_fab);
        followFab = rootView.findViewById(R.id.follow_fab);
        profileFab = rootView.findViewById(R.id.profile_fab);

        //profile assign
        loadProfileViews();

        if (isMe) {
            chatFab.setVisibility(View.GONE);
            followFab.setVisibility(View.GONE);
            profileFab.setVisibility(View.VISIBLE);

            profileFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), "Settings", Snackbar.LENGTH_LONG).show();
                }
            });

        } else {
            chatFab.setVisibility(View.VISIBLE);
            followFab.setVisibility(View.VISIBLE);
            profileFab.setVisibility(View.GONE);
            chatFab.hide();

            loadFollowingList(); //seeing other user's profile

            followFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onProfileFollowClick();
                }
            });

            chatFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), InChatActivity.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            });
        }


        try {
            Glide.with(getActivity()).asBitmap().load(imgURL).into(profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }

        //toolbar
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_toolbar_layout);
        toolbar = rootView.findViewById(R.id.toolbar_profile);
        listener.changeProfileToolBar(toolbar);
        appBarLayout = rootView.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(this);
        toolbar.setTitle("");

        //profile pic click event
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildProfileSheetDialog();
            }
        });

        //followers listeners
        followingLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProfileFollowingsClick(userID);
            }
        });
        followersLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProfileFollowersClick(userID);
            }
        });

        return rootView;
    }

    //loading MYSELF (fUSer) following list to see if i follow this user already
    private void loadFollowingList() {

        followingRef.child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    followingList.add(ds.getValue(String.class)); //filter only the ones i follow
                }
                if (followingList.contains(userID)) {
                    chatFab.show();
                    followFab.setImageResource(R.drawable.unfollow_user_icon_64);
                    followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                } else {
                    chatFab.hide();
                    followFab.setImageResource(R.drawable.follow_user_icon_64);
                    followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onProfileFollowClick() {

        if (!followingList.contains(userID)) { //follow user

            //add the user to my following list
            followingList.add(userID);
            followingRef.child(fUser.getUid()).setValue(followingList);

            //get user's followers list to add myself
            followersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    followersList.clear();

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        followersList.add(ds.getValue(String.class));
                    }
                    //add myself as a user
                    if (!followersList.contains(fUser.getUid())) {
                        followersList.add(fUser.getUid());
                        followersRef.child(userID).setValue(followersList);
                        followersTv.setText(followersList.size() + "");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //change fab apperance
            chatFab.show();
            followFab.setImageResource(R.drawable.unfollow_user_icon_64);
            followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.you_now_follow) + " " + nameTv.getText().toString(), Snackbar.LENGTH_SHORT).show();

        } else { //unfollow user

            followingList.remove(userID);
            followingRef.child(fUser.getUid()).setValue(followingList);

            //remove myself from his followers
            followersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    followersList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            followersList.add(ds.getValue(String.class));
                        }
                    }
                    //remove myself from his followers list
                    if (followersList.contains(fUser.getUid())) {
                        followersList.remove(fUser.getUid());
                        followersRef.child(userID).setValue(followersList);
                        followersTv.setText(followersList.size() + "");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //change fab apperance
            chatFab.hide();
            followFab.setImageResource(R.drawable.follow_user_icon_64);
            followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.unfollow_from) + " " + nameTv.getText().toString(), Snackbar.LENGTH_SHORT).show();
        }

        //loadDetailsViews();

    }

    private void loadDetailsViews() {
        //following count
        followingRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followingTv.setText(snapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //followers count
        followersRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followersTv.setText(snapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadProfileViews() {

        loadDetailsViews();

        //assign personal details
        usersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    User user = snapshot.getValue(User.class);

                    //setting icons
                    if (getActivity() != null) {

                        if (user.getGender().equals(getString(R.string.male))) {
                            genderIv.setImageResource(R.drawable.man_icon);
                        } else if (user.getGender().equals(getString(R.string.female))) {
                            genderIv.setImageResource(R.drawable.woman_icon);
                        } else {
                            genderIv.setImageResource(R.drawable.other_icon);
                        }

                        if (user.getTitle().equals(getString(R.string.dog_owner))) {
                            typeIv.setImageResource(R.drawable.dog_owner_icon);
                        } else {
                            typeIv.setImageResource(R.drawable.dog_walker_icon);
                        }
                    }

                    //set text
                    genderAgeTv.setText(user.getGender());
                    locationTv.setText(user.getLocation());
                    typeTv.setText(user.getTitle());
                    nameTv.setText(user.getFullName()); //display name
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void buildProfileSheetDialog() {

        final BottomSheetDialog bottomDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_profile, null);

        LinearLayout showPic, takePic, selectPic;
        showPic = bottomSheetView.findViewById(R.id.select_display);
        takePic = bottomSheetView.findViewById(R.id.select_take_pic);
        selectPic = bottomSheetView.findViewById(R.id.select_choose_pic);

        showPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.image_display_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                alertDialog = builder.setView(dialogView).show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final ProgressBar progressBar = dialogView.findViewById(R.id.img_loader_bar);
                final ImageView imageView = dialogView.findViewById(R.id.img_display);
                progressBar.setVisibility(View.VISIBLE);

                try {
                    Glide.with(dialogView).load(imgURL).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.failed_upload_image, Snackbar.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(imageView);
                } catch (Exception e) {

                }
                bottomDialog.dismiss();
            }
        });
        bottomDialog.setContentView(bottomSheetView);
        bottomDialog.show();
    }

    //profile pic animation transition
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
            // Collapsed


        } else if (verticalOffset == 0) {
            // Expanded

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Toast.makeText(getActivity(), getString(R.string.settings), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_item_friends:
                Toast.makeText(getActivity(), getString(R.string.friends), Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

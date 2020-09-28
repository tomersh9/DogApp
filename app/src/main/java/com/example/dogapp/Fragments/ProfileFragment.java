package com.example.dogapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Activities.LoginActivity;
import com.example.dogapp.Activities.MainActivity;
import com.example.dogapp.Adapters.ReviewAdapter;
import com.example.dogapp.Enteties.Review;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelComment;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    private final String PROFILE_FRAGMENT_TAG = "profile_fragment_tag";

    //Firebase
    private List<String> followingList = new ArrayList<>();
    private List<String> followersList = new ArrayList<>();
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following");
    private DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers");
    private String userID, imgURL; //for any user of the app
    private boolean isMe;
    private boolean isWalker;
    private int ratingNum = 0;
    private String otherUserName;

    //reviews list (only for walkers)
    private List<Review> reviewList = new ArrayList<>();
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private ProgressBar progressBar;

    //views
    private ImageView profileIv, genderIv, typeIv;
    private TextView nameTv, followingTv, followersTv, criticsCountTv, genderAgeTv, locationTv, typeTv, aboutMeTv;
    private LinearLayout followersLayoutBtn, followingLayoutBtn, criticsLayoutBtn;
    private RelativeLayout aboutMeLayout;
    private FloatingActionButton chatFab, followFab, profileFab;
    private LinearLayout row1, row2, row3;
    private RelativeLayout dogSizeLayoutBtn, rangeLayoutBtn, lastCallLayoutBtn, paymentLayoutBtn, expLayoutBtn, ratingLayoutBtn;
    private TextView sizesTv, rangeTv, lastCallTv, paymentTv, expTv, ratingTv;


    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    //Dialogs
    private AlertDialog alertDialog;

    //camera and gallery
    Uri fileUri;
    Bitmap bitmap1, bitmap2;

    // Activity requests
    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST = 2;
    final int SELECT_IMAGE = 3;
    boolean isFromCamera;
    boolean permission = true;
    String email;

    public interface OnProfileFragmentListener {

        void changeProfileToolBar(Toolbar toolbar);

        void onProfileFollowingsClick(String userID);

        void onProfileFollowersClick(String userID);

        void onProfileBackPress();
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
        row1 = rootView.findViewById(R.id.row_1);
        row2 = rootView.findViewById(R.id.row_2);
        row3 = rootView.findViewById(R.id.row_3);
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
        aboutMeLayout = rootView.findViewById(R.id.profile_list_about_me_layout_item);
        chatFab = rootView.findViewById(R.id.chat_fab);
        followFab = rootView.findViewById(R.id.follow_fab);
        profileFab = rootView.findViewById(R.id.profile_fab);

        //dog walker relevant
        dogSizeLayoutBtn = rootView.findViewById(R.id.profile_list_dog_sizes_layout_item);
        rangeLayoutBtn = rootView.findViewById(R.id.profile_list_km_range_layout_item);
        lastCallLayoutBtn = rootView.findViewById(R.id.profile_list_last_call_layout_item);
        paymentLayoutBtn = rootView.findViewById(R.id.profile_list_payment_layout_item);
        expLayoutBtn = rootView.findViewById(R.id.profile_list_experience_layout_item);
        ratingLayoutBtn = rootView.findViewById(R.id.profile_list_rating_layout_item);
        criticsLayoutBtn = rootView.findViewById(R.id.critics_layout);
        criticsCountTv = rootView.findViewById(R.id.critics_count_tv);
        sizesTv = rootView.findViewById(R.id.profile_item_dog_sizes_tv);
        rangeTv = rootView.findViewById(R.id.profile_item_km_range_tv);
        lastCallTv = rootView.findViewById(R.id.profile_item_last_call_tv);
        paymentTv = rootView.findViewById(R.id.profile_item_payment_tv);
        expTv = rootView.findViewById(R.id.profile_item_exp_tv);
        ratingTv = rootView.findViewById(R.id.profile_item_rating_tv);

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
                if (isMe) {
                    buildProfileSheetDialog();
                } else {
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
                }

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

        //**************//
        loadAllReviews();


        return rootView;
    }

    private void assignWalkerListeners(final String sizes, final Integer range, final boolean lastCall, final int paymentPerWalk, final String exp) {

        dogSizeLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomDialog(R.drawable.dog_icon_128, getString(R.string.good_with_dogs_at_sizes), sizes);
            }
        });

        rangeLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomDialog(R.drawable.trek_icon_128, getString(R.string.is_willing_in_range_of), range + " " + getString(R.string.km));
            }
        });

        lastCallLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer;
                if (lastCall) {
                    answer = getString(R.string.yes);
                } else {
                    answer = getString(R.string.no);
                }
                createCustomDialog(R.drawable.deadline_icon_128, getString(R.string.allows_last_minute_call), answer);
            }
        });

        paymentLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomDialog(R.drawable.pay_icon_128, getString(R.string.service_cost_per_trip), paymentPerWalk + " " + getString(R.string.ils));
            }
        });

        expLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomDialog(R.drawable.exp_icon_128, getString(R.string.dog_walking_exp_title), exp + " " + getString(R.string.years_of_exp));
            }
        });

        ratingLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMe) { //can make review
                    buildReviewAddSheetDialog();
                } else { //can only see my rating

                    //TODO put actual data
                    createCustomDialog(R.drawable.icon_star_100, getString(R.string.review_rating), getString(R.string.total_rating_reviews));
                }

            }
        });

        criticsLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!reviewList.isEmpty()) {
                    buildAllReviewsSheetDialog(); //build dialog first
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.no_curr_reviews,Snackbar.LENGTH_LONG).show();
                }
                //loadAllReviews(); //load reviews list after
            }
        });
    }

    private void buildAllReviewsSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_all_reviews, null);

        progressBar = bottomSheetView.findViewById(R.id.reviews_progress_bar);
        TextView title = bottomSheetView.findViewById(R.id.bottom_sheet_reviews_title);
        title.setText(getString(R.string.all_reviews_about) + " " + otherUserName);

        reviewsRecyclerView = bottomSheetView.findViewById(R.id.reviews_recycler);
        reviewsRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //load posts here
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews");
        reviewsRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Review review = ds.getValue(Review.class);
                        reviewList.add(review);
                    }

                    reviewAdapter = new ReviewAdapter(reviewList, getActivity());
                    reviewsRecyclerView.setAdapter(reviewAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    //only if WALKER!!!!!!!!
    private void loadAllReviews() {

        //ONLY TAKING THE LIST OF REVIEWS WITHOUT RECYCLER

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reviews");
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Review review = ds.getValue(Review.class);
                    reviewList.add(review); //adding all previous reviews
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*progressBar.setVisibility(View.VISIBLE);

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews");
        reviewsRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Review review = ds.getValue(Review.class);
                        reviewList.add(review);
                    }

                    reviewAdapter = new ReviewAdapter(reviewList, getActivity());
                    reviewsRecyclerView.setAdapter(reviewAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });*/
    }

    private void createCustomDialog(int icon, String title, String body) {

        final AlertDialog alertDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.profile_items_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        alertDialog = builder.setView(dialogView).show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView titleTv = dialogView.findViewById(R.id.profile_dialog_title);
        TextView bodyTv = dialogView.findViewById(R.id.profile_dialog_body);
        ImageView iconIv = dialogView.findViewById(R.id.profile_dialog_icon);
        final Button okBtn = dialogView.findViewById(R.id.profile_dialog_btn);

        titleTv.setText(title);
        bodyTv.setText(body);
        iconIv.setImageResource(icon);
        iconIv.animate().scaleX(1f).scaleY(1f).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                okBtn.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        }).start();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
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

                    //for dialogs including name
                    otherUserName = user.getFullName();

                    email = user.getEmail();
                    imgURL = user.getPhotoUrl();
                    int age = user.getAge();

                    try {
                        Glide.with(getActivity()).asBitmap().load(imgURL).placeholder(R.drawable.account_icon).into(profileIv);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }

                    //set views and text
                    nameTv.setText(user.getFullName());
                    locationTv.setText(user.getLocation());
                    aboutMeTv.setText(user.getAboutMe());

                    if(getActivity()!=null) {
                        if (user.getGender() == 0) {
                            genderAgeTv.setText(getString(R.string.male) + ", " + age);
                            genderIv.setImageResource(R.drawable.man_icon);
                        } else if (user.getGender() == 1) {
                            genderAgeTv.setText(getString(R.string.female) + ", " + age);
                            genderIv.setImageResource(R.drawable.woman_icon);
                        } else {
                            genderAgeTv.setText(getString(R.string.other) + ", " + age);
                            genderIv.setImageResource(R.drawable.other_icon);
                        }
                    }

                    //USER TYPE
                    if (!user.getType()) { // NOT WALKER
                        typeTv.setText(R.string.dog_owner);
                        typeIv.setImageResource(R.drawable.dog_owner_icon);
                        isWalker = false;
                        row1.setVisibility(View.GONE);
                        row2.setVisibility(View.GONE);
                        row3.setVisibility(View.GONE);
                        criticsLayoutBtn.setVisibility(View.GONE);

                    } else { //WALKER
                        typeTv.setText(R.string.dog_walker);
                        typeIv.setImageResource(R.drawable.dog_walker_icon);
                        isWalker = true;
                        row1.setVisibility(View.VISIBLE);
                        row2.setVisibility(View.VISIBLE);
                        row3.setVisibility(View.VISIBLE);
                        criticsLayoutBtn.setVisibility(View.VISIBLE);

                        String sizes = user.getDogSizesList(); // 0 2 4
                        String size = sizes.substring(1, sizes.length() - 1);
                        Integer range = user.getKmRange();
                        String exp = user.getExperience();
                        boolean lastCall = user.getLastCall();
                        int paymentPerWalk = user.getPaymentPerWalk();


                        if(getActivity()!=null) {
                            sizesTv.setText(size);
                            rangeTv.setText(range + " " + getActivity().getString(R.string.km));
                            if (lastCall) {
                                lastCallTv.setText(R.string.yes);
                            } else {
                                lastCallTv.setText(R.string.no);
                            }
                            paymentTv.setText(paymentPerWalk + " " + getString(R.string.ils));
                            expTv.setText(exp + " " + getString(R.string.years_of_exp));
                        }
                        assignWalkerListeners(size, range, lastCall, paymentPerWalk, exp);
                    }

                    //setting icons
                    if (getActivity() != null) {
                        //move here maybe
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

        //only for walkers
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reviews");
        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    criticsCountTv.setText(snapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildReviewAddSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_add_review, null);

        ImageButton sendReviewBtn = bottomSheetView.findViewById(R.id.review_btn);
        final EditText sendReviewEt = bottomSheetView.findViewById(R.id.review_et);
        final ImageView star1 = bottomSheetView.findViewById(R.id.star_1_sheet);
        final ImageView star2 = bottomSheetView.findViewById(R.id.star_2_sheet);
        final ImageView star3 = bottomSheetView.findViewById(R.id.star_3_sheet);
        final ImageView star4 = bottomSheetView.findViewById(R.id.star_4_sheet);
        final ImageView star5 = bottomSheetView.findViewById(R.id.star_5_sheet);

        final List<ImageView> starsImgList = new ArrayList<>();
        starsImgList.add(star1);
        starsImgList.add(star2);
        starsImgList.add(star3);
        starsImgList.add(star4);
        starsImgList.add(star5);

        ratingNum = 0;

        //**********Rating Click Listeners*************//
        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.star_full_128);
                star2.setImageResource(R.drawable.star_empty_128);
                star3.setImageResource(R.drawable.star_empty_128);
                star4.setImageResource(R.drawable.star_empty_128);
                star5.setImageResource(R.drawable.star_empty_128);

                ratingNum = 1;
            }
        });
        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.star_full_128);
                star2.setImageResource(R.drawable.star_full_128);
                star3.setImageResource(R.drawable.star_empty_128);
                star4.setImageResource(R.drawable.star_empty_128);
                star5.setImageResource(R.drawable.star_empty_128);

                ratingNum = 2;
            }
        });
        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.star_full_128);
                star2.setImageResource(R.drawable.star_full_128);
                star3.setImageResource(R.drawable.star_full_128);
                star4.setImageResource(R.drawable.star_empty_128);
                star5.setImageResource(R.drawable.star_empty_128);

                ratingNum = 3;
            }
        });
        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.star_full_128);
                star2.setImageResource(R.drawable.star_full_128);
                star3.setImageResource(R.drawable.star_full_128);
                star4.setImageResource(R.drawable.star_full_128);
                star5.setImageResource(R.drawable.star_empty_128);

                ratingNum = 4;
            }
        });
        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.star_full_128);
                star2.setImageResource(R.drawable.star_full_128);
                star3.setImageResource(R.drawable.star_full_128);
                star4.setImageResource(R.drawable.star_full_128);
                star5.setImageResource(R.drawable.star_full_128);

                ratingNum = 5;
            }
        });

        loadAllReviews(); //to fill the list first

        sendReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendReviewEt.getText().equals("") || ratingNum == 0) {
                    Toast.makeText(getActivity(), R.string.pls_leave_review_and_rating, Toast.LENGTH_SHORT).show();
                } else {

                    //dismiss this dialog
                    loadAllReviews();

                    //start loading dialog

                    //create a user form myself to create Review instance
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //add myself to his list
                            User user = snapshot.getValue(User.class);
                            String timeStamp = String.valueOf(System.currentTimeMillis());
                            Review review = new Review(user.getId(), user.getFullName(), user.getLocation(), user.getPhotoUrl(), timeStamp, sendReviewEt.getText().toString(), ratingNum);


                            //****** LOAD REVIEWS AND UPDATE THEM*******///
                            //add to list and update firebase "Reviews"
                            reviewList.add(review);

                            //start loader here!!!!!!!!!!!!!!!!!!

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reviews").child(userID);
                            reference.setValue(reviewList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //end loader here!
                                        Toast.makeText(getActivity(), reviewList.size() + "", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    } else {
                                        bottomSheetDialog.dismiss();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void buildProfileSheetDialog() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED && hasReadPermission != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
        }


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

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "from");
                fileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_REQUEST);
                bottomDialog.dismiss();
            }
        });

        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE);
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
        if (!isMe) {
            inflater.inflate(R.menu.profile_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_profile_back:
                listener.onProfileBackPress();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                bitmap2 = null;
                try {
                    bitmap1 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profileIv.setImageBitmap(bitmap1);
                //getActivity().getContentResolver().delete(fileUri,null, null); // have to transfer to register button
                //pressTv.setVisibility(View.GONE);
                isFromCamera = true;
                if (bitmap1 != null) {
                    profileIv.setImageBitmap(bitmap1);
                    handleUpload(bitmap1);

                }
                //alertDialog.dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                fileUri = null;
            }

        }

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK && permission == true) {
                fileUri = data.getData();
                bitmap1 = null;
                try {
                    bitmap2 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap2 != null) {
                    profileIv.setImageBitmap(bitmap2);
                    handleUpload(bitmap2);

                }

                //pressTv.setVisibility(View.GONE);
                isFromCamera = false;
                if (isFromCamera)
                    getActivity().getContentResolver().delete(fileUri, null, null);
                //alertDialog.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "No permissions!!!", Toast.LENGTH_SHORT).show();
                permission = false;
            } else {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
                permission = true;
            }
        }

    }

    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Images").child(email + ".jpeg");

        storage.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(storage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    private void getDownloadUrl(StorageReference storage) {
        storage.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setUserProfileUrl(uri);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void setUserProfileUrl(Uri uri) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        if (user != null) {
            user.updateProfile(request)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            usersRef.child(fUser.getUid()).child("photoUrl").setValue(fUser.getPhotoUrl().toString());
                            final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");
                            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    //update my posts
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ModelPost post = ds.getValue(ModelPost.class);
                                            if (post.getuId().equals(fUser.getUid())) {
                                                Map<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("uPic", fUser.getPhotoUrl().toString());
                                                postRef.child(ds.getKey()).updateChildren(hashMap);
                                            }

                                            //update my comments
                                            /*postRef.child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()) {
                                                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            ModelComment comment = dataSnapshot.getValue(ModelComment.class);
                                                            if(comment.getuId().equals(fUser.getUid())) {
                                                                Map<String,Object> picMap = new HashMap<>();
                                                                picMap.put("uPic",fUser.getPhotoUrl().toString());
                                                                postRef.child("Comments").child(comment.getcId()).updateChildren(picMap);
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });*/
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //listener.stopLoader();
                            //listener.createConfirmDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //listener.stopLoader();
                        }
                    });
        }
    }

}

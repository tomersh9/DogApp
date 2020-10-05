package com.example.dogapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Adapters.ReviewAdapter;
import com.example.dogapp.Adapters.SliderAdapter;
import com.example.dogapp.Enteties.Review;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelComment;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.Models.SliderItem;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener, SliderAdapter.MySliderAdapterListener {

    private final String PROFILE_FRAGMENT_TAG = "profile_fragment_tag";

    //Firebase
    private List<String> followingList = new ArrayList<>();
    private List<String> followersList = new ArrayList<>();
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following");
    private DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers");
    private boolean isAnonymous;
    private String userID, imgURL, coverURL; //for any user of the app
    private boolean isMe;
    private boolean isWalker;
    private int ratingNum = 0;
    private String otherUserName;
    private Integer walkerRating;

    //reviews list (only for walkers)
    private List<Review> reviewList = new ArrayList<>();
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private ProgressBar progressBar;

    //views
    private float scaleXProfile, scaleYProfile;
    private ImageView profileIv, genderIv, typeIv, coverIv;
    private TextView nameTv, followingTv, followersTv, criticsCountTv, genderAgeTv, locationTv, typeTv, aboutMeTv;
    private LinearLayout followersLayoutBtn, followingLayoutBtn, criticsLayoutBtn;
    private RelativeLayout uploadPicsHintLayout;
    private FloatingActionButton chatFab, followFab, profileFab, sliderFab;
    private LinearLayout row1, row2, row3, row4;
    private RelativeLayout dogSizeLayoutBtn, rangeLayoutBtn, lastCallLayoutBtn, paymentLayoutBtn, expLayoutBtn, locationLayoutBtn;
    private TextView sizesTv, rangeTv, lastCallTv, paymentTv, expTv, ratingTv;
    //private TextView displayNameTv;
    private CoordinatorLayout coordinatorLayout;

    //rating stars
    private RelativeLayout toolbarStarsLayout;
    private ImageView starView1, starView2, starView3, starView4, starView5;


    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressBar profileProgressBar;
    private ProgressBar coverProgressBar;
    private ProgressBar deleteSliderDialogProgressBar;
    private boolean isProfilePicture; //to define clicks form profile and cover
    private boolean isSliderGallery = false;
    private boolean isProfileGallery = false;
    private boolean isCoverGallery = false;

    //Dialogs
    private AlertDialog alertDialog;
    private AlertDialog deleteSliderDialog;

    //camera and gallery
    Uri fileUri;
    Bitmap bitmap1, bitmap2;

    // Activity requests
    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST = 2;
    final int SELECT_IMAGE = 3;
    final int CAMERA_REQUEST_SLIDER = 4;
    final int SELECT_IMAGE_SLIDER = 5;
    boolean isFromCamera;
    boolean permission = true;
    String email;

    //View Pager 2
    private ViewPager2 viewPager2;
    private List<SliderItem> sliderItems = new ArrayList<>();
    private SliderAdapter sliderAdapter;

    //location
    private Geocoder geocoder;
    private Handler handler = new Handler();

    ///Following notifications
    private final String SERVER_KEY = "AAAAsSPUwiM:APA91bF5T2kokP05wtjBjEwMiUXAuB9OXF4cCSgqf4HV9ST1kzKuD9w3ncboYoGTZxMQbBSv0EocqTcycHE4gGzFDDeGIYkyLolsd3W1gY1ZPu5qCHjpNAh-H3g0Y-JvNUIZ1iOm8uOW";
    private final String BASE_URL = "https://fcm.googleapis.com/fcm/send";

    public interface OnProfileFragmentListener {

        void changeProfileToolBar(Toolbar toolbar);

        void onProfileFollowingsClick(String userID);

        void onProfileFollowersClick(String userID);

        void onProfileBackPress();

        void onProfileSettingsClick();
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
        isAnonymous = fUser.isAnonymous();

        if (userID.equals(fUser.getUid())) {
            isMe = true;
        } else {
            isMe = false;
        }

        //location geoCoder
        geocoder = new Geocoder(getActivity());

        //assign views
        uploadPicsHintLayout = rootView.findViewById(R.id.upload_pics_layout_to_hide);
        coordinatorLayout = rootView.findViewById(R.id.profile_coordinator);
        viewPager2 = rootView.findViewById(R.id.photo_view_pager);
        row1 = rootView.findViewById(R.id.row_1);
        row2 = rootView.findViewById(R.id.row_2);
        row3 = rootView.findViewById(R.id.row_3);
        row4 = rootView.findViewById(R.id.row_4);
        profileIv = rootView.findViewById(R.id.profile_frag_iv);
        coverIv = rootView.findViewById(R.id.profile_cover_iv);
        scaleXProfile = profileIv.getScaleX();
        scaleYProfile = profileIv.getScaleY();
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
        sliderFab = rootView.findViewById(R.id.slider_fab);
        profileProgressBar = rootView.findViewById(R.id.app_bar_progress_bar);
        coverProgressBar = rootView.findViewById(R.id.cover_progress_bar);
        locationLayoutBtn = rootView.findViewById(R.id.profile_list_location_layout_item);

        //TODO assign only needed!
        //dog walker relevant
        dogSizeLayoutBtn = rootView.findViewById(R.id.profile_list_dog_sizes_layout_item);
        rangeLayoutBtn = rootView.findViewById(R.id.profile_list_km_range_layout_item);
        lastCallLayoutBtn = rootView.findViewById(R.id.profile_list_last_call_layout_item);
        paymentLayoutBtn = rootView.findViewById(R.id.profile_list_payment_layout_item);
        expLayoutBtn = rootView.findViewById(R.id.profile_list_experience_layout_item);
        criticsLayoutBtn = rootView.findViewById(R.id.critics_layout);
        criticsCountTv = rootView.findViewById(R.id.critics_count_tv);
        sizesTv = rootView.findViewById(R.id.profile_item_dog_sizes_tv);
        rangeTv = rootView.findViewById(R.id.profile_item_km_range_tv);
        lastCallTv = rootView.findViewById(R.id.profile_item_last_call_tv);
        paymentTv = rootView.findViewById(R.id.profile_item_payment_tv);
        expTv = rootView.findViewById(R.id.profile_item_exp_tv);
        toolbarStarsLayout = rootView.findViewById(R.id.profile_toolbar_stars_hide);
        starView1 = rootView.findViewById(R.id.star_1_bar);
        starView2 = rootView.findViewById(R.id.star_2_bar);
        starView3 = rootView.findViewById(R.id.star_3_bar);
        starView4 = rootView.findViewById(R.id.star_4_bar);
        starView5 = rootView.findViewById(R.id.star_5_bar);

        //profile assign
        loadProfileViews();

        if (isMe) {
            chatFab.setVisibility(View.GONE);
            followFab.setVisibility(View.GONE);
            profileFab.setVisibility(View.VISIBLE);
            //sliderFab.setVisibility(View.VISIBLE);

            profileFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onProfileSettingsClick();
                }
            });

            if (!isWalker) { //edit sliding photos
                sliderFab.setVisibility(View.VISIBLE);
                sliderFab.hide();
                sliderFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildSliderBottomDialog();
                    }
                });
            }

        } else {
            chatFab.setVisibility(View.VISIBLE);
            followFab.setVisibility(View.VISIBLE);
            profileFab.setVisibility(View.GONE);
            sliderFab.setVisibility(View.GONE);
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
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() != 0) {
            listener.changeProfileToolBar(toolbar);
        }
        appBarLayout = rootView.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(this);
        toolbar.setTitle("");

        //profile pic click event
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isProfilePicture = true; //entering profile picture

                if (isMe) {
                    buildProfileSheetDialog(true); //dialog for profile image, else cover image
                } else {
                    loadImageDialog(imgURL);
                }

            }
        });

        coverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isProfilePicture = false; //entering cover picture

                if (isMe) {
                    buildProfileSheetDialog(false); //cover photo not profile

                } else if (!coverURL.equals("coverUrl")) {
                    loadImageDialog(coverURL);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.no_cover_to_show, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        try { //profile picture load
            Glide.with(getActivity()).asBitmap().load(imgURL).into(profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }

        //followers listeners
        followingLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnonymous) {
                    listener.onProfileFollowingsClick(userID);
                } else {
                    Snackbar.make(coordinatorLayout, R.string.only_reg_user, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        followersLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnonymous) {
                    listener.onProfileFollowersClick(userID);
                } else {
                    Snackbar.make(coordinatorLayout, R.string.only_reg_user, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        //**************//
        loadAllReviews();

        //View Pager
        //sliderAdapter.setSliderAdapterList(sliderItems);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager2.setPageTransformer(compositePageTransformer);

        return rootView;
    }

    private void buildSliderBottomDialog() {

        final BottomSheetDialog bottomDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_profile_slider, null);

        LinearLayout takePic, selectPic;
        takePic = bottomSheetView.findViewById(R.id.slider_take_pic);
        selectPic = bottomSheetView.findViewById(R.id.slider_select_pic);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture(CAMERA_REQUEST_SLIDER);
                bottomDialog.dismiss();
            }
        });

        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfileGallery = false;
                isCoverGallery = false;
                isSliderGallery = true;
                askStoragePermissions(SELECT_IMAGE_SLIDER);
                bottomDialog.dismiss();
            }
        });

        bottomDialog.setContentView(bottomSheetView);
        bottomDialog.show();
    }

    private void openGallery(int requestCode) {
        //TODO ask permissions here!!!
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    private void takePicture(int requestCode) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from");
        fileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, requestCode);
    }

    private void askStoragePermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED && hasReadPermission != PackageManager.PERMISSION_GRANTED) { //no permissions
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else { //have permissions
                openGallery(requestCode);
            }
        }
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

        criticsLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnonymous) {
                    buildAllReviewsSheetDialog();
                } else {
                    Snackbar.make(coordinatorLayout, R.string.only_reg_user, Snackbar.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void buildAllReviewsSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_all_reviews, null);

        progressBar = bottomSheetView.findViewById(R.id.reviews_progress_bar);
        Button addReviewBtn = bottomSheetView.findViewById(R.id.add_review_btn);
        TextView title = bottomSheetView.findViewById(R.id.bottom_sheet_reviews_title);
        title.setText(getString(R.string.all_reviews_about) + " " + otherUserName);
        final TextView noCurrReviews = bottomSheetView.findViewById(R.id.no_curr_reviews);

        if (!isMe) {
            addReviewBtn.setVisibility(View.VISIBLE);
            addReviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buildReviewAddSheetDialog();
                    bottomSheetDialog.dismiss();
                }
            });

        } else {
            addReviewBtn.setVisibility(View.GONE);
        }

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

                    Collections.reverse(reviewList);
                    reviewAdapter = new ReviewAdapter(reviewList, getActivity());
                    reviewsRecyclerView.setAdapter(reviewAdapter);
                }
                progressBar.setVisibility(View.GONE);

                if (!reviewList.isEmpty()) {
                    noCurrReviews.setVisibility(View.GONE);
                } else {
                    noCurrReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    //only if walker
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
    }

    private void createCustomDeleteDialog(final int position) {

        View dialogView = getLayoutInflater().inflate(R.layout.profile_location_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        deleteSliderDialog = builder.setView(dialogView).show();
        deleteSliderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final RelativeLayout hideOnLoadLayout = dialogView.findViewById(R.id.delete_dialog_hide_layout);
        deleteSliderDialogProgressBar = dialogView.findViewById(R.id.delete_dialog_progress_bar);
        final Button yesBtn = dialogView.findViewById(R.id.profile_dialog_yes_btn);
        final Button noBtn = dialogView.findViewById(R.id.profile_dialog_no_btn);
        ImageView iconIv = dialogView.findViewById(R.id.profile_dialog_icon);

        iconIv.animate().scaleX(1f).scaleY(1f).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                yesBtn.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                noBtn.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        }).start();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideOnLoadLayout.setVisibility(View.GONE);
                deleteSliderDialogProgressBar.setVisibility(View.VISIBLE);

                //remove local list
                SliderItem item = sliderItems.get(position);
                sliderItems.remove(item);
                sliderAdapter.notifyItemRemoved(position); // doesn't work well

                //delete from storage and DB
                deleteSlidePic(item.getPhotoUrl());
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSliderDialog.dismiss();
            }
        });
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

                    final User user = snapshot.getValue(User.class);

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                List<Address> addresses = geocoder.getFromLocationName(user.getLocation(), 1);
                                final Address bestAddress = addresses.get(0);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        locationTv.setText(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();

                    //for dialogs including name
                    otherUserName = user.getFullName();

                    email = user.getEmail();
                    imgURL = user.getPhotoUrl();
                    coverURL = user.getCoverUrl();
                    int age = user.getAge();

                    //cover picture load
                    try {
                        Glide.with(getActivity()).asBitmap().load(coverURL).into(coverIv);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }

                    nameTv.setText(user.getFullName());


                    String aboutString = user.getAboutMe();
                    if (aboutString.isEmpty() && isMe) {
                        aboutMeTv.setText(R.string.tell_about_yourself);
                    } else {
                        aboutMeTv.setText(aboutString);
                    }

                    if (getActivity() != null) {

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

                        //view pager
                        row4.setVisibility(View.VISIBLE);
                        getPictureList(user.getDogPicList());
                        typeTv.setText(R.string.dog_owner);
                        typeIv.setImageResource(R.drawable.dog_owner_icon);
                        isWalker = false;
                        row1.setVisibility(View.GONE);
                        row2.setVisibility(View.GONE);
                        row3.setVisibility(View.GONE);
                        criticsLayoutBtn.setVisibility(View.GONE);
                        toolbarStarsLayout.setVisibility(View.GONE);

                    } else { //WALKER

                        row4.setVisibility(View.GONE);
                        typeTv.setText(R.string.dog_walker);
                        typeIv.setImageResource(R.drawable.dog_walker_icon);
                        isWalker = true;
                        row1.setVisibility(View.VISIBLE);
                        row2.setVisibility(View.VISIBLE);
                        row3.setVisibility(View.VISIBLE);
                        criticsLayoutBtn.setVisibility(View.VISIBLE);
                        toolbarStarsLayout.setVisibility(View.VISIBLE);
                        walkerRating = user.getRating();

                        assignStarts(walkerRating);

                        if (getActivity() != null) {

                            Integer range = user.getKmRange();

                            //key-value
                            Integer exp = user.getExperience();
                            String[] expArr = getResources().getStringArray(R.array.exp_years_array);
                            String expString = expArr[exp];

                            boolean lastCall = user.getLastCall();
                            int paymentPerWalk = user.getPaymentPerWalk();

                            //get values of the integer-array keys to present in display
                            List<Integer> indexList = user.getDogSizesList(); //keys
                            List<String> valuesList = new ArrayList<>(); //values

                            String[] keysArr = getResources().getStringArray(R.array.dog_sizes_array);
                            for (Integer index : indexList) {
                                valuesList.add(keysArr[index]); //get actual RTL values of list
                            }
                            String result = valuesList.toString();
                            result = result.substring(1, result.length() - 1);

                            sizesTv.setText(result);
                            rangeTv.setText(range + " " + getActivity().getString(R.string.km));
                            if (lastCall) {
                                lastCallTv.setText(R.string.yes);
                            } else {
                                lastCallTv.setText(R.string.no);
                            }
                            paymentTv.setText(paymentPerWalk + " " + getString(R.string.ils));
                            expTv.setText(expString + " " + getString(R.string.years_of_exp));

                            assignWalkerListeners(result, range, lastCall, paymentPerWalk, expString);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPictureList(List<SliderItem> dogPicList) {

        if (dogPicList != null) {
            sliderItems = dogPicList;
        }

        if (isMe) {
            if (sliderItems.isEmpty()) {
                uploadPicsHintLayout.setVisibility(View.VISIBLE);
            } else {
                uploadPicsHintLayout.setVisibility(View.GONE);
            }
        }

        sliderAdapter = new SliderAdapter(sliderItems);
        viewPager2.setAdapter(sliderAdapter);
        sliderAdapter.setListener(ProfileFragment.this);
        sliderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPictureClicked(int position) {
        loadImageDialog(sliderItems.get(position).getPhotoUrl());
    }

    @Override
    public void onPictureLongClicked(final int position, View view) {

        if (isMe) {
            createCustomDeleteDialog(position);
        }
    }

    private void deleteSlidePic(String path) {

        StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        storage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (sliderItems.isEmpty()) {
                    uploadPicsHintLayout.setVisibility(View.VISIBLE);
                }
                //update DB
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
                Map<String, Object> hashMap = new HashMap<>();
                hashMap.put("dogPicList", sliderItems);
                reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(coordinatorLayout, R.string.delete_succeed, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(coordinatorLayout, R.string.delete_failed, Snackbar.LENGTH_SHORT).show();
                        }
                        deleteSliderDialogProgressBar.setVisibility(View.GONE);
                        deleteSliderDialog.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                deleteSliderDialogProgressBar.setVisibility(View.GONE);
                deleteSliderDialog.dismiss();
                Snackbar.make(coordinatorLayout, R.string.delete_failed, Snackbar.LENGTH_SHORT).show();
            }
        });


    }

    private void assignStarts(Integer walkerRating) {
        List<ImageView> stars = new ArrayList<>();
        stars.add(starView1);
        stars.add(starView2);
        stars.add(starView3);
        stars.add(starView4);
        stars.add(starView5);
        for (int i = 0; i < walkerRating; i++) {
            stars.get(i).setImageResource(R.drawable.star_full_128);
        }
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

        if (isAnonymous) {
            Snackbar.make(coordinatorLayout, R.string.only_reg_user, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!followingList.contains(userID)) { //follow user

            //add the user to my following list
            followingList.add(userID);
            followingRef.child(fUser.getUid()).setValue(followingList);
            sendToToken(userID);

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
            //change fab appearance
            chatFab.show();
            followFab.setImageResource(R.drawable.unfollow_user_icon_64);
            followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.you_now_follow) + " " + nameTv.getText().toString(), Snackbar.LENGTH_SHORT).show();

        } else { //unFollow user

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
            //change fab appearance
            chatFab.hide();
            followFab.setImageResource(R.drawable.follow_user_icon_64);
            followFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.unfollow_from) + " " + nameTv.getText().toString(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sendToToken(String userID) {
        //setting data with JSON objects to get the children
        final JSONObject rootJson = new JSONObject(); //we put here "data" and "to"
        final JSONObject dataJson = new JSONObject();

        try {
            dataJson.put("message", "follow");
            dataJson.put("isFollow", "check");
            dataJson.put("fullName", fUser.getDisplayName());
            dataJson.put("imgURL", fUser.getPhotoUrl().toString());
            dataJson.put("uID", fUser.getUid());
            rootJson.put("to", "/topics/" + userID);
            rootJson.put("data", dataJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //create POST request
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, BASE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) { //POST REQUEST class implementation

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + SERVER_KEY);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return rootJson.toString().getBytes(); //return the root object with data inside
            }
        };

        //sending the actual request
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
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

        final ProgressBar bottomSheetReviewProgressBar = bottomSheetView.findViewById(R.id.review_sheet_progress_bar);
        final RelativeLayout hideBottomSheetReview = bottomSheetView.findViewById(R.id.hide_bottom_review);

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

                    //start loading dialog
                    hideBottomSheetReview.setVisibility(View.INVISIBLE);
                    bottomSheetReviewProgressBar.setVisibility(View.VISIBLE);

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

                            //getting all previous ratings
                            int avg = 0;
                            for (Review review1 : reviewList) {
                                avg += review1.getRateNumber();
                            }
                            walkerRating = avg / reviewList.size();

                            //update walker's review
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userID);
                            Map<String, Object> hashMap = new HashMap<>();
                            hashMap.put("rating", walkerRating);
                            ref.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Toast.makeText(getActivity(), walkerRating + " success", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Toast.makeText(getActivity(), walkerRating + " fail", Toast.LENGTH_SHORT).show();
                                    }
                                    assignStarts(walkerRating);
                                }
                            });

                            //update reviews list
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reviews").child(userID);
                            reference.setValue(reviewList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //end loader here!
                                        bottomSheetDialog.dismiss();
                                    } else {
                                        bottomSheetDialog.dismiss();
                                    }
                                    bottomSheetReviewProgressBar.setVisibility(View.GONE);
                                    hideBottomSheetReview.setVisibility(View.VISIBLE);
                                    buildAllReviewsSheetDialog();
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

    //TODO make this 1 time
    private void buildProfileSheetDialog(final boolean isProfile) {

        if (isProfile) {
            final BottomSheetDialog bottomDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_profile, null);

            LinearLayout showPic, takePic, selectPic;
            showPic = bottomSheetView.findViewById(R.id.select_display);
            takePic = bottomSheetView.findViewById(R.id.select_take_pic);
            selectPic = bottomSheetView.findViewById(R.id.select_choose_pic);

            showPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadImageDialog(fUser.getPhotoUrl().toString());
                    bottomDialog.dismiss();
                }
            });

            takePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture(CAMERA_REQUEST);
                    bottomDialog.dismiss();
                }
            });

            selectPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isProfileGallery = true;
                    isCoverGallery = false;
                    isProfileGallery = false;
                    askStoragePermissions(SELECT_IMAGE);
                    bottomDialog.dismiss();
                }
            });

            bottomDialog.setContentView(bottomSheetView);
            bottomDialog.show();

        } else { //cover photo

            final BottomSheetDialog bottomDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_cover, null);

            LinearLayout showPic, takePic, selectPic;
            showPic = bottomSheetView.findViewById(R.id.cover_show_pic);
            takePic = bottomSheetView.findViewById(R.id.cover_take_pic);
            selectPic = bottomSheetView.findViewById(R.id.cover_choose_pic);

            showPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!coverURL.equals("coverUrl")) {
                        loadImageDialog(coverURL);
                        bottomDialog.dismiss();
                    } else {
                        bottomDialog.dismiss();
                        Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.no_cover_to_show, Snackbar.LENGTH_SHORT).show();
                    }

                }
            });

            takePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture(CAMERA_REQUEST);
                    bottomDialog.dismiss();
                }
            });

            selectPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isCoverGallery = true;
                    isProfileGallery = false;
                    isSliderGallery = false;
                    askStoragePermissions(SELECT_IMAGE);
                    bottomDialog.dismiss();
                }
            });

            bottomDialog.setContentView(bottomSheetView);
            bottomDialog.show();
        }
    }

    //opens picture full res with Glide
    private void loadImageDialog(String url) {

        View dialogView = getLayoutInflater().inflate(R.layout.image_display_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        alertDialog = builder.setView(dialogView).show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final ProgressBar progressBar = dialogView.findViewById(R.id.img_loader_bar);
        final ImageView imageView = dialogView.findViewById(R.id.img_display);
        final ImageButton backBtn = dialogView.findViewById(R.id.img_dialog_back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.GONE);
                alertDialog.dismiss();
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        try {
            Glide.with(dialogView).load(url).listener(new RequestListener<Drawable>() {
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
    }

    //profile pic animation transition
    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

            // Collapsed
            profileIv.animate().scaleX(scaleXProfile / 1.5f).scaleY(scaleYProfile / 1.5f).setDuration(300).start();
            if (isMe) {
                profileFab.hide();
                if (!isWalker) {
                    sliderFab.show();
                }
            } else {

                if (followingList.contains(userID)) {
                    chatFab.hide();
                }
                followFab.hide();
            }


        } else if (verticalOffset == 0) {
            // Expanded
            profileIv.animate().scaleX(1f).scaleY(1f).setDuration(300).start();

            if (isMe) {

                profileFab.show();

                if (!isWalker) {
                    sliderFab.hide();
                }

            } else {
                if (followingList.contains(userID)) {
                    chatFab.show();
                }
                followFab.show();
            }

        } else {
            //in the middle
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
                isFromCamera = true;
                if (bitmap1 != null) {
                    handleUpload(bitmap1, isProfilePicture);
                }
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
                    handleUpload(bitmap2, isProfilePicture);

                }

                isFromCamera = false;
                if (isFromCamera) {
                    getActivity().getContentResolver().delete(fileUri, null, null);
                }
            }
        }

        if (requestCode == CAMERA_REQUEST_SLIDER) {

            if (resultCode == Activity.RESULT_OK) {

                bitmap2 = null;
                try {
                    bitmap1 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isFromCamera = true;
                if (bitmap1 != null) {
                    uploadSliderPic(bitmap1);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                fileUri = null;
            }

        }

        if (requestCode == SELECT_IMAGE_SLIDER) {

            if (resultCode == Activity.RESULT_OK && permission == true) {

                fileUri = data.getData();
                bitmap1 = null;
                try {
                    bitmap2 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap2 != null) {
                    uploadSliderPic(bitmap2);
                }
                isFromCamera = false;
                if (isFromCamera) {
                    getActivity().getContentResolver().delete(fileUri, null, null);
                }
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

                if (isSliderGallery) {
                    openGallery(SELECT_IMAGE_SLIDER);
                    isSliderGallery = false;
                } else if (isProfileGallery) {
                    openGallery(SELECT_IMAGE);
                    isProfileGallery = false;
                } else {
                    openGallery(SELECT_IMAGE);
                    isCoverGallery = false;
                }
            }
        }

    }

    private void uploadSliderPic(Bitmap bitmap) {

        //update DB
        //update database field
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Slider_pics").child(fUser.getUid()).child(sliderItems.size() + ".jpeg");
        storage.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storage.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        uploadPicsHintLayout.setVisibility(View.GONE);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
                                        SliderItem item = new SliderItem(uri.toString());
                                        sliderItems.add(item);
                                        sliderAdapter.notifyItemInserted(sliderItems.size());
                                        Map<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("dogPicList", sliderItems);
                                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Snackbar.make(coordinatorLayout, R.string.picture_added, Snackbar.LENGTH_SHORT).show();
                                                } else {
                                                    Snackbar.make(coordinatorLayout, R.string.failed_to_add_picture, Snackbar.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void handleUpload(Bitmap bitmap, final boolean isProfilePicture) {

        if (isProfilePicture) {

            profileProgressBar.setVisibility(View.VISIBLE);
            profileIv.setVisibility(View.INVISIBLE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Profiles").child(email + ".jpeg");

            storage.putBytes(baos.toByteArray())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            getDownloadUrl(storage, true);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        } else {

            coverProgressBar.setVisibility(View.VISIBLE);
            coverIv.setVisibility(View.INVISIBLE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Covers").child(email + ".jpeg");

            storage.putBytes(baos.toByteArray())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            getDownloadUrl(storage, false);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

    }

    private void getDownloadUrl(StorageReference storage, final boolean isProfilePicture) {
        storage.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (isProfilePicture) {
                            //update firebase user profile photo
                            setUserProfileUrl(uri);
                        } else {
                            //update user field cover photo
                            setUserCoverUrl(uri);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    //update cover photo url
    private void setUserCoverUrl(final Uri uri) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("coverUrl", uri.toString());
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) { //TODO local

                    coverURL = uri.toString();

                    try {
                        Glide.with(getActivity()).asBitmap().load(uri).listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.failed_update_cover, Snackbar.LENGTH_SHORT).show();
                                coverProgressBar.setVisibility(View.GONE);
                                coverIv.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.cover_photo_updated, Snackbar.LENGTH_SHORT).show();
                                coverProgressBar.setVisibility(View.GONE);
                                coverIv.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }).into(coverIv);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }

                } else {
                    coverIv.setVisibility(View.VISIBLE);
                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.failed_update_cover, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    //update profile photo url
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
                            usersRef.child(fUser.getUid()).child("photoUrl").setValue(fUser.getPhotoUrl().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        try {
                                            Glide.with(getActivity()).asBitmap().load(fUser.getPhotoUrl()).listener(new RequestListener<Bitmap>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                    profileProgressBar.setVisibility(View.GONE);
                                                    profileIv.setVisibility(View.VISIBLE);
                                                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.profile_photo_update_fail, Snackbar.LENGTH_SHORT).show();
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                    profileProgressBar.setVisibility(View.GONE);
                                                    profileIv.setVisibility(View.VISIBLE);
                                                    Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.profile_pho_change, Snackbar.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                            }).into(profileIv);
                                        } catch (Exception ex) {
                                            ex.getMessage();
                                        }
                                    }
                                }
                            });

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
                                                hashMap.put("uPic", fUser.getPhotoUrl().toString());
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
                                                                picMap.put("uPic", fUser.getPhotoUrl().toString());
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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        }
    }

}

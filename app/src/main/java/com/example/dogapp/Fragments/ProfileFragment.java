package com.example.dogapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.dogapp.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    //Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser fUser = firebaseAuth.getCurrentUser();

    //views
    private ImageView profileIv;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private float x, y;

    //bottom sheet
    LinearLayout bottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    //options inside
    LinearLayout showPic, takePic, selectPic;

    //Dialogs
    private AlertDialog alertDialog;

    public interface OnProfileFragmentListener {
        void changeToolBar(Toolbar toolbar);
    }

    OnProfileFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnProfileFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnProfileFragmentListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        //bottom sheet behavior
        bottomSheet = rootView.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //options inside
        showPic = rootView.findViewById(R.id.select_display);
        selectPic = rootView.findViewById(R.id.select_choose_pic);
        takePic = rootView.findViewById(R.id.select_take_pic);

        //profile scaling animations
        profileIv = rootView.findViewById(R.id.profile_frag_iv);
        x = profileIv.getScaleX();
        y = profileIv.getScaleY();

        //assign profile image
        if (fUser.getPhotoUrl() != null) {
            Glide.with(this).asBitmap().load(fUser.getPhotoUrl()).into(profileIv);
        }

        //toolbar
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_toolbar_layout);
        toolbar = rootView.findViewById(R.id.toolbar_profile);
        listener.changeToolBar(toolbar);
        appBarLayout = rootView.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(this);

        //profile pic click event
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);}
        });

        //bottom sheet listeners
        showPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                View dialogView = getLayoutInflater().inflate(R.layout.image_display_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                alertDialog = builder.setView(dialogView).show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final ProgressBar progressBar = dialogView.findViewById(R.id.img_loader_bar);
                final ImageView imageView = dialogView.findViewById(R.id.img_display);
                progressBar.setVisibility(View.VISIBLE);

                try {
                    Glide.with(dialogView).load(fUser.getPhotoUrl()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.failed_upload_image,Snackbar.LENGTH_SHORT).show();
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
        });
        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
            }
        });
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open camera
            }
        });

        return rootView;
    }

    //profile pic animation transition
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
            // Collapsed
            profileIv.animate().scaleX(0).scaleY(0).setDuration(300).start();
            collapsingToolbarLayout.setTitle(getString(R.string.my_profile));
        } else if (verticalOffset == 0) {
            // Expanded
            profileIv.animate().scaleX(x).scaleY(y).setDuration(200).start();
            collapsingToolbarLayout.setTitle(fUser.getDisplayName());
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

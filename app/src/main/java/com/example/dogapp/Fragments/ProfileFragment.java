package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogapp.R;
import com.google.android.material.appbar.AppBarLayout;

public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    ImageView profileIv;
    AppBarLayout appBarLayout;
    Toolbar toolbar;

    float x, y;

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

        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        profileIv = rootView.findViewById(R.id.profile_frag_iv);
        x = profileIv.getScaleX();
        y = profileIv.getScaleY();

        toolbar = rootView.findViewById(R.id.toolbar_profile);
        listener.changeToolBar(toolbar);

        appBarLayout = rootView.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(this);

        return rootView;
    }

    //profile pic animation transition
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
            // Collapsed
            profileIv.animate().scaleX(0).scaleY(0).setDuration(300).start();
        } else if (verticalOffset == 0) {
            // Expanded
            profileIv.animate().scaleX(x).scaleY(y).setDuration(200).start();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Toast.makeText(getActivity(), "Options", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

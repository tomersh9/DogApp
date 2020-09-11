package com.example.dogapp.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.dogapp.BoardAdapter;
import com.example.dogapp.R;
import com.google.android.material.tabs.TabLayout;

public class ExploreFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.explore_fragment,container,false);
        viewPager = rootView.findViewById(R.id.board_view_pager);
        tabLayout = rootView.findViewById(R.id.tab_layout);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setting pager and adapter
        BoardAdapter boardAdapter = new BoardAdapter(getChildFragmentManager(),0);
        AdoptBoardFragment adoptBoardFragment = new AdoptBoardFragment();
        WalkerBoardFragment walkerBoardFragment = new WalkerBoardFragment();
        boardAdapter.addFragment(adoptBoardFragment,"Adopt");
        boardAdapter.addFragment(walkerBoardFragment,"Dog Walkers");
        viewPager.setAdapter(boardAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}

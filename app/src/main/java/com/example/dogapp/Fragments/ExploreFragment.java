package com.example.dogapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.dogapp.Adapters.BoardsPagerAdapter;
import com.example.dogapp.R;
import com.google.android.material.tabs.TabLayout;

public class ExploreFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.explore_fragment, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        viewPager = rootView.findViewById(R.id.board_view_pager);
        tabLayout = rootView.findViewById(R.id.tab_layout);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setting pager and adapter
        BoardsPagerAdapter boardsPagerAdapter = new BoardsPagerAdapter(getChildFragmentManager(), 0);
        DiscoverFriendsFragment discoverFriendsFragment = new DiscoverFriendsFragment();
        WalkerBoardFragment walkerBoardFragment = new WalkerBoardFragment();
        boardsPagerAdapter.addFragment(walkerBoardFragment, getString(R.string.dog_walkers));
        boardsPagerAdapter.addFragment(discoverFriendsFragment, getString(R.string.discover_friends));

        viewPager.setAdapter(boardsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.group_icon_64);
        tabLayout.getTabAt(1).setIcon(R.drawable.dog_helper_icon_64);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}

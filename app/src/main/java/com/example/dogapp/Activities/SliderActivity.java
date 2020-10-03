package com.example.dogapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.dogapp.Adapters.StarterSliderPagerAdapter;
import com.example.dogapp.Enteties.StarterSliderPagerItem;
import com.example.dogapp.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class SliderActivity extends AppCompatActivity {

    private ViewPager sliderPager;
    private StarterSliderPagerAdapter adapter;
    private TabLayout dotsTabLayout;
    private List<StarterSliderPagerItem> items = new ArrayList<>();
    private TextView nextTv, backTv;
    private int position = 0; //curr position when opens
    private Button finishBtn;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slider_activity_layout);

        //fixed portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sliderPager = findViewById(R.id.slider_view_pager);
        dotsTabLayout = findViewById(R.id.dots_tab_layout);
        nextTv = findViewById(R.id.slider_next_btn);
        backTv = findViewById(R.id.slider_back_btn);
        finishBtn = findViewById(R.id.finish_btn);

        items.add(new StarterSliderPagerItem(R.drawable.compass_icon_512,"Explore","You can search friends and dog walkers to follow their updates"));
        items.add(new StarterSliderPagerItem(R.drawable.write_icon_512,"Post updates","You can post anything that's on your mind for your followers to know. You can also write reviews for dog walkers you had experience with"));
        items.add(new StarterSliderPagerItem(R.drawable.work_icon_512,"Business account","You can sign up as a dog walker to offer your service to all the accounts of the app"));

        adapter = new StarterSliderPagerAdapter(items,this);
        sliderPager.setAdapter(adapter);
        dotsTabLayout.setupWithViewPager(sliderPager);

        dotsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(tab.getPosition() == items.size() - 1) {
                    nextTv.setVisibility(View.GONE);
                    startAnimation();
                } else {
                    nextTv.setVisibility(View.VISIBLE);
                    endAnimation();
                }

                if(tab.getPosition() > 0) {
                    backTv.setVisibility(View.VISIBLE);
                }

                if(tab.getPosition() == 0) {
                    backTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        nextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = sliderPager.getCurrentItem();

                if(position < items.size()) {
                    position++;
                    sliderPager.setCurrentItem(position); //move pages by button
                }

                if(position > 0) {
                    backTv.setVisibility(View.VISIBLE);
                } else if(position == 0) {
                    backTv.setVisibility(View.GONE);
                }
             }
        });

        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = sliderPager.getCurrentItem();

                if(position > 0) {
                    position--;
                    sliderPager.setCurrentItem(position);
                }

                if(position == 0) {
                    backTv.setVisibility(View.GONE);
                }
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SliderActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startAnimation() {
        finishBtn.setVisibility(View.VISIBLE);
        finishBtn.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
    }

    private void endAnimation() {
        finishBtn.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                finishBtn.setVisibility(View.INVISIBLE);
            }
        }).start();
    }
}

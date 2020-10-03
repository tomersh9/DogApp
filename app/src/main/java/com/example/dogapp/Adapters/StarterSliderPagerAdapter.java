package com.example.dogapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.dogapp.Enteties.StarterSliderPagerItem;
import com.example.dogapp.R;

import java.util.List;

public class StarterSliderPagerAdapter extends PagerAdapter {

    private List<StarterSliderPagerItem> items;
    private Context context;
    //private LayoutInflater layoutInflater;

    public StarterSliderPagerAdapter(List<StarterSliderPagerItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view  = LayoutInflater.from(context).inflate(R.layout.slider_layout_item,container,false);

        StarterSliderPagerItem item = items.get(position);

        ImageView iconView = view.findViewById(R.id.slider_item_icon);
        TextView titleTv = view.findViewById(R.id.slider_item_title);
        TextView bodyTv = view.findViewById(R.id.slider_item_body);

        iconView.setImageResource(item.getIcon());
        titleTv.setText(item.getTitle());
        bodyTv.setText(item.getBody());

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
        container.removeView((RelativeLayout)object);
    }
}

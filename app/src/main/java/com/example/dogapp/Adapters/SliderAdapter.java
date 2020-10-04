package com.example.dogapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Models.SliderItem;
import com.example.dogapp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private List<SliderItem> sliderAdapterList;
    private MySliderAdapterListener listener;

    public interface MySliderAdapterListener {
        void onPictureLongClicked(int position, View view);

        void onPictureClicked(int position);
    }

    public void setListener(MySliderAdapterListener listener) {
        this.listener = listener;
    }

    public SliderAdapter(List<SliderItem> sliderAdapterList) {
        this.sliderAdapterList = sliderAdapterList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.photo_slide_container, parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Glide.with(holder.itemView).asBitmap().load(sliderAdapterList.get(position).getPhotoUrl()).override(1000, 1000)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return sliderAdapterList.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slide);

            itemView.setOnClickListener(new View.OnClickListener() { //open image
                @Override
                public void onClick(View v) {
                    listener.onPictureClicked(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() { //open to delete image
                @Override
                public boolean onLongClick(View v) {
                    listener.onPictureLongClicked(getAdapterPosition(), v);
                    return false;
                }
            });

        }
    }
}
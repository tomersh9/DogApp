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

    public interface MySliderAdapterListener
    {
        void onPictureLongClicked(int position, View view);
    }

    public void setListener(MySliderAdapterListener listener) {
        this.listener = listener;
    }

    public void setSliderAdapterList(List<SliderItem> sliderAdapterList) {
        this.sliderAdapterList = sliderAdapterList;
    }

    public SliderAdapter(List<SliderItem> sliderAdapterList) {
        this.sliderAdapterList = sliderAdapterList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.photo_slide_container,parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        //holder.setImage(sliderAdapterList.get(position));
        //Glide.with(holder.itemView).load("https://firebasestorage.googleapis.com/v0/b/dogapp-4a37e.appspot.com/o/Profiles%2Fkk%40kk.kk.jpeg?alt=media&token=677494f0-1fc0-4f38-ba24-2bb208beeb40").into(holder.imageView);
        Glide.with(holder.itemView).asBitmap().load(sliderAdapterList.get(position).getPhotoUrl()).override(1000,1000)
                .into(holder.imageView);
        //holder.imageButton.setVisibility(View.INVISIBLE);

    }

    @Override
    public int getItemCount() {
        return sliderAdapterList.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView imageView;
        //private ImageButton imageButton;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slide);
            //imageButton = itemView.findViewById(R.id.close_btn);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onPictureLongClicked(getAdapterPosition(),v);
                    return false;
                }
            });

        }

        void setImage(SliderItem sliderItem)
        {
            //imageView.setImageResource(sliderItem.getPhotoUrl());
            //Glide.with(itemView).load(Uri.parse(sliderItem.getPhotoUrl())).into(imageView);
        }
    }
}
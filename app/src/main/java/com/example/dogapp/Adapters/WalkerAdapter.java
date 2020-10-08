package com.example.dogapp.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WalkerAdapter extends RecyclerView.Adapter<WalkerAdapter.WalkerViewHolder> implements Filterable {

    private List<User> walkersList;
    private List<User> usersFull;
    private MyWalkerAdapterListener listener;
    private Context context;
    private String userID;

    //location
    private Geocoder geocoder;
    private Address bestAddress;
    private List<Address> addresses;

    public interface MyWalkerAdapterListener {
        void onWalkerClicked(int pos);
    }

    public WalkerAdapter(List<User> walkersList, Context context,String userID) {
        this.walkersList = walkersList;
        this.usersFull = new ArrayList<>(walkersList);
        this.context = context;
        this.userID = userID;
        if(context!=null) {
            this.geocoder = new Geocoder(context);
        }
    }

    public void setWalkerAdapterListener(MyWalkerAdapterListener listener) {
        this.listener = listener;
    }

    public class WalkerViewHolder extends RecyclerView.ViewHolder {

        ImageView profileIv;
        TextView nameTv, locationTv, ageGenderTv, expTv, paymentTv;
        ImageView star1, star2, star3, star4, star5;
        ImageView meIcon;
        List<ImageView> starList;

        public WalkerViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.walker_cell_image);
            nameTv = itemView.findViewById(R.id.walker_cell_name_tv);
            locationTv = itemView.findViewById(R.id.walker_cell_location_tv);
            ageGenderTv = itemView.findViewById(R.id.walker_cell_gender_age_tv);
            expTv = itemView.findViewById(R.id.walker_cell_experience_tv);
            paymentTv = itemView.findViewById(R.id.walker_cell_payment_tv);
            meIcon = itemView.findViewById(R.id.walker_cell_me_icon);

            star1 = itemView.findViewById(R.id.star_1);
            star2 = itemView.findViewById(R.id.star_2);
            star3 = itemView.findViewById(R.id.star_3);
            star4 = itemView.findViewById(R.id.star_4);
            star5 = itemView.findViewById(R.id.star_5);

            starList = new ArrayList<>();
            starList.add(star1);
            starList.add(star2);
            starList.add(star3);
            starList.add(star4);
            starList.add(star5);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onWalkerClicked(getAdapterPosition());
                }
            });
        }
    }

    @NonNull
    @Override
    public WalkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.walker_card_view, null);
        WalkerViewHolder viewHolder = new WalkerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final WalkerViewHolder holder, int position) {
        //get walker instance
        final User walkerUser = walkersList.get(position);

        //for experience
        String[] expArr = context.getResources().getStringArray(R.array.exp_years_array);

        //assign views with his data
        holder.nameTv.setText(walkerUser.getFullName());
        holder.expTv.setText(expArr[walkerUser.getExperience()] + " " + context.getString(R.string.years_of_exp));
        holder.paymentTv.setText(walkerUser.getPaymentPerWalk() + " " + context.getString(R.string.ils) + " " + context.getString(R.string.per_walk));
        int age = walkerUser.getAge();

        //gender rtl
        if (walkerUser.getGender() == 0) {
            holder.ageGenderTv.setText(context.getString(R.string.male) + ", " + age);
        } else if (walkerUser.getGender() == 1) {
            holder.ageGenderTv.setText(context.getString(R.string.female) + ", " + age);
        } else {
            holder.ageGenderTv.setText(context.getString(R.string.other) + ", " + age);
        }

        holder.locationTv.setText(walkerUser.getLocation());
       /* final Handler handler = new Handler();
        Thread thread1 = new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                    addresses = geocoder.getFromLocationName(walkerUser.getLocation(), 1);
                    if (!addresses.isEmpty()) {
                        bestAddress = addresses.get(0);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.locationTv.setText(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.locationTv.setText(walkerUser.getLocation());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();*/

        //assign stars
        int rating = walkerUser.getRating();

        for (ImageView view : holder.starList) {
            view.setImageResource(R.drawable.star_empty_128);
        }

        for (int i = 0; i < rating; i++) {
            holder.starList.get(i).setImageResource(R.drawable.star_full_128);
        }

        //if me
        if(userID.equals(walkerUser.getId())) {
            holder.meIcon.setVisibility(View.VISIBLE);
        } else {
            holder.meIcon.setVisibility(View.GONE);
        }


        //assign profile image with Glide
        try {
            Glide.with(holder.itemView).asBitmap().load(walkerUser.getPhotoUrl()).placeholder(R.drawable.user_drawer_icon_256).into(holder.profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    @Override
    public int getItemCount() {
        return walkersList.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //back thread automatically

            List<User> filteredList = new ArrayList<>(); //only filtered items

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(usersFull); //return full list if has no filter!
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim(); //the filter
                for (User user : usersFull) { //adding matching items to the filtered list
                    /*try {
                        addresses = geocoder.getFromLocationName(user.getLocation(), 1);
                        if(!addresses.isEmpty()) {
                            bestAddress = addresses.get(0);
                            user.setLocation(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    if (user.getFullName().toLowerCase().contains(filterPattern) || user.getLocation().toLowerCase().contains(filterPattern)){
                        filteredList.add(user);
                    }
                }
            }
            //assign the final filtered list to the result and return them
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override //publish results on the UI
        protected void publishResults(CharSequence constraint, FilterResults results) {
            walkersList.clear();
            walkersList.addAll((List) results.values); //changing original list
            notifyDataSetChanged();
        }
    };
}
package com.example.dogapp.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.dogapp.Services.PlaceApi;

import java.util.ArrayList;

public class AutoCompleteAdapter extends ArrayAdapter implements Filterable {

    private ArrayList<String> results;
    private int resource;
    private Context context;
    private PlaceApi placeApi = new PlaceApi();

    public AutoCompleteAdapter(Context context, int resId) {
        super(context, resId);
        this.context = context;
        this.resource = resId;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public String getItem(int pos) {
        return results.get(pos);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    results = placeApi.autoComplete(constraint.toString());

                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }

            }
        };
        return filter;
    }


}

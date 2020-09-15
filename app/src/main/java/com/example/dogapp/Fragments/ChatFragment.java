package com.example.dogapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogapp.R;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    ArrayAdapter<String> arrayAdapter;
    //add recycler view of people

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        ListView listView = view.findViewById(R.id.my_list);
        List<String> myList = new ArrayList<>();
        myList.add("Hello");
        myList.add("WEEE");
        myList.add("BBSS");
        myList.add("FFGGGGGGG");
        myList.add("FF");
        myList.add("Hello");
        myList.add("WEEE");
        myList.add("BBSS");
        myList.add("FFGGGGGGG");
        myList.add("FF");
        myList.add("Hello");
        myList.add("WEEE");
        myList.add("BBSS");
        myList.add("FFGGGGGGG");
        myList.add("שתוק");
        myList.add("שתוק");
        myList.add("כלב");
        myList.add("ערבי");
        myList.add("בן אלף זונה");
        myList.add("שרמוטנהה");
        myList.add("דגכדגכ");
        myList.add("כככ");
        myList.add("כככ");
        myList.add("עעעע");

        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, myList);
        listView.setAdapter(arrayAdapter);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setQueryHint("Search people");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        arrayAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}

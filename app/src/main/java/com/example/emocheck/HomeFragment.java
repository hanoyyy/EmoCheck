package com.example.emocheck;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.emocheck.homepage.DataClass;
import com.example.emocheck.homepage.MyAdapter;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapter adapter;
    DataClass androidData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        dataList = new ArrayList<>();

        androidData = new DataClass("Welcome", R.string.welcome, "", R.drawable.welcome_detail);
        dataList.add(androidData);

        androidData = new DataClass("What is EmoCheck?", R.string.whatis, "Click Here", R.drawable.whatis_detail);
        dataList.add(androidData);

        androidData = new DataClass("How to Start EmoCheck?", R.string.guide, "Click Here", R.drawable.guide_detail);
        dataList.add(androidData);

        androidData = new DataClass("Let's get to know us!", R.string.developers, "Click Here", R.drawable.developers_detail);
        dataList.add(androidData);

        adapter = new MyAdapter(requireActivity(), dataList);
        recyclerView.setAdapter(adapter);
    }
}
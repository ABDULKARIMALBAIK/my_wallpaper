package com.abdulkarimalbaik.dev.mywallpapers.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.ListWallpaper;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.abdulkarimalbaik.dev.mywallpapers.R;
import com.abdulkarimalbaik.dev.mywallpapers.ViewHolder.ListWallpaperViewHolder;
import com.abdulkarimalbaik.dev.mywallpapers.ViewWallpaper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class TrendingFragment extends Fragment {

    RecyclerView recyclerView;

    FirebaseDatabase database;
    FirebaseRecyclerAdapter<WallpaperItem,ListWallpaperViewHolder> adapter;
    DatabaseReference categoryWallpaper;


    public static TrendingFragment instance = null;

    public TrendingFragment() {

        // Required empty public constructor
        database = FirebaseDatabase.getInstance();
        categoryWallpaper = database.getReference(Common.STR_WALLPAPER);

        adapter = new FirebaseRecyclerAdapter<WallpaperItem, ListWallpaperViewHolder>(
                WallpaperItem.class,
                R.layout.layout_wallpaper_item,
                ListWallpaperViewHolder.class,
                categoryWallpaper.orderByChild("viewCount").limitToLast(10)  //sort desc with limit to 10
        ) {
            @Override
            protected void populateViewHolder(ListWallpaperViewHolder holder, final WallpaperItem model, int position) {

                Picasso.get()
                        .load(model.getImageUrl())
                        .into(holder.wallpaper);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Intent intent = new Intent(getActivity() , ViewWallpaper.class);
                        Common.wallpaperItem = model;
                        Common.select_wallpaper_key = adapter.getRef(position).getKey();
                        startActivity(intent);
                    }
                });
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_popular, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_trending);
        recyclerView.setHasFixedSize(true);

        //Because Firebase return ascending sort list , so we need reverse to show largest item is first
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);

        loadTrendingList();

        return view;
    }

    private void loadTrendingList() {

        recyclerView.setAdapter(adapter);
    }

    public static TrendingFragment getInstance(){

        if (instance == null)
            instance = new TrendingFragment();

        return instance;
    }

}

package com.abdulkarimalbaik.dev.mywallpapers.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.ListWallpaper;
import com.abdulkarimalbaik.dev.mywallpapers.Model.CategoryItem;
import com.abdulkarimalbaik.dev.mywallpapers.R;
import com.abdulkarimalbaik.dev.mywallpapers.UploadWallpaper;
import com.abdulkarimalbaik.dev.mywallpapers.ViewHolder.CategoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import android.support.design.widget.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class CategoryFragment extends Fragment {

    public static CategoryFragment instance = null;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference categoryBackground;

    //FirebaseUI Adapter
    FirebaseRecyclerAdapter<CategoryItem , CategoryViewHolder> adapter;

    //Views
    RecyclerView recyclerView;
    FloatingActionButton fabUpload;

    public CategoryFragment() {

        database = FirebaseDatabase.getInstance();
        categoryBackground = database.getReference(Common.STR_CATEGORY_BACKGROUND);

        adapter = new FirebaseRecyclerAdapter<CategoryItem, CategoryViewHolder>(
                CategoryItem.class,
                R.layout.layout_category_item,
                CategoryViewHolder.class,
                categoryBackground
        ) {
            @Override
            protected void populateViewHolder(CategoryViewHolder holder, final CategoryItem model, int position) {

                holder.category_name.setText(model.getName());
                Picasso.get()
                        .load(model.getImageLink())
                        .into(holder.background_image);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Common.CATEGORY_ID_SELECTED = adapter.getRef(position).getKey(); //Key of category
                        Common.CATEGORY_SELECTED = model.getName();
                        Intent intent = new Intent(getActivity() , ListWallpaper.class);
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

        View view =  inflater.inflate(R.layout.fragment_category, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_category);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity() , 2));

        if (Common.isConnectionToInternet(getActivity()))
            recyclerView.setAdapter(adapter);
        else
            Toast.makeText(getActivity(), "Please check your connection by Internet !", Toast.LENGTH_SHORT).show();

        fabUpload = (FloatingActionButton)view.findViewById(R.id.fabUpload);
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity() , UploadWallpaper.class);
                startActivity(intent);
            }
        });

        return view;
    }

    public static CategoryFragment getInstance(){

        if (instance == null)
            instance = new CategoryFragment();

        return instance;
    }

}

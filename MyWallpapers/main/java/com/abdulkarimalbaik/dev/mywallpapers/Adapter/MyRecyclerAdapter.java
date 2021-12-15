package com.abdulkarimalbaik.dev.mywallpapers.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;
import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.ListWallpaper;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.abdulkarimalbaik.dev.mywallpapers.R;
import com.abdulkarimalbaik.dev.mywallpapers.ViewHolder.ListWallpaperViewHolder;
import com.abdulkarimalbaik.dev.mywallpapers.ViewWallpaper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecyclerAdapter  extends RecyclerView.Adapter<ListWallpaperViewHolder>{

    //We will use this class to create an adapter to load recents that saved in RoomDatabase
    private Context context;
    private List<Recents> recents;

    public MyRecyclerAdapter(Context context, List<Recents> recents) {
        this.context = context;
        this.recents = recents;
    }

    @NonNull
    @Override
    public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(context).inflate(R.layout.layout_wallpaper_item , parent , false);

        return new ListWallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListWallpaperViewHolder holder, int position) {

        Picasso.get()
                .load(recents.get(position).getImageLink())
                .into(holder.wallpaper);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {

                WallpaperItem wallpaperItem = new WallpaperItem();
                wallpaperItem.setImageUrl(recents.get(position).getImageLink());
                wallpaperItem.setCategoryId(recents.get(position).getCategoryId());

                Intent intent = new Intent(context , ViewWallpaper.class);
                Common.wallpaperItem = wallpaperItem;
                Common.select_wallpaper_key = recents.get(position).getKey();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recents.size();
    }

}

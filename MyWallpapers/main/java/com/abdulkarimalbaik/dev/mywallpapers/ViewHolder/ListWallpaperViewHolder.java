package com.abdulkarimalbaik.dev.mywallpapers.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.R;

public class ListWallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView wallpaper;
    private ItemClickListener itemClickListener;

    public ListWallpaperViewHolder(View itemView) {
        super(itemView);

        wallpaper = (ImageView)itemView.findViewById(R.id.image);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v , getAdapterPosition());
    }
}

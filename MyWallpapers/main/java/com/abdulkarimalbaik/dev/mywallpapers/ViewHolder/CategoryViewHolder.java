package com.abdulkarimalbaik.dev.mywallpapers.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView category_name;
    public ImageView background_image;

    private ItemClickListener itemClickListener;

    public CategoryViewHolder(View itemView) {
        super(itemView);

        category_name = (TextView)itemView.findViewById(R.id.name);
        background_image = (ImageView)itemView.findViewById(R.id.image);

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

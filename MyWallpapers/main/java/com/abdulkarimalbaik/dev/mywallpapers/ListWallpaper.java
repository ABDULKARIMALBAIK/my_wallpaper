package com.abdulkarimalbaik.dev.mywallpapers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Interface.ItemClickListener;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.abdulkarimalbaik.dev.mywallpapers.ViewHolder.ListWallpaperViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ListWallpaper extends AppCompatActivity {

    RecyclerView recyclerView;

    FirebaseDatabase database;
    DatabaseReference wallpapers;
    FirebaseRecyclerAdapter<WallpaperItem , ListWallpaperViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_wallpaper);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(Common.CATEGORY_SELECTED);
        setSupportActionBar(toolbar);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        database = FirebaseDatabase.getInstance();
        wallpapers = database.getReference(Common.STR_WALLPAPER);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_list_wallpaper);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this , 2));

        if (Common.isConnectionToInternet(ListWallpaper.this))
            loadBackgroundList();
        else
            Toast.makeText(ListWallpaper.this, "Please check your connection by Internet !", Toast.LENGTH_SHORT).show();
    }

    private void loadBackgroundList() {

        adapter = new FirebaseRecyclerAdapter<WallpaperItem, ListWallpaperViewHolder>(
                WallpaperItem.class,
                R.layout.layout_wallpaper_item,
                ListWallpaperViewHolder.class,
                wallpapers.orderByChild("categoryId").equalTo(Common.CATEGORY_ID_SELECTED)
        ) {
            @Override
            protected void populateViewHolder(ListWallpaperViewHolder holder, final WallpaperItem model, int position) {

                Picasso.get()
                        .load(model.getImageUrl())
                        .into(holder.wallpaper);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Intent intent = new Intent(ListWallpaper.this , ViewWallpaper.class);
                        Common.wallpaperItem = model;
                        Common.select_wallpaper_key = adapter.getRef(position).getKey();
                        startActivity(intent);
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}

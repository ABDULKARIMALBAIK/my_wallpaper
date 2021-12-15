package com.abdulkarimalbaik.dev.mywallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Database.DataSource.RecentsRepository;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.LocalDatabase;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.RecentsDataSource;
import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;
import com.abdulkarimalbaik.dev.mywallpapers.Helper.SaveImageHelper;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewWallpaper extends AppCompatActivity {

    private static final int DOWNLOAD_PERMISSION_CODE_REQUEST = 1000;
    private static final int INSTAGRAM_PERMISSION_CODE_REQUEST = 2000;
    private static final int MESSENGER_PERMISSION_CODE_REQUEST = 3000;
    private static final int WHATSAPP_PERMISSION_CODE_REQUEST = 4000;

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fabWallpaper;
    FloatingActionButton fabDownload;
    ImageView imageView;
    CoordinatorLayout rootLayout;

    FloatingActionMenu mainFloating;
    com.github.clans.fab.FloatingActionButton fabFacebook;
    com.github.clans.fab.FloatingActionButton fabInstagram;
    com.github.clans.fab.FloatingActionButton fabMessenger;
    com.github.clans.fab.FloatingActionButton fabWhatsapp;

    //Room Database
    CompositeDisposable compositeDisposable;
    RecentsRepository recentsRepository;
    Recents recents;

    //Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ViewWallpaper.this);
            try{

                wallpaperManager.setBitmap(bitmap);
                Snackbar.make(rootLayout , "Wallpaper was set !" , Snackbar.LENGTH_SHORT).show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    private Target facebookTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)){

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();

                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);

        rootLayout = (CoordinatorLayout)findViewById(R.id.rootLayout);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        if (getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Init Database
        initDatabase();

        //init facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setTitle(Common.CATEGORY_SELECTED);

        imageView = (ImageView)findViewById(R.id.imageThumb);
        Picasso.get()
                .load(Common.wallpaperItem.getImageUrl())
                .into(imageView);

        //Add to recents database
        if (Common.isConnectionToInternet(ViewWallpaper.this))
            addToRecents();
        else
            Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();


        fabWallpaper = (FloatingActionButton)findViewById(R.id.fabWallpaper);
        fabWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (Common.isConnectionToInternet(ViewWallpaper.this))
                    setWallpaper();

                else
                    Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
            }
        });

        fabDownload = (FloatingActionButton)findViewById(R.id.fabDownload);
        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , DOWNLOAD_PERMISSION_CODE_REQUEST);
                }
                else {

                    if (Common.isConnectionToInternet(ViewWallpaper.this))
                        downloadImage();
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mainFloating = (FloatingActionMenu)findViewById(R.id.menu);

        fabFacebook = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.facebook_item);
        fabFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(ViewWallpaper.this, "Share successful !", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(ViewWallpaper.this, "Share cancelled !", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(ViewWallpaper.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                Picasso.get()
                        .load(Common.wallpaperItem.getImageUrl())
                        .into(facebookTarget);
            }
        });

        fabInstagram = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.instagram_item);
        fabInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                if (ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , INSTAGRAM_PERMISSION_CODE_REQUEST);
                }
                else {

                    if (Common.isConnectionToInternet(ViewWallpaper.this))
                        shareInstagram(bitmap);
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }

            }
        });

        fabMessenger = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.messenger_item);
        fabMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                if (ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , MESSENGER_PERMISSION_CODE_REQUEST);
                }
                else {

                    if (Common.isConnectionToInternet(ViewWallpaper.this))
                        shareMessenger(bitmap);
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fabWhatsapp  = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.whatsapp_item);
        fabWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                if (ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ViewWallpaper.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , WHATSAPP_PERMISSION_CODE_REQUEST);
                }
                else {

                    if (Common.isConnectionToInternet(ViewWallpaper.this))
                        shareWhatsapp(bitmap);
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //increase viewCount wallpaperItem
        if (Common.isConnectionToInternet(ViewWallpaper.this)){
            increaseViewCount();
        }
        else
            Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();

    }

    private void shareWhatsapp(Bitmap bitmap) {

        Uri data;
        File file = Common.createFile(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

            data = FileProvider.getUriForFile(ViewWallpaper.this, "com.abdulkarimalbaik.dev.mywallpapers.myprovider", file);
            grantUriPermission(getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else
            data = Uri.fromFile(file);


        Intent intent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (intent != null)
        {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.setPackage("com.whatsapp");
            shareIntent.putExtra(Intent.EXTRA_STREAM, data);

            startActivity(shareIntent);

        }
        else
        {
            Toast.makeText(this, "Please download Whatsapp app !", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareMessenger(Bitmap bitmap) {

        Uri data;
        File file = Common.createFile(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

            data = FileProvider.getUriForFile(ViewWallpaper.this, "com.abdulkarimalbaik.dev.mywallpapers.myprovider", file);
            grantUriPermission(getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else
            data = Uri.fromFile(file);


        Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
        if (intent != null)
        {
            Intent sendIntent = new Intent();
            sendIntent.setType("image/jpeg");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM , data);
            sendIntent.setPackage("com.facebook.orca");

            startActivity(sendIntent);
        }
        else
        {
            Toast.makeText(this, "Please download Messenger app !", Toast.LENGTH_SHORT).show();
        }

    }

    private void shareInstagram(Bitmap bitmap) {

        Uri data;
        File file = Common.createFile(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

            data = FileProvider.getUriForFile(ViewWallpaper.this, "com.abdulkarimalbaik.dev.mywallpapers.myprovider", file);
            grantUriPermission(getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else
            data = Uri.fromFile(file);

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null)
        {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");
            shareIntent.putExtra(Intent.EXTRA_STREAM, data);
            shareIntent.setType("image/jpeg");

            startActivity(shareIntent);

        }
        else
        {
            Toast.makeText(this, "Please download Instagram app !", Toast.LENGTH_SHORT).show();
        }

    }

    private void increaseViewCount() {

        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .child(Common.select_wallpaper_key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("viewCount")){

                            WallpaperItem wallpaperItem = dataSnapshot.getValue(WallpaperItem.class);
                            long count = wallpaperItem.getViewCount() + 1;

                            //Update
                            Map<String , Object> update_view = new HashMap<>();
                            update_view.put("viewCount" , count);

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_wallpaper_key)
                                    .updateChildren(update_view)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ViewWallpaper.this, "can't update view count", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {  //if viewCount is not set (default 1)

                            Map<String , Object> update_view = new HashMap<>();
                            update_view.put("viewCount" ,Long.valueOf(1));


                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_wallpaper_key)
                                    .updateChildren(update_view)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ViewWallpaper.this, "can't set dafault view count", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initDatabase() {

        compositeDisposable = new CompositeDisposable();
        LocalDatabase database = LocalDatabase.getInstance(this);
        recentsRepository = RecentsRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));
    }

    private void addToRecents() {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {

                recents = new Recents(
                        Common.wallpaperItem.getImageUrl(),
                        Common.wallpaperItem.getCategoryId(),
                        String.valueOf(System.currentTimeMillis()),
                        Common.select_wallpaper_key
                );

                try {
                    recentsRepository.insertRecents(recents);
                    e.onComplete();

                }
                catch (Exception s){s.printStackTrace();
                    Toast.makeText(ViewWallpaper.this, s.getMessage(), Toast.LENGTH_SHORT).show();}

            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Toast.makeText(ViewWallpaper.this, "Inserted !", Toast.LENGTH_LONG).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(ViewWallpaper.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        compositeDisposable.add(disposable);

//        recents = new Recents(
//                        Common.wallpaperItem.getImageUrl(),
//                        Common.wallpaperItem.getCategoryId(),
//                        String.valueOf(System.currentTimeMillis())
//                );
//
//        Common.recents.add(recents);

    }

    private void setWallpaper() {

        Picasso.get()
                .load(Common.wallpaperItem.getImageUrl())
                .into(target);
    }

    private void downloadImage() {

        AlertDialog dialog = new SpotsDialog(ViewWallpaper.this);
        dialog.show();
        dialog.setMessage("Please waiting...");

        String name = UUID.randomUUID().toString() + ".jpg";
        String description = "Description image";

        Picasso.get()
                .load(Common.wallpaperItem.getImageUrl())
                .into(new SaveImageHelper(
                        getApplicationContext(),
                        dialog,
                        getApplicationContext().getContentResolver(),
                        name,
                        description));
    }

    @Override
    protected void onStop() {
        Picasso.get().cancelRequest(target);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Picasso.get().cancelRequest(target);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case DOWNLOAD_PERMISSION_CODE_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    if (Common.isConnectionToInternet(ViewWallpaper.this))
                        downloadImage();
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
                else
                    Snackbar.make(rootLayout , "You can't download any Wallpaper !" , Snackbar.LENGTH_SHORT).show();

                break;
            }
            case INSTAGRAM_PERMISSION_CODE_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    if (Common.isConnectionToInternet(ViewWallpaper.this)){

                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        shareInstagram(bitmap);
                    }
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
                else
                    Snackbar.make(rootLayout , "You can't share on Instagram !" , Snackbar.LENGTH_SHORT).show();

                break;
            }
            case MESSENGER_PERMISSION_CODE_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    if (Common.isConnectionToInternet(ViewWallpaper.this)){

                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        shareMessenger(bitmap);
                    }
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
                else
                    Snackbar.make(rootLayout , "You can't share on Messenger !" , Snackbar.LENGTH_SHORT).show();

                break;
            }
            case WHATSAPP_PERMISSION_CODE_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    if (Common.isConnectionToInternet(ViewWallpaper.this)){

                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        shareWhatsapp(bitmap);
                    }
                    else
                        Toast.makeText(ViewWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
                }
                else
                    Snackbar.make(rootLayout , "You can't share on Whatsapp !" , Snackbar.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}

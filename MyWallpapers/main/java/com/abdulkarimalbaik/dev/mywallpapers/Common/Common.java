package com.abdulkarimalbaik.dev.mywallpapers.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.abdulkarimalbaik.dev.mywallpapers.Remote.IComputerVision;
import com.abdulkarimalbaik.dev.mywallpapers.Remote.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Common {

    public static final String API_KEY_Azure_VISION = ".....................................";
    public static final String BASE_URL = ".................................";

    public static IComputerVision getComputerVisionAPI(){

        return RetrofitClient.getInstance(BASE_URL).create(IComputerVision.class);
    }

    public static String getAPIAdultEndPoint(){

        return new StringBuilder(BASE_URL).append("...............................").toString();
    }

    public static final String STR_CATEGORY_BACKGROUND = "CategoryBackground";
    public static final String STR_WALLPAPER = "Wallpapers";
    public static String CATEGORY_SELECTED;
    public static String CATEGORY_ID_SELECTED;
    public static WallpaperItem wallpaperItem = new WallpaperItem();

    public static String select_wallpaper_key;

    public static boolean isConnectionToInternet(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null){

                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static File createFile(Bitmap bitmap){

        //Make sure you set permission Read/Write external storage AND set Provider that exists in Manifest

        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/My_Wallpapers";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir.getAbsoluteFile(), new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString());
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
}

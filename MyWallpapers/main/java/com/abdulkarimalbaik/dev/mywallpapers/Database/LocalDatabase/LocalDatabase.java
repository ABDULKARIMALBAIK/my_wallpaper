package com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;

import static com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.LocalDatabase.DATABASE_VERSION;

@Database(entities = Recents.class , version = DATABASE_VERSION , exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ABDULKARIMLiveWallpaper";

    public abstract RecentsDAO recentsDAO();
    private static LocalDatabase instance;

    public static LocalDatabase getInstance(Context context){

        if (instance == null){

            instance = Room.databaseBuilder(context , LocalDatabase.class , DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() //Fix error: Cannot access database on the main thread
                    .build();
        }
        return instance;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }
}

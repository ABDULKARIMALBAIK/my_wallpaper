package com.abdulkarimalbaik.dev.mywallpapers.Database.DataSource;

import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;

import java.util.List;

import io.reactivex.Flowable;

public interface IRecentsDataSource {

    Flowable<List<Recents>> getAllRecents();

    void insertRecents(Recents... recents);
    void updateRecents(Recents... recents);
    void deleteRecents(Recents... recents);
    void deleteAllRecents();
}

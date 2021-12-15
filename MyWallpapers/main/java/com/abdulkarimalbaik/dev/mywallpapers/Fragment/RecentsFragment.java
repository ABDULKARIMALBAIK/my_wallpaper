package com.abdulkarimalbaik.dev.mywallpapers.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.abdulkarimalbaik.dev.mywallpapers.Adapter.MyRecyclerAdapter;
import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Database.DataSource.RecentsRepository;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.LocalDatabase;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.RecentsDataSource;
import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;
import com.abdulkarimalbaik.dev.mywallpapers.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("ValidFragment")
public class RecentsFragment extends Fragment {

    public static RecentsFragment instance = null;
    RecyclerView recycler_recents;
    FloatingActionButton fabDelete;

    List<Recents> recentsList = new ArrayList<>();
    CompositeDisposable compositeDisposable;
    RecentsRepository recentsRepository;
    MyRecyclerAdapter adapter;
    Context context;


    @SuppressLint("ValidFragment")
    public RecentsFragment(Context context) {

        // Required empty public constructor
        //Init Database
        this.context = context;
        compositeDisposable = new CompositeDisposable();
        LocalDatabase database = LocalDatabase.getInstance(context);
        recentsRepository = RecentsRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recents, container, false);

        recycler_recents = (RecyclerView)view.findViewById(R.id.recycler_recents);
        recycler_recents.setHasFixedSize(true);
        recycler_recents.setLayoutManager(new GridLayoutManager(context , 2));
        recentsList = new ArrayList<>();
        adapter = new MyRecyclerAdapter(context , recentsList);
        recycler_recents.setAdapter(adapter);

        fabDelete = (FloatingActionButton)view.findViewById(R.id.fabDelete);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteAllItem();
                adapter.notifyDataSetChanged();
            }
        });

        loadRecents();

        return view;
    }

    private void deleteAllItem() {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {

                recentsRepository.deleteAllRecents();
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                               @Override
                               public void accept(Object o) throws Exception {

                                   Toast.makeText(context, "Delete success !", Toast.LENGTH_SHORT).show();
                               }
                           }
                , new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void loadRecents() {

        compositeDisposable.add(recentsRepository.getAllRecents()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<Recents>>() {
            @Override
            public void accept(List<Recents> recents) throws Exception {

                getAllRecentSuccess(recents);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void getAllRecentSuccess(List<Recents> recents) {

        recentsList.clear();
        recentsList.addAll(recents);
        adapter.notifyDataSetChanged();
    }

    public static RecentsFragment getInstance(Context context){

        if (instance == null)
            instance = new RecentsFragment(context);

        return instance;
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }


}

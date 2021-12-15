package com.abdulkarimalbaik.dev.mywallpapers.Adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.abdulkarimalbaik.dev.mywallpapers.Fragment.CategoryFragment;
import com.abdulkarimalbaik.dev.mywallpapers.Fragment.TrendingFragment;
import com.abdulkarimalbaik.dev.mywallpapers.Fragment.RecentsFragment;

public class MyFragmentAdapter extends FragmentPagerAdapter {

    private Context context;

    public MyFragmentAdapter(FragmentManager fm , Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:{

                return CategoryFragment.getInstance();
            }
            case 1:{

                return TrendingFragment.getInstance();
            }
            case 2:{

                return RecentsFragment.getInstance(context);
            }

        }
        return  null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0:{

                return "Category";
            }
            case 1:{

                return "Trending";
            }
            case 2:{

                return "Recents";
            }

        }

        return "";
    }
}

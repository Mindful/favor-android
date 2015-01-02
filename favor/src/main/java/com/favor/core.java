package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.favor.library.Contact;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public class core extends FragmentActivity {
    FavorPager mDemoCollectionPagerAdapter;
    ViewPager mViewPager;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new FavorPager(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }


    public static class FavorPager extends FragmentPagerAdapter {
        public FavorPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        //TODO: is it worth caching fragments? I want to assume getItem isn't called every time we scroll, but if it is
        //we should definitely save the application some work and cache
        @Override
        public Fragment getItem(int position) {
            if (position == 0) return new ContactSelectFragment();
            else return new VisualizeFragment();
        }
    }
}

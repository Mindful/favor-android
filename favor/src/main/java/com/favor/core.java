package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.favor.library.Logger;

/**
 * Created by josh on 12/31/14.
 */
public class core extends FavorActivity {
    FavorPager mPagerAdapter;
    ViewPager mViewPager;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPagerAdapter =
                new FavorPager(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override public void onPageScrollStateChanged(int arg0){
            }
            @Override public void onPageScrolled(int arg0,float arg1, int arg2){
            }
            @Override public void onPageSelected(int position){
                if (position != 0){
                    mPagerAdapter.propagateContactData();
                }
            }
        });
        mViewPager.setAdapter(mPagerAdapter);
    }


    public static class FavorPager extends FragmentPagerAdapter {
        public FavorPager(FragmentManager fm) {
            super(fm);
        }

        private ContactSelectFragment contactFrag;
        private VisualizeFragment visualizationFrag;

        @Override
        public int getCount() {
            return 3;
        }

        public void propagateContactData(){
            if (visualizationFrag != null){
                visualizationFrag.setContacts(contactFrag.selectedContacts());
            }
        }

        //TODO: is it worth caching fragments? I want to assume getItem isn't called every time we scroll, but if it is
        //we should definitely save the application some work and cache
        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                contactFrag =  new ContactSelectFragment();
                return contactFrag;
            }
            else {
                visualizationFrag = new VisualizeFragment();
                if (contactFrag != null && contactFrag.selectedContacts() != null){
                    visualizationFrag.setContacts(contactFrag.selectedContacts());
                }
                return visualizationFrag;
            }
        }
    }
}

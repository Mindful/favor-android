package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Processor;
import com.favor.ui.GraphableResult;

/**
 * Created by josh on 12/31/14.
 */
public class CoreActivity extends FavorActivity {
    FavorPager mPagerAdapter;
    ViewPager mViewPager;

    private static final int SELECT_PAGE = 0;
    private static final int VISUALIZE_PAGE = 1;
    private static final int METRICS_PAGE = 2;



    public void onCreate(Bundle savedInstanceState) {


        //TODO: this activity needs to keep a careful watch on the selected contracts and the selected metric so we know
        //when we need to fetch fresh data and when we can just keep using the same result


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
                if (position != SELECT_PAGE){
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
            Logger.info("Propagate");
            if (visualizationFrag != null){
                GraphableResult res = new GraphableResult(contactFrag.selectedContacts(), Processor.batchMessageCount(Core.getCurrentAccount(), contactFrag.selectedContacts(), -1, -1, true),
                        Processor.batchMessageCount(Core.getCurrentAccount(), contactFrag.selectedContacts(), -1, -1, false));
                visualizationFrag.setData(res);
            }
        }

        //TODO: is it worth caching fragments? I want to assume getItem isn't called every time we scroll, but if it is
        //we should definitely save the application some work and cache
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case SELECT_PAGE:
                    contactFrag =  new ContactSelectFragment();
                    return contactFrag;
                case VISUALIZE_PAGE:
                    visualizationFrag = new VisualizeFragment();
                    propagateContactData();
                    return visualizationFrag;
                default:
                    return new ListFragment();
            }
        }
    }
}

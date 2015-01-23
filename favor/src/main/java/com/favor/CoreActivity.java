package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import com.favor.library.Contact;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Processor;
import com.favor.ui.GraphableResult;
import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public class CoreActivity extends FavorActivity {
    private FavorPager mPagerAdapter;
    private ViewPager mViewPager;

    private GraphableResult result;
    private ArrayList<Contact> previousResultContacts;



    public void onCreate(Bundle savedInstanceState) {


        //TODO: this activity needs to keep a careful watch on the selected contracts and the selected metric so we know
        //when we need to fetch fresh data and when we can just keep using the same result


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        if (savedInstanceState != null) result = Parcels.unwrap(savedInstanceState.getParcelable("RESULT"));
        else result =  new GraphableResult(new ArrayList<Contact>(), new long[]{}, new long[]{});

        mPagerAdapter = new FavorPager(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override public void onPageScrollStateChanged(int arg0){
                if (arg0 == ViewPager.SCROLL_STATE_IDLE){
                    Logger.info("datchange");
                    //TODO: only notify if the data (result) has actually changed
                    mPagerAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onPageScrolled(int arg0,float arg1, int arg2){
            }
            @Override public void onPageSelected(int position){
            }
        });
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable("RESULT", Parcels.wrap(result));
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateData(ArrayList<Contact> selectedContacts){
        result = new GraphableResult(selectedContacts, Processor.batchMessageCount(Core.getCurrentAccount(),selectedContacts, -1, -1, true),
                Processor.batchMessageCount(Core.getCurrentAccount(), selectedContacts, -1, -1, false));
    }


    public class FavorPager extends FragmentPagerAdapter {

        private static final int SELECT_PAGE = 0;
        private static final int VISUALIZE_PAGE = 1;
        private static final int METRICS_PAGE = 2;


        public FavorPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public Fragment getItem(int position) {
            switch (position){
                case SELECT_PAGE:
                    return new ContactSelectFragment();
                case VISUALIZE_PAGE:
                    return new VisualizeFragment();
                default:
                    return new ListFragment();
            }
        }


        @Override
        public int getItemPosition(Object object) {
            //TODO: make all the fragments inherit from a single class and cast to that and then use an update method
            if (object instanceof VisualizeFragment){
                VisualizeFragment vf = (VisualizeFragment)object;
                vf.data = result;
                vf.redrawChart();
            }
            return super.getItemPosition(object);
        }
    }


}

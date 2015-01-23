package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.favor.library.Contact;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Processor;
import com.favor.ui.GraphableResult;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by josh on 12/31/14.
 */
public class CoreActivity extends FavorActivity {
    private FavorPager mPagerAdapter;
    private ViewPager mViewPager;

    private GraphableResult result;
    private ArrayList<Contact> contacts;

    private static final String RESULTNAME = "RESULT";
    private static final String CONTACTNAME = "CONTACTS";

    public void setContacts(ArrayList<Contact> in){
        contacts = in;
    }



    public void onCreate(Bundle savedInstanceState) {


        //TODO: this activity needs to keep a careful watch on the selected contracts and the selected metric so we know
        //when we need to fetch fresh data and when we can just keep using the same result


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        if (savedInstanceState != null){
            result = Parcels.unwrap(savedInstanceState.getParcelable(RESULTNAME));
            contacts = (ArrayList<Contact>) savedInstanceState.getSerializable(CONTACTNAME);
        }
        else result =  new GraphableResult(new ArrayList<Contact>(), new long[]{}, new long[]{});

        mPagerAdapter = new FavorPager(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override public void onPageScrollStateChanged(int scrollState){
                if (scrollState == ViewPager.SCROLL_STATE_IDLE){
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
        savedInstanceState.putParcelable(RESULTNAME, Parcels.wrap(result));
        savedInstanceState.putSerializable(CONTACTNAME, contacts);
        super.onSaveInstanceState(savedInstanceState);
    }


    public GraphableResult getResult(){
         if (contacts != null && !result.getContacts().equals(contacts)) {
            result = new GraphableResult(contacts, Processor.batchMessageCount(Core.getCurrentAccount(),contacts, -1, -1, true),
            Processor.batchMessageCount(Core.getCurrentAccount(), contacts, -1, -1, false));
        }
        return result;
    }


    private class FavorPager extends FragmentPagerAdapter {

        static final int SELECT_PAGE = 0;
        static final int VISUALIZE_PAGE = 1;
        static final int METRICS_PAGE = 2;

        HashMap<Integer, Fragment> fragments = new HashMap<Integer, Fragment>();


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
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            Fragment f = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, f);
            return f;
        }


        @Override
        public int getItemPosition(Object object) {
            //TODO: make all the fragments inherit from a single class and cast to that and then use an update method
            if (object instanceof VisualizeFragment){
                VisualizeFragment vf = (VisualizeFragment)object;
                vf.redrawChart(getResult());
            }
            return super.getItemPosition(object);
        }
    }


}

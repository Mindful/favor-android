/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.favor;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;
import com.favor.library.*;
import com.favor.ui.GraphableResult;
import com.favor.util.Querier;
import com.favor.util.QueryDetails;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by josh on 12/31/14.
 */
public class CoreActivity extends FavorActivity {


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public void setStartTime(boolean startTime) {
            this.startTime = startTime;
            //TODO: fetch min and max date based on the first and last received/sent message
            if (startTime){
                Logger.info("Selecting start time");
            } else {
                Logger.info("Selecting end time");
            }

        }

        private CoreActivity getParentAct(){
            return (CoreActivity) getActivity();
        }


        private long minDate;
        private long maxDate;
        private boolean startTime;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker


            final Calendar c = Calendar.getInstance();
            long date = -1;
            if (startTime) date = getParentAct().getStartDate();
            else date = getParentAct().getEndDate();

            if (date != -1) c.setTimeInMillis(date);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog ret = new DatePickerDialog(getActivity(), this, year, month, day);

            //TODO: this needs to be calculated based on the first and last received or sent message; may influence the default
//            ret.getDatePicker().setMaxDate();
//            ret.getDatePicker().setMinDate();

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            if (startTime) getParentAct().setStartDate(c.getTimeInMillis());
            else getParentAct().setEndDate(c.getTimeInMillis());
            // Do something with the date chosen by the user
        }
    }

    public void showDatePickerDialog(View v) {

        DatePickerFragment frag = new DatePickerFragment();

        switch (v.getId()){
            case R.id.start_date:
                frag.setStartTime(true);
                break;
            case R.id.end_date:
                frag.setStartTime(false);
                break;
        }

        frag.show(getSupportFragmentManager(), "datePicker");
    }

    public void clearDate(View v){
        Button b = null;
        switch(v.getId()){
            case R.id.start_date_clear:
                this.queryDetails.resetStartDate();
                b = (Button) menu.getMenu().findViewById(R.id.start_date);
                break;
            case R.id.end_date_clear:
                this.queryDetails.resetStartDate();
                b = (Button) menu.getMenu().findViewById(R.id.end_date);
                break;
        }

        final Button fb = b;
        fb.post(new Runnable() {
            @Override
            public void run() {
                fb.setText("None");
            }
        });
    }

    private FavorPager mPagerAdapter;
    private ViewPager mViewPager;
    private SlidingMenu.CanvasTransformer mTransformer;

    private GraphableResult result;

    private GraphableResult.GraphTypes graphType;
    private QueryDetails queryDetails;

    private SlidingMenu menu;

    private static final String RESULTNAME = "RESULT";
    private static final String QUERYDETAILSNAME = "QUERYDETAILS";
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

    public void setEndDate(long endDate) {
        this.queryDetails.setEndDate(endDate);
        final long date = endDate;
        final Button t = (Button) menu.getMenu().findViewById(R.id.end_date);

        //Has to be run in the UI thread, not the event response thread
        t.post(new Runnable() {
            @Override
            public void run() {
                t.setText(dateFormatter.format(date));
            }
        });
    }

    public void setStartDate(long startDate) {
        this.queryDetails.setStartDate(startDate);
        final long date = startDate;
        final Button t = (Button) menu.getMenu().findViewById(R.id.start_date);

        //Has to be run in the UI thread, not the event response thread
        t.post(new Runnable() {
            @Override
            public void run() {
                t.setText(dateFormatter.format(date));
            }
        });
    }

    public void setAnalytic(Querier.AnalyticType type){
        Logger.info("SET ANALYTIC TYPE "+type);
        this.queryDetails.setAnalyticType(type);
    }

    public void setContacts(ArrayList<Contact> in){
        queryDetails.setContacts(in);
    }

    public long getEndDate() {
        return queryDetails.getEndDate();
    }

    public long getStartDate() {
        return queryDetails.getStartDate();
    }


    public void onCreate(Bundle savedInstanceState) {


        //TODO: this activity needs to keep a careful watch on the selected contracts and the selected metric so we know
        //when we need to fetch fresh data and when we can just keep using the same result


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        if (savedInstanceState != null){
            result = Parcels.unwrap(savedInstanceState.getParcelable(RESULTNAME));
            queryDetails = (QueryDetails) savedInstanceState.getSerializable(QUERYDETAILSNAME);
        }
        else {
            queryDetails = new QueryDetails();
            result =  new GraphableResult(queryDetails, new long[]{}, new long[]{});
        }

        mPagerAdapter = new FavorPager(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override public void onPageScrollStateChanged(int scrollState){
                if (scrollState == ViewPager.SCROLL_STATE_IDLE){
                    //TODO: only notify if the data (result) has actually changed
                    mPagerAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onPageSelected(int position){
                switch (position) {
                    case 0:
                        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                        break;
                    default:
                        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
                        break;
                }
            }
            @Override public void onPageScrolled(int arg0,float arg1, int arg2){
            }
        });
        mViewPager.setAdapter(mPagerAdapter);

        mTransformer =  new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                canvas.scale(percentOpen, 1, 0, 0);
            }
        };

        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.core_menu);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setBehindScrollScale(0.0f);

        menu.setBehindCanvasTransformer(mTransformer);

        menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

            @Override
            public void onClosed() {
                Logger.info("--SLIDING MENU CLOSED");
                //TODO: check if we need to recompute, regraph, etc. - this is where things would potentially be refreshed
            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable(RESULTNAME, Parcels.wrap(result));
        savedInstanceState.putSerializable(QUERYDETAILSNAME, queryDetails);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void messageRefreshResponse(){
        //TODO: recompute the possible start and end dates for selection here based on any new info we have
        //about fetched messages
        Logger.info("RESPOND TO REFRESH");
        result = Querier.launchQuery(queryDetails); //Recompute current result
        mPagerAdapter.messageRefreshUpdateFragments();
    }

    @Override
    public void addressRefreshResponse() {
        //TODO: this
    }


    public GraphableResult getResult(){
        Logger.info("GET RESULT QUERY DETAILS"+queryDetails);
         if (queryDetails!= null && !result.queryDetailsEquals(queryDetails)) {
             Logger.info("Launch new query to get result");
            result = Querier.launchQuery(queryDetails);
        }
        Logger.info("Result:"+result.toString());
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                refreshMessages();
                return true;
            case android.R.id.home:
                menu.toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class FavorPager extends FragmentPagerAdapter {

        static final int SELECT_PAGE = 0;
        static final int VISUALIZE_PAGE = 1;
        static final int METRICS_PAGE = 2;

        HashMap<Integer, Fragment> fragments = new HashMap<Integer, Fragment>();

        public void messageRefreshUpdateFragments(){
            //((ContactSelectFragment)fragments.get(SELECT_PAGE)).messageRefreshResponse(); //necessary if we start listing data about contacts on their tiles
            //((MetricsFragment)fragments.get(METRICS_PAGE)).messageRefreshResponse();
            ((VisualizeFragment)fragments.get(VISUALIZE_PAGE)).redrawChart(getResult());
        }

        public void addressRefreshResponse(){
            //((ContactSelectFragment)fragments.get(SELECT_PAGE)).messageRefreshResponse();
            //((MetricsFragment)fragments.get(METRICS_PAGE)).messageRefreshResponse(); //necessary if this lists more than queried contacts
        }


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

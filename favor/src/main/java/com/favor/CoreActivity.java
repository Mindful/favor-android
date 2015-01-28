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
import android.view.animation.Interpolator;
import android.widget.DatePicker;
import android.widget.Toast;
import com.favor.library.*;
import com.favor.ui.GraphableResult;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by josh on 12/31/14.
 */
public class CoreActivity extends ActionBarActivity {


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public void setStartTime(boolean startTime) {
            //TODO: fetch min and max date based on the first and last received/sent message
            if (startTime){
                Logger.info("Selecting start time");
                saveKey = START_SAVE_KEY;
            } else {
                Logger.info("Selecting end time");
                saveKey = END_SAVE_KEY;
            }

        }

        private static final String START_SAVE_KEY = "STARTDATE";
        private static final String END_SAVE_KEY = "ENDDATE";


        private long minDate;
        private long maxDate;
        private String saveKey;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog ret = new DatePickerDialog(getActivity(), this, year, month, day);

            //TODO: this needs to be calculated based on the first and last received or sent message; may influence the default
//            ret.getDatePicker().setMaxDate();
//            ret.getDatePicker().setMinDate();

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
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

    private FavorPager mPagerAdapter;
    private ViewPager mViewPager;
    private SlidingMenu.CanvasTransformer mTransformer;

    private GraphableResult result;
    private ArrayList<Contact> contacts;

    private SlidingMenu menu;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG);
                //TODO: this should be more complex, and display more to the user (like a loading swirl), it should also reload the activity in most cases
                //or call some sort of method that must be overriden so activties know how to reload themselves on refresh
                Core.getCurrentAccount().updateMessages();
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

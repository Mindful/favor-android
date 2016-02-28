package com.favor.app;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import com.favor.library.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import com.favor.graphs.FavorGraph;
import com.favor.graphs.MultiBarGraph;

import java.util.ArrayList;
import java.util.List;

public class ContactCompareActivity extends FavorActivity {

    ArrayList<Contact> contacts;
    AccountManager account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getSerializableExtra(ContactSelectFragment.CONTACT_LIST) != null){
            contacts = (ArrayList<Contact>) getIntent().getSerializableExtra(ContactSelectFragment.CONTACT_LIST);
            account = Reader.accountManagers()[0]; //TODO: this should be better
        } else {
            throw new RuntimeException("Cannot initialize contact compare activity without contacts");
        }
        setContentView(R.layout.activity_contact_compare);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new GraphCollectionPagerAdapter(getSupportFragmentManager(), this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_compare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class GraphCollectionPagerAdapter extends FragmentPagerAdapter {

        public final static int GRAPH_COUNT = 4;
        private Context context;

        public GraphCollectionPagerAdapter(FragmentManager fm, Activity inputActivity) {
            super(fm);
            context = inputActivity;
        }

        @Override
        public Fragment getItem(int position) {
            FavorGraph graph = null;
            switch(position){
//                case 0:
//                    MultiBarGraph barGraph = new MultiBarGraph(context);
//                    barGraph.setValueData(Util.contactListToNames(contacts),
//                            Processor.batchAverageCharCount(account, contacts, -1, -1, true),
//                            Processor.batchAverageCharCount(account, contacts, -1, -1, false));
//                    graph = barGraph;
//                case 1:
//                    break;
//                case 2:
//                    break;
//                case 3:
//                    break;
                default:
                    MultiBarGraph barGraph = new MultiBarGraph(context);
                    barGraph.setValueData(Util.contactListToNames(contacts),
                            Processor.batchAverageCharCount(account, contacts, -1, -1, true),
                            Processor.batchAverageCharCount(account, contacts, -1, -1, false));
                    graph = barGraph;
//                default:
//                    throw new IndexOutOfBoundsException("No contact comparison graph at index "+position);
            }
            GraphFragment fragment = new GraphFragment(graph);
            return fragment;
        }

        @Override
        public int getCount() {
            return GRAPH_COUNT;
        }
    }

}

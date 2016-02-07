package com.favor.app;

import android.accounts.Account;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.favor.library.*;
import com.favor.util.MPAndroidChartAdapter;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import lecho.lib.hellocharts.view.LineChartView;

public class ContactStatsActivity extends FavorActivity {

    private Contact contact;
    private AccountManager account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_stats);
        if (getIntent().getSerializableExtra(ContactSelectFragment.CONTACT) != null){
            contact = (Contact) getIntent().getSerializableExtra(ContactSelectFragment.CONTACT);
            getSupportActionBar().setTitle(contact.getDisplayName());
            setContactAccount(Reader.accountManagers()[0]); //TODO: be smarter about the default, pick the most relevant type
        } else {
            Logger.error("Contact activity opened with unkown contact");
            getSupportActionBar().setTitle(R.string.unknown_contact);
        }
    }

    private void setContactAccount(AccountManager inputAccount){
        account = inputAccount;
        getSupportActionBar().setSubtitle(Core.stringFromType(account.getType()) + " (" +account.getAccountName()+ ")");
        updateGraphs();
    }

    private void updateGraphs(){
        LineChartView msgTotalOverTime = (LineChartView) findViewById(R.id.total_msg_over_time_chart);
        HorizontalBarChart avgCharCount = (HorizontalBarChart) findViewById(R.id.avg_character_count_chart);
        HorizontalBarChart responseTime = (HorizontalBarChart) findViewById(R.id.response_time_chart);

        msgTotalOverTime.setValueTouchEnabled(false); //TODO: this right? different library than the other two charts
        avgCharCount.setTouchEnabled(false);
        avgCharCount.setDrawGridBackground(false);
        responseTime.setTouchEnabled(false);
        responseTime.setDrawGridBackground(false);


        double sentResponseTime = Processor.responseTimeNintieth(account, contact, -1, -1, true);
        double recResponseTime = Processor.responseTimeNintieth(account, contact, -1, -1, true);
//        MPAndroidChartAdapter.sentReceivedBar(responseTime, sentResponseTime, recResponseTime);
        MPAndroidChartAdapter.sentReceivedBar(responseTime, 5.0, 8.0);

        double sentAvgCharCount = Processor.averageCharcount(account, contact, -1, -1, true);
        double recAvgCharCount = Processor.averageCharcount(account, contact, -1, -1, true);
        MPAndroidChartAdapter.sentReceivedBar(avgCharCount, sentAvgCharCount, recAvgCharCount);

        //TODO: msgs over total time should be broken up into days over the last two weeks
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_stats, menu);
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
}

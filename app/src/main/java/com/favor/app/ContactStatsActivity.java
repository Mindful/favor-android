package com.favor.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.favor.graphs.HorizontalTwoBarGraph;
import com.favor.graphs.TimeLapseTwoLineGraph;
import com.favor.library.*;

public class ContactStatsActivity extends FavorActivity {

    private Contact contact;
    private AccountManager account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_stats);

        TextView title = (TextView) findViewById(R.id.avg_character_count_chart_title);
        if (getIntent().getSerializableExtra(ContactSelectFragment.CONTACT) != null){
            contact = (Contact) getIntent().getSerializableExtra(ContactSelectFragment.CONTACT);
            getSupportActionBar().setTitle(contact.getDisplayName());
            setContactAccount(Util.TEMP_ACCOUNT_FUNCTION()); //TODO: be smarter about the default, pick the most relevant type
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
        TimeLapseTwoLineGraph msgTotalOverTime = (TimeLapseTwoLineGraph) findViewById(R.id.total_msg_over_time_chart);
        HorizontalTwoBarGraph avgCharCount = (HorizontalTwoBarGraph) findViewById(R.id.avg_character_count_chart);
        HorizontalTwoBarGraph responseTime = (HorizontalTwoBarGraph) findViewById(R.id.response_time_chart);


        double sentResponseTime = Processor.responseTimeNintieth(account, contact, -1, -1, true);
        double recResponseTime = Processor.responseTimeNintieth(account, contact, -1, -1, false);
        responseTime.setTwoValueData(contact.getDisplayName(), sentResponseTime, recResponseTime);
        responseTime.setDefaults();


        double sentAvgCharCount = Processor.averageCharcount(account, contact, -1, -1, true);
        double recAvgCharCount = Processor.averageCharcount(account, contact, -1, -1, false);
        avgCharCount.setTwoValueData(contact.getDisplayName(), sentAvgCharCount, recAvgCharCount);
        avgCharCount.setDefaults();

        //TODO: msgs over total time should be broken up into days over the last two weeks since a message was sent/rec
        //from this contact

        TextView sentLegendText = (TextView) findViewById(R.id.sent_legend_textview);
        TextView recLegendText = (TextView) findViewById(R.id.rec_legend_textview);

        sentLegendText.setText(contact.getDisplayName());
        recLegendText.setText(Core.getContext().getString(R.string.sent_name));

        msgTotalOverTime.setDefaults();
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

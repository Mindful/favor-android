package com.favor.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.favor.ui.graph.Graph;
import com.favor.util.Algorithms;
import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.R;

public class GraphActivity extends Activity {

	private static List<Contact> prevContacts;
	private static Graph graph;
	private static final String items[] = {"Friend Score", "Character Count", "Message Count", "Response Time"};
	//DO NOT REORDER THE ITEMS[] ARRAY - PLACEMENTS CORRESPOND TO A SWITCH STATEMENT
	private static int currentItem = 1; 
	
	public static void clearPrevContacts()
	{
		prevContacts = null;
	}
	//TODO: should default to 0 once Friend Score works, and should be saved in preferences
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		showGraph();
		setupActionBar();
	}

	private void showGraph() {
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.clearView();
		webView.getSettings();
		webView.setBackgroundColor(0x808080);

		ContactArrayAdapter aa = ContactArrayAdapter.getSingleton();
		List<Contact> allContacts = aa.getContacts();
		List<Contact> contacts = new ArrayList<Contact>();
		for (Contact c : allContacts) 
		{
			if (c.isSelected()) 
			{
				contacts.add(c);
			}
		}

		
		//new graph code
		//calc new data if we have to, else use old graph and just show it
		setGraph(contacts);
		graph.show(this, webView);
		setTitle(items[currentItem]);
	}
	
	private void setGraph(List<Contact> contacts)
	{
		if (graph == null || !contacts.equals(prevContacts))
		{
			prevContacts = contacts;
			List<String> names = new ArrayList<String>(contacts.size());
			for (Contact c: contacts)
			{
				names.add(c.getName());
			}
			graph = Graph.newGraph(names, query(contacts));
		}
	}
	
	private Object query(List<Contact> contacts)
	{
		long fromDate = -1; //TODO: this is here so that we can eventually specify date
		long untilDate = -1;
		//switch based on currentItem
		/*	private static final String items[] = {"Friend Score", "Character Count", "Message Count", "Response Time"};*/
		switch (currentItem)
		{
		case 0:
			if (contacts.size() == 1)
			{
				long[][] relationshipScore = new long[1][]; //Seems pointless, has to be [1][x] for Doughnut
				relationshipScore[0] = Algorithms.relationshipScore(contacts.get(0).getAddress());
				return (Object) relationshipScore;
			}
			else
			{
				long[] friendScores = new long[contacts.size()];
				for (int i = 0; i < contacts.size(); i++)
				{
					friendScores[i] = Algorithms.friendScore(contacts.get(i).getAddress());
				}
				return (Object) friendScores; 
			}
		case 1:
			long[][] characterCounts = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i++)
			{
				characterCounts[i] = Algorithms.charCount(contacts.get(i).getAddress(), fromDate, untilDate);
			}
			return (Object) characterCounts;
		case 2:
			long[][] messageCounts = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i ++)
			{
				messageCounts[i] = Algorithms.messageCount(contacts.get(i).getAddress(), fromDate, untilDate);
			}
			return (Object) messageCounts;
		case 3:
			long[][] responseTimes = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i++)
			{
				responseTimes[i] = Algorithms.responseTime(contacts.get(i).getAddress(), fromDate, untilDate);
			}
			return (Object) responseTimes;
		default:
			throw new RuntimeException("Invalid selection for query");
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.graph_bar, menu);
		return true;
	}

	static int selected = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_switch_graph:
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Select Metric")
					.setSingleChoiceItems(items, selected,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (which != selected)
										selected = which;
								}

							})
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface d, int choice) 
								{
									if (selected != -1) 
									{
										prevContacts = null; //so that we regenerate a new graph
										currentItem = selected;
										showGraph();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface d,
										int choice) {
									d.cancel();
								}
							});
			ab.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

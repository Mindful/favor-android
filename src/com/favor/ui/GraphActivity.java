package com.favor.ui;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.MenuInflater;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
//import android.annotation.TargetApi;
//import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
//import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
//import android.view.Menu;
//import android.view.MenuItem;

import com.favor.ui.graph.Graph;
import com.favor.util.Algorithms;
import com.favor.util.DataHandler;
import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.widget.GraphView;
import com.favor.R;

public class GraphActivity extends SherlockActivity {

	private static List<Contact> prevContacts;
	private static Graph graph;
	private static final String items[] = {"Friend Score", "Character Count", "Message Count", "Response Time"};
	//DO NOT REORDER THE ITEMS[] ARRAY - PLACEMENTS CORRESPOND TO A SWITCH STATEMENT
	private static int currentItem = 0; 
	
	private final String SAVED_METRIC = "metric";
	
	public static Graph getGraph(){ return graph;}
	
	public static void clearPrevContacts()
	{
		prevContacts = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentItem = getSharedPreferences(DataHandler.PREFS_NAME, Context.MODE_PRIVATE).getInt(SAVED_METRIC, 0);
		setContentView(R.layout.activity_graph);
		showGraph();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showGraph() {
		GraphView view = (GraphView) findViewById(R.id.graph_view);
		view.clearView();
		//webView.getSettings();

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

		
		setGraph(contacts);
		graph.show();
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
			graph = Graph.newGraph(names, query(contacts), this);
		}
		graph.updateView((GraphView) findViewById(R.id.graph_view));
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
			//TODO: these algo methods should just take contacts, as should the DB queries. Addresses should rarely
			//need to be referenced directly
			if (contacts.size() == 1)
			{
				long[][] relationshipScore = new long[1][]; //Seems pointless, has to be [1][x] for Doughnut
				relationshipScore[0] = Algorithms.relationshipScore(contacts.get(0));
				return (Object) relationshipScore;
			}
			else
			{
				long[] friendScores = new long[contacts.size()];
				for (int i = 0; i < contacts.size(); i++)
				{
					friendScores[i] = Algorithms.friendScore(contacts.get(i));
				}
				return (Object) friendScores; 
			}
		case 1:
			long[][] characterCounts = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i++)
			{
				characterCounts[i] = Algorithms.charCount(contacts.get(i), fromDate, untilDate);
			}
			return (Object) characterCounts;
		case 2:
			long[][] messageCounts = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i ++)
			{
				messageCounts[i] = Algorithms.messageCount(contacts.get(i), fromDate, untilDate);
			}
			return (Object) messageCounts;
		case 3:
			long[][] responseTimes = new long[contacts.size()][];
			for (int i = 0; i < contacts.size(); i++)
			{
				responseTimes[i] = Algorithms.responseTime(contacts.get(i), fromDate, untilDate);
			}
			return (Object) responseTimes;
		default:
			throw new RuntimeException("Invalid selection for query");
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.graph_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_switch_graph:
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Select Metric")
					.setSingleChoiceItems(items, currentItem,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (which != currentItem)
										currentItem = which;
								}

							})
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface d, int choice) 
								{
									if (currentItem != -1) 
									{
										SharedPreferences.Editor edit = getSharedPreferences(DataHandler.PREFS_NAME, Context.MODE_PRIVATE).edit();
										edit.putInt(SAVED_METRIC, currentItem);
										edit.apply();
										prevContacts = null; //so that we regenerate a new graph
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

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
import com.favor.ui.graph.CharCountGraph;
import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.R;

public class GraphBarActivity extends Activity {

	private Graph[] cachedGraphs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph_bar);

		cachedGraphs = new Graph[2];

		showBar(CharCountGraph.class);
		setupActionBar();
	}

	private void showBar(Class<? extends Graph> clazz) {
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.clearView();

		ContactArrayAdapter aa = ContactArrayAdapter.getSingleton();
		List<Contact> contacts = new ArrayList<Contact>();

		for (Contact c : aa.getContacts()) {
			if (c.isSelected()) {
				contacts.add(c);
			}
		}

		for (Graph g : cachedGraphs) {
			if (g != null && g.getClass().equals(clazz)) {
				g.showBar(this, webView, contacts);
				return;
			}
		}

		try {
			Graph graph = clazz.newInstance();

			for (int i = 0; i < cachedGraphs.length; i++) {
				if (cachedGraphs[i] == null)
					cachedGraphs[i] = graph;
			}

			graph.showBar(this, webView, contacts);
		} catch (Exception e) {

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
			final String items[] = { "Response Time", "Response Ratio", "Character Count", 
					"Character Ratio", "Friend Score", "Relationship Score" };

			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Dialog Title")
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
								@SuppressWarnings("unchecked")
								public void onClick(DialogInterface d,
										int choice) {
									if (selected != -1) {
										try {
											Class<? extends Graph> clazz = (Class<? extends Graph>) Class
													.forName("com.favor.ui.graph.Graph"
															+ items[selected]);
											showBar(clazz);
										} catch (ClassNotFoundException e) {
										}
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

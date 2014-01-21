package com.pytacular.checkitcloudchecklist;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;
import com.parse.ParseUser;
import com.parse.PushService;

public class MainActivity extends SherlockActivity implements
		OnItemClickListener {

	protected static final int RESULT_SPEECH = 1;
	private EditText mTaskInput;
	private ListView mListView;
	private TaskAdapter mAdapter;
	ProgressDialog proDialog;
	private SpeechRecognizer sr;

	private final Handler handler = new Handler();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addTabs(); // Adding tabs

		if (savedInstanceState != null) {
			int index = savedInstanceState.getInt("selected_tab_index", 0);
			getSupportActionBar().setSelectedNavigationItem(index);
		}

		if (InternetStatus.getInstance(this).isOnline(this)) {
			Log.i("MainActivity : ", "Internet connection detected");
		} else {
			Log.i("MainActivity : ", "Internet connection not detected");
		}

		/**
		 * Below is the complete parse usage init(context, APP ID, Client ID).
		 * Add your APP ID and CLient ID given by parse.
		 */
		Parse.initialize(this, "",""); //add APP ID & CLIENT ID given by parse respectively.
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseAnalytics.trackAppOpened(getIntent());
		ParseObject.registerSubclass(Task.class);

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}

		mAdapter = new TaskAdapter(this, new ArrayList<Task>());

		mTaskInput = (EditText) findViewById(R.id.task_input);
		mListView = (ListView) findViewById(R.id.task_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		updateData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int index = getSupportActionBar().getSelectedNavigationIndex();
		outState.putInt("selected_tab_index", index);
	}

	public void updateData() {
		ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.findInBackground(new FindCallback<Task>() {
			@Override
			public void done(List<Task> tasks, ParseException error) {
				if (tasks != null) {
					mAdapter.clear();
					for (int i = 0; i < tasks.size(); i++) {
						mAdapter.add(tasks.get(i));
					}
				}
			}
		});
	}

	public void createTask(View v) {
		if (mTaskInput.getText().length() > 0) {
			Task t = new Task();
			t.setACL(new ParseACL(ParseUser.getCurrentUser()));
			t.setUser(ParseUser.getCurrentUser());
			t.setDescription(mTaskInput.getText().toString());
			t.setCompleted(false);
			t.saveEventually();
			mAdapter.insert(t, 0);
			mTaskInput.setText("");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Task task = mAdapter.getItem(position);
		TextView taskDescription = (TextView) view
				.findViewById(R.id.task_description);

		task.setCompleted(!task.isCompleted());

		if (task.isCompleted()) {
			taskDescription.setPaintFlags(taskDescription.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			taskDescription.setPaintFlags(taskDescription.getPaintFlags()
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
		}

		task.saveEventually();
	}

	/**
	 * Creating tabs
	 */
	private void addTabs() {
		/*
		 * ActionBar bar = getSupportActionBar();
		 * 
		 * 
		 * String defaultTab = getResources().getString(R.string.check_list);
		 * ActionBar.Tab currentTab = bar.newTab();
		 * currentTab.setText(defaultTab); currentTab.setTabListener(new
		 * TabListener(this, defaultTab, CheckList.class));
		 * bar.addTab(currentTab);
		 * 
		 * bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		 */
	}

	/**
	 * Responding to menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		// return true;
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.action_refresh);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			// on selecting show progress spinner for 1s
			public boolean onMenuItemClick(MenuItem item) {
				// item.setActionView(R.layout.progress_action);
				handler.postDelayed(new Runnable() {
					public void run() {
						refresh.setActionView(null);
					}
				}, 1000);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SPEECH: {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> text = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				mTaskInput.setText(text.get(0));
			}
			break;
		}

		}
	}

	/**
	 * When an item from a menu is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBar menubar = getSupportActionBar();
		switch (item.getItemId()) {
		case R.id.action_speech:

			Intent speechintent = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			speechintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					"en-US");
			try {
				startActivityForResult(speechintent, RESULT_SPEECH);
				mTaskInput.setText("");
			} catch (ActivityNotFoundException a) {
				Toast t = Toast.makeText(getApplicationContext(),
						"Your device does not support this feature.",
						Toast.LENGTH_SHORT);
				t.show();
			}

			return true;
		case R.id.action_refresh:
			// switch to a progress animation
			item.setActionView(R.layout.indeterminate_progress_action);
			updateData();
			return true;
		case R.id.action_settings:
			/*
			 * Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show(); Was
			 * using this for debugging
			 */
			break;
		case R.id.action_logout:
			ParseUser.logOut();
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return true;
			/*
			 * case R.id.action_switch_theme: Toast.makeText(this,
			 * "Switch Theme", Toast.LENGTH_SHORT).show(); Intent intent = new
			 * Intent(this, TabSwipeTestActivity.class);
			 * this.startActivity(intent); break;
			 */

		default:
			break;
		}

		return true;
	}
}

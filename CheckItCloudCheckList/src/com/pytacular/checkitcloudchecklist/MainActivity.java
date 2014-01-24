/*******************************************************************************
 * Copyright 2014 Bhavyanshu Parasher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.pytacular.checkitcloudchecklist;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
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
		OnItemClickListener, OnItemLongClickListener {

	protected static final int RESULT_SPEECH = 1;
	private EditText mTaskInput;
	private ListView mListView;
	private TaskAdapter mAdapter;
	ProgressDialog proDialog;
	private SpeechRecognizer sr;
	final String PREFS_NAME = "MyPrefsFile";
	private final Handler handler = new Handler();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// addTabs(); // Adding tabs
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if (settings.getBoolean("firstlaunch", true)) {
			// the app is being launched for first time, do something
			Log.d("Comments", "First time");
			showOverLay();
			// record the fact that the app has been started at least once
			settings.edit().putBoolean("firstlaunch", false).commit();
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
		Parse.initialize(this, "",""); 
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
		mListView.setOnItemLongClickListener(this);
		updateData();
	}

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void infoBox(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Developer Information");
		final SpannableString s = new SpannableString(this.getText(R.string.devinfo));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		alertDialog.setMessage(s);
		alertDialog.setCancelable(false);
	    alertDialog.setNegativeButton("Close",new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	            }
	        });
	    alertDialog.setNegativeButton("Copy Website Link",new DialogInterface.OnClickListener() {
            @SuppressWarnings("deprecation")
			public void onClick(DialogInterface dialog, int id) {
            	if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            		@SuppressWarnings("deprecation")
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
            	    clipboard.setText("http://bhavyanshu.me");
            	    Toast.makeText(getApplicationContext(), "Link Copied to Clipboard", Toast.LENGTH_SHORT).show();
            	} else {
            	    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE); 
            	    android.content.ClipData clip = android.content.ClipData.newPlainText("Website",getResources().getString(R.string.dev_website));
            	    clipboard.setPrimaryClip(clip);
            	    Toast.makeText(getApplicationContext(), "Link Copied to Clipboard", Toast.LENGTH_SHORT).show();
            	}
                dialog.cancel();
            }
        });
	    alertDialog.create();
		alertDialog.show();
	}

	private void showOverLay() {

		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		dialog.setContentView(R.layout.overlay_view);

		LinearLayout layout = (LinearLayout) dialog
				.findViewById(R.id.overlayLayout);
		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
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

	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos,
			long id) {
		Task task = mAdapter.getItem(pos);
		task.setCompleted(!task.isCompleted());
		if (task.isCompleted()) {
			Toast.makeText(getApplicationContext(),
					"Not CheckedIt yet. Click to CheckIt!", Toast.LENGTH_LONG)
					.show();
			return false;
		} else {
			task.getObjectId();
			Log.i("getObjectId :", task.getObjectId());
			task.deleteInBackground();
			mAdapter.remove(task);
			mAdapter.notifyDataSetChanged();
			return true;
		}

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
		case R.id.action_help:
			showOverLay();
			return true;
		case R.id.action_devinfo:
			infoBox();
			return true;	
		default:
			break;
		}
		return true;
	}
}

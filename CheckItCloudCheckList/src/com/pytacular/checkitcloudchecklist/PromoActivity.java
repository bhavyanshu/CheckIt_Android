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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;
import com.parse.ParseUser;
import com.parse.PushService;

public class PromoActivity extends SherlockActivity implements
		OnItemClickListener {

	protected static final int RESULT_SPEECH = 1;
	private ListView mListView;
	private PromoAdapter mAdapter;
	ProgressDialog proDialog;
	private SpeechRecognizer sr;
	private final Handler handler = new Handler();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_promo_main);
		if (InternetStatus.getInstance(this).isOnline(this)) {
			//Do nothing
		} else {
			//Log.i("MainActivity : ", "Internet connection not detected");
			//Display error to the user or ask user to login again?
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} 
		//PushService.setDefaultPushCallback(this, PromoActivity.class);
		ParseAnalytics.trackAppOpened(getIntent());
		ParseObject.registerSubclass(Promo.class);
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
		mAdapter = new PromoAdapter(this, new ArrayList<Promo>());
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
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
		ParseQuery<Promo> query = ParseQuery.getQuery(Promo.class);
		query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.orderByDescending("createdAt");
		query.findInBackground(new FindCallback<Promo>() {
			@Override
			public void done(List<Promo> promo, ParseException error) {
				if (promo != null) {
					mAdapter.clear();
					for (int i = 0; i < promo.size(); i++) {
						mAdapter.add(promo.get(i));
					}
				}
			}
		});
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Promo promo = mAdapter.getItem(position);
		TextView promoURL = (TextView) view.findViewById(R.id.promo_url);
		Log.i("URL:",promoURL.getText().toString());
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(promoURL.getText().toString()));
		startActivity(browserIntent);
	}


	/**
	 * Responding to menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.promomenu, menu);
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


	/**
	 * When an item from a menu is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBar menubar = getSupportActionBar();
		switch (item.getItemId()) {
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
		case R.id.action_share_app:
			try
			{ Intent i = new Intent(Intent.ACTION_SEND);  
			  i.setType("text/plain");
			  i.putExtra(Intent.EXTRA_SUBJECT, "CheckIt Cloud CheckList");
			  String sAux = "\nHey, check out this free checklist app. Sync your daily to-do list with cloud for free.\n";
			  sAux = sAux + "https://play.google.com/store/apps/details?id=com.pytacular.checkitcloudchecklist \n\n";
			  i.putExtra(Intent.EXTRA_TEXT, sAux);  
			  startActivity(Intent.createChooser(i, "Select One"));
			}
			catch(Exception e)
			{ //e.toString();
			}   
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

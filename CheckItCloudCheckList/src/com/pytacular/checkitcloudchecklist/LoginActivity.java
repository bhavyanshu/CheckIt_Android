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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends SherlockActivity {

	private EditText mUsernameField;
	private EditText mPasswordField;
	private TextView mErrorField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mUsernameField = (EditText) findViewById(R.id.login_username);
		mPasswordField = (EditText) findViewById(R.id.login_password);
		mErrorField = (TextView) findViewById(R.id.error_messages);

		if (InternetStatus.getInstance(this).isOnline(this)) {
			// Pass
		} else {
			mErrorField
					.setText("No internet connection available. Please check your connection settings.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void signIn(final View v) {
		v.setEnabled(false);
		ParseUser.logInInBackground(mUsernameField.getText().toString(),
				mPasswordField.getText().toString(), new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (user != null) {
							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							startActivity(intent);
							finish();
						} else {
							// Signup failed. Look at the ParseException to see
							// what happened.
							switch (e.getCode()) {
							case ParseException.USERNAME_TAKEN:
								mErrorField
										.setText("Sorry, this username has already been taken.");
								break;
							case ParseException.USERNAME_MISSING:
								mErrorField
										.setText("Sorry, you must supply a username to register.");
								break;
							case ParseException.PASSWORD_MISSING:
								mErrorField
										.setText("Sorry, you must supply a password to register.");
								break;
							case ParseException.OBJECT_NOT_FOUND:
								mErrorField
										.setText("Sorry, those credentials were invalid.");
								break;
							case ParseException.CONNECTION_FAILED:
								mErrorField
										.setText("Internet connection was not found. Please see your connection settings.");
								break;
							default:
								mErrorField.setText(e.getLocalizedMessage());
								break;
							}
							v.setEnabled(true);
						}
					}
				});
	}

	public void showRegistration(View v) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * When an item from a menu is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBar menubar = getSupportActionBar();
		switch (item.getItemId()) {
		case R.id.action_settings:
			/*
			 * Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show(); Was
			 * using this for debugging
			 */
			break;
		case R.id.action_help:
			showOverLay();
			return true;
		default:
			break;
		}

		return true;
	}

	public void showOverLay() {

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

}

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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;


public class TabListener implements ActionBar.TabListener {
 
    private final FragmentActivity mActivity; 
    private final String mTag; 
    private final Class mFragmentClass; 
    private Fragment mFragment;
 
    public TabListener(FragmentActivity activity, String tag, Class fragmentClass) {
        mActivity = activity;
        mTag = tag;
        mFragmentClass = fragmentClass;
        mFragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
    }
	
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) { 
        if (mFragment == null) {
            mFragment = Fragment.instantiate(mActivity, mFragmentClass.getName());      
            // place in the default root viewgroup - android.R.id.content
            ft.replace(android.R.id.content, mFragment, mTag); 
        } else { 
            if(mFragment.isDetached()) 
                ft.attach(mFragment); 
        }
    }
 
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (mFragment != null){ 
            ft.detach(mFragment);
        }
    }
 
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}

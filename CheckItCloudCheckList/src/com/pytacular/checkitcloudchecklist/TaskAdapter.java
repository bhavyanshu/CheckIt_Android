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

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TaskAdapter extends ArrayAdapter<Task> {
	private Context mContext;
	private List<Task> mTasks;
	
	public TaskAdapter(Context context, List<Task> objects) {
		super(context, R.layout.task_row_item, objects);
		this.mContext = context;
		this.mTasks = objects;
	}

	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null){
			LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
			convertView = mLayoutInflater.inflate(R.layout.task_row_item, null);
		}
		
		Task task = mTasks.get(position);
		
		TextView descriptionView = (TextView) convertView.findViewById(R.id.task_description);
		// Loading font from assets
		Typeface tfIron=Typeface.createFromAsset(getContext().getAssets(), "fonts/comicsans.ttf");
		// applying to the view
		descriptionView.setTypeface(tfIron);
		descriptionView.setText(task.getDescription());
		
		if(task.isCompleted()){
			descriptionView.setPaintFlags(descriptionView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}else{
			descriptionView.setPaintFlags(descriptionView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
		}	
		return convertView;
	}

}

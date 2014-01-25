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

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Promo")
public class Promo extends ParseObject{
	public Promo(){
		
	}
	
	public boolean isCompleted(){
		return getBoolean("completed");
	}
	
	public void setCompleted(boolean complete){
		put("completed", complete);
	}
	
	public String getDescription(){
		return getString("description");
	}
	
	public String getURL(){
		return getString("url");
	}
}

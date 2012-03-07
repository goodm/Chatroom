package com.lukasz.chat.pusher;

/*	Copyright (C) 2011 Emory Myers
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 *  Contributors: Martin Linkhorst
 */
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PusherCallback extends Handler {

	public PusherCallback() {
		super(Looper.getMainLooper());
	}

	public PusherCallback(Looper looper) {
		super(looper);
	}

	public void handleMessage(Message message) 
	{
		Log.d("MESSAGE","Reveive some message");
		Bundle payload = message.getData();
		String eventName = payload.getString("eventName");
		String channelName = payload.getString("channelName");
		String eventData = payload.getString("eventData");

		onEvent(eventName, eventData, channelName);
	}

	public void onEvent(String eventName, JSONObject eventData, String channelName) {

	}

	public void onEvent(String eventName, String eventData, String channelName) {
		try {
			JSONObject parsedEventData = new JSONObject(eventData);
			onEvent(eventName, parsedEventData, channelName);
		} catch (JSONException e) {
		}
	}
}

package com.lukasz.chat;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class PusherChannel implements PusherEventEmitter {
	private static final String LOG_TAG = "Pusher";

	private String mName;

	private List<PusherCallback> mGlobalCallbacks = new ArrayList<PusherCallback>();
	private Map<String, List<PusherCallback>> mLocalCallbacks = new HashMap<String, List<PusherCallback>>();

	public PusherChannel(String name) {
		mName = name;
	}

	public boolean isPrivate() {
		return mName.startsWith("private-");
	}

	public void bind(String event, PusherCallback callback) {
		/* if there are no callbacks for that event assigned yet, initialize the list */
		if (!mLocalCallbacks.containsKey(event)) {
			mLocalCallbacks.put(event, new ArrayList<PusherCallback>());
		}

		/* add the callback to the event's callback list */
		mLocalCallbacks.get(event).add(callback);

		Log.d(LOG_TAG, "bound to event " + event + " on channel " + mName);
	}

	public void bindAll(PusherCallback callback) {
		mGlobalCallbacks.add(callback);

		Log.d(LOG_TAG, "bound to all events on channel " + mName);
	}

	public void unbind(PusherCallback callback) {
		/* remove all matching callbacks from the global callback list */
		while (mGlobalCallbacks.remove(callback))
			;

		/* remove all matching callbacks from each local callback list */
		for (List<PusherCallback> localCallbacks : mLocalCallbacks.values()) {
			while (localCallbacks.remove(callback))
				;
		}
	}

	public void unbindAll() {
		/* remove all callbacks from the global callback list */
		mGlobalCallbacks.clear();
		/* remove all local callback lists, that is removes all local callbacks */
		mLocalCallbacks.clear();
	}

	public void dispatchEvents(String eventName, String eventData) {
		Bundle payload = new Bundle();
		payload.putString("eventName", eventName);
		payload.putString("eventData", eventData);
		payload.putString("channelName", mName);

		Message msg = Message.obtain();
		msg.setData(payload);

		for (PusherCallback callback : mGlobalCallbacks) {
			callback.sendMessage(msg);
		}

		/* do we have a callback bound to that event? */
		if (mLocalCallbacks.containsKey(eventName)) {
			/* execute each callback */
			for (PusherCallback callback : mLocalCallbacks.get(eventName)) {
				callback.sendMessage(msg);
			}
		}
	}

	public String getName() {
		return mName;
	}
}
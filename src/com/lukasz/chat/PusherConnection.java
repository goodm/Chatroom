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

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.roderick.weberknecht.WebSocket;
import de.roderick.weberknecht.WebSocketConnection;
import de.roderick.weberknecht.WebSocketEventHandler;
import de.roderick.weberknecht.WebSocketException;
import de.roderick.weberknecht.WebSocketMessage;

class PusherConnection {
	private static final String LOG_TAG = "Pusher";

	public Pusher mPusher;
	public WebSocket mWebSocket;

	public PusherConnection(Pusher pusher) {
		mPusher = pusher;
	}

	public void connect() {
		try {
			URI url = new URI(mPusher.getUrl());
			Log.d(LOG_TAG, "Connecting to " + url.toString());

			mWebSocket = new WebSocketConnection(url);
			mWebSocket.setEventHandler(new WebSocketEventHandler() 
			{
				public void onOpen() {
					Log.d(LOG_TAG, "Successfully opened Websocket");
				}

				public void onMessage(WebSocketMessage message) 
				{
					

					try {
						JSONObject parsed = new JSONObject(message.getText());
						String eventName = parsed.getString("event");
						String channelName = parsed.optString("channel", null);
						String eventData = parsed.getString("data");

						
						
						if (eventName.equals(Pusher.PUSHER_EVENT_CONNECTION_ESTABLISHED)) 
						{
							JSONObject parsedEventData = new JSONObject(eventData);
							String socketId = parsedEventData.getString("socket_id");
							mPusher.onConnected(socketId);
						} else {
							mPusher.dispatchEvents(eventName, eventData, channelName);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				public void onClose() 
				{
					Log.d(LOG_TAG, "Successfully closed Websocket");
				}
			});
			
			mWebSocket.connect();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (mWebSocket != null) {
			try {
				mWebSocket.close();
			} catch (WebSocketException e) {
				e.printStackTrace();
			}
		}
		mPusher.onDisconnected();
	}

	public void send(String eventName, JSONObject eventData, String channelName) {
		if (mWebSocket == null)
			return;

		if (mWebSocket.isConnected()) {
			try {
				JSONObject message = new JSONObject();
				message.put("event", eventName);
				message.put("data", eventData);

				if (channelName != null) {
					message.put("channel", channelName);
				}

				mWebSocket.send(message.toString());

				Log.d(LOG_TAG, "sent message " + message.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WebSocketException e) {
				e.printStackTrace();
			}
		}
	}
}
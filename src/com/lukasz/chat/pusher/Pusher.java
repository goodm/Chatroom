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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import com.lukasz.chat.Main;

import android.app.Activity;
import android.util.Log;

public class Pusher implements PusherEventEmitter {
	private static final String LOG_TAG = "Pusher";

	private static final String PUSHER_CLIENT = "android-Android_Pusher";
	private static final String VERSION = "1.11.1";

	protected static final String PUSHER_EVENT_CONNECTION_ESTABLISHED = "pusher:connection_established";
	protected static final String PUSHER_EVENT_SUBSCRIBE = "pusher:subscribe";
	protected static final String PUSHER_EVENT_UNSUBSCRIBE = "pusher:unsubscribe";

	private static final String PUSHER_AUTH_ALGORITHM = "HmacSHA256";

	private static final String PUSHER_HOST = "ws.pusherapp.com";

	private static final String WS_SCHEME = "ws://";
	private static final String WSS_SCHEME = "wss://";

	private static final int WS_PORT = 80;
	private static final int WSS_PORT = 443;

	private String mPusherKey;
	private String mPusherSecret;
	private boolean mEncrypted;

	private String mSocketId;
	private PusherConnection mConnection = new PusherConnection(this);

	public PusherChannel mGlobalChannel = new PusherChannel("pusher_global_channel");
	public Map<String, PusherChannel> mLocalChannels = new HashMap<String, PusherChannel>();

	public Main a;
	
	public Pusher(String pusherKey, String pusherSecret,Main a, boolean encrypted) 
	{
		this.a = a;
		init(pusherKey, pusherSecret, encrypted);
	}

	public Pusher(String pusherKey, String pusherSecret) {
		init(pusherKey, pusherSecret, true);
	}

	public Pusher(String pusherKey, boolean encrypted) {
		init(pusherKey, null, encrypted);
	}

	public Pusher(String pusherKey) {
		init(pusherKey, null, true);
	}

	private void init(String pusherKey, String pusherSecret, boolean encrypted) {
		mPusherKey = pusherKey;
		mPusherSecret = pusherSecret;
		mEncrypted = encrypted;
	}

	public void connect() 
	{
		mConnection.connect();
	}

	public boolean isConnected() {
		return mSocketId != null;
	}

	public void disconnect() {
		mConnection.disconnect();
	}

	public void onConnected(String socketId) {
		mSocketId = socketId;
		subscribeToAllChannels();
	}

	public void onDisconnected() {
		mSocketId = null;
	}

	public String getUrl() {
		return getScheme() + getHost() + ":" + getPort() + getPath();
	}

	private String getScheme() {
		return mEncrypted ? WSS_SCHEME : WS_SCHEME;
	}

	protected String getHost() {
		return PUSHER_HOST;
	}

	private int getPort() {
		return mEncrypted ? WSS_PORT : WS_PORT;
	}

	private String getPath() {
		return "/app/" + mPusherKey + "?client=" + PUSHER_CLIENT + "&version=" + VERSION;
	}

	public void bind(String event, PusherCallback callback) {
		mGlobalChannel.bind(event, callback);
	}

	public void bindAll(PusherCallback callback) {
		mGlobalChannel.bindAll(callback);
	}

	public void unbind(PusherCallback callback) {
		mGlobalChannel.unbind(callback);
	}

	public void unbindAll() {
		mGlobalChannel.unbindAll();
	}

	public PusherChannel subscribe(String channelName) {
		PusherChannel channel = createLocalChannel(channelName);
		sendSubscribeMessage(channel);
		return channel;
	}

	public void unsubscribe(String channelName) {
		/* TODO: just mark as unsubscribed in order to keep the bindings */
		PusherChannel channel = removeLocalChannel(channelName);

		if (channel == null)
			return;

		sendUnsubscribeMessage(channel);
	}

	public void subscribeToAllChannels() {
		for (PusherChannel channel : mLocalChannels.values()) {
			sendSubscribeMessage(channel);
		}
	}

	private void unsubscribeFromAllChannels() {
		for (PusherChannel channel : mLocalChannels.values()) {
			sendUnsubscribeMessage(channel);
		}

		/* TODO: just mark the channels as unsubscribed in order to keep the bindings */ 
		mLocalChannels.clear();
	}

	private void sendSubscribeMessage(PusherChannel channel) {
		if (!isConnected())
			return;

		try {
			String eventName = PUSHER_EVENT_SUBSCRIBE;

			JSONObject eventData = new JSONObject();
			eventData.put("channel", channel.getName());

			if (channel.isPrivate()) {
				String authInfo = authenticate(channel.getName());
				eventData.put("auth", authInfo);
			}

			sendEvent(eventName, eventData, null);

			Log.d(LOG_TAG, "subscribed to channel " + channel.getName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void sendUnsubscribeMessage(PusherChannel channel) {
		if (!isConnected())
			return;

		try {
			String eventName = PUSHER_EVENT_UNSUBSCRIBE;

			JSONObject eventData = new JSONObject();
			eventData.put("channel", channel.getName());

			sendEvent(eventName, eventData, null);

			Log.d(LOG_TAG, "unsubscribed from channel " + channel.getName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void sendEvent(String eventName, JSONObject eventData, String channelName) {
		mConnection.send(eventName, eventData, channelName);
	}

	public void dispatchEvents(String eventName, String eventData, String channelName) 
	{
		mGlobalChannel.dispatchEvents(eventName, eventData);
		a.onMessageReceive(eventName,eventData);
		PusherChannel localChannel = mLocalChannels.get(channelName);

		if (localChannel == null)
			return;

		localChannel.dispatchEvents(eventName, eventData);
	}

	/* TODO: refactor */
	private String authenticate(String channelName) {
		if (!isConnected()) {
			Log.e(LOG_TAG, "pusher not connected, can't create auth string");
			return null;
		}

		try {
			String stringToSign = mSocketId + ":" + channelName;

			SecretKey key = new SecretKeySpec(mPusherSecret.getBytes(), PUSHER_AUTH_ALGORITHM);

			Mac mac = Mac.getInstance(PUSHER_AUTH_ALGORITHM);
			mac.init(key);
			byte[] signature = mac.doFinal(stringToSign.getBytes());

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < signature.length; ++i) {
				sb.append(Integer.toHexString((signature[i] >> 4) & 0xf));
				sb.append(Integer.toHexString(signature[i] & 0xf));
			}

			String authInfo = mPusherKey + ":" + sb.toString();

			Log.d(LOG_TAG, "Auth Info " + authInfo);

			return authInfo;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		return null;
	}

	private PusherChannel createLocalChannel(String channelName) {
		PusherChannel channel = new PusherChannel(channelName);
		mLocalChannels.put(channelName, channel);
		return channel;
	}

	private PusherChannel removeLocalChannel(String channelName) {
		return mLocalChannels.remove(channelName);
	}
}

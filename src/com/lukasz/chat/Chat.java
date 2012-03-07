package com.lukasz.chat;

import org.json.JSONObject;

import com.lukasz.chat.pusher.Pusher;
import com.lukasz.chat.pusher.PusherCallback;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Chat extends Application 
{
	/* the log tag constant */
	private static final String LOG_TAG = "Pusher";

	public static final String PUSHER_APP_KEY = "e6fd70f644a0b834cef9";
	public static final String PUSHER_APP_SECRET = "d0bbaeedcc5f06cdf5d3";

	public static final String PRIVATE_CHANNEL = "private-channel";

	public Pusher pusher;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();
	}
	
	public void setUpPusher(Main m)
	{
		pusher = new Pusher(PUSHER_APP_KEY, PUSHER_APP_SECRET,m, false);
		pusher.bindAll(new PusherCallback() 
		{
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) 
			{
				Log.d(LOG_TAG, "Received " + eventData.toString() + " for event '" + eventName + "' on channel '" + channelName + "'.");
			}
		});
	}
	
	public void setPrefs(String user)
	{
		editor.putString("user", user);
		editor.commit();
	}
	
	public void clearSettings()
	{
		editor.putString("user", null);
		editor.commit();
	}
	
	public String getUser()
	{
		return prefs.getString("user", null);
	}
	
	public void startChat()
	{
		pusher.connect();
		pusher.subscribe(PRIVATE_CHANNEL);
	}
}

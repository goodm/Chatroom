package com.lukasz.chat;

import org.json.JSONObject;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class Chat extends Application 
{
	/* the log tag constant */
	private static final String LOG_TAG = "Pusher";

	public static final String PUSHER_APP_KEY = "e6fd70f644a0b834cef9";
	public static final String PUSHER_APP_SECRET = "d0bbaeedcc5f06cdf5d3";

	public static final String PRIVATE_CHANNEL = "private-channel";

	public Pusher pusher;
	
	private Main main;
	
	public Chat(Main m)
	{
		this.main = m;
		
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
	
	public void startChat()
	{
		Start start = new Start();
    	start.execute("...");
	}
	
    private class Start extends AsyncTask<String, Void, String> 
    {
    	@Override
    	protected String doInBackground(String... urls) 
    	{
    		String response = "";    		
			pusher.connect();
			pusher.subscribe(PRIVATE_CHANNEL);
    		return response;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) 
    	{	    		
    		//loading.setVisibility(View.INVISIBLE);
    		//startView.startAnimation(a);
    	}
    }
}

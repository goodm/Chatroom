package com.lukasz.chat;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	/* the log tag constant */
	private static final String LOG_TAG = "Pusher";

	private static final String PUSHER_APP_KEY = "e6fd70f644a0b834cef9";
	private static final String PUSHER_APP_SECRET = "d0bbaeedcc5f06cdf5d3";

	private static final String PRIVATE_CHANNEL = "private-channel";

	private Pusher mPusher;
	private CustomHandler myHandler;
	
	//Views
	private RelativeLayout loginLayout;
	private EditText nickEdit;
	private String nick = "";
	
	//Chat View
	private LinearLayout chatList;
	private ScrollView chatScroll;
	private View chat;
	private LayoutInflater inflater;
	private EditText input;
	private Animation a;
	
	private ProgressBar loading;
	private boolean load = false;
	
	private String message;
	private String name;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myHandler = new CustomHandler();
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		chat = inflater.inflate(R.layout.chat, null);
		
		a = AnimationUtils.loadAnimation(this, R.anim.in);
		
		setUpViews();
		
		
		mPusher = new Pusher(PUSHER_APP_KEY, PUSHER_APP_SECRET,this, false);
		mPusher.bindAll(new PusherCallback() 
		{
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) 
			{
				//Toast.makeText(Main.this,"Received\nEvent: " + eventName + "\nChannel: " + channelName + "\nData: " + eventData.toString(),Toast.LENGTH_LONG).show();

				Log.d(LOG_TAG, "Received " + eventData.toString() + " for event '" + eventName + "' on channel '" + channelName + "'.");
			}
		});
		
		/*sendButton = (Button) findViewById(R.id.send_button);



		sendButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				try 
				{
					String eventName = "client-text";
					String channelName = PRIVATE_CHANNEL;
					JSONObject eventData = new JSONObject(eventDataField.getText().toString());
					mPusher.sendEvent(eventName, eventData, channelName);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});*/
	}

	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.enter:
				nick = nickEdit.getText().toString();
				if(nick.length()>0)
				{
					if(load == false)
					{
						loading.setVisibility(View.VISIBLE);
						load = true;
						Start start = new Start();
	                	start.execute("...");
					}
				}
				else
				{
					Toast.makeText(Main.this,"You must enter your name/nick.",Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.send:
				String mess = input.getText().toString();
				if(mess.length()>0)
				{
					try 
					{
						String eventName = "client-new_message";
						String channelName = PRIVATE_CHANNEL;
						JSONObject eventData = new JSONObject("{name:"+ nick +",message:\""+mess+"\"}");
						mPusher.sendEvent(eventName, eventData, channelName);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
					
					input.setText("");					
					addMessage(nick, mess,"#FFFFFF");
				}
				break;
			case R.id.clear:
				chatList.removeAllViews();
				break;
		}
	}
	
    private class Start extends AsyncTask<String, Void, String> 
    {
    	@Override
    	protected String doInBackground(String... urls) 
    	{
    		String response = "";    		
			mPusher.connect();
			mPusher.subscribe(PRIVATE_CHANNEL);
    		return response;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) 
    	{	    		
    		loading.setVisibility(View.INVISIBLE);
			setContentView(chat);
			setUpChatViews();
    		Toast.makeText(Main.this,"Welcome " + nick + ".",Toast.LENGTH_LONG).show();
    	}
    }
	
	private void setUpChatViews()
	{
		chatList = (LinearLayout)chat.findViewById(R.id.chatList);		
		chatScroll = (ScrollView)chat.findViewById(R.id.chatScroll);
		input = (EditText)chat.findViewById(R.id.text);
	}
	
	private void setUpViews()
	{
		loginLayout = (RelativeLayout)findViewById(R.id.loginLayout);
		nickEdit = (EditText)findViewById(R.id.nameEnter);	
		loading = (ProgressBar)findViewById(R.id.loading);
		loading.setVisibility(View.INVISIBLE);
	}
	
	class CustomHandler extends Handler
	{
	    @Override
	    public void handleMessage(Message msg) 
	    {
	        super.handleMessage(msg);
	        addMessage(name,message,"#efefef");
	    }
	}
	
	public void onMessageReceive(String event,String data)
	{		
		if(event.compareToIgnoreCase("new_message") == 0)
		{
			try
			{
				JSONObject d = new JSONObject(data);
				name = d.getString("name");
				message = d.getString("message");
				myHandler.sendEmptyMessage(0);
			}
			catch(JSONException e)
			{
				Log.e("JSON","error " + e.toString());
			}
		}
		else if(event.compareToIgnoreCase("client-new_message") == 0)
		{
			try
			{
				JSONObject d = new JSONObject(data);
				name = d.getString("name");
				message = d.getString("message");
				myHandler.sendEmptyMessage(0);
			}
			catch(JSONException e)
			{
				Log.e("JSON","error " + e.toString());
			}
		}
	}
	
	public void addMessage(String name, String mess, String colour)
	{
		View chatMessage = inflater.inflate(R.layout.text, null);
		
		Date date = new Date();
		
		RelativeLayout back = (RelativeLayout)chatMessage.findViewById(R.id.back);
		back.setBackgroundColor(Color.parseColor(colour));
		
		TextView text = (TextView)chatMessage.findViewById(R.id.name);
		text.setText(name+":");
		
		int hour = date.getHours();
		String h = String.valueOf(hour);
		if(hour<10)
		{
			h = "0"+h;
		}
		
		int min = date.getMinutes();
		String m = String.valueOf(min);
		if(min<10)
		{
			m = "0"+m;
		}
		
		int sec = date.getSeconds();
		String s = String.valueOf(sec);
		if(sec<10)
		{
			s = "0"+s;
		}
		
		text = (TextView)chatMessage.findViewById(R.id.time);		
		text.setText(h+":"+m +":"+s);
		
		text = (TextView)chatMessage.findViewById(R.id.message);
		text.setText(mess);
		
		chatList.addView(chatMessage);
		chatMessage.startAnimation(a);
		
		chatScroll.post(new Runnable() 
		{ 
	        public void run() 
	        { 
	        	chatScroll.scrollTo(0, chatScroll.getBottom());
	        } 
		});
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(LOG_TAG, "onDestroy");
		mPusher.disconnect();
	}
}
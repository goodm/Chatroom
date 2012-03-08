package com.lukasz.chat;

import org.json.JSONObject;

import com.lukasz.chat.login.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class Main extends Activity 
{
	private String nick = "";
	
	//Chat View
	private LinearLayout chatList;
	private ScrollView chatScroll;
	private ScrollView touch;
	private View startView;
	private View chat;
	private LayoutInflater inflater;
	private EditText input;

	private ProgressBar loading;
	private boolean load = false;
	
	private MessageReceiver messager;

	private Animation a;
	private Animation b;
	
	private Animation chatOut;
	private Animation chatIn;
	
    private Chat app;

	private AlertDialog alert;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		dialogs();
		app = (Chat)getApplication();
		app.setUpPusher(this);
				
		if(app.getUser() == null)
		{
			Intent i = new Intent(getApplicationContext(), Login.class);
    		startActivity(i);
    		finish();
		}
		else
		{
			if(isOnline())
			{
				alert.show();
				nick = app.getUser();
				inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				chat = inflater.inflate(R.layout.chat, null);
		 		setContentView(chat);
		 		setUpChatViews();
				setUpAnimations();
				Start start = new Start();
		    	start.execute("...");
		 		messager = new MessageReceiver(Main.this,inflater,chatScroll,chatList);
			}
			else
			{
				finish();
			}
		}
	}

    private class Start extends AsyncTask<String, Void, String> 
    {
    	@Override
    	protected String doInBackground(String... urls) 
    	{
    		app.startChat();
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) 
    	{	    		
    		alert.cancel();
    	}
    }
	
	private void setUpAnimations() 
	{
		a = AnimationUtils.loadAnimation(this, R.anim.out);
		b = AnimationUtils.loadAnimation(this, R.anim.in);
		chatIn = AnimationUtils.loadAnimation(this, R.anim.chatin);
		chatOut = AnimationUtils.loadAnimation(this, R.anim.chatout);

		chatOut.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationEnd(Animation animation) 
			{

			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
		
		a.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationEnd(Animation animation) 
			{
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
	}

	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.send:
				String mess = input.getText().toString();
				if(mess.length()>0)
				{
					try 
					{
						String eventName = "client-new_message";
						String channelName = app.PRIVATE_CHANNEL;
						JSONObject eventData = new JSONObject("{name:"+ nick +",message:\""+mess+"\"}");
						app.pusher.sendEvent(eventName, eventData, channelName);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
					
					input.setText("");					
					messager.addMessage(nick, mess,"#FFFFFF");
				}
				break;
		}
	}
	

	
    public void onMessageReceive(String event,String data)
    {
    	if(messager != null)
    	{
    		messager.onMessageReceive(event, data);
    	}
    }
    
	private void setUpChatViews()
	{
		chatList = (LinearLayout)chat.findViewById(R.id.chatList);		
		chatScroll = (ScrollView)chat.findViewById(R.id.chatScroll);
		input = (EditText)chat.findViewById(R.id.text);
	}
	
	public void addView(View v)
	{
		chatList.addView(v);
		chatScroll.post(new Runnable() 
		{ 
	        public void run() 
	        { 
	        	chatScroll.scrollTo(0, chatScroll.getBottom());
	        } 
		});
	}
		
	public boolean isOnline() 
    {
        NetworkInfo info = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info==null || !info.isConnected()) 
        {
                return false;
        }
        if (info.isRoaming()) 
        {
                return true;
        }
        return true;
    }
	
	private void dialogs()
    {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Loading");
    	alert = builder.create();
    }
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		app.pusher.disconnect();
	}
}
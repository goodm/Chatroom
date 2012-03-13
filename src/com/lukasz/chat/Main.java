package com.lukasz.chat;

import org.json.JSONException;
import org.json.JSONObject;

import com.lukasz.chat.login.Login;
import com.lukasz.chat.surface.Panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class Main extends Activity 
{
	private String nick = "";
	
	//Chat View
	private LinearLayout chatList;
	private ScrollView chatScroll;
	private View chat;
	private LayoutInflater inflater;
	private EditText input;
	
	private MessageReceiver messager;

	private Animation a;
	
	private Animation chatOut;
	
    private Chat app;

	private AlertDialog alert;
	
	private Panel p;
	
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
						String channelName = Chat.PRIVATE_CHANNEL;
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
	
	public void addObject(float x, float y)
	{
		try 
		{
			String eventName = "client-new_object";
			String channelName = Chat.PRIVATE_CHANNEL;
			JSONObject eventData = new JSONObject("{x:"+ x +",y:"+y+"}");
			app.pusher.sendEvent(eventName, eventData, channelName);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public void sendMove(int index,int xpos, int ypos) 
	{
		try 
		{
			String eventName = "client-move_object";
			String channelName = Chat.PRIVATE_CHANNEL;
			JSONObject eventData = new JSONObject("{i:" + index + ",x:"+ xpos +",y:"+ypos+"}");
			app.pusher.sendEvent(eventName, eventData, channelName);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
    public void onMessageReceive(String event,String data)
    {
    	if(messager != null)
    	{
    		if(event.compareToIgnoreCase("new_message") == 0 || event.compareToIgnoreCase("client-new_message") == 0)
    		{
    			messager.onMessageReceive(data);
    		}
    		else if(event.compareToIgnoreCase("new_object") == 0 || event.compareToIgnoreCase("client-new_object") == 0)
    		{
    			p.addNew(data);
    		} 
    		else if(event.compareToIgnoreCase("move_object") == 0 || event.compareToIgnoreCase("client-move_object") == 0)
    		{
    			Log.d("RECEIVE", "MOVE");
    			p.moveObject(data);
    		}   
    	}
    }
    
	private void setUpChatViews()
	{
		chatList = (LinearLayout)chat.findViewById(R.id.chatList);		
		chatScroll = (ScrollView)chat.findViewById(R.id.chatScroll);
		input = (EditText)chat.findViewById(R.id.text);
		p = (Panel)findViewById(R.id.surface);
		p.setMain(Main.this);
	}
	
	public void addView(View v,String t)
	{
		p.addText(t);
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
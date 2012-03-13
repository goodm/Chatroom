package com.lukasz.chat;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MessageReceiver 
{
	private CustomHandler myHandler;
	private String message;
	private String name;
	private LayoutInflater inflater;
	private Animation a;
	
	private Main main;
	
	private ArrayList<View> messages;
	
	public MessageReceiver(Main ac, LayoutInflater i,ScrollView chatScroll,LinearLayout chatList)
	{
		this.inflater = i;
		this.main = ac;
		messages = new ArrayList();
		a = AnimationUtils.loadAnimation(ac.getBaseContext(), R.anim.in);
		myHandler = new CustomHandler();		
	}
	
	public void onMessageReceive(String data)
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
	
	class CustomHandler extends Handler
	{
	    @Override
	    public void handleMessage(Message msg) 
	    {
	        super.handleMessage(msg);
	        addMessage(name,message,"#efefef");
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
		
		main.addView(chatMessage,mess);
		messages.add(chatMessage);
		chatMessage.startAnimation(a);
	}
	
	public void setText(float s)
	{
		
		
		int len = messages.size();
		
		TextView t = null;
		
		for(int i = 0; i< len;i++)
		{
			t = (TextView)messages.get(i).findViewById(R.id.time);
			
			Float current = t.getTextSize();
			
			Float size = current + s;
			
			if(size <= 3)
			{
				size = 3.0f;
			}
			
			if(size >= 20)
			{
				size = 20.0f;
			}
			
			t.setTextSize(size);
			t = (TextView)messages.get(i).findViewById(R.id.message);
			t.setTextSize(size);
			t = (TextView)messages.get(i).findViewById(R.id.name);
			t.setTextSize(size);
		}
	}
}

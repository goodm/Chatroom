package com.lukasz.chat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

	//Views
	private RelativeLayout loginLayout;
	private EditText nickEdit;
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
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	
    private Chat app;
    
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		app = (Chat)getApplication();
		
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		chat = inflater.inflate(R.layout.chat, null);
		startView = inflater.inflate(R.layout.main, null);
 		setContentView(startView);

		
		setUpAnimations();
		setUpViews();
						
 		startView.startAnimation(b);
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
				chatList.setVisibility(View.INVISIBLE);
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
				setContentView(chat);
				chat.startAnimation(b);
				setUpChatViews();
				messager = new MessageReceiver(Main.this,inflater,chatScroll,chatList);
	    		Toast.makeText(Main.this,"Welcome " + nick + ".",Toast.LENGTH_LONG).show();
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
			case R.id.enter:
				nick = nickEdit.getText().toString();
				if(nick.length()>0)
				{
					if(load == false)
					{
						loading.setVisibility(View.VISIBLE);
						load = true;
						app.startChat();
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
		
		// Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() 
        {
            public boolean onTouch(View v, MotionEvent event) 
            {
                return gestureDetector.onTouchEvent(event);
            }
        };

        //touch.setOnTouchListener(gestureListener);
	}
	
	private void setUpViews()
	{
		loginLayout = (RelativeLayout)startView.findViewById(R.id.loginLayout);
		nickEdit = (EditText)startView.findViewById(R.id.nameEnter);	
		loading = (ProgressBar)startView.findViewById(R.id.loading);
		loading.setVisibility(View.INVISIBLE);
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
	
	class MyGestureDetector extends SimpleOnGestureListener 
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
        {
            try 
            {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
                {

                }  
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
                {

                }
            } 
            catch (Exception e) 
            {
                // nothing
            }
            return false;
        }
    }
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		app.pusher.disconnect();
	}
}
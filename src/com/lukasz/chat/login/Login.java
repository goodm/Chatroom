package com.lukasz.chat.login;

import com.lukasz.chat.Chat;
import com.lukasz.chat.Main;
import com.lukasz.chat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

public class Login extends Activity 
{
	private Chat app;
	private ProgressBar loading;
	private EditText name;
	private String nick;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loading = (ProgressBar)findViewById(R.id.loading);
		loading.setVisibility(View.INVISIBLE);
		name = (EditText)findViewById(R.id.nameEnter);
		app = (Chat)getApplication();
	}
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.enter:
				nick = name.getText().toString();
				if(nick != null)
				{
					
					loading.setVisibility(View.VISIBLE);
					app.setPrefs(nick);
					Start start = new Start();
			    	start.execute("...");
				}
				break;
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
			Intent i = new Intent(getApplicationContext(), Main.class);
    		startActivity(i);
    	}
    }
}

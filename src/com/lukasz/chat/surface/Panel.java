package com.lukasz.chat.surface;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Panel extends GLSurfaceView implements SurfaceHolder.Callback
{
	public static float mWidth;
	public static float mHeight;
	private ViewThread mThread;
	private ArrayList<Element>mElements = new ArrayList<Element>();
	
	
	private boolean touched = false;
	
	private int s = 0;
	private boolean adding = true;
	private int xpos;
	private int ypos;
		
	private boolean grabed = false;
	
	private boolean type = false;
	
	private boolean moving = false;
	
	public Panel(Context context,AttributeSet attrs)
	{
		super(context,attrs);
		getHolder().addCallback(this);
		mThread = new ViewThread(this);
	}
		
	public void doDraw(long elapse, Canvas canvas)
	{
		canvas.drawColor(Color.BLACK);
		synchronized(mElements)
		{
			for(Element element : mElements)
			{
				element.doDraw(canvas);
			}
		}
	}

	public void moveAll(float x, float y, float z)
	{
		for(Element element : mElements)
		{
			element.sensMove(x, y, z);
		}
	}
	
	public void animate(long elapsedTime)
	{
		synchronized(mElements)
		{
			for(int i=0;i<mElements.size();i++)
			{
				if(moving == false)
				{
					mElements.get(i).pos(xpos,ypos);
					mElements.get(i).animate(elapsedTime);
				}
				else
				{
					mElements.get(i).pos(xpos,ypos);
					mElements.get(i).moveAll(elapsedTime);
				}
				
				/*
				for(int j=0;j<mElements.size();j++)
				{
					if(i!=j)
					{
						if((mElements.get(i).getXpos() < mElements.get(j).getXpos() && mElements.get(i).getXpos() + mElements.get(i).getW() > mElements.get(j).getXpos()) || ((mElements.get(j).getXpos() < mElements.get(i).getXpos() && mElements.get(j).getXpos() + mElements.get(j).getW() > mElements.get(i).getXpos())))
						{
							if((mElements.get(i).getYpos() > mElements.get(j).getYpos() && mElements.get(j).getYpos() + mElements.get(j).getH()> mElements.get(i).getYpos()) || (mElements.get(j).getYpos() > mElements.get(i).getYpos() && mElements.get(i).getYpos() + mElements.get(i).getH()> mElements.get(j).getYpos()))
							{
								mElements.get(i).t = true;
							}
							else
							{
								mElements.get(i).t = false;	
							}
						}
						else
						{
							mElements.get(i).t = false;
						}
					}
				}
				*/
			}	
			
			
		}
	}
	
	public void change()
	{
		if(type == true)
		{
			type = false;
		}
		else
		{
			type = true;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		synchronized(mElements)
		{
			int action = event.getAction();
				xpos = (int) event.getX();
				ypos = (int) event.getY();
				
				switch(action)
				{
				   case MotionEvent.ACTION_DOWN:		
					   for(Element element : mElements)
					   {
						   if(grabed == false)
						   {
							   if((int) event.getX() < element.getXpos() + element.getW() && (int) event.getX() > element.getXpos())
							   {
								   if((int) event.getY() < element.getYpos() + element.getH() && (int) event.getY() > element.getYpos())
								   {										
									   element.move = true;									   									   
									   grabed = true;
									   moving = false;
								   }
							   }
							   else
							   {
								   if(moving == false)
								   {
									   element.pos(xpos,ypos);
									   element.countDif();
								   }								   								   
							   }
							   
							   
						   }
					   }
					   
					   if(grabed == false)
					   {
						   moving = true;
					   }
					   
				   break;
				   case MotionEvent.ACTION_UP:
					   grabed = false;
					   moving = false;
				   	   for(Element element : mElements)
				   	   {				   		  
				   		   element.move = false; 
				   	   }
				   break;
				}				
			}		
		return true;
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) 
	{
		mWidth = width;
		mHeight = height;					
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		setEGLContextClientVersion(2);
		Random rand = new Random();
		
		mElements.add(new Element(getResources(), rand.nextInt(300), rand.nextInt(400)));
		mElements.add(new Element(getResources(), rand.nextInt(300), rand.nextInt(400)));
		mElements.add(new Element(getResources(), rand.nextInt(300), rand.nextInt(400)));
		mElements.add(new Element(getResources(), rand.nextInt(300), rand.nextInt(400)));
		mElements.add(new Element(getResources(), rand.nextInt(300), rand.nextInt(400)));
		
		if(!mThread.isAlive())
		{
			mThread = new ViewThread(this);
			mThread.setRunning(true);
			mThread.start();						
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		if(mThread.isAlive())
		{
			mThread.setRunning(false);
		}		
	}
}

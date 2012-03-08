package com.lukasz.chat.surface;

import java.util.ArrayList;
import java.util.Random;

import com.lukasz.chat.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

public class Element 
{
	private int mX;
	private int mY;
	
	private int xtemp;
	private int ytemp;
	
	private Bitmap mBitmap;
	
	private int speed=2;
	
	public boolean move = false;
	public boolean go = false;
	
	private Random rand;
	
	private Resources res;
	
	private int xpos;
	private int ypos;
	
	private float z;
	private float y;
	
	public boolean t = false;
	
	public Element(Resources r, int x, int y)
	{
		res = r;
		rand = new Random();
		mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
		mX = x - mBitmap.getWidth()/2;
		mY = y - mBitmap.getHeight()/2;
	}
	
	public void animate(long elapsedTime)
	{			
		if(move == true)
		{
			mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
			mX -= (mX - (xpos - mBitmap.getWidth()/2))/speed;
			mY -= (mY - (ypos - mBitmap.getHeight()/2))/speed;			
		}
		else
		{
			if(t == false)
			{
				mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
				Random rand = new Random();
				mX -= (int) z/(speed+rand.nextInt(10));
				mY -= (int) (y+45)/(speed+rand.nextInt(10));
			}
		}
		checkBordes();
	}
	
	public void moveAll(long elapsedTime)
	{					
		mX = xpos - xtemp;
		mY = ypos - ytemp;			
		
		checkBordes();
	}
	
	public void sensMove(float x, float y, float z)
	{
		this.z = z;
		this.y = y;
	}
	
	public void countDif()
	{
		xtemp = xpos - mX;
		ytemp = ypos - mY;
		Log.i("COUNT", String.valueOf(xtemp) + " " + String.valueOf(xtemp));
	}
	
	public void pos(int pX,int pY)
	{
		xpos = pX;
		ypos = pY;
	}
	
	public void goTO(int pX,int pY)
	{
		xpos = pX;
		ypos = pY;
		move = true;		
	}
	
	public int getXpos()
	{
		return mX;
	}
	
	public int getYpos()
	{
		return mY;
	}
	
	public int getW()
	{
		return mBitmap.getWidth();
	}
	
	public int getH()
	{
		return mBitmap.getHeight();
	}
		
	private void checkBordes()
	{
		if(mX <= 0)
		{
			mX = 0;
		}
		else if(mX + mBitmap.getWidth() >= Panel.mWidth)
		{
			mX = (int) Panel.mWidth - mBitmap.getWidth();
		}
		
		if(mY <= 0)
		{
			mY = 0;
		}
		else if(mY + mBitmap.getHeight() >= Panel.mHeight)
		{
			mY = (int) Panel.mHeight - mBitmap.getHeight();
		}
	}
	
	public void doDraw(Canvas canvas)
	{
		canvas.drawBitmap(mBitmap, mX, mY, null);
	}
}

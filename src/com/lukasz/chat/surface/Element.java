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
	
	private Bitmap mBitmap;
	
	private int speed=2;
	
	public boolean move = false;
	public boolean go = false;

	
	private Resources res;
	
	private int xpos;
	private int ypos;
	
	public boolean t = false;
	private boolean pMove = false;
	private int tempXpos;
	private int tempYpos;
	
	public Element(Resources r, int x, int y)
	{
		res = r;
		mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
		mX = x - mBitmap.getWidth()/2;
		mY = y - mBitmap.getHeight()/2;
	}
	
	public void animate(long elapsedTime)
	{			
		if(move == true)
		{
			pMove = false;
			mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
			mX -= (mX - (xpos - mBitmap.getWidth()/2))/speed;
			mY -= (mY - (ypos - mBitmap.getHeight()/2))/speed;			
		}
		
		if(pMove  == true && move == false)
		{
			mBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
			mX -= (mX - (tempXpos - mBitmap.getWidth()/2))/speed;
			mY -= (mY - (tempYpos - mBitmap.getHeight()/2))/speed;	
			
			if(mX < tempXpos+10 && mX > tempXpos-10 && mY < tempYpos+10 && mY > tempYpos-10)
			{
				pMove = false;
			}
		}
		
		checkBordes();
	}
	
	public void pos(int pX,int pY)
	{
		xpos = pX;
		ypos = pY;
	}
	
	public void goTO(int pX,int pY)
	{
		tempXpos = pX;
		tempYpos = pY;
		pMove = true;		
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

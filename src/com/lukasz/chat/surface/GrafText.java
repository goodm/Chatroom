package com.lukasz.chat.surface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class GrafText 
{
	private String text;
	private int x;
	private int y;
	private Paint paint;
	
	public GrafText(String mt, int mX, int mY)
	{
		this.text = mt;
		this.x = mX;
		this.y = mY;
		createText();
	}
	
	private void createText()
	{
		paint = new Paint();      
		paint.setStyle(Style.FILL);  
		paint.setColor(Color.WHITE);
		paint.setTextSize(15);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
	}
	
	public void doDraw(Canvas canvas)
	{
		canvas.drawText(text, x, y, paint);
	}
}

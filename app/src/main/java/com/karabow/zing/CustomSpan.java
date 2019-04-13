package com.karabow.zing;

import com.karabow.zing.R.style;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class CustomSpan extends ReplacementSpan
{

	private Rect rect;
	
	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint p)
	{
		rect = new Rect();
		canvas.getClipBounds(rect);
		p.setTextAlign(Paint.Align.RIGHT);
		p.setTextSize(getTextSize());
		p.setColor(ChatList.getCurrentActivity().getResources().getColor(R.color.battleship_grey));
		canvas.drawText(text, start, end, rect.right, rect.bottom, p);
	}

	@Override
	public int getSize(Paint arg0, CharSequence arg1, int arg2, int arg3,FontMetricsInt arg4) 
	{
		// TODO Auto-generated method stub
		arg4 =arg0.getFontMetricsInt();
		arg4.descent =2;
		return 15;
	}
	
	
	
	public Rect getRect()
	{
		return rect;
	}
	
	private int getTextSize()
	{
		final float GESTURE_THRESHOLD_DIP = 12.0F;
		float scale = ChatList.getCurrentActivity().getResources().getDisplayMetrics().density;
		int size = (int)(GESTURE_THRESHOLD_DIP*scale+0.5f);
		return size;
		
	}

}

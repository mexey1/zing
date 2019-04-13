package com.karabow.zing;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

public class PrepareBitmap 
{
	public static Bitmap resizeImage(Resources res, int id, int requiredWidth, int requiredHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		//Setting inJustDecodeBounds to true would cause the dimensions of the image to be gotten without actually loading it to memory
        options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, id, options);
        //just to make sure width and height passed always have values
        if(requiredWidth==0 || requiredWidth<0)
            requiredWidth =100;
        if(requiredHeight == 0 || requiredHeight<0)
            requiredHeight = 100;
		Log.d("Background Bitmap", "Out Widhth"+options.outWidth+ "Height "+options.outHeight+"reqW "+requiredWidth);
		int sampleSize= computeSampleSize(options,requiredWidth,requiredHeight);
		//a new instance of BitmapFactory.Options is created and the sample size is set
		//options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		options.inJustDecodeBounds = false;
		//The returned bitmap would be scaled to something small to fit in our imageview
		try
		{
			Bitmap bmp = BitmapFactory.decodeResource(res, id, options);
			Log.d("Background Bitmap", "Sample Size"+sampleSize+ "Width "+bmp.getWidth()+"Height "+bmp.getHeight());
			return bmp;
		}
		catch(OutOfMemoryError e)
		{
			e.printStackTrace();
			sampleSize= computeSampleSize(options,500,500);
			//a new instance of BitmapFactory.Options is created and the sample size is set
			options = new BitmapFactory.Options();
			options.inSampleSize = sampleSize;
			Bitmap bmp = BitmapFactory.decodeResource(res, id, options);
			return bmp;
			
		}
		
	}
	
	/**
	 * A method to resize an image when the file path is known
	 * @param imagePath: path to the image.
	 * @param requiredWidth: width to which to resize the image.
	 * @param requiredHeight: height to which to resize the image.
	 * @return
	 */
	//Overloaded method to resize images when filepath is known
	public static Bitmap resizeImage(String imagePath,int requiredWidth, int requiredHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		int sampleSize=0;
		if(requiredHeight == 0 || requiredWidth == 0)
		{
			if(options.outWidth > 400 || options.outHeight >400)
			{
				sampleSize = computeSampleSize(options,400,400);
			}
			else
				sampleSize = computeSampleSize(options,requiredWidth,requiredHeight);
		}
		else
		{
			sampleSize = computeSampleSize(options,requiredWidth,requiredHeight);
		}
		
		
		options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
		return bmp;
	}
	
	/**
	 * A private method to compute the size as to which to sample the image. This method is called by 
	 * any of the resize image method
	 */
	private static int computeSampleSize(BitmapFactory.Options opt, int width, int height)
	{
		synchronized(PrepareBitmap.class)
		{
			int sampleSize = 1;
			int oldWidth = opt.outWidth;
			int oldHeight = opt.outHeight;
			
			int x =(int)(oldWidth/2);
			int y = (int)(oldHeight/2);
			Log.d("Sizes", "old width"+oldWidth +"nwe x"+x+"Old height"+height+"new height "+y);
			Log.d("Sample Sizes", "old width"+oldWidth +"nwe x"+x+"Old height"+height+"new height "+y);
			int count =0;
			/*final int w = 333;
			final int h = 333;*/
			
			/*if(oldWidth <= 400 && oldHeight <=400)
				return 1;
			else if (oldWidth <= 800 && oldHeight <=800)
				return 2;
			else if(oldWidth <= 1200 && oldHeight <=1200)
				return 4;
			else if (oldWidth <= 1600 && oldHeight <=1600)
				return 8;
			else
				return 16;*/
			
				if(x >= width || y >= height)
				{
					while(x >= width || y >= height)
					{
						sampleSize*=2;
						x/=sampleSize;
						y/=sampleSize;
						//increase the sampleSize by a factor of 2
						//count++;
					}
				}
			
			return sampleSize;
		}
		
	}
	
	
	
	public static Bitmap resizeImage(byte[] imgBytes,int requiredWidth, int requiredHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length,options);
        //just to make sure width and height passed always have values
        if(requiredWidth==0 || requiredWidth<0)
            requiredWidth =100;
        if(requiredHeight == 0 || requiredHeight<0)
            requiredHeight = 100;
		int sampleSize = computeSampleSize(options,requiredWidth,requiredHeight);
		options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		
		Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length,options);
		return bmp;
	}
	/**
	 * A method to make the edges of the image round.
	 * @param bmp: bitmap to draw
	 * @param radius: radius of the rounded corner
	 * @return: a rectangular image with rounded corners
	 */
	public static Bitmap drawRoundedRect(Bitmap bmp, float radius)
	{
		Bitmap roundBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(roundBmp);
		Rect rect = new Rect();
		rect.set(0, 0, bmp.getWidth(), bmp.getHeight());
		RectF rectf = new RectF(rect);
		Paint paint = new Paint();
		int color = 0xff424242;
		paint.setColor(color);
		canvas.drawRoundRect(rectf,radius,radius,paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		paint.setAntiAlias(true);
		paint.setDither(true);
		
		canvas.drawBitmap(bmp, rect, rect, paint);
		bmp.recycle();
	    return roundBmp;
	}
	
	/**
	 * 
	 * @param pix: byte array of the profile picture
	 * @return: profile picture as bitmap
	 */
	public static Bitmap decodeProfilePix(byte[] pix)
	{
		Bitmap bmp = BitmapFactory.decodeByteArray(pix, 0, pix.length);
		return bmp;
	}
	
	
	/**
	 * THIS METHOD HELPS RESIZE THE PROFILE PIX THAT WOULD BE SHOWN IN THE CHAT WINDOW
	 * IT RESIZES AND MAKES THE IMAGE HAVE ROUNDED EDGES
	 * @param pix: byte array of the icture
	 * @return image as a Drawable
	 */
	public static Drawable setChatWindowProfilePix(byte[] pix,int width, int height)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
		{
			cont  = ChatList.getCurrentActivity();
		}
		else if(ChatWindow.getActivity() != null)
		{
			cont = ChatWindow.getActivity();
		}
		
		if(pix.length == 0)
		{
			Drawable drawable = cont.getResources().getDrawable(R.drawable.user);
			return drawable;
		}
		
		else
		{
			Bitmap bmp = resizeImage(pix, width,height);
			Bitmap bmps = drawRoundedRect(bmp,10);
			bmp.recycle();
			return  new BitmapDrawable(cont.getResources(),bmps);
		}
	}
	
	/**
	 * this method helps to draw a circular version of the bitmap that is passed into it...
	 * 
	 * @param res: reference to a resource object
	 * @param resId: ID of the resource to be drawn
	 * @param color: color of the background bitmap
	 * @param transparency: shade to draw.
	 * @return circular bitmap
	 */
	public static Bitmap drawCircularColoredImage(Resources res, int resId, int color,int transparency)
	{
		Bitmap bmp = BitmapFactory.decodeResource(res, resId);
		Bitmap bmps =  Bitmap.createBitmap(bmp.getWidth()+40,bmp.getHeight()+40,Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bmps);
		
		Paint paint = new Paint();
		//int col = 0xff870808;
		paint.setColor(color);
		paint.setAntiAlias(true);
		//canvas.drawColor(color);
		//float radius = (float)
		canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,bmps.getHeight()/2 , paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		canvas.drawBitmap(bmp, 20, 20, paint);
		if(transparency > 0)
		{
			paint.setColor(res.getColor(R.color.black));
			paint.setAlpha(transparency);
			canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,bmps.getHeight()/2 , paint);
		}
		bmp.recycle();
		return bmps;
	}
	
	/**
	 * this method helps to draw a circular version of the bitmap that is passed into it...
	 * 
	 * @param bmp: bitmap to draw
	 * @param width: width of bitmap
	 * @param height: height
	 * @return circular bitmap
	 */
	public static Bitmap drawCircularImage(Bitmap bmp, int width, int height)
	{
		//Bitmap bmp2 = BitmapFactory.decodeResource(ChatWindow.getActivity().getResources(), R.drawable.orca_chat_head_overlay);
		Bitmap bmps =  Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),Config.ARGB_8888);
		BitmapShader bmpsh = new BitmapShader(bmp,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
		Canvas canvas = new Canvas(bmps);
		Paint paint = new Paint();
		paint.setShader(bmpsh);
		int col = 0xff870808;
		
		paint.setAntiAlias(true);
		paint.setColor(col);
		//paint.setStyle(Paint.Style.STROKE);
		//canvas.drawBitmap(bmp, 0, 0, paint);
		//canvas.drawColor(color);
		//float radius = (float)
		//paint.setColor(ChatWindow.getActivity().getResources().getColor(android.R.color.transparent));

		//paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		//canvas.drawBitmap(bmp2,0,0,paint);
		int radius = Math.min(width, height)/2;
		int min = Math.min(bmp.getWidth(), bmp.getHeight());
		if(radius > min/2)
		{
			canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,min/2, paint);
		}
		else
		{
			canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,radius, paint);
		}
		//Toast.makeText(ChatWindow.getActivity(), Integer.toString(bmp.getHeight())+""+Integer.toString(bmp.getWidth()), Toast.LENGTH_LONG).show();
		
		bmp.recycle();
		return bmps;
	}
	
	/**
	 * this method is not used in the app...don't know why i wrote it in the first place
	 * @param res: reference to a resource object
	 * @param resId:resource to be drawn in circular form
	 * @param color: color to draw
	 * @param transparency: transparency of the overlaying bitmap
	 * @return
	 */
	public static Bitmap drawCircularsColoredImage(Resources res, int resId, int color,int transparency)
	{
		Bitmap bmp = BitmapFactory.decodeResource(res, resId);
		Bitmap bmps =  Bitmap.createBitmap(bmp.getWidth()+40,bmp.getHeight()+40,Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bmps);
		
		Paint paint = new Paint();
		//int col = 0xff870808;
		paint.setColor(color);
		paint.setAntiAlias(true);
		//canvas.drawColor(color);
		//float radius = (float)
		canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,bmps.getHeight()/2 , paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		canvas.drawBitmap(bmp, 20, 24, paint);
		if(transparency > 0)
		{
			paint.setColor(res.getColor(R.color.black));
			paint.setAlpha(transparency);
			canvas.drawCircle(bmps.getWidth()/2, bmps.getHeight()/2,bmps.getHeight()/2 , paint);
		}
		bmp.recycle();
		return bmps;
	}
	
	/**
	 * THIS METHOD HELPS RESIZE THE BACKGROUND DRAWABLE FOR THE CHAT WINDOW TO
	 * AVOID OUT OF MEMORY EXCEPTION
	 * @param pix: byte array of the Picture
	 * @return image as a Drawable
	 */
	public static Drawable setChatWindowBackground(int resId,int width, int height)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
		{
			cont  = ChatList.getCurrentActivity();
		}
		else if(ChatWindow.getActivity() != null)
		{
			cont = ChatWindow.getActivity();
		}
        //just to make sure width and height passed always have values
        if(width==0 || width<0)
            width =100;
        if(height == 0 || height<0)
            height = 100;
		Log.d("First Call", "Width "+width+"Height "+height);
		Bitmap bmp = resizeImage(cont.getResources(),resId, width,height);
		return  new BitmapDrawable(cont.getResources(),bmp);
		
	}

	public static Bitmap drawShadedBackground(Bitmap bmp)
	{
		// TODO Auto-generated method stub
		
		Bitmap bmps =  Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bmps);
		
		Paint paint = new Paint();
		int col = 0x000000;
		
		paint.setAntiAlias(true);
		//paint.setAlpha(200);
		//canvas.drawColor(color);
		//float radius = (float)
		//.drawRect(0,0,8,9,paint);
		
		canvas.drawBitmap(bmp, 0, 0, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		paint.setColor(col);
		paint.setAlpha(51);
		canvas.drawRect(0, 0, bmp.getWidth(), bmp.getHeight(), paint);
		
		
		
		
		bmp.recycle();
		bmp = null;
		return bmps;
	}
	
	public static int[] getImageSize(String imagePath)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		int size[] = new int[2];
		size[0] = options.outWidth;
		size[1] = options.outHeight;
		return size;
	}
	
	public static Bitmap decodeRegion(String path,int width, int height)
	{
		try
		{
			BitmapRegionDecoder brd = BitmapRegionDecoder.newInstance(path, false);
			Log.d("data", "Width"+brd.getWidth()+"Height"+brd.getHeight());
            //just to make sure width and height passed always have values
            if(width==0 || width<0)
                width =100;
            if(height == 0 || height<0)
                height = 100;
			Rect rect = new Rect();
			rect.set(0, 0, height, width);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize =2;
			return brd.decodeRegion(rect,opts);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static Bitmap displayImageWithoutScaling(String path,int width,int height)
	{
		
		Bitmap bmp1 =  BitmapFactory.decodeFile(path, null);
        //just to make sure width and height passed always have values
        if(width==0 || width<0)
          width =100;
        if(height == 0 || height<0)
            height = 100;
        Log.d("Width et Height","Width"+Integer.toString(width)+"Height"+Integer.toString(height));
		Bitmap bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp2);
		Rect rect = new Rect(0,0,width,height);
		Paint paint = new Paint();
		canvas.drawBitmap(bmp1, null, rect, paint);
		bmp1.recycle();
		bmp1 = null;
		return bmp2;
	}
}

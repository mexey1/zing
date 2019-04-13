package com.karabow.zing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import github.ankushsachdeva.emojicon.EmojiconTextView;




public class Profile extends Fragment
{
	private ImageView img_view;
	private Fragment frag;
	private int count;
	private View view;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(container == null)
		{
			Log.d("This Is Null", "Container");
		
			return null;
		}
		Log.d("This Is Null", "Containerfggf");
		view =  inflater.inflate(R.layout.profile, container, false);
		frag  = this;
		
		img_view = (ImageView)view.findViewById(R.id.profilePixImageView);
		final byte[] img_bytes = DatabaseHelper.getProfilePix(getActivity());
		if(img_bytes != null && img_bytes.length >0)
		{
			img_view.post(new Runnable()
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					Bitmap bmp = PrepareBitmap.resizeImage(img_bytes, 333, 333);
					Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp,7f);
					Log.d("IS BITMAP NULL", Boolean.toString(bmp instanceof Bitmap));
					if(bmp != null)
					{
						img_view.setImageBitmap(bmps);
						try
						{
							File dir = android.os.Environment.getExternalStorageDirectory();
							String path = dir.getAbsolutePath()+"/Zing/DP/";
							File file = new File(path);
							if(!file.exists())
							{
								file.mkdirs();
							}
							file = new File(file.getAbsolutePath()+"/hello.jpg");
							file.createNewFile();
							//ByteArrayOutputStream baos = new ByteArrayOutputStream();
							FileOutputStream fos = new FileOutputStream(file);
							bmps.compress(CompressFormat.JPEG, 100, fos);
							fos.close();
							//byte buff [] = baos.toByteArray();
							
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
			});
			//Log.d("PROFILE PICTURE", img_string);
				
		}
		
		
		
		final ImageView remove_pix = (ImageView)view.findViewById(R.id.remove_pix);
		final ImageView change_pix = (ImageView)view.findViewById(R.id.change_pix);
		
		remove_pix.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					remove_pix.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					remove_pix.setBackgroundColor(0);
					
				}
				return false;
			}
			
		});
		remove_pix.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				if(img_bytes.length > 0)
				{
					BitmapDrawable bmpd = (BitmapDrawable)img_view.getDrawable();
					Bitmap bmp = bmpd.getBitmap();
					bmp.recycle();
					bmp = null;
					img_view.setImageDrawable(getResources().getDrawable(R.drawable.user));
					DatabaseHelper.updateProfilePix(new byte[0], "0");
				}
			}
			
		});
		
		change_pix.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					change_pix.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					change_pix.setBackgroundColor(0);
					
				}
				return false;
			}
			
		});
		change_pix.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Intent pick_pix = new Intent();
				pick_pix.setAction(Intent.ACTION_PICK);
				pick_pix.setType("image/*");
				frag.startActivityForResult(pick_pix, 1);
			}
			
		});
		
		final EmojiconTextView zing_id = (EmojiconTextView)view.findViewById(R.id.zingIdEditText);
		zing_id.setText(DatabaseHelper.getZingID(getActivity()));
		final ImageView smiley = (ImageView)view.findViewById(R.id.edit_id);
		smiley.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					smiley.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					smiley.setBackgroundColor(0);
					
				}
				return false;
			}
		});
		
		smiley.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Intent zing_edit = new Intent();
				zing_edit.setClass(ChatList.getCurrentActivity(), ZingIdEditActivity.class);
				frag.startActivity(zing_edit);
			}
			
		});
		
		
		
		
		
		
		return view;
	}
	
	@Override
	public void onActivityResult(int requestcode, int resultcode, Intent data)
	{
		if(resultcode == Activity.RESULT_OK)
		{
			Uri image_uri = data.getData();
			final String path = getImagePath(image_uri);
			/*
			 * for now, we just hard coded the image width and height
			 */
			img_view.post(new Runnable()
			{

				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					Bitmap bmp = null;
					int [] size = PrepareBitmap.getImageSize(path);
					if(size[0]>640 || size[1]>640)
						bmp = PrepareBitmap.resizeImage(path, size[0], size[1]);//PrepareBitmap.decodeRegion(path, 640, 640);
					else
						bmp = PrepareBitmap.resizeImage(path, 640, 640);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bmp.compress(CompressFormat.JPEG, 100, baos);
					DatabaseHelper.updateProfilePix(baos.toByteArray(),Long.toString(Time.getTimeStamp().getTime()));
					bmp.recycle();
					bmp = null;
					//Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp,7f);
					Log.d("IS BITMAP NULL", Boolean.toString(bmp instanceof Bitmap));
					
						img_view.post(new Runnable()
						{

							@Override
							public void run() 
							{
								// TODO Auto-generated method stub
								Bitmap bmp = PrepareBitmap.resizeImage(path, img_view.getWidth(), img_view.getHeight());
								Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp,4f);
								bmp.recycle();
								bmp = null;
								if(bmps != null)
								   img_view.setImageBitmap(bmps);
							}
							
						});
				}
			});
			/*Bitmap bmp = PrepareBitmap.resizeImage(path, 333, 333);
			Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp, 7f);
			img_view.setImageBitmap(bmps);*/
			
			
		}
	}
	
	private String getImagePath(Uri uri)
	{
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = frag.getActivity().managedQuery(uri,proj,null,null,null);
		
		int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		if(column_index == -1)
			return "invalid_column";
		else
		{
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		
	}
	
	
	
	
	

}

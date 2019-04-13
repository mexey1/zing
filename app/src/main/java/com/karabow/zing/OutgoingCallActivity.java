package com.karabow.zing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import github.ankushsachdeva.emojicon.EmojiconTextView;

public class OutgoingCallActivity extends Activity 
{
	private static String mac_address, name;
	private static Activity act;
	private Bitmap bmp;
	/*public OutgoingCallActivity(String mac_address)
	{
		this.mac_address = mac_address;
	}*/
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outgoing_phone_call);
		act = this;
		this.getActionBar().hide();
		/*
		 * SET VARIOUS ICON IMAGES
		 */
		
		ImageView call_img = (ImageView)findViewById(R.id.endIcon);
		final Bitmap bmp2 = PrepareBitmap.drawCircularColoredImage(this.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0);
		call_img.setImageBitmap(bmp2);
		
		EmojiconTextView names = (EmojiconTextView)act.findViewById(R.id.name);
		names.setText(name);
		final ImageView pro_pix = (ImageView)findViewById(R.id.profilePix);
		byte[] pixBytes = DatabaseHelper.getZIngerProfilePix(mac_address);
		if(pixBytes.length > 0)
		{
			 bmp = PrepareBitmap.resizeImage(pixBytes, 300,300);
		}
		else
		{
			Bitmap bmp1 = PrepareBitmap.resizeImage(act.getResources(), R.drawable.user, 150, 150);
			bmp = PrepareBitmap.drawCircularImage(bmp1, 150, 150);
			
			//bmp1.recycle();
		}
			
		
		pro_pix.post(new Runnable()
		{
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				pro_pix.setImageBitmap(PrepareBitmap.drawCircularImage(bmp,300,300));
			}
			
		});
		
		call_img.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(null);
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),51));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(null);
					//Toast.makeText(act, "up", Toast.LENGTH_LONG).show();
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0));
				}
				return false;
			}
			
		});
		
		call_img.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				
				//FrameLayout fl = (FrameLayout)act.findViewById(R.id.incoming_call_layout);
				//fl.setEnabled(false);
				
				OutgoingCallAsyncTask.finish(true);
				OutgoingCallAsyncTask.cancelCall();
				/*v.postDelayed(new Runnable()
				{

					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						act.finish();
					}
					
				}, 300);*/
				
			}
			
		});
		
		OutgoingCallAsyncTask ocat = new OutgoingCallAsyncTask();
		ocat.execute(DatabaseHelper.getIpAddress(DatabaseHelper.getId(mac_address)));
		
	}
	
	public static void setMacAddress(String mac)
	{
		mac_address = mac;
	}

    public static String getMacAddress()
    {
        return mac_address;
    }
	
	@Override 
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
		}
	}

	public static void setZingId(String zing_id) 
	{
		// TODO Auto-generated method stub
		name = zing_id;
	}
	
	

}

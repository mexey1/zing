package com.karabow.zing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import github.ankushsachdeva.emojicon.EmojiconTextView;

public class ProfileView extends Activity
{
	private Activity act;
	private String mac ;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		act = this;
		setContentView(R.layout.profile_view);
		act.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		mac = this.getIntent().getStringExtra("mac_address");
		final ImageView pro_pix = (ImageView)act.findViewById(R.id.pix);
		if(mac != null)
		{
			byte[] pix = DatabaseHelper.getZIngerProfilePix(mac);
			if(pix.length > 0)
			{
				int width = (int)(100*act.getResources().getDisplayMetrics().density);
				Bitmap bmp = PrepareBitmap.resizeImage(pix, width,width);
				pro_pix.setImageBitmap(bmp);
			}
			else
				pro_pix.setImageResource(R.drawable.user);
		}
		
		pro_pix.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) 
			{
				// TODO Auto-generated method stub
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					pro_pix.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					pro_pix.setBackgroundColor(0);
				}
				return false;
			}
			
		});
		
		final String details [] = DatabaseHelper.getZingStatus(mac);
		EmojiconTextView name  = (EmojiconTextView)act.findViewById(R.id.name);
		name.setText(details[0]);
		
		EmojiconTextView status  = (EmojiconTextView)act.findViewById(R.id.status);
		status.setText(details[1]);
		
		final ImageView call_icon = (ImageView)act.findViewById(R.id.call_icon);
		call_icon.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					call_icon.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					call_icon.setBackgroundColor(0);
					OutgoingCallActivity.setMacAddress(mac);
					OutgoingCallActivity.setZingId(details[0]);
					Intent call_activity_intent = new Intent(act,OutgoingCallActivity.class);
					act.startActivity(call_activity_intent);
				}
				return true;	
			}
			
		});
		
		final ImageView text_icon = (ImageView)act.findViewById(R.id.text_icon);
		text_icon.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					text_icon.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					text_icon.setBackgroundColor(0);
					act.finish();
				}
				return true;	
			}
			
		});
		
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
		}
	}
	

}

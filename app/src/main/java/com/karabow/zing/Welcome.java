package com.karabow.zing;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Welcome extends Activity
{
	private Activity act;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		this.overridePendingTransition(R.anim.animation_enter,R.anim.animation_leave);
		act = this;
		act.getActionBar().hide();
		ImageView img = (ImageView)this.findViewById(R.id.imageView1);
		img.postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				Intent chat_list_intent = new Intent(act, ChatList.class);
				act.startActivity(chat_list_intent);
				act.finish();
			}
			
		}, 4000);
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

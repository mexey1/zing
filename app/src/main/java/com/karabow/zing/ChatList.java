package com.karabow.zing;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashMap;


public class ChatList extends FragmentActivity
{
	private static Context  cont;
    private static Activity act;
	private ChatListAdapter adapter;
	private static MediaPlayer mp, mp1, mp2;
	private static Typeface type;
	private static HashMap<String,AvailabilityTimer>hashmap;
	private ViewPager vpager;

    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter,R.anim.animation_leave);
		cont = this;
        act = this;
		//this.getActionBar().hide();
		this.setContentView(R.layout.chat_list);

		this.getActionBar().hide();
		vpager = (ViewPager)findViewById(R.id.chatspager);
		final ChatListAdapter cla = new ChatListAdapter(this.getSupportFragmentManager());
		adapter = cla;
		vpager.setAdapter(cla);
		vpager.setOffscreenPageLimit(2);

        Intent service = new Intent(this,ZingService.class);
        this.startService(service);
		instantiateHashMap();
		/*
		 * ADD LISTENER TO THE VIEW PAGER TO DETECT SWIPES AND PAGE CHANGES
		 */
			
		vpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() 
		{
			
			@Override
			public void onPageSelected(int arg0) 
			{
				if(arg0 == 0)
				{
					selectProfile();
				}
				else if(arg0 == 1)
				{
					//zingers
					/*ProgressBar pb = (ProgressBar)Zingers.getZingersView().findViewById(R.id.scan_progress);
					TextView tv = (TextView)Zingers.getZingersView().findViewById(R.id.scan_text);
					
					pb.setVisibility(ProgressBar.GONE);
					tv.setVisibility(TextView.GONE);
					
					ScrollView sv = (ScrollView)Zingers.getZingersView().findViewById(R.id.scrollView1);
					sv.setVisibility(ScrollView.VISIBLE);*/
					selectZingers();
					
				}
				else if(arg0 == 2)
				{
					//chats
					
					selectChats();
					//cla.recreatePageThree(vpager);
					//vpager.addView(ChatListAdapter.recreatePageThree(), 2);
				}
				else
				{
					//settings
					selectSettings();
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) 
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{
				// TODO Auto-generated method stub
			}
		});
		
		/*
		 * INSTANTIATE THE SEGOEUI YYPEFACE
		 * THIS SHOULD BE DONE AT MOST ONCE
		 * MULTIPLE INSTANTIATIONS COULD MAKE THE APP CRASH
		 * THIS IS DUE TO A BUG IN THE TYPEFACE LOADING IN ANDROID
		 */
		
			type = Typeface.createFromAsset(this.getAssets(), "fonts/segoeui.ttf");
			
		
		RelativeLayout pro_lay = (RelativeLayout)findViewById(R.id.profile_click);
		pro_lay.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				vpager.setCurrentItem(0,true);
				selectProfile();
				return true;
			}
			
		});
		
		RelativeLayout zin_lay = (RelativeLayout)findViewById(R.id.zingers_click);
		zin_lay.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				vpager.setCurrentItem(1,true);
				selectZingers();
				return true;
			}
			
		});
		
		RelativeLayout cha_lay = (RelativeLayout)findViewById(R.id.chats_click);
		cha_lay.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				//cla.recreatePageThree(vpager);
				vpager.setCurrentItem(2,true);
				selectChats();
				return true;
			}
			
		});
		
		RelativeLayout set_lay = (RelativeLayout)findViewById(R.id.settings_click);
		set_lay.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				vpager.setCurrentItem(3,true);
				selectSettings();
				return true;
			}
			
		});
		
		ChatList.setCurrentActivity(this);
        Intent intent  = this.getIntent();
        String type = intent.getStringExtra("type");
        if(type != null)
        {
            if(type.equals("message"))
            {
                vpager.setCurrentItem(2);
                int id = DatabaseHelper.getId(intent.getStringExtra("macAddress"));
                String ip_address = DatabaseHelper.getIpAddress(id);
                Intent intent1 = new Intent(act, ChatWindow.class);
                String zing_id = DatabaseHelper.getZingerId(id);
                String mac = intent.getStringExtra("macAddress");
                //new Network().startBroadcast();
                //new ReceiveBroadcastMessage();
                ChatWindow.setProperties(id,ip_address,mac,zing_id);
                act.startActivity(intent1);
                DatabaseHelper.updateMessages(mac);
            }
            else if(type.equals("call"))
            {

                vpager.setCurrentItem(2);
                String mac = intent.getStringExtra("macAddress");
                if(mac == null)
                {
                    Intent intent1 = new Intent(act,IncomingCallActivity.class);
                    act.startActivity(intent1);
                }
                else
                {
                    int id = DatabaseHelper.getId(mac);
                    if(id > -1)
                    {

                        String ip_address = DatabaseHelper.getIpAddress(id);
                        Intent intent1 = new Intent(act, ChatWindow.class);
                        String zing_id = DatabaseHelper.getZingerId(id);
                        intent.getStringExtra("hello");
                        //new Network().startBroadcast();
                        //new ReceiveBroadcastMessage();
                        ChatWindow.setProperties(id,ip_address,mac,zing_id);
                        act.startActivity(intent1);
                        DatabaseHelper.updateMessages(mac);
                    }
                    else
                    {
                        Toast.makeText(act, "This zinger no longer exists", Toast.LENGTH_LONG).show();
                    }

                }



            }
        }
        else if(intent.getBooleanExtra("delete",true))
        {
            DatabaseHelper.dropZingers();
            DatabaseHelper.createZingers();
        }


	}
	
	/*public static Activity getActivity()
	{
		return act;
	}*/
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
			int item = vpager.getCurrentItem();
			if(item == 2)
			{
				Chats frag = (Chats)adapter.getItem(item);
				if(frag!= null)
					frag.setUserVisibleHint(true);
			}
			
		}
        else
            ChatList.setCurrentActivity(ZingService.getContextFromService());
		/*else if(!hasFocus)
		{
			ChatList.setCurrentActivity(null);
		}*/
	}
	private void selectProfile()
	{
		ImageView profile = (ImageView)act.findViewById(R.id.profile_view);
		profile.setImageDrawable(act.getResources().getDrawable(R.drawable.status_black));
		
		ImageView zingers = (ImageView)act.findViewById(R.id.zingers_view);
		zingers.setImageDrawable(act.getResources().getDrawable(R.drawable.online));
		
		ImageView chats = (ImageView)act.findViewById(R.id.chats_view);
		chats.setImageDrawable(act.getResources().getDrawable(R.drawable.chatbox));
		
		ImageView settings = (ImageView)act.findViewById(R.id.settings_view);
		settings.setImageDrawable(act.getResources().getDrawable(R.drawable.settings));
	}
	
	private void selectZingers()
	{
		ImageView profile = (ImageView)act.findViewById(R.id.profile_view);
		profile.setImageDrawable(act.getResources().getDrawable(R.drawable.status));
		
		ImageView zingers = (ImageView)act.findViewById(R.id.zingers_view);
		zingers.setImageDrawable(act.getResources().getDrawable(R.drawable.online_black));
		
		ImageView chats = (ImageView)act.findViewById(R.id.chats_view);
		chats.setImageDrawable(act.getResources().getDrawable(R.drawable.chatbox));
		
		ImageView settings = (ImageView)act.findViewById(R.id.settings_view);
		settings.setImageDrawable(act.getResources().getDrawable(R.drawable.settings));
	}
	
	private void selectChats()
	{
		ImageView profile = (ImageView)act.findViewById(R.id.profile_view);
		profile.setImageDrawable(act.getResources().getDrawable(R.drawable.status));
		
		ImageView zingers = (ImageView)act.findViewById(R.id.zingers_view);
		zingers.setImageDrawable(act.getResources().getDrawable(R.drawable.online));
		
		final ImageView chats = (ImageView)act.findViewById(R.id.chats_view);
		if(chats.getBackground() != null)
			chats.setImageDrawable(null);
		chats.setImageDrawable(null);
		chats.post(new Runnable()
		{
			public void run()
			{
				chats.setImageDrawable(act.getResources().getDrawable(R.drawable.chatbox_black));
			}
		});
		//chats.setImageDrawable(act.getResources().getDrawable(R.drawable.chatbox_black));
		
		ImageView settings = (ImageView)act.findViewById(R.id.settings_view);
		settings.setImageDrawable(act.getResources().getDrawable(R.drawable.settings));
	}
	
	private void selectSettings()
	{
		ImageView profile = (ImageView)act.findViewById(R.id.profile_view);
		profile.setImageDrawable(act.getResources().getDrawable(R.drawable.status));
		
		ImageView zingers = (ImageView)act.findViewById(R.id.zingers_view);
		zingers.setImageDrawable(act.getResources().getDrawable(R.drawable.online));
		
		ImageView chats = (ImageView)act.findViewById(R.id.chats_view);
		chats.setImageDrawable(act.getResources().getDrawable(R.drawable.chatbox));
		
		final ImageView settings = (ImageView)act.findViewById(R.id.settings_view);
		if(settings.getBackground() != null)
			settings.setImageDrawable(null);
		settings.setImageDrawable(null);
		settings.post(new Runnable()
		{
			public void run()
			{
				settings.setImageDrawable(act.getResources().getDrawable(R.drawable.settings_black));
			}
		});
		
	}
	
	public ChatListAdapter getAdapter()
	{
		return adapter;
	}
	
	public static MediaPlayer getMediaPlayerSend()
	{
		return mp;
	}
	
	public static MediaPlayer getMediaPlayerReceive()
	{
		return mp1;
	}
	
	public static MediaPlayer getMediaPlayerReceivedFromCurrentZinger()
	{
		return mp2;
	}
	
	public static Typeface getSegoeTypeface()
	{
		return type;
	}
	
	private static void instantiateHashMap()
	{
		if(hashmap == null)
		{
			hashmap = new HashMap<String,AvailabilityTimer>();
		}
	}
	
	public static HashMap<String,AvailabilityTimer> getHashMap()
	{
		return hashmap;
		
	}
	
	/**
	 * this method guarantees to always return a valid Activity that is not null
	 */
	public static Context getCurrentActivity()

	{
		return cont;
	}
	
	public static void setCurrentActivity(Context ac)
	{
		cont = ac;
	}
	
	@Override
	public boolean onKeyUp(int code, KeyEvent k)
	{
		if(code== KeyEvent.KEYCODE_BACK)
		{
			
			if(EmojiDisplay.isEmojiVisible())
				EmojiDisplay.closeEmoji();
			else
			{
				//EmojiDisplay.closeEmoji();
				act.finish();
				act = null;
				System.gc();
			}
		}
		return true;
	}

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ChatList.setCurrentActivity(ZingService.getContextFromService());
        //BroadcastMessage.closePort();
        //ReceiveBroadcastMessage.closePort();
        //Network.stopBroadcast();

    }

    @Override
    public void onNewIntent(Intent intent) {

        String type = intent.getStringExtra("type");
        if (type != null) {
            if (type.equals("message")) {
                vpager.setCurrentItem(2);
                int id = DatabaseHelper.getId(intent.getStringExtra("macAddress"));
                String ip_address = DatabaseHelper.getIpAddress(id);
                Intent intent1 = new Intent(act, ChatWindow.class);
                String zing_id = DatabaseHelper.getZingerId(id);
                String mac = intent.getStringExtra("macAddress");
                //new Network().startBroadcast();
                //new ReceiveBroadcastMessage();
                ChatWindow.setProperties(id, ip_address, mac, zing_id);
                act.startActivity(intent1);
            } else if (type.equals("call")) {

                vpager.setCurrentItem(2);
                Intent intent1 = new Intent(act, IncomingCallActivity.class);
                //new Network().startBroadcast();
                //new ReceiveBroadcastMessage();
                //intent1.addFlags(Intent. FLAG_ACTIVITY_REORDER_TO_FRONT |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                act.startActivity(intent1);
            }
        }
    }

}

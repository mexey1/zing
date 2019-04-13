package com.karabow.zing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Zingers extends Fragment
{
	private static int counts;
	private static RelativeLayout view;
	private Network network = null;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(container == null)
		{
			Log.d("This Is Null", "Container");
		
			return null;
		}
		
		view =  (RelativeLayout)inflater.inflate(R.layout.scroll_container, container, false);
		Log.d("This Is Null", "Containerfggf");
		displayZingers(true);
		final Button jg = (Button)view.findViewById(R.id.join_group);
		final Button cg = (Button)view.findViewById(R.id.create_group);
		
		jg.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				Network net = new Network();
				net.enableWifi(getActivity());
				
				ProgressBar pb = (ProgressBar)view.findViewById(R.id.scan_progress);
				TextView tv = (TextView)view.findViewById(R.id.scan_text);
				
				pb.setVisibility(ProgressBar.VISIBLE);
				tv.setVisibility(TextView.VISIBLE);
				jg.setVisibility(Button.GONE);
				cg.setVisibility(Button.GONE);
			}
		});
		
		cg.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				Network net = new Network();
				net.enableHotspot(getActivity());
				
				ProgressBar pb = (ProgressBar)view.findViewById(R.id.scan_progress);
				TextView tv = (TextView)view.findViewById(R.id.scan_text);
				
				pb.setVisibility(ProgressBar.VISIBLE);
				tv.setVisibility(TextView.VISIBLE);
				jg.setVisibility(Button.GONE);
				cg.setVisibility(Button.GONE);
			}
		});
		
		final ImageView wifi = (ImageView)view.findViewById(R.id.wifi_popup);
		wifi.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					wifi.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					wifi.setBackgroundColor(0);
				}
				return false;	
			}
			
		});
		
		wifi.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
			}

			
		});
		return view;
	}
	/**
	 * This method returns the view hosting all Zingers.
	 * @return ViewGroup
	 */
	public static ViewGroup getZingersView()
	{
		return view;
	}
	
	public void hideProgressBar()
	{
		//super.onResume();
		if(view != null)
		{
			ProgressBar pb = (ProgressBar)view.findViewById(R.id.scan_progress);
			TextView tv = (TextView)view.findViewById(R.id.scan_text);
			
			Button jg = (Button)view.findViewById(R.id.join_group);
			Button cg = (Button)view.findViewById(R.id.create_group);
			
			pb.setVisibility(ProgressBar.GONE);
			tv.setVisibility(TextView.GONE);
			cg.setVisibility(Button.GONE);
			jg.setVisibility(Button.GONE);
			ScrollView sv = (ScrollView)view.findViewById(R.id.scrollView1);
			sv.setVisibility(ScrollView.VISIBLE);
		}
	}
	
	public void displayZingers(boolean isVisible)
	{
		if(isVisible)
		{
			if(view != null)
			{
				LinearLayout lay = (LinearLayout)view.findViewById(R.id.chats_layout);
				lay.removeAllViews();
				LayoutInflater inflater = LayoutInflater.from(view.getContext());
				JSONObject [] zingers = DatabaseHelper.getZingers();
				
				if(zingers.length > 0)
				{
					hideProgressBar();
					Log.d("Zingers Length Ooof", Integer.toString(zingers.length));
				}
				
				int count = 0;
				/*if(lay.getChildCount() == 0)
				{*/
					while(count < zingers.length)
					{
						try
						{	
							View cps = inflater.inflate(R.layout.chat_person_status, lay,false);
							TextView name = (TextView)cps.findViewById(R.id.name_status);
							TextView status = (TextView)cps.findViewById(R.id.status);
							ImageView img = (ImageView)cps.findViewById(R.id.indicator);
							name.setText(zingers[count].getString("zing_id"));
							status.setText(zingers[count].getString("status"));
							status.setTextColor(ChatList.getCurrentActivity().getResources().getColor(R.color.battleship_grey));
							status.setTextSize(14);
							status.setEllipsize(TextUtils.TruncateAt.END);
							img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.zing_online));
							
							final byte pix[] = DatabaseHelper.getZIngerProfilePix(zingers[count].getString("mac_address"));
							Toast.makeText(ChatList.getCurrentActivity(), "Profile Picture Bytes"+Integer.toString(pix.length), Toast.LENGTH_LONG).show();
							if(pix.length >0 )
							{
								final ImageView imgs = (ImageView)cps.findViewById(R.id.profile_pix);
								imgs.post(new Runnable()
								{

									@Override
									public void run() 
									{
										// TODO Auto-generated method stub
										Bitmap bmp = PrepareBitmap.resizeImage(pix, 200, 200);
										imgs.setImageBitmap(PrepareBitmap.drawRoundedRect(bmp,7f));
									}
									
								});
								
							}
							cps.setId(zingers[count].getInt("id"));
							cps.setOnTouchListener(new OnTouchListener()
							{

								@Override
								public boolean onTouch(View v, MotionEvent event) 
								{
									
									android.content.Context act = ChatList.getCurrentActivity();
									if(event.getAction() ==  MotionEvent.ACTION_DOWN)
									{
										v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
										
									}
									else if(event.getAction() == MotionEvent.ACTION_UP)
									{
									
										v.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.linear_border));
										Intent intent = new Intent(act,ChatWindow.class);
										act.startActivity(intent);
										ChatWindow.setProperties(v.getId(),
																 DatabaseHelper.getIpAddress(v.getId()),
																 DatabaseHelper.getMacAddress(v.getId()),
                                                                 DatabaseHelper.getZingerId(v.getId()));
									}
									
									if(event.getAction() == MotionEvent.ACTION_MOVE)
									{
										
									}
											
									//event.
									return true;
								}
							});
							
							
								lay.addView(cps);
								count++;
						}
						catch(JSONException e)
						{
							e.printStackTrace();
						}
					}
			 } 
		}
		
		/*else
		{
				if(view != null)
				{
					int count = view.getChildCount();
					for(int c=0; c<count;c++)
					{
						View child =view.getChildAt(c);
						ImageView imgs = (ImageView)child.findViewById(R.id.profile_pix);
						if(imgs != null)
						{
							BitmapDrawable bmpd = (BitmapDrawable)imgs.getDrawable();
							Bitmap bmp = bmpd.getBitmap();
							bmp=null;
							child = null;
							System.gc();
						}
					}
					
				}
		}*/
	}
	
	public void setUserVisibleHint(boolean isVisible)
	{
		
	}

	

}

package com.karabow.zing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import github.ankushsachdeva.emojicon.EmojiconTextView;

public class Chats extends Fragment
{
	
	//private static boolean shouldDeleteDatabases;
	private static RelativeLayout view;
	private static Activity act;
	private LayoutInflater inflater;
	private LinearLayout lay;
	//lay.removeAllViews();
	private JSONObject [] zingers;
	private int loop;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(container == null)
		{
			Log.d("This Is Null Chats", "Container");
			return null;
		}
		Log.d("This Is Chats Null", "Containerfggf");
		view =  (RelativeLayout)inflater.inflate(R.layout.recents, container, false);
		act = this.getActivity();
		//LinearLayout lay = (LinearLayout)view.findViewById(R.id.chats_layout);
		//displayRecentMessages(true);
		//loop++;
		return view;
	}
	
	public static View getChatsView()
	{
		return view;
	}
	
	public static Activity getFragActivity()
	{
		return act;
	}
	
	public void hideProgressBar()
	{
		//super.onResume();
		TextView pb = (TextView)view.findViewById(R.id.scan_progress);
		TextView tv = (TextView)view.findViewById(R.id.scan_text);
		pb.setVisibility(TextView.GONE);
		tv.setVisibility(TextView.GONE);
		ScrollView sv = (ScrollView)view.findViewById(R.id.scrollView1);
		sv.setVisibility(ScrollView.VISIBLE);
	}
	@Override
	public void setUserVisibleHint(boolean isVisible)
	{
		if(isVisible)
		{
			if(view != null )
			{
				inflater = LayoutInflater.from(view.getContext());
				lay = (LinearLayout)view.findViewById(R.id.chats_layout);
				//lay.removeAllViews();
				zingers = DatabaseHelper.getLastMessages();
				Toast.makeText(view.getContext(), "Length of Zingers"+zingers.length, Toast.LENGTH_LONG).show();
				if(zingers.length > 0)
				{
					hideProgressBar();
					zingers = arrangeMessages(zingers);
				}
				
				else 
					return;
				
					int count =0;
					View view = null;
					if(loop == 0)
					{
						while(count < zingers.length)
						{
							addChat(count);
							count++;
						}
					}
					//loop++;
				
				
				/*if(!shouldDeleteDatabases)
				{
					DatabaseHelper.dropZingers();
					DatabaseHelper.createZingers();
					shouldDeleteDatabases = true;
				}*/
				
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
					child =null;
				}
			}
		}*/
	}
	
	/**
	 * This method helps to arrange the last messages from most recent to least recent...
	 */
	private JSONObject[] arrangeMessages(JSONObject[] ar)
	{
		int count =0,loop=0;
		//count = ar.length;
		JSONObject j = null;
		try
		{
			while(count < ar.length)
			{
				loop = count+1;
				while(loop < ar.length)
				{
					/*if(loop >= ar.length)
						break;*/
					String date_time = ar[count].getString("time");
					Timestamp ts = Timestamp.valueOf(date_time);
					long date = ts.getTime();
					long next_date = Timestamp.valueOf(ar[loop].getString("time")).getTime();
					Toast.makeText(ChatList.getCurrentActivity(),ar[count].getString("zing_id")
							  +Long.toString(date)+ar[loop].getString("zing_id")+Long.toString(next_date), Toast.LENGTH_LONG).show();
					if(date < next_date)
					{
						j = ar[count];
						ar[count] = ar[loop];
						ar[loop] = j;
					}
					else
					{
						loop++;
						continue;
					}
					loop++;
					
				}
				count++;
			}
			
			return ar;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private void addChat(int count)
	{
		try
		{
			//view = lay.getChildAt(count);
			/*
			 * Check if the Chat has been added to the Chats layout...If it hasn't call addNewChat method
			 * if it has, call updateChat method
			 */
			if(lay.findViewById(zingers[count].getInt("id"))==null)
			{
				addNewChat(count);
			}
			else
			{
				updateChat(count);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	private void addNewChat(int count)
	{
		try
		{
			String date_time = zingers[count].getString("time");
			Timestamp ts = Timestamp.valueOf(date_time);
			String time = Time.getTime(ts);
			String type = zingers[count].getString("type");
			View cps = inflater.inflate(R.layout.chat_person, lay,false);
			EmojiconTextView name = (EmojiconTextView)cps.findViewById(R.id.name);
			EmojiconTextView last_message = (EmojiconTextView)cps.findViewById(R.id.last_message);
			TextView time_view = (TextView)cps.findViewById(R.id.time);
			time_view.setText(time);
			//ImageView img = (ImageView)cps.findViewById(R.id.indicator);
			name.setText(zingers[count].getString("zing_id"));
			if(type.equals("text"))
				last_message.setText(zingers[count].getString("message"));
			else if(type.equals("picture"))
				displayPictureMessage(last_message);
			else if(type.equals("audio"))
				displayAudioMessage(last_message);
			else
				displayVideoMessage(last_message);
			last_message.setTextColor(ChatList.getCurrentActivity().getResources().getColor(R.color.battleship_grey));
			last_message.setTextSize(14);
			last_message.setEllipsize(TextUtils.TruncateAt.END);
			//img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.zing_online));
			
			final byte pix[] = DatabaseHelper.getZIngerProfilePix(zingers[count].getString("mac_address"));
			final ImageView imgs = (ImageView)cps.findViewById(R.id.profile_pix);
			if(pix.length >0 )
			{
				/*
				 * We would need to resize the profile pix t fit into our small image view.
				 * this resampling should not be done on the UI thread
				 * We achieve this by spawning a new thread and inserting the bitmap to the imageview
				 * on the UI thread.
				 */
				Thread thread = new Thread(new Runnable()
				{

					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						final Bitmap bmp = PrepareBitmap.resizeImage(pix, 180, 180);
						act.runOnUiThread(new Runnable()
						{

							@Override
							public void run() 
							{
								imgs.setImageBitmap(PrepareBitmap.drawRoundedRect(bmp,7f));
							}
							
						});
					}
					
				});
				thread.start();
			}
			final ImageView img = (ImageView)cps.findViewById(R.id.msg_status);
			//img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
			String status = zingers[count].getString("status");
			if(status.equals("unread"))
			{
				int unread_status = zingers[count].getInt("unread_status");
				if(unread_status == 0)
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_read));
					/*
					 * Add an id to the msg_status object. The id would be checked to update the database message to a read value
					 */
					img.setTag("read");
				}
					
				else
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
					/*
					 * Add an id to the msg_status object. The id would be checked to update the database message to a read value
					 */
					img.setTag("unread");
				}
					
			}
			else
			{
				int unread_status = zingers[count].getInt("send_status");
				if(unread_status == 0)
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_failed));
					//img.setId(-1);
					img.setTag("unsent");
				}
				else
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_ok));
					//img.setId(-2);
					img.setTag("sent");
				}
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_ok));
			}
			cps.setId(zingers[count].getInt("id"));
			cps.setOnTouchListener(new OnTouchListener()
			{

				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					Activity act = ((Activity)ChatList.getCurrentActivity());
					
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
						if(((String)img.getTag()).equals("unread"))
						{
							DatabaseHelper.updateMessages(DatabaseHelper.getMacAddress(v.getId()));
							img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_read));
						}
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
	
	private void updateChat(int count)
	{
		try
		{
			LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
			View l = chats_layout.findViewById(zingers[count].getInt("id"));
			String date_time = zingers[count].getString("time");
			Timestamp ts = Timestamp.valueOf(date_time);
			String time = Time.getTime(ts);
			String type = zingers[count].getString("type");
			
			TextView name = (TextView)l.findViewById(R.id.name);
			EmojiconTextView last_message = (EmojiconTextView)l.findViewById(R.id.last_message);
			TextView time_view = (TextView)l.findViewById(R.id.time);
			time_view.setText(time);
			//ImageView img = (ImageView)cps.findViewById(R.id.indicator);
			name.setText(zingers[count].getString("zing_id"));
			if(type.equals("text"))
				last_message.setText(zingers[count].getString("message"));
			else if(type.equals("picture"))
				displayPictureMessage(last_message);
			else if(type.equals("audio"))
				displayAudioMessage(last_message);
			else
				displayVideoMessage(last_message);
			last_message.setTextColor(ChatList.getCurrentActivity().getResources().getColor(R.color.battleship_grey));
			last_message.setTextSize(14);
			last_message.setEllipsize(TextUtils.TruncateAt.END);
			//img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.zing_online));
			
			ImageView img = (ImageView)l.findViewById(R.id.msg_status);
			String status = zingers[count].getString("status");
			if(status.equals("unread"))
			{
				int unread_status = zingers[count].getInt("unread_status");
				if(unread_status == 0)
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_read));
					/*
					 * Add an id to the msg_status object. The id would be checked to update the database message to a read value
					 */
					img.setTag("read");
				}
					
				else
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
					/*
					 * Add an id to the msg_status object. The id would be checked to update the database message to a read value
					 */
					img.setTag("unread");
				}
					
			}
			else
			{
				int unread_status = zingers[count].getInt("send_status");
				if(unread_status == 0)
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_failed));
					img.setTag("unsent");
				}
				else
				{
					img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_ok));
					img.setTag("sent");
				}
					//img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.send_ok));
			}
			//cps.setId(zingers[count].getInt("id"));
			
				lay.removeView(l);
				lay.addView(l,count);
				//count++;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		
		
		
		
		/*
		 * 
		 final byte pix[] = DatabaseHelper.getZIngerProfilePix(zingers[count].getString("mac_address"));
			final ImageView imgs = (ImageView)l.findViewById(R.id.profile_pix);
		 * if(pix.length >0 )
			{
				/*
				 * We would need to resize the profile pix t fit into our small image view.
				 * this resampling should not be done on the UI thread
				 * We achieve this by spawning a new thread and inserting the bitmap to the imageview
				 * on the UI thread.
				 
				Thread thread = new Thread(new Runnable()
				{

					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						final Bitmap bmp = PrepareBitmap.resizeImage(pix, 180, 180);
						act.runOnUiThread(new Runnable()
						{

							@Override
							public void run() 
							{
								imgs.setImageBitmap(PrepareBitmap.drawRoundedRect(bmp,7f));
							}
							
						});
					}
					
				});
				thread.start();
			}
		 */
	}
	
	private void displayPictureMessage(EmojiconTextView tv)
	{
			Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.chats_image_unread); 
			int[] size = computeSize(20,20);
			d.setBounds(0, 0, size[0], size[1]); 
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
            SpannableString ss = new SpannableString("ane Picture");
            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
            tv.setText(ss); 
	}
	
	private void displayAudioMessage(EmojiconTextView tv)
	{
		Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.ic_voice_note); 
		int[] size = computeSize(20,20);
		d.setBounds(0, 0, size[0], size[1]); 
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
        SpannableString ss = new SpannableString("ane Audio");
        ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
        tv.setText(ss); 
	}
	
	private void displayVideoMessage(EmojiconTextView tv)
	{
		Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.video_msg); 
		int[] size = computeSize(20,20);
		d.setBounds(0, 0, size[0], size[1]); 
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
        SpannableString ss = new SpannableString("ane Video");
        ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
        tv.setText(ss); 
	}
	
	private int[] computeSize(int... size)
	{
		WindowManager wm = (WindowManager)act.getSystemService(Activity.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int dpi = dm.densityDpi;
		Log.d("DPI", Integer.toString(dpi));
		size[0] = (int)((dpi*size[0])/160);
		size[1] = (int)((dpi*size[1])/160);
		return size;
	}
	
}

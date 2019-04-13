package com.karabow.zing;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

//import github.ankushsachdeva.emojicon.*;
;
public class ChatWindow extends Activity 
{

	private boolean hasBeenOpened = false;
	private static Activity act;
	private static String ip_address,mac_address,zing_id;
	private static int id;
	private int lastMessageId;
	private ScrollView lays;
	private LinearLayout main_lay;
	private int count,year,month,day,week;
	private TextView date;
	private String myMacAddress;
	//private final int hello;
	
	@Override
	public boolean onKeyUp(int code, KeyEvent k)
	{
		if(code== KeyEvent.KEYCODE_BACK)
		{
			if(EmojiDisplay.isEmojiVisible())
			{
				EmojiDisplay.closeEmoji();
			}
			else
			{
				finish();
				BitmapDrawable bmpd = (BitmapDrawable)main_lay.getBackground();
				Bitmap bmp = bmpd.getBitmap();
				bmp.recycle();
				bmp=null;
				main_lay = null;
				act = null;
				System.gc();
			}
			
		}
		return true;
	}
	
	
	
	@TargetApi(14)
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		act = this;
		setContentView(R.layout.chat_window);
		
		final SharedPreferences spref = act.getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
    	myMacAddress = spref.getString("mac", "mac");
    	myMacAddress = myMacAddress.replace(":", "");
		
		ActionBar abar = this.getActionBar();
		abar.setCustomView(R.layout.chat_window_actionbar);
		abar.setDisplayShowCustomEnabled(true);
		abar.setDisplayShowHomeEnabled(false);
		abar.setDisplayShowTitleEnabled(false);
		
		//if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
			//abar.setIcon(android.R.color.transparent);
		//abar.setDisplayHomeAsUpEnabled(true);
		/*
		 * To avoid out of memory exception while loading the background drawable for this window,
		 * we have to downsample the image before drawing it..
		 */
		main_lay = (LinearLayout)this.findViewById(R.id.main_layout);
		main_lay.post(new Runnable()
		{

			@Override
			public void run()
			{
				int size[] = computeSize(240,240);
				Log.d("Back", "Width "+size[0]+"Height "+size[1]);
				// TODO Auto-generated method stub
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
				{
					main_lay.setBackgroundDrawable(PrepareBitmap.setChatWindowBackground(R.drawable.default_wallpaper, size[0], size[1]));
				}
				else
				{
					main_lay.setBackgroundDrawable(PrepareBitmap.setChatWindowBackground(R.drawable.default_wallpaper,size[0], size[1]));
				}
			}
			
		});
		
		View v = abar.getCustomView();
		ImageView img = (ImageView)v.findViewById(R.id.pix);
		
		final TextView name = (TextView)v.findViewById(R.id.name);
		name.setText(zing_id);
		
		name.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) 
			{
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					name.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					name.setBackgroundColor(0);
				}
				return false;
			}
			
		});
		
		name.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(act,ProfileView.class);
				//intent.setClassName(act, ProfileView.class.toString());
				intent.putExtra("mac_address", mac_address);
				intent.putExtra("ip_address", ip_address);
				act.startActivity(intent);
			}
			
		});
		final ImageView add_view = (ImageView)v.findViewById(R.id.add_icon);
		add_view.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					add_view.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					add_view.setBackgroundColor(0);
					selectFileType(v);
				}
				return true;	
			}
			
		});
		
		final ImageView call_icon = (ImageView)v.findViewById(R.id.call_icon);
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
					OutgoingCallActivity.setMacAddress(mac_address);
					OutgoingCallActivity.setZingId(zing_id);
					Intent call_activity_intent = new Intent(act,OutgoingCallActivity.class);
					act.startActivity(call_activity_intent);
				}
				return true;	
			}
			
		});
		
		img.setImageDrawable(PrepareBitmap.setChatWindowProfilePix(DatabaseHelper.getZIngerProfilePix(mac_address),180, 180));
		abar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		
		final LinearLayout back_layout =(LinearLayout)v.findViewById(R.id.back_layout);
		back_layout.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					back_layout.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					back_layout.setBackgroundColor(0);
				}
				return false;
			}
			
		});
		
		back_layout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				act.finish();
				act=null;
			}
			
		});
		abar.show();
		ArrayList<JSONObject> listOfMsgs= DatabaseHelper.getMessages(mac_address);
		displayMessages(listOfMsgs);
		
		lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
		final EditText msg = (EditText)act.findViewById(R.id.editText1);
		
		lays.post(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				lays.fullScroll(ScrollView.FOCUS_DOWN);
			}
			
		});
		
		//Load the Emoji into Memory
		LinearLayout lay = (LinearLayout)act.findViewById(R.id.bottomm_layout);
		new EmojiDisplay(lay,act);
		EmojiDisplay.closeEmoji();
	}
	
	public void onWindowFocusChanged (boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
			final SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
	    	myMacAddress = spref.getString("mac", "mac");
	    	myMacAddress = myMacAddress.replace(":", "");
		}
		
		if(hasFocus == true && !hasBeenOpened)
		{
			hasBeenOpened = true;
			
		}
		
		
		else if(!hasFocus)
		{
			/*act = null;
			finish();
			/*int count = lays.getChildCount();
			for(int c=0; c<count;c++)
			{
				View v = lays.getChildAt(c);
				v = null;
			}*/
		}
		
	}
	
	
	
	public void sendMessage(View v)
	{
		LinearLayout lay = (LinearLayout)act.findViewById(R.id.chats_display);
		final ScrollView lays = (ScrollView)act.findViewById(R.id.scrollView1);
		LayoutInflater li = LayoutInflater.from(v.getContext());
		EditText msg = (EditText)act.findViewById(R.id.editText1);
		String msgToSend = msg.getText().toString().trim();
		
		// Don't send if message is empty
		if(msgToSend.isEmpty())
			return;
		
		View msg_layout = null;
		msg_layout = li.inflate(R.layout.sent_msg_single_line, lay,false);
		
		TextView text = (TextView)msg_layout.findViewById(R.id.msg);
		//text.setText(msgToSend);
		text.setTextSize(18);
		text.setTypeface(ChatList.getSegoeTypeface());
		ImageView msg_status = (ImageView)msg_layout.findViewById(R.id.send_status_image);
		
		msg_layout.setBackgroundResource(R.drawable.bubble_yellow);
		String timet = Time.getTime();
		int msg_length = msgToSend.length();
		SpannableStringBuilder ssb = new SpannableStringBuilder(msgToSend+" "+timet);
		
		
		/*
		 * Set the size of the msg_status icon to be proportional to the density of the display.
		 */
		
		int[]size = computeSize(12,10);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
		lp.gravity=Gravity.BOTTOM;
		lp.leftMargin= -3;
		msg_status.setLayoutParams(lp);
		msg_status.setPadding(-1, 0, 0, -1);
		
		
		CustomSpan cs =new CustomSpan();
		ssb.setSpan(cs,msg_length+1,ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		/*if(width/rect.width() <=1)
		{
			ssb = new SpannableStringBuilder(msgToSend+"   "+timet);
			CustomSpan cs =new CustomSpan();
			ssb.setSpan(cs,msg_length+3,ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		else
		{
			CustomSpan cs =new CustomSpan();
			ssb.setSpan(cs,msg_length+1,ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}*/
		
		/*
		 * CustomSpan is a Replacement Span that writes the time at the right most position of the textview.
		 */
		TextPaint tp = text.getPaint();
		int width = (int)tp.measureText(ssb.toString());
		text.setMinWidth(width);
		text.setText(ssb);
		
		msg_layout.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v) 
			{
				showPopup(v,lays);
				return true;
			}
		});
		
		lay.addView(msg_layout);
		lays.post(new Runnable()
		{
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				lays.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		//lays.fullScroll(ScrollView.FOCUS_DOWN);
		msg.setText("");
	
		//msg.requestFocus();
		//msg.requestFocusFromTouch();
		//msg.performClick();
		
		if(count == 1)
		{
			EmojiDisplay.closeEmoji();
			ImageView img = (ImageView)act.findViewById(R.id.add_smiley);
			img.setImageDrawable(act.getResources().getDrawable(R.drawable.smiley));
			count--;
		}
		
		Log.d("MY IP ADDRESS", "MY IP ADDRESS IS" + getIpAddress());
		ChatMessageSend sndThread = new ChatMessageSend(getIpAddress(),msgToSend,id,msg_status);
		Log.d("MY MESSAGE", msgToSend);
		//snd.setText(msgToSend);
		sndThread.start();
	}
	
	public static int getId()
	{
		return id;
	}
	
	public static String getIpAddress()
	{
		return ip_address;
	}
	
	public static Activity getActivity()
	{
		return act;
	}
	
	public static void setProperties(int iD, String ...props)
	{
		id = iD;
		ip_address = props[0];
		mac_address = props[1];
		zing_id = props[2];
	}
	
	public void selectFileType(View v)
	{
		Rect rect = new Rect();
		v.getGlobalVisibleRect(rect);
		
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		LinearLayout lay = (LinearLayout)act.findViewById(R.id.chats_layout);
		View vw = li.inflate(R.layout.attach_file, lay);
		RelativeLayout gallery = (RelativeLayout)vw.findViewById(R.id.gallery);
		RelativeLayout audio = (RelativeLayout)vw.findViewById(R.id.audio);
		RelativeLayout video = (RelativeLayout)vw.findViewById(R.id.video);
		RelativeLayout file = (RelativeLayout)vw.findViewById(R.id.file);
		/*
		 * SET TOUCH LISTENERS
		 */
		gallery.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					/*
					 * A REQUEST CODE VALUE OF 1 IS RETURNED TO MAKE THE APP KNOW THAT A PICTURE IS WHAT WAS SELECTED
					 */
					v.setBackgroundColor(0);
					Intent sign_up_intent = new Intent();
					sign_up_intent.setType("image/*");
					sign_up_intent.setAction(Intent.ACTION_GET_CONTENT);
					pop.dismiss();
					act.startActivityForResult(Intent.createChooser(sign_up_intent, "Select Picture"),1);
				}
				return true;
			}
			
		});
		
		audio.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					/*
					 * A REQUEST CODE VALUE OF 2 IS RETURNED TO MAKE THE APP KNOW THAT A PICTURE IS WHAT WAS SELECTED
					 */
					v.setBackgroundColor(0);
					Intent sign_up_intent = new Intent();
					sign_up_intent.setType("audio/*");
					sign_up_intent.setAction(Intent.ACTION_GET_CONTENT);
					pop.dismiss();
					//Intent.createChooser(sign_up_intent, "Choose a picture");
					act.startActivityForResult(Intent.createChooser(sign_up_intent, "Select an audio file"),2);
				}
				return true;
			}
			
		});
		
		video.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					/*
					 * A REQUEST CODE VALUE OF 3 IS RETURNED TO MAKE THE APP KNOW THAT A PICTURE IS WHAT WAS SELECTED
					 */
					v.setBackgroundColor(0);
					Intent sign_up_intent = new Intent();
					sign_up_intent.setType("video/*");
					sign_up_intent.setAction(Intent.ACTION_GET_CONTENT);
					pop.dismiss();
					//Intent.createChooser(sign_up_intent, "Choose a picture");
					act.startActivityForResult(Intent.createChooser(sign_up_intent, "Select a video file"),3);
				}
				return true;
			}
			
		});
		
		file.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					/*
					 * A REQUEST CODE VALUE OF 3 IS RETURNED TO MAKE THE APP KNOW THAT A PICTURE IS WHAT WAS SELECTED
					 */
					v.setBackgroundColor(0);
					Intent sign_up_intent = new Intent();
					sign_up_intent.setType("file/*");
					sign_up_intent.setAction(Intent.ACTION_GET_CONTENT);
					pop.dismiss();
					//Intent.createChooser(sign_up_intent, "Choose a picture");
					act.startActivityForResult(Intent.createChooser(sign_up_intent, "Select a file"),4);
				}
				return true;
			}
			
		});
		pop.setContentView(vw);
		pop.setHeight(LayoutParams.WRAP_CONTENT);
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		//pop.showAsDropDown(v);
		pop.setTouchable(true);
		pop.setOutsideTouchable(true);
		pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.empty));
		pop.setTouchInterceptor(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_OUTSIDE)
				{
					pop.dismiss();
					return false;
				}
				return false;
			}
		});
		pop.showAtLocation(v, Gravity.TOP, 0, rect.bottom-14);
		
		
	}
	
	//method called after file has ben selected
	protected void onActivityResult(int requestcode, int resultcode, Intent data)
	{
		if(resultcode == Activity.RESULT_CANCELED)
			return;
		
		else if (resultcode == Activity.RESULT_OK && requestcode == 1)
		{
			Uri uri = data.getData();
			String path = getImagePath(uri);
			sendPicture(path);
		}
		
		else if(resultcode == Activity.RESULT_OK && requestcode == 2)
		{
			Uri audio_uri = data.getData();
			String path = getAudioPath(audio_uri);
			sendAudioFile(path);
		}
		
		else if(resultcode == Activity.RESULT_OK && requestcode == 3)
		{
			Uri audio_uri = data.getData();
			String path = getVideoPath(audio_uri);
			sendVideoFile(path);
		}
		
		else 
		{
			Uri audio_uri = data.getData();
			String path = getAudioPath(audio_uri);
			sendAudioFile(path);
		}
		
	}
	
	//called to retrieve the path of the audio file selected
	private String getAudioPath(Uri uri)
	{
		String[] proj = {MediaStore.Audio.Media.DATA};
		Cursor cursor = act.managedQuery(uri,proj,null,null,null);
		
		int column_index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
		if(column_index == -1)
			return "invalid_column";
		else
		{
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		
	}
	
	private String getVideoPath(Uri uri)
	{
		String[] proj = {MediaStore.Video.Media.DATA};
		Cursor cursor = act.managedQuery(uri,proj,null,null,null);
		
		int column_index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
		if(column_index == -1)
			return "invalid_column";
		else
		{
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		
	}
	
	//called to retrieve the path to the image file selected
	private String getImagePath(Uri uri)
	{
		String[] proj = { MediaColumns.DATA }; //{MediaStore.Images.Media.DATA};
		Cursor cursor = act.managedQuery(uri,proj,null,null,null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		if(column_index == -1)
			return "invalid_column";
		else
		{
			cursor.moveToFirst();
	        return cursor.getString(column_index);
		}
			
		/*else
		{

			public String getAbsolutePath(Uri uri) {
				 String[] projection = { MediaColumns.DATA };

				   Cursor cursor = managedQuery(uri, projection, null, null, null);
				   if (cursor != null) 
				   {
				        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				        cursor.moveToFirst();
				        return cursor.getString(column_index);
				   } else
				     return null;
				  }}*/
		
	}
	
	private void sendPicture(String path)
	{
		try
		{
			Log.d("file path", path);
			File file = new File(path);
			Log.d("File Name",file.getName());
			//form file description
			JSONObject jobj = new JSONObject();
			jobj.put("file_name", file.getName());
			jobj.put("file_type", "picture");
			jobj.put("mac_address", myMacAddress);
			jobj.put("file_size", Long.toString(file.length()));
			final FileSend fs = new FileSend(path,jobj,file,getIpAddress());
			/*act.runOnUiThread(new Runnable()
			{
				public void run()
				{
					fs.execute(new Object[2]);
				}
			});*/
			
		}
		
		catch(Exception e)
		{
			//Log.d("file path", path);
			e.printStackTrace();
		}
	}
	
	private void sendAudioFile(String path)
	{
		try
		{
			final File file = new File(path);
			Log.d("File Name",file.getName());
			//form file description
			final JSONObject jobj = new JSONObject();
			jobj.put("file_name", file.getName());
			jobj.put("file_type", "audio");
			jobj.put("mac_address", myMacAddress);
			jobj.put("file_size", Long.toString(file.length()));
			new FileSend(path,jobj,file,getIpAddress());
			
			
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendVideoFile(String path)
	{
		try
		{
			final File file = new File(path);
			Log.d("File Name",file.getName());
			//form file description
			final JSONObject jobj = new JSONObject();
			jobj.put("file_name", file.getName());
			jobj.put("file_type", "video");
			jobj.put("mac_address", myMacAddress);
			jobj.put("file_size", Long.toString(file.length()));
			new FileSend(path,jobj,file,getIpAddress());
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void showEmoji(View v)
	{
		LinearLayout lay = (LinearLayout)act.findViewById(R.id.bottomm_layout);
		
		
		if(count == 0)
		{
			
			
			/*EditText msg = (EditText)act.findViewById(R.id.editText1);
			msg.performClick();*/
			
			EmojiDisplay.showEmoji(lay);
			EmojiDisplay.setEmojiVisibile(true);
			((ImageView)v).setImageDrawable(act.getResources().getDrawable(R.drawable.smiley2));
			count++;
		}
		
		else 
		{
			count--;
			((ImageView)v).setImageDrawable(act.getResources().getDrawable(R.drawable.smiley));
			EmojiDisplay.closeEmoji();
			EmojiDisplay.setEmojiVisibile(false);
		}
		
	}

	public void loadMoreMessages(View v)
	{
		ArrayList<JSONObject> listOfMsgs = DatabaseHelper.getMessages(mac_address, lastMessageId);
		
		boolean hasMore = DatabaseHelper.canFetchMessages(mac_address, lastMessageId);
		final LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
		Button load_more = (Button)ChatWindow.getActivity().findViewById(R.id.load_more);
		final ScrollView lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
		
		final  int y1 = lay.getHeight();
		displayMessages(listOfMsgs);
		
		lays.post(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				int y2 = lay.getHeight();
				lays.scrollTo(0, y2-y1);
			}
			
		});
		
		
		if(hasMore)
		{
			lay.removeView(load_more);
			lay.addView(load_more,0);
		}
		else
			load_more.setVisibility(Button.GONE);
	}
	/**
	 * This method is called upon initialization of the ChatWindow to fetch messages from 
	 * the database and display them
	 * @param listOfMsgs: an array list of JSONObjects containing message to be displayed and metadata
	 */
	private void displayMessages(ArrayList<JSONObject> listOfMsgs)
	{
		boolean hasMore = false;
		//ArrayList<JSONObject> listOfMsgs = params[0];
		if(listOfMsgs == null)
			return;
		try {
				Calendar today = Calendar.getInstance();
				Calendar prev = null;
				Calendar cal = null;
				year = today.get(Calendar.YEAR);
				month = today.get(Calendar.MONTH);
				day = today.get(Calendar.DATE);
				week = today.get(Calendar.WEEK_OF_MONTH);
				
				for(int i = 0; i < listOfMsgs.size(); ++i)
				{
					if(i ==0)
					{
						lastMessageId = listOfMsgs.get(i).getInt("least_recent_msg_id");
						Log.d("This is the Number",Integer.toString(lastMessageId));
						//hasMore = DatabaseHelper.canFetchMessages(getId(), lastMessageId);
						hasMore = DatabaseHelper.canFetchMessages(mac_address, lastMessageId);
						Log.d("Do u have more", Boolean.toString(hasMore));
						if(hasMore)
						{
							Button load_more = (Button)ChatWindow.getActivity().findViewById(R.id.load_more);
							load_more.setVisibility(Button.VISIBLE);
						}
					}
					String fromId = listOfMsgs.get(i).getString("from");
					final String msg = listOfMsgs.get(i).getString("message");
					String date_time = listOfMsgs.get(i).getString("created");
					Timestamp ts = Timestamp.valueOf(date_time);
					
					cal = Calendar.getInstance();;
					cal.setTimeInMillis(ts.getTime());
					Log.d("Year",Integer.toString(cal.get(Calendar.YEAR)));
					/*
					 * Check if the years are the same, if they are... Proceed to check if months are the same
					 * and if they are, proceed to check if the dates are the same
					 */
					if(i == 0)
					{
						prev = cal;
					}
					else
					{
						if(prev.get(Calendar.YEAR)!=cal.get(Calendar.YEAR) || 
						   prev.get(Calendar.MONTH)!=cal.get(Calendar.MONTH) || prev.get(Calendar.DATE)!=cal.get(Calendar.DATE))
						{
							addDate(prev);
						}
						prev = cal;
					}
					
					
					String time = Time.getTime(ts);
					int sent = listOfMsgs.get(i).getInt("sent");
					String type = listOfMsgs.get(i).getString("type");
					
					if(type.equals("text"))
					{
						if(fromId.equals(myMacAddress))
						{
							if(ChatWindow.getActivity() != null)
							{
								final LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								View msg_layout = null;
								
								msg_layout = li.inflate(R.layout.sent_msg_single_line, lay,false);
								
								
								TextView text = (TextView)msg_layout.findViewById(R.id.msg);
								
								text.setTextSize(18);
								text.setTypeface(ChatList.getSegoeTypeface());
								
								int msg_length = msg.length();
								SpannableStringBuilder ssb = new SpannableStringBuilder(msg+" "+time);
								
								/*
								 * CustomSpan is a Replacement Span that writes the time at the right most position of the textview.
								 */
								CustomSpan cs =new CustomSpan();
								ssb.setSpan(cs,msg_length+1,ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
								/*
								 * Set the size of the msg_status icon to be proportional to the density of the display.
								 */
								
								int[]size = computeSize(12,10);
								TextPaint tp = text.getPaint();
								int width = (int)tp.measureText(ssb.toString());
								text.setMinWidth(width);
								text.setText(ssb);
								ImageView img = (ImageView)msg_layout.findViewById(R.id.send_status_image);
								LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
								lp.gravity=Gravity.BOTTOM;
								lp.leftMargin= -3;
								img.setLayoutParams(lp);
								img.setPadding(-1, 0, 0, -1);
								/**
								 * CHECK THE VALUE OF SENT IN THE CURRENT JSONOBJECT...IF -1 INDETERMINATE
								 * IF 0 FAILED,IF 1 SENT
								 */
								if(sent == 0)
									img.setImageDrawable(act.getResources().getDrawable(R.drawable.send_failed));
								else if(sent == 1)
									img.setImageDrawable(act.getResources().getDrawable(R.drawable.send_ok));
								msg_layout.setBackgroundResource(R.drawable.bubble_yellow);
								msg_layout.setOnLongClickListener(new OnLongClickListener()
								{
									@Override
									public boolean onLongClick(View v) 
									{
										showPopup(v,lay);
										return true;
									}
								});
								lay.addView(msg_layout,0);
							}
						}
						
						else
						{
							final LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
							LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
							View msg_layout = li.inflate(R.layout.received_msg_layout, lay,false);
							TextView text = (TextView)msg_layout.findViewById(R.id.msg);
							
							text.setTextSize(18);
							text.setTypeface(ChatList.getSegoeTypeface());
							
							int msg_length = msg.length();
							SpannableStringBuilder ssb = new SpannableStringBuilder(msg+" "+time);
							
							/*
							 * CustomSpan is a Replacement Span that writes the time at the right most position of the textview.
							 */
							CustomSpan cs =new CustomSpan();
							ssb.setSpan(cs,msg_length+1,ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
							
							TextPaint tp = text.getPaint();
							int width = (int)tp.measureText(ssb.toString());
							text.setMinWidth(width);
							text.setText(ssb);
							msg_layout.setBackgroundResource(R.drawable.bubble_green);
							msg_layout.setOnLongClickListener(new OnLongClickListener()
							{
								@Override
								public boolean onLongClick(View v) 
								{
									showPopup(v,lay);
									return true;
								}
							});
							lay.addView(msg_layout,0);
						}
					}
					
					else if(type.equals("picture"))
					{
						if(ChatWindow.getActivity() != null)
						{
							if(fromId.equals(myMacAddress))
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.sent_image, lays, false);
								final ImageView img = (ImageView)view.findViewById(R.id.sent_image);
								
								final File file = new File(msg);
								//Bitmap bmp = null;
								if(file.exists())
								{
									final int size1[] = PrepareBitmap.getImageSize(msg);
										
									Log.d(msg,"Width "+Integer.toString(size1[0])+"Height "+Integer.toString(size1[1]));
									Log.d(msg,"Width "+Double.toString(0.425*size1[0])+"Height "+Double.toString(0.46*size1[1]));
									
									
									if(size1[0]>=240 && size1[1]>240)
									{
										/*
										 * Resizing of bitmaps on UI thread would slow loading of data 
										 * We load the data off the UI 
										 */
										Thread thread = new Thread(new Runnable()
										{
											@Override
											public void run() 
											{
												// TODO Auto-generated method stub
												/*
												 * compute the pixel width and height on the current display that 
												 * would produce an image of width and height =240 on a 160dpi screen
												 */
												final int size[] =computeSize(240,240);
												Bitmap bmp = PrepareBitmap.resizeImage(msg, size[0],size[1]);
												final Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp, 4f);
												bmp.recycle();
												/*
												 * Now that we have loaded the bitmap image, lets add it up...
												 */
                                                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                                                handler.post(new Runnable()
												{
													@Override
													public void run() 
													{
														// TODO Auto-generated method stub
														LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
														img.setLayoutParams(lp);
														img.setAdjustViewBounds(true);
														if(bmps.getWidth() < size[0])
															img.setMaxWidth(bmps.getWidth());
														else
															img.setMaxWidth(getMaxWidth());
														if(bmps.getHeight() < size[1])
															img.setMaxHeight(bmps.getHeight());
														else
															img.setMaxHeight(size[1]);
														img.setImageBitmap(bmps);
													}
												});
												
												File dir = android.os.Environment.getExternalStorageDirectory();
												String path = dir.getAbsolutePath()+"/Zing/Pg/";
												File fil = new File(path);
												fil.mkdirs();
												try
												{
													fil = new File(fil.getAbsolutePath()+"/"+file.getName());
													if(!fil.exists())
													{
														//fil.mkdirs();
														fil.createNewFile();
														FileOutputStream fis = new FileOutputStream(fil);
														ByteArrayOutputStream baos = new ByteArrayOutputStream();
														//Bitmap bmp = PrepareBitmap.resizeImage(pix_bytes, 640, 640);
														bmps.compress(CompressFormat.JPEG, 100, baos);
														fis.write(baos.toByteArray());
													}
												}
												catch(Exception e)
												{
													e.printStackTrace();
												}
											}
											
										});
										thread.start();
									}
									else
									{
										Thread thread = new Thread(new Runnable()
										{

											@Override
											public void run() 
											{
												// TODO Auto-generated method stub
												final int size[] =computeSize(size1[0],size1[1]);
												final Bitmap bmp = PrepareBitmap.displayImageWithoutScaling(msg,size[0],size[1]);
                                                Handler handler = new Handler(Looper.getMainLooper());
                                                handler.post(new Runnable()
												{
													public void run()
													{
														LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(bmp.getWidth(),bmp.getHeight());
														img.setLayoutParams(lp);
														img.setAdjustViewBounds(true);
														img.setMaxWidth(getMaxWidth());
														img.setMaxHeight(size[1]);
														img.setImageBitmap(bmp);
													}
												});
											}
											
										});
										thread.start();
									}
								}
								else
								{
									/*
									 * image sizes are hardcoded because they are known to be 72px X 72px
									 */
									int size[] =computeSize(72,72);
									LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
									img.setLayoutParams(lp);
									img.setImageResource(R.drawable.ic_missing_thumbnail_picture);
								}
									
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								/*
								 * SET TAG TO THE IMAGEVIEW...TAG IS THE FILE PATH TO THE PICTURE
								 */
								img.setTag(msg);
								ImageView stat = (ImageView)view.findViewById(R.id.send_status);
								/**
								 * CHECK THE VALUE OF SENT IN THE CURRENT JSONOBJECT...IF -1 INDETERMINATE
								 * IF 0 FAILED,IF 1 SENT
								 */
								if(sent == 0)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_failed));
								else if(sent == 1)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_ok));
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
								view.setOnClickListener(new OnClickListener()
								{

									@Override
									public void onClick(View v) 
									{
										// TODO Auto-generated method stub
										try
										{
											File file = new File((String)img.getTag());
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), mtp);
											//Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
											act.startActivity(intent);
										}
										catch(Exception f)
										{
											f.printStackTrace();
										}
									}
								});
								lays.addView(view,0);
							}
							else
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.received_image, lays, false);
								final ImageView img = (ImageView)view.findViewById(R.id.received_image);
								final File file = new File(msg);
								if(file.exists())
								{
									final int size1[] = PrepareBitmap.getImageSize(msg);
									
									Log.d(msg,"Width "+Integer.toString(size1[0])+"Height "+Integer.toString(size1[1]));
									Log.d(msg,"Width "+Double.toString(0.425*size1[0])+"Height "+Double.toString(0.46*size1[1]));
									if(size1[0]>=240 && size1[1]>240)
									{
										/*
										 * Resizing of bitmaps on UI thread would slow loading of data 
										 * We load the data off the UI 
										 */
										Thread thread = new Thread(new Runnable()
										{
											@Override
											public void run() 
											{
												// TODO Auto-generated method stub
												/*
												 * compute the pixel width and height on the current display that 
												 * would produce an image of width and height =240 on a 160dpi screen
												 */
												final int size[] =computeSize(240,240);
												Bitmap bmp = PrepareBitmap.resizeImage(msg, size[0],size[1]);
												final Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp, 4f);
												bmp.recycle();
												/*
												 * Now that we have loaded the bitmap image, lets add it up...
												 */
                                                Handler handler = new Handler(Looper.getMainLooper());
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // TODO Auto-generated method stub
                                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                                                        img.setLayoutParams(lp);
                                                        img.setAdjustViewBounds(true);
                                                        if (bmps.getWidth() < size[0])
                                                            img.setMaxWidth(bmps.getWidth());
                                                        else
                                                            img.setMaxWidth(getMaxWidth());
                                                        if (bmps.getHeight() < size[1])
                                                            img.setMaxHeight(bmps.getHeight());
                                                        else
                                                            img.setMaxHeight(size[1]);
                                                        img.setImageBitmap(bmps);
                                                    }
                                                });
												
												File dir = android.os.Environment.getExternalStorageDirectory();
												String path = dir.getAbsolutePath()+"/Zing/Pg/";
												File fil = new File(path);
												fil.mkdirs();
												try
												{
													fil = new File(fil.getAbsolutePath()+"/"+file.getName());
													if(!fil.exists())
													{
														//fil.mkdirs();
														fil.createNewFile();
														FileOutputStream fis = new FileOutputStream(fil);
														ByteArrayOutputStream baos = new ByteArrayOutputStream();
														//Bitmap bmp = PrepareBitmap.resizeImage(pix_bytes, 640, 640);
														bmps.compress(CompressFormat.JPEG, 100, baos);
														fis.write(baos.toByteArray());
													}
												}
												catch(Exception e)
												{
													e.printStackTrace();
												}
											}
											
										});
										thread.start();
									}
									else
									{
										Thread thread = new Thread(new Runnable()
										{

											@Override
											public void run() 
											{
												// TODO Auto-generated method stub
												final int size[] =computeSize(size1[0],size1[1]);
												final Bitmap bmp = PrepareBitmap.displayImageWithoutScaling(msg,size[0],size[1]);
                                                Handler handler = new Handler(Looper.getMainLooper());
                                                handler.post(new Runnable()
												{
													public void run()
													{
														LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(bmp.getWidth(),bmp.getHeight());
														img.setLayoutParams(lp);
														img.setAdjustViewBounds(true);
														img.setMaxWidth(getMaxWidth());
														img.setMaxHeight(size[1]);
														img.setImageBitmap(bmp);
													}
												});
											}
											
										});
										thread.start();
									}
								}
								else
								{
									/*
									 * image sizes are hardcoded because they are known to be 72px X 72px
									 */
									int size[] =computeSize(72,72);
									LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
									img.setLayoutParams(lp);
									img.setImageResource(R.drawable.ic_missing_thumbnail_picture);
								}
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								//img.setImageBitmap(bmps);
								/*
								 * SET TAG TO THE IMAGEVIEW...TAG IS THE FILE PATH TO THE PICTURE
								 */
								img.setTag(msg);
								
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_green));
								view.setOnClickListener(new OnClickListener()
								{

									@Override
									public void onClick(View v) 
									{
										// TODO Auto-generated method stub
										try
										{
											File file = new File((String)img.getTag());
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), mtp);
											//Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
											act.startActivity(intent);
										}
										catch(Exception f)
										{
											f.printStackTrace();
										}
									}
									
								});
								lays.addView(view,0);
							}
						
							
						}
					}
					
					else if(type.equals("audio"))
					{
						if(ChatWindow.getActivity() != null)
						{
							if(fromId.equals(myMacAddress))
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.sent_audio, lays, false);
								ImageView play_icon = (ImageView)view.findViewById(R.id.play);
								play_icon.setTag(msg);
								final File file = new File((String)play_icon.getTag());
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								TextView title_view = (TextView)view.findViewById(R.id.title);
								title_view.setText(file.getName());
								title_view.setTextSize(15);
								title_view.setTypeface(ChatList.getSegoeTypeface());
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								/**
								 * ADD TOUCH LISTENER TO THE PLAY ICON
								 */
								play_icon.setOnTouchListener(new OnTouchListener()
								{
									@Override
									public boolean onTouch(View v,MotionEvent event)
									{
										if(event.getAction() == MotionEvent.ACTION_DOWN)
										{
											v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
										}
										else if(event.getAction() == MotionEvent.ACTION_UP)
										{
											v.setBackgroundColor(0);
											ImageView img = (ImageView)v;
											img.setImageDrawable(act.getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
											
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), mtp);
											//Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
											act.startActivity(intent);
											//playAudio(img);
										}
										return true;
									}
									
								});
								ImageView stat = (ImageView)view.findViewById(R.id.send_status);
								/**
								 * CHECK THE VALUE OF SENT IN THE CURRENT JSONOBJECT...IF -1 INDETERMINATE
								 * IF 0 FAILED,IF 1 SENT
								 */
								if(sent == 0)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_failed));
								else if(sent == 1)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_ok));
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
								lays.addView(view,0);
							}
							else
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.received_audio, lays, false);
								ImageView play_icon = (ImageView)view.findViewById(R.id.play);
								play_icon.setTag(msg);
								final File file = new File((String)play_icon.getTag());
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								TextView title_view = (TextView)view.findViewById(R.id.title);
								title_view.setText(file.getName());
								title_view.setTextSize(15);
								title_view.setTypeface(ChatList.getSegoeTypeface());
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								/**
								 * ADD TOUCH LISTENER TO THE PLAY ICON
								 */
								play_icon.setOnTouchListener(new OnTouchListener()
								{

									@Override
									public boolean onTouch(View v,MotionEvent event)
									{
										if(event.getAction() == MotionEvent.ACTION_DOWN)
										{
											v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
										}
										else if(event.getAction() == MotionEvent.ACTION_UP)
										{
											ImageView img = (ImageView)v;
											img.setImageDrawable(act.getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), mtp);
											//Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
											act.startActivity(intent);
										}
										return true;
									}
									
								});
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_green));
								lays.addView(view,0);
							}
						
							
						}

					}
					
					else if(type.equals("video"))
					{
						if(ChatWindow.getActivity() != null)
						{
							if(fromId.equals(myMacAddress))
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.sent_video, lays, false);
								ImageView play_icon = (ImageView)view.findViewById(R.id.play);
								play_icon.setTag(msg);
								final File file = new File((String)play_icon.getTag());
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								TextView title_view = (TextView)view.findViewById(R.id.title);
								title_view.setText(file.getName());
								title_view.setTextSize(15);
								title_view.setTypeface(ChatList.getSegoeTypeface());
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								/**
								 * ADD TOUCH LISTENER TO THE PLAY ICON
								 */
								play_icon.setOnTouchListener(new OnTouchListener()
								{
									@Override
									public boolean onTouch(View v,MotionEvent event)
									{
										if(event.getAction() == MotionEvent.ACTION_DOWN)
										{
											v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
										}
										else if(event.getAction() == MotionEvent.ACTION_UP)
										{
											v.setBackgroundColor(0);
											ImageView img = (ImageView)v;
											img.setImageDrawable(act.getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
											
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1).trim().toLowerCase());
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), "video/*");
											//name.substring(pos+1)
											Toast.makeText(act, name, Toast.LENGTH_LONG).show();
											act.startActivity(intent);
											//playAudio(img);
										}
										return true;
									}
									
								});
								ImageView stat = (ImageView)view.findViewById(R.id.send_status);
								/**
								 * CHECK THE VALUE OF SENT IN THE CURRENT JSONOBJECT...IF -1 INDETERMINATE
								 * IF 0 FAILED,IF 1 SENT
								 */
								if(sent == 0)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_failed));
								else if(sent == 1)
									stat.setImageDrawable(act.getResources().getDrawable(R.drawable.send_ok));
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
								lays.addView(view,0);
							}
							else
							{
								LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
								LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
								View view = li.inflate(R.layout.received_video, lays, false);
								ImageView play_icon = (ImageView)view.findViewById(R.id.play);
								play_icon.setTag(msg);
								final File file = new File((String)play_icon.getTag());
								TextView time_view = (TextView)view.findViewById(R.id.time);
								time_view.setText(time);
								TextView title_view = (TextView)view.findViewById(R.id.title);
								title_view.setText(file.getName());
								title_view.setTextSize(15);
								title_view.setTypeface(ChatList.getSegoeTypeface());
								time_view.setTextSize(10);
								time_view.setTypeface(ChatList.getSegoeTypeface());
								/**
								 * ADD TOUCH LISTENER TO THE PLAY ICON
								 */
								play_icon.setOnTouchListener(new OnTouchListener()
								{

									@Override
									public boolean onTouch(View v,MotionEvent event)
									{
										if(event.getAction() == MotionEvent.ACTION_DOWN)
										{
											v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
										}
										else if(event.getAction() == MotionEvent.ACTION_UP)
										{
											ImageView img = (ImageView)v;
											img.setImageDrawable(act.getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
											
											String name = file.getName();
											int pos = name.indexOf('.');
											String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1).trim().toLowerCase());
											Intent intent = new Intent();
											intent.setType(mtp);
											intent.setAction(Intent.ACTION_VIEW);
											intent.setDataAndType(Uri.fromFile(file), "video/*");
											Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
											act.startActivity(intent);
										}
										return true;
									}
									
								});
								view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_green));
								lays.addView(view,0);
							}
						}
					}
					else
					{
                        Log.d("Message", msg);
                        if(ChatWindow.getActivity() != null)
                        {
                            Log.d("Message 2", msg+" "+fromId+" "+myMacAddress);

                            if(fromId.equals(myMacAddress))
                            {
                                Log.d("Message", msg);
                                LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
                                LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
                                View view = li.inflate(R.layout.call_msg, lays, false);
                                ImageView call_status = (ImageView)view.findViewById(R.id.call_status);
                                if(msg.startsWith("you"))
                                {
                                    TextView call_text = (TextView)view.findViewById(R.id.call_text);
                                    call_status.setImageResource(R.drawable.outgoing_call);

                                    SpannableString ss = new SpannableString(msg+" "+zing_id+time);
                                    ss.setSpan(new CustomSpan(),ss.length()-time.length(),ss.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    lays.addView(view,0);
                                    call_text.setText(ss);
                                }
                                else
                                {
                                    TextView call_text = (TextView)view.findViewById(R.id.call_text);
                                    call_status.setImageResource(R.drawable.outgoing_call);

                                    SpannableString ss = new SpannableString(msg+" "+zing_id+time);
                                    ss.setSpan(new CustomSpan(),ss.length()-time.length(),ss.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    lays.addView(view,0);
                                    call_text.setText(ss);
                                }
                            }

                            else
                            {
                                LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
                                LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
                                View view = li.inflate(R.layout.call_msg, lays, false);
                                ImageView call_status = (ImageView)view.findViewById(R.id.call_status);
                                if(msg.startsWith("missed"))
                                {
                                    TextView call_text = (TextView)view.findViewById(R.id.call_text);
                                    call_status.setImageResource(R.drawable.missed_call);

                                    SpannableString ss = new SpannableString("you "+msg+" "+zing_id+time);
                                    ss.setSpan(new CustomSpan(),ss.length()-time.length(),ss.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    call_text.setText(ss);
                                    lays.addView(view,0);
                                }

                                else if(msg.startsWith("received"))
                                {
                                    TextView call_text = (TextView)view.findViewById(R.id.call_text);
                                    call_status.setImageResource(R.drawable.received_call);

                                    SpannableString ss = new SpannableString("you "+msg+" "+zing_id+time);
                                    ss.setSpan(new CustomSpan(),ss.length()-time.length(),ss.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    call_text.setText(ss);
                                    lays.addView(view,0);
                                }

                                else if(msg.startsWith("declined"))
                                {
                                    TextView call_text = (TextView)view.findViewById(R.id.call_text);
                                    call_status.setImageResource(R.drawable.declined_call);

                                    SpannableString ss = new SpannableString("you "+msg+" "+zing_id+time);
                                    ss.setSpan(new CustomSpan(),ss.length()-time.length(),ss.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    call_text.setText(ss);
                                    lays.addView(view,0);
                                }

                            }
                        }
					}
					
			}
				
				
				/**
				 * CHECK IF THIS CONVERSATION HAS MORE UNFETCHED MESSAGES IN THE DATABASE,
				 * IF SO, REMOVE THE LOAD MORE MESSAGES BUTTON FROM ITS CURRENT POSITION AND ADD IT TO POSITION 0
				 * ELSE MAKE THE BUTTON GONE...
				 */
				LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
				Button load_more = (Button)ChatWindow.getActivity().findViewById(R.id.load_more);
				if(hasMore)
				{
					lay.removeView(load_more);
					lay.addView(load_more,0);
					load_more.setVisibility(Button.VISIBLE);
				}
				else
				{
					load_more.setVisibility(Button.GONE);
					addDate(cal);
				}
				
		}
		
		catch(JSONException e)
		{
			e.printStackTrace();
			
		}
		
		
	}
	
	public static String getRecipientMacAddress()
	{
		return mac_address;
	}

	private void showPopup(final View arg0, ViewGroup lays)
	{
		//Toast.makeText(arg0.getContext(), "Long Click", Toast.LENGTH_LONG).show();
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(arg0.getContext());
		RelativeLayout view = (RelativeLayout)li.inflate(R.layout.chat_options, lays, false);
		//view.setBackgroundResource(R.drawable.options);
		LinearLayout lay = (LinearLayout)view.getChildAt(0);
		TextView tview = (TextView)lay.findViewById(R.id.textView1);
		tview.setOnClickListener(new OnClickListener()
		
		{

			@Override
			public void onClick(View v) 
			{
				
				ClipboardManager clip = (ClipboardManager)v.getContext().getSystemService(Activity.CLIPBOARD_SERVICE);
				TextView text = (TextView)arg0.findViewById(R.id.msg);
				clip.setText(text.getText());
				pop.dismiss();
				
			}
			
		});
		
		tview.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.gray));
					
				}
				
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					v.setBackgroundDrawable(null);
				}
				return false;
			}
			
		});
		pop.setContentView(view);
		pop.setTouchable(true);
		pop.setOutsideTouchable(true);
		pop.setBackgroundDrawable(arg0.getResources().getDrawable(R.drawable.empty));
		pop.setTouchInterceptor(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_OUTSIDE)
				{
					pop.dismiss();
					return false;
				}
				
				return false;
			}
			
		});
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		pop.setHeight(LayoutParams.WRAP_CONTENT);
		
		int[] location = new int[2];
		arg0.getLocationOnScreen(location);
		float height = arg0.getHeight()/((TextView)arg0.findViewById(R.id.msg)).getLineCount();
		pop.showAtLocation(arg0, Gravity.TOP, location[0]-arg0.getWidth()/2, location[1]-(Math.round(height)/2+30));
		ImageView img = (ImageView)view.getChildAt(1);
		img.setTranslationX(arg0.getWidth()/2+location[0]);
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
	
	private int getMaxWidth()
	{
		WindowManager wm = (WindowManager)act.getSystemService(Activity.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int dpi = dm.densityDpi;
		int size = width - (int)((dpi*40)/160);
		return size;
	}
	
	private void addDate(Calendar cal)
	{
		if(year == cal.get(Calendar.YEAR))
		{
			if(month == cal.get(Calendar.MONTH))
			{
				if(day == cal.get(Calendar.DATE))
				{
					String dates = Time.getDateAsString(cal);
					date = new TextView(ChatList.getCurrentActivity());
					TextPaint tp = date.getPaint();
					Rect rect = new Rect();
					tp.getTextBounds(dates, 0,dates.length(), rect);
					
					
					int size[] = computeSize(100,rect.height()+8);
					date.setGravity(Gravity.CENTER);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
                    lp.topMargin = 10;
                    lp.bottomMargin = 10;
					lp.gravity = Gravity.CENTER;
					date.setLayoutParams(lp);
					date.setText("TODAY");
					//date.setPadding(20,20, 20, 20);
					Log.d("date1", "TODAY");
					date.setBackgroundResource(R.drawable.date_balloon);
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					lays.addView(date,0);
				}
				
				else if((day-cal.get(Calendar.DATE) == 1))
				{
					String dates = Time.getDateAsString(cal);
					date = new TextView(ChatList.getCurrentActivity());
					TextPaint tp = date.getPaint();
					Rect rect = new Rect();
					tp.getTextBounds(dates, 0,dates.length(), rect);
					
					
					int size[] = computeSize(100,rect.height()+8);
					date.setGravity(Gravity.CENTER);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
					lp.gravity = Gravity.CENTER;
                    lp.topMargin = 10;
                    lp.bottomMargin = 10;
					date.setLayoutParams(lp);
					date.setText("YESTERDAY");
					Log.d("date2", "Yesterday");
					date.setBackgroundResource(R.drawable.date_balloon);
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					lays.addView(date,0);
				}
				
				else if(week == cal.get(Calendar.WEEK_OF_MONTH))
				{
					String dates = Time.getDayOfWeek(cal);
					date = new TextView(ChatList.getCurrentActivity());
					TextPaint tp = date.getPaint();
					Rect rect = new Rect();
					tp.getTextBounds(dates, 0,dates.length(), rect);
					
					
					int size[] = computeSize(100,rect.height()+8);
					date.setGravity(Gravity.CENTER);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
					lp.gravity = Gravity.CENTER;
                    lp.topMargin = 10;
                    lp.bottomMargin = 10;
					date.setLayoutParams(lp);
					date.setText(dates.toUpperCase());
					Log.d("date3", Time.getDayOfWeek(cal));
					date.setBackgroundResource(R.drawable.date_balloon);
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					lays.addView(date,0);
				}
				
				else
				{
					String dates = Time.getDateAsString(cal);
					date = new TextView(ChatList.getCurrentActivity());
					TextPaint tp = date.getPaint();
					Rect rect = new Rect();
					tp.getTextBounds(dates, 0,dates.length(), rect);
					
					
					int size[] = computeSize(100,rect.height()+8);
					date.setGravity(Gravity.CENTER);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
					lp.gravity = Gravity.CENTER;
                    lp.topMargin = 10;
                    lp.bottomMargin = 10;
					date.setLayoutParams(lp);
					Log.d("date4", Time.getDateAsString(cal));
					date.setText(dates.toUpperCase());
					date.setBackgroundResource(R.drawable.date_balloon);
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					lays.addView(date,0);
				}
			}
			
			else
			{
				String dates = Time.getDateAsString(cal);
				date = new TextView(ChatList.getCurrentActivity());
				TextPaint tp = date.getPaint();
				Rect rect = new Rect();
				tp.getTextBounds(dates, 0,dates.length(), rect);
				
				
				int size[] = computeSize(100,rect.height()+8);
				date.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
				lp.gravity = Gravity.CENTER;
                lp.topMargin = 10;
                lp.bottomMargin = 10;
				date.setLayoutParams(lp);
				date.setText(dates.toUpperCase());
				Log.d("date5", Time.getDateAsString(cal));
				date.setBackgroundResource(R.drawable.date_balloon);
				LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
				lays.addView(date,0);
			}
		}
		
		else
		{
			String dates = Time.getDateAsString(cal);
			date = new TextView(ChatList.getCurrentActivity());
			TextPaint tp = date.getPaint();
			Rect rect = new Rect();
			tp.getTextBounds(dates, 0,dates.length(), rect);
			
			
			int size[] = computeSize(100,rect.height()+8);
			date.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size[0],size[1]);
			lp.gravity = Gravity.CENTER;
            lp.topMargin = 10;
            lp.bottomMargin = 10;
			date.setLayoutParams(lp);
			date.setText(dates.toUpperCase());
			Log.d("date6", Time.getDateAsString(cal));
			date.setBackgroundResource(R.drawable.date_balloon);
			LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
			lays.addView(date,0);
		}
	}
	
	
	
}
package com.karabow.zing;


import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;


public class ChatMessageHandler extends AsyncTask<Socket,String,String> 
{
	private JSONObject jobj;
	private DataInputStream dis ;
	private Socket sock;
	private volatile int save=0;
	private Activity act,acts;
	private String file_type, macAddress, zing_id;
	private ChatMessageHandler cmh;
	private LinearLayout lay;
	private LayoutInflater li;
	private Thread thread;
	private View view;
	private File file;
	private String myMacAddress;
	
	public ChatMessageHandler(Socket sock)
	{
		this.sock = sock;
		cmh = this;
		final SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
    	myMacAddress = spref.getString("mac", "mac");
    	myMacAddress = myMacAddress.replace(":", "");
		
	}
	@Override
	protected String doInBackground(Socket... arg0) 
	{
		try
		{
			//sock = arg0[0];
			//Create a DataInputStream whih is hooked up to the Socket's InputStream
			dis = new DataInputStream(sock.getInputStream());
			
			//byte[] data = new byte[2*1024];
			//Read the first String data from the stream, this is the json data
			String data = dis.readUTF();
			//dis.read(buffer)
			//Create a JSON Object from the data
			jobj = new JSONObject(data);
			zing_id = jobj.getString("zing_id");
			file_type = jobj.getString("file_type");
			macAddress = jobj.getString("mac_address").trim();
			if(!file_type.equals("message"))
			{
				/**
				 * cause this current thread to pause action until user selects an action
				 */
				//Toast.makeText(ChatWindow.getActivity(), "Waiting", Toast.LENGTH_LONG).show();
				publishProgress("hello");
				Thread.sleep(10000);
                int count = 0;
                while(count < 4)
                {
                    if(save != 1)
                    {
                        Thread.sleep(1000);
                        count++;
                    }
                    else
                        break;
                }
				//Thread.sleep(2000);
				//thread.join();
				
				//.wait();
			}
			Log.d("this sent",data);
			
			//if the content of the json is message, return the string...
			if(jobj.getString("file_type").equals("message"))
			{
				return "text";
			}
			else
			{
				if(save == 1)
				{
					return saveFile();
				}

				else
                {
                    /*
                     Write a message to the sender that the file transfer request was not honoured.
                     */
                    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                    dos.writeUTF("ref");
                    sock.close();
                    return null;
                }

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void onProgressUpdate(String...strings )
	{
		/*if(ChatWindow.getActivity() != null )
			acts = ChatWindow.getActivity();
		else if(ChatList.getActivity() != null)
			acts = ChatList.getActivity();*/
		acts = ((Activity)ChatList.getCurrentActivity());
		AlertDialog.Builder alert = new AlertDialog.Builder(acts);
		alert.setMessage("A file tranfer from "+zing_id+" has been requested, do you wish to accept?");
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				save = 1;
				dialog.dismiss();
				/*if(ChatWindow.getActivity() != null)
				{
					li = LayoutInflater.from(ChatWindow.getActivity());
					lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					if(file_type.equals("picture"))
					{
						view = li.inflate(R.layout.received_image, lay, false);
						view.setBackground(acts.getResources().getDrawable(R.drawable.bubble_pending));
						lay.addView(view);
					}
						
				}*/
				//thread.start();
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				//alert.
				//progress.dismiss();
				try
				{
						save = 2;
						//sock.close();
						dialog.dismiss();
						//thread.start();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		alert.setCancelable(false);
		if(!acts.isFinishing())
		{
		    //show dialog
			alert.show();
		}
		else
		{
			onProgressUpdate(strings);
		}

	}
	
	public void onPostExecute(String data)
	{
		
		Uri sounduri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.purr); 
		/*if(ChatWindow.getActivity() != null )
			act = ChatWindow.getActivity();
		else if(ChatList.getActivity() != null)
			act = ChatList.getActivity();*/
        if(ChatList.getCurrentActivity() instanceof Activity)
		    act = ((Activity)ChatList.getCurrentActivity());
        /*
         *Just in case the program somehow gets to this point. We have to just save the message and exit.
         */
        else
        {
            try
            {
                if(data != null && data.equals("text"))
                {
                    String msgReceived = jobj.getString("msg").trim();
                    DatabaseHelper.addMessageReceived(macAddress, myMacAddress, msgReceived,true,"text");

                }
                else if(data != null && file_type.equals("picture") )
                {
                    DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"picture");
                }

                else if(data != null && file_type.equals("audio") )
                {
                    DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"audio");
                }
                else if(data != null && file_type.equals("video") )
                {
                    DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"video");
                }
                    return;

            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

		MediaPlayer mp = MediaPlayer.create(act, sounduri);
		
		mp.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				// TODO Auto-generated method stub
				mp.stop();
				mp.release();
				mp = null;
			}
			
		});
		mp.start();
		if(data != null && data.equals("text") )
		{
			try
			{
				String msgReceived = jobj.getString("msg").trim();
				//make provision to allow for me to know if it is the current chat activity
				if(ChatWindow.getActivity() != null && ChatWindow.getRecipientMacAddress().equals(macAddress))
				{	   
						LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
						LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
						final ScrollView lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
						View msg_layout = li.inflate(R.layout.received_msg_layout, lay,false);
						TextView text = (TextView)msg_layout.findViewById(R.id.msg);
						text.setTextSize(18);
						text.setTypeface(ChatList.getSegoeTypeface());
						
						int msg_length = msgReceived.length();
						SpannableStringBuilder ssb = new SpannableStringBuilder(msgReceived+" "+Time.getTime());
						
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
						//ChatList.getMediaPlayerReceivedFromCurrentZinger();
						/*
						 * Store the received textual message in the database
						 */
						DatabaseHelper.addMessageReceived(macAddress, myMacAddress, msgReceived,false,"text");
						/*
						 * Let's update the Chats Fragment, making this msg the first and updating the text data
						 */
						if(Chats.getFragActivity() != null && Chats.getChatsView()!= null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							/*
							 * retrieve the ID for the sender of this message from the Zinger's table
							 */
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null )
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
					            tv.setText(msgReceived); 
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
				}
				
				//ensure ChatList is not null and the message received does not belong to the current zinger
				else if(ChatList.getCurrentActivity() != null)
				{	
					showNotification(msgReceived);
					DatabaseHelper.addMessageReceived(macAddress, myMacAddress, msgReceived,true,"text");
					if(Chats.getFragActivity() != null && Chats.getChatsView()!= null)
					{
						LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
						/*
						 * retrieve the ID for the sender of this message from the Zinger's table
						 */
						View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
						
						if(l!=null )
						{
							github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
							Log.d("is L null", Boolean.toString(l==null));
				            tv.setText(msgReceived); 
				            ImageView img = (ImageView)l.findViewById(R.id.msg_status);
				            Log.d("is img null", Boolean.toString(img==null));
							img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
							chats_layout.removeView(l);
							chats_layout.addView(l, 0);
						}
					}
				}

                else
                {

                }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}	
	}
		
		else if(data != null && file_type.equals("picture") )
		{
			if(ChatWindow.getActivity() != null && ChatWindow.getRecipientMacAddress().equals(macAddress))
			{	
				
				final ScrollView lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
				//View view = li.inflate(R.layout.received_image, lay, false);
				li = LayoutInflater.from(ChatWindow.getActivity());
				lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
				view = li.inflate(R.layout.received_image, lay, false);
				view.setVisibility(View.VISIBLE);
				final ImageView img = (ImageView)view.findViewById(R.id.received_image);
				file = new File(data);
				drawImage(img,data);
				img.setTag(data);
				TextView time = (TextView)view.findViewById(R.id.time);
				time.setText(Time.getTime());
				time.setTextSize(10);
				time.setTypeface(ChatList.getSegoeTypeface());
				img.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						// TODO Auto-generated method stub
						File file = new File((String)v.getTag());
						String name = file.getName();
						int pos = name.indexOf('.');
						String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
						Intent intent = new Intent();
						intent.setType(mtp);
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(file), mtp);
						Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
						act.startActivity(intent);
					}
					
				});
				view.setBackgroundResource(R.drawable.bubble_green);
				lay.addView(view);
				lays.post(new Runnable()
				{
					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						lays.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,false,"picture");
				
				if(ChatList.getCurrentActivity() != null)
				{
					if(ChatList.getCurrentActivity() != null)
					{
						if(Chats.getFragActivity() != null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null )
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
								Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.chats_image_unread); 
								int[] size = computeSize(20,20);
								d.setBounds(0, 0, size[0], size[1]); 
					            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
					            SpannableString ss = new SpannableString("ane Picture");
					            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					            tv.setText(ss); 
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
					}
				}
				
			}
			else if(ChatList.getCurrentActivity() != null)
			{
				showNotification("Picture received");
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"picture");
				if(ChatList.getCurrentActivity() != null)
				{
					if(ChatList.getCurrentActivity() != null)
					{
						if(Chats.getFragActivity() != null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null)
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
								Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.chats_image_unread); 
								int[] size = computeSize(20,20);
								d.setBounds(0, 0, size[0], size[1]); 
					            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
					            SpannableString ss = new SpannableString("ane Picture");
					            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					            ImageView img = (ImageView)l.findViewById(R.id.msg_status);
								img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
					            tv.setText(ss); 
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
					}
				}
			}
		}
		
		else if(data != null && file_type.equals("audio") )
		{
			if(ChatWindow.getActivity() != null && ChatWindow.getRecipientMacAddress().equals(macAddress))
			{	
				LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
				LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
				final ScrollView lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
				View view = li.inflate(R.layout.received_audio, lay, false);
				ImageView play_icon = (ImageView)view.findViewById(R.id.play);
				play_icon.setTag(data);
				final File file = new File((String)play_icon.getTag());
				TextView time = (TextView)view.findViewById(R.id.time);
				time.setText(Time.getTime());
				TextView title = (TextView)view.findViewById(R.id.title);
				title.setText(file.getName());
				title.setTextSize(15);
				title.setTypeface(ChatList.getSegoeTypeface());
				time.setTextSize(10);
				time.setTypeface(ChatList.getSegoeTypeface());
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
							v.setBackgroundColor(ChatWindow.getActivity().getResources().getColor(R.color.baby_blue));
						}
						else if(event.getAction() == MotionEvent.ACTION_UP)
						{
							v.setBackgroundColor(0);
							ImageView img = (ImageView)v;
							img.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
							
							String name = file.getName();
							int pos = name.indexOf('.');
							String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
							Intent intent = new Intent();
							intent.setType(mtp);
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file), mtp);
							Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
							act.startActivity(intent);
							//playAudio(img);
						}
						return true;
					}
					
				});
				view.setBackgroundResource(R.drawable.bubble_green);
				lay.addView(view);
				lays.post(new Runnable()
				{
					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						lays.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,false,"audio");
				
				if(ChatList.getCurrentActivity() != null)
				{
					if(ChatList.getCurrentActivity() != null)
					{
						if(Chats.getFragActivity() != null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null)
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
								Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.ic_voice_note); 
								int[] size = computeSize(20,20);
								d.setBounds(0, 0, size[0], size[1]); 
					            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
					            SpannableString ss = new SpannableString("ane Audio");
					            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					            tv.setText(ss); 
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
					}
				}
				
			}
			else if(ChatList.getCurrentActivity() != null)
			{
				
				showNotification("Audio received");
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"audio");
					if(ChatList.getCurrentActivity() != null)
					{
						if(Chats.getFragActivity() != null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null )
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
								Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.ic_voice_note); 
								int[] size = computeSize(20,20);
								d.setBounds(0, 0, size[0], size[1]); 
					            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
					            SpannableString ss = new SpannableString("ane Audio");
					            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					            tv.setText(ss); 
					            ImageView img = (ImageView)l.findViewById(R.id.msg_status);
								img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
					}
				
			}
		}
		
		else if(data != null && file_type.equals("video") )
		{
			if(ChatWindow.getActivity() != null && ChatWindow.getRecipientMacAddress().equals(macAddress))
			{	
				LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
				LinearLayout lay = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
				final ScrollView lays = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
				View view = li.inflate(R.layout.received_video, lay, false);
				ImageView play_icon = (ImageView)view.findViewById(R.id.play);
				play_icon.setTag(data);
				final File file = new File((String)play_icon.getTag());
				TextView time = (TextView)view.findViewById(R.id.time);
				time.setText(Time.getTime());
				TextView title = (TextView)view.findViewById(R.id.title);
				title.setText(file.getName());
				title.setTextSize(15);
				title.setTypeface(ChatList.getSegoeTypeface());
				time.setTextSize(10);
				time.setTypeface(ChatList.getSegoeTypeface());
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
							v.setBackgroundColor(ChatWindow.getActivity().getResources().getColor(R.color.baby_blue));
						}
						else if(event.getAction() == MotionEvent.ACTION_UP)
						{
							v.setBackgroundColor(0);
							ImageView img = (ImageView)v;
							img.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.inline_audio_pause_pressed));
							
							String name = file.getName();
							int pos = name.indexOf('.');
							String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
							Intent intent = new Intent();
							intent.setType(mtp);
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file), mtp);
							Toast.makeText(act, name.substring(pos+1), Toast.LENGTH_LONG).show();
							act.startActivity(intent);
							//playAudio(img);
						}
						return true;
					}
					
				});
				view.setBackgroundResource(R.drawable.bubble_green);
				lay.addView(view);
				lays.post(new Runnable()
				{
					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						lays.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,false,"video");
				
					if(ChatList.getCurrentActivity() != null)
					{
						if(Chats.getFragActivity() != null)
						{
							LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
							View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
							
							if(l!=null)
							{
								github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
								Log.d("is L null", Boolean.toString(l==null));
								Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.video_msg); 
								int[] size = computeSize(20,20);
								d.setBounds(0, 0, size[0], size[1]); 
					            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
					            SpannableString ss = new SpannableString("ane Video");
					            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					            tv.setText(ss); 
								chats_layout.removeView(l);
								chats_layout.addView(l, 0);
							}
						}
				}
				
			}
			else if(ChatList.getCurrentActivity() != null)
			{
				showNotification("Video received");
				DatabaseHelper.addMessageReceived(macAddress, myMacAddress, data,true,"video");
				
				if(ChatList.getCurrentActivity() != null)
				{
					if(Chats.getFragActivity() != null)
					{
						LinearLayout chats_layout = (LinearLayout)Chats.getChatsView().findViewById(R.id.chats_layout);
						View l = chats_layout.findViewById(DatabaseHelper.getId(macAddress));
						
						if(l!=null)
						{
							github.ankushsachdeva.emojicon.EmojiconTextView tv = (github.ankushsachdeva.emojicon.EmojiconTextView)l.findViewById(R.id.last_message);
							Log.d("is L null", Boolean.toString(l==null));
							Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.video_msg); 
							int[] size = computeSize(20,20);
							d.setBounds(0, 0, size[0], size[1]); 
				            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM); 
				            SpannableString ss = new SpannableString("ane Video");
				            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				            tv.setText(ss); 
				            ImageView img = (ImageView)l.findViewById(R.id.msg_status);
							img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.msg_status_unread));
							chats_layout.removeView(l);
							chats_layout.addView(l, 0);
						}
					}
				}
			}
		}
	}

	private Bitmap messageCount() 
	{
		Bitmap output = Bitmap.createBitmap(30,30, Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setColor(ChatList.getCurrentActivity().getResources().getColor(R.color.baby_blue));
		Canvas canvas = new Canvas(output);
		canvas.drawCircle(15, 15, 15, paint);
		paint.setColor(ChatList.getCurrentActivity().getResources().getColor(R.color.white));
		canvas.drawText("1", 15, 15, paint);
		return output;
	}
	
	private String saveFile()
	{
		try
		{
			//create a byte array to read any non-text data 
			Log.d("Saving File", "Yes");
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            dos.writeUTF("ok");
			byte buff[] = new byte[8*1024];
			//Read the filr name from the JSON Object
			String file_name = jobj.getString("file_name");
			//Get the directory for the External Storage
			File root = android.os.Environment.getExternalStorageDirectory();
			
			//attempt creating a new directory using the file_type
			File dir = new File(root.getAbsolutePath()+"/Zing/"+jobj.getString("file_type")+"/");
			if(dir.exists() == false)
			{
				dir.mkdirs();
			}
			
			//create a new file in the current directory
			File file = new File(dir.getAbsolutePath()+"/"+Time.getTimeStamp().getTime()+file_name);
            Log.d("Filename",file.getAbsolutePath());
			file.createNewFile();
			//create a new BufferedOutputStream and wrap it over a FileOutputStream
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			long bytes_read = 0;
			int count =0;
			while((count = dis.read(buff)) != -1)
			{
				bytes_read+=count;
				bos.write(buff,0,count);
				Log.d("Bytes Written",Long.toString(bytes_read));
			}
			
			bos.flush();
			bos.close();
			dis.close();
			return file.getAbsolutePath();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return " ";
			
		}
	}
	
	private void showPopup(final View arg0, ScrollView lays)
	{
		
			Toast.makeText(arg0.getContext(), "Long Click", Toast.LENGTH_LONG).show();
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
			img.setTranslationX(location[0]);
	}
	
	private void showNotification(String message)
	{
		
		NotificationManager nm = (NotificationManager)act.getSystemService(Context.NOTIFICATION_SERVICE);
		Uri sound = Uri.parse("android.resource://com.karabow.zing/"+R.raw.purr); 
		NotificationCompat.Builder ncb = new NotificationCompat.Builder(ChatList.getCurrentActivity());
		ncb.setSmallIcon(R.drawable.ic_launcher);
		ncb.setContentTitle(zing_id);
		ncb.setContentText(message);
		ncb.setSound(sound);
		nm.notify(0, ncb.build());
		ViewGroup anchor = null;
		Activity act = null;
		
		if(ChatWindow.getActivity() != null)
		{
			anchor = (ViewGroup) ChatWindow.getActivity().getActionBar().getCustomView();
			act = ChatWindow.getActivity();
		}
		else if(ChatList.getCurrentActivity() != null)
		{
			anchor = (LinearLayout)(LinearLayout)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.main_layout);
			act = ((Activity)ChatList.getCurrentActivity());
		}
		LayoutInflater li = LayoutInflater.from(act);
		View v = li.inflate(R.layout.notification, anchor, false);
		TextView name = (TextView)v.findViewById(R.id.name);
		name.setText(zing_id);
		TextView msg = (TextView)v.findViewById(R.id.text);
		msg.setText(message);
		final ImageView image = (ImageView)v.findViewById(R.id.imageView1);
		
		final byte []pix = DatabaseHelper.getZIngerProfilePix(macAddress);
		
		image.post(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				Bitmap bmp1 =null,bmp2=null;
				Activity act = null;
				if(ChatWindow.getActivity() != null )
					act = ChatWindow.getActivity();
				else if(ChatList.getCurrentActivity() != null)
					act = ((Activity)ChatList.getCurrentActivity());
				if(pix!=null && pix.length > 0)
				{
					bmp1 = PrepareBitmap.resizeImage(pix, image.getWidth(),image.getHeight());
					bmp2 = PrepareBitmap.drawCircularImage(bmp1, 5*image.getWidth(),5*image.getHeight());
				}
				else if(pix==null || pix.length==0)
				{
					bmp1 = PrepareBitmap.resizeImage(act.getResources(), R.drawable.user, image.getWidth(),image.getHeight());
					bmp2 = PrepareBitmap.drawCircularImage(bmp1, 5*image.getWidth(),5*image.getHeight());
				}
				image.setImageBitmap(bmp2);
				
			}
			
		});
		
		final PopupWindow pop = new PopupWindow();
		pop.setContentView(v);
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		final LinearLayout not_lay = (LinearLayout)v.findViewById(R.id.notification_layout);
		pop.setWidth(LayoutParams.MATCH_PARENT);
		pop.setHeight(160);
		TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,-1.0f,Animation.RELATIVE_TO_PARENT,0.0f
													  ,Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
		ta.setDuration(1000);
		/*
		 *Start entry animation 
		 */
		not_lay.startAnimation(ta);
		/*
		 * Wait 2.5seconds before starting exit animation
		 */
		not_lay.postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,0.0f,Animation.RELATIVE_TO_PARENT,1.0f
						  									  ,Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
				ta.setDuration(1000);
				ta.setAnimationListener(new AnimationListener()
				{
					@Override
					public void onAnimationEnd(Animation animation)
					{
						// TODO Auto-generated method stub
						pop.dismiss();
					}

					@Override
					public void onAnimationRepeat(
							Animation animation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationStart(Animation animation) 
					{
						// TODO Auto-generated method stub
						
					}
					
				});
				not_lay.startAnimation(ta);
			}
			
		}, 2500);
		/*
		 * perform more background checks to make sure anchor is not null
		 */
		if(anchor == null)
		{
			if(ChatList.getCurrentActivity() != null)
			{
				anchor = (LinearLayout)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.main_layout);
				if(anchor == null)
				{
					if(ChatWindow.getActivity() != null)
						anchor = (ViewGroup) ChatWindow.getActivity().getActionBar().getCustomView();
				}
			}
			else if(ChatWindow.getActivity() != null)
			{
				anchor = (ViewGroup) ChatWindow.getActivity().getActionBar().getCustomView();
			}
		}
		pop.showAtLocation(anchor, Gravity.TOP, 0, -10);
	}
	
	private void showFile(String fileType)
	{
		
	}
	
	private void drawImage(final ImageView img,final String msg)
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
                    ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
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
                    ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
					{
						public void run()
						{
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
							img.setLayoutParams(lp);
							img.setAdjustViewBounds(true);
							if(bmp.getWidth() < size[0])
								img.setMaxWidth(bmp.getWidth());
							else
								img.setMaxWidth(getMaxWidth());
							if(bmp.getHeight() < size[1])
								img.setMaxHeight(bmp.getHeight());
							else
								img.setMaxHeight(size[1]);
							img.setImageBitmap(bmp);
						}
					});
				}
			});
			thread.start();
		}
	}
	
	private int[] computeSize(int... size)
	{
		WindowManager wm = (WindowManager)ChatList.getCurrentActivity().getSystemService(Activity.WINDOW_SERVICE);
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
		WindowManager wm = (WindowManager)ChatList.getCurrentActivity().getSystemService(Activity.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int dpi = dm.densityDpi;
		int size = width - (int)((dpi*40)/160);
		return size;
	}
}



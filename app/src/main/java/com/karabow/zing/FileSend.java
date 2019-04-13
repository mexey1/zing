package com.karabow.zing;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;



public class FileSend extends AsyncTask<Object,Long,String>
{
	private int DEST_PORT = 3500;
	private JSONObject data;
	private File file;
	private String path;
	private LinearLayout pb;
	private ImageView send_status;
	private View view2;
	private volatile boolean sent,beginSending;
	private String ip_address;
	private String myMacAddress;
	
	public  FileSend(Object... params)
	{
		this.path = (String)params[0];
		data = (JSONObject)params[1];
		file = (File)params[2];
		ip_address = (String)params[3];
		try
		{
			if(data.getString("file_type").equals("picture"))
				showPictureCommentBox();
			else if(data.getString("file_type").equals("audio"))
			{
				showAudioCommentBox();
			}
			else if(data.getString("file_type").equals("video"))
				showVideoCommentBox();
			else
				showFileCommentBox();
				//showAudioCommentBox();
		}
		catch(JSONException e)
		{
			
		}
	}
	@Override
	protected String doInBackground(Object... params)
	{
		try
		{
			//Log.d("IP Me", ip_address);
			Log.d("IP Me", DatabaseHelper.getIpAddress(ChatWindow.getId()));
			Socket sock = new Socket(ChatWindow.getIpAddress(), DEST_PORT);
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            DataInputStream dis = new DataInputStream(sock.getInputStream());
			data.put("zing_id", DatabaseHelper.getZingID(ChatWindow.getActivity()));
			dos.writeUTF(data.toString());
			String response = dis.readUTF();
            if(response.equals("off"))
            {
                DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress(),
                        file.getAbsolutePath(),0, data.getString("file_type"));
                sock.close();
                return "off";
            }
            else if(response.equals("ref"))//file was refused
            {
                DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress(),
                        file.getAbsolutePath(),0, data.getString("file_type"));
                sock.close();
                return "ref";
            }

			byte buff[] = new byte[8*1024];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long file_length = file.length();
			int count =0;
			long bytes_read=0;
			while((count =bis.read(buff)) != -1)
			{
				dos.write(buff,0,count);
				bytes_read+=count;
				Log.d("Size written", Long.toString(bytes_read));
				//int progress = (int)((bytes_read/file_length)*100);
				//Thread.sleep(4000);
				publishProgress(bytes_read);
			}
			bis.close();
			sock.close();
			sent = true;
			Log.d("Has File been sent", Boolean.toString(sent));
			DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress(),
									  file.getAbsolutePath(),1, data.getString("file_type"));
			
		}
		catch(Exception e)
		{
			try
			{
				e.printStackTrace();
				sent = false;
				DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress(),
						  file.getAbsolutePath(),0, data.getString("file_type"));
			}
			catch(Exception f)
			{
				f.printStackTrace();
			}
			return null;
		}
		return null;
	}

    @Override
	public void onPostExecute(String data)
	{
		if(data!=null && data.equals("off"))
            Toast.makeText(ChatList.getCurrentActivity(),"Sorry, this person is offline and cannot accept files",Toast.LENGTH_LONG).show();
        else if(data!=null && data.equals("ref"))
            Toast.makeText(ChatList.getCurrentActivity(),"File transfer request was not honoured",Toast.LENGTH_LONG).show();

	}
	
	public void onProgressUpdate(Long...vals)
	{
		if(vals != null && vals[0]>0 && pb != null)
		{
			int width = view2.getWidth();
			View view = view2.findViewById(R.id.progress_bar);
			int bar_width = (int)((width*vals[0])/file.length());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(bar_width,LinearLayout.LayoutParams.MATCH_PARENT);
			view.setLayoutParams(lp);
		}
		   // pb.setProgress(vals[0]);
		if(send_status != null && sent)
		{
			send_status.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.send_ok));
			pb.setVisibility(ProgressBar.GONE);
		}
	}
	
	@Override
	public void onPreExecute()
	{
		final SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
    	myMacAddress = spref.getString("mac", "mac");
    	myMacAddress = myMacAddress.replace(":", "");
	}
	
	/**
	 * method to display a dialog box for comments to be entered.
	 */
	private void showPictureCommentBox()
	{
		final Dialog dial = new Dialog(ChatWindow.getActivity());
		
		dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dial.setContentView(R.layout.display_file_to_send);
		//Configuration config = getResources().getConfiguration();
		DisplayMetrics config = ChatWindow.getActivity().getResources().getDisplayMetrics();
		int height = (int)(config.heightPixels/2.5); 
		int width = (int)(config.heightPixels/1.9); 
		dial.getWindow().setLayout(width,height);
		Button butt = (Button)dial.findViewById(R.id.send);
		butt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				try
				{
					LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					View view = li.inflate(R.layout.sent_image, lays, false);
					
					final ImageView img = (ImageView)view.findViewById(R.id.sent_image);
					img.setTag(file.getAbsolutePath());
					drawImage(img,file.getAbsolutePath());
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
							Toast.makeText(ChatWindow.getActivity(), name.substring(pos+1), Toast.LENGTH_LONG).show();
							ChatWindow.getActivity().startActivity(intent);
						}
					});
					view2 = li.inflate(R.layout.progress_bar, lays, false);
					pb = (LinearLayout)view.findViewById(R.id.progressBar1);
					pb.addView(view2);
					pb.setVisibility(LinearLayout.VISIBLE);
					EditText commentBox = (EditText)dial.findViewById(R.id.commentBox);
					String comment = commentBox.getText().toString();
					data.put("comment", comment.trim());
					if(!comment.trim().isEmpty())
					{
						TextView comment_view = (TextView)view.findViewById(R.id.comment);
						comment_view.setText(comment);
					}
					TextView time = (TextView)view.findViewById(R.id.time);
					time.setText(Time.getTime());
					view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
					lays.addView(view);
					beginSending = true;
					//doInBackground(new Object());
					execute(new Object());
					dial.dismiss();
					
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
				
			}
			
		});
		dial.show();
	}
	/**
	 * method to display a dialog box for comments to be entered.
	 */
	private void showAudioCommentBox()
	{
		final Dialog dial = new Dialog(ChatWindow.getActivity());
		dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dial.setContentView(R.layout.display_file_to_send);
		//Configuration config = getResources().getConfiguration();
		DisplayMetrics config = ChatWindow.getActivity().getResources().getDisplayMetrics();
		int height = (int)(config.heightPixels/2.5); 
		int width = (int)(config.heightPixels/1.9); 
		dial.getWindow().setLayout(width,height);
		Button butt = (Button)dial.findViewById(R.id.send);
		butt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				try
				{
					/*
					 * ADD FUNCTIONALITY TO STORE URI TO SENT AUDIO FILES
					 */
					LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					final ScrollView lay = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
					View view = li.inflate(R.layout.sent_audio, lays, false);
					view2 = li.inflate(R.layout.progress_bar, lays, false);
					pb = (LinearLayout)view.findViewById(R.id.progressBar1);
					pb.addView(view2);
					ImageView play_icon = (ImageView)view.findViewById(R.id.play);
					play_icon.setTag(file.getAbsolutePath());
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
								File file = new File((String)v.getTag());
								String name = file.getName();
								int pos = name.indexOf('.');
								String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
								Intent intent = new Intent();
								intent.setType(mtp);
								intent.setAction(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(file), mtp);
								Toast.makeText(ChatWindow.getActivity(), name.substring(pos+1), Toast.LENGTH_LONG).show();
								ChatWindow.getActivity().startActivity(intent);
								//playAudio(img);
							}
							return true;
						}
					});
					view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
					TextView time = (TextView)view.findViewById(R.id.time);
					TextView title = (TextView)view.findViewById(R.id.title);
					title.setText(file.getName());
					time.setText(Time.getTime());
					lays.addView(view);
					lay.post(new Runnable()
					{
						@Override
						public void run() 
						{
							// TODO Auto-generated method stub
							lay.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});
					execute(new Object());
					dial.dismiss();
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
			
		});
		dial.show();
	}
	/**
	 * method to display a dialog box for comments to be entered.
	 */
	private void showVideoCommentBox()
	{
		final Dialog dial = new Dialog(ChatWindow.getActivity());
		dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dial.setContentView(R.layout.display_file_to_send);
		//Configuration config = getResources().getConfiguration();
		DisplayMetrics config = ChatWindow.getActivity().getResources().getDisplayMetrics();
		int height = (int)(config.heightPixels/2.5); 
		int width = (int)(config.heightPixels/1.9); 
		dial.getWindow().setLayout(width,height);
		Button butt = (Button)dial.findViewById(R.id.send);
		butt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				try
				{
					/*
					 * ADD FUNCTIONALITY TO STORE URI TO SENT AUDIO FILES
					 */
					LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					final ScrollView lay = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
					View view = li.inflate(R.layout.sent_video, lays, false);
					view2 = li.inflate(R.layout.progress_bar, lays, false);
					pb = (LinearLayout)view.findViewById(R.id.progressBar1);
					pb.addView(view2);
					ImageView play_icon = (ImageView)view.findViewById(R.id.play);
					play_icon.setTag(file.getAbsolutePath());
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
								File file = new File((String)v.getTag());
								String name = file.getName();
								int pos = name.indexOf('.');
								String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
								Intent intent = new Intent();
								intent.setType(mtp);
								intent.setAction(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(file), mtp);
								Toast.makeText(ChatWindow.getActivity(), name.substring(pos+1), Toast.LENGTH_LONG).show();
								ChatWindow.getActivity().startActivity(intent);
								//playAudio(img);
							}
							return true;
						}
					});
					view.setBackgroundDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.bubble_yellow));
					TextView time = (TextView)view.findViewById(R.id.time);
					TextView title = (TextView)view.findViewById(R.id.title);
					title.setText(file.getName());
					time.setText(Time.getTime());
					lays.addView(view);
					lay.post(new Runnable()
					{
						@Override
						public void run() 
						{
							// TODO Auto-generated method stub
							lay.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});
					execute(new Object());
					dial.dismiss();
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
			
		});
		dial.show();
	}
	/**
	 * method to display a dialog box for comments to be entered.
	 */
	private void showFileCommentBox()
	{
		final Dialog dial = new Dialog(ChatWindow.getActivity());
		dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dial.setContentView(R.layout.display_file_to_send);
		//Configuration config = getResources().getConfiguration();
		DisplayMetrics config = ChatWindow.getActivity().getResources().getDisplayMetrics();
		int height = (int)(config.heightPixels/2.5); 
		int width = (int)(config.heightPixels/1.9); 
		dial.getWindow().setLayout(width,height);
		Button butt = (Button)dial.findViewById(R.id.send);
		butt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				try
				{
					/*
					 * ADD FUNCTIONALITY TO STORE URI TO SENT AUDIO FILES
					 */
					LayoutInflater li = LayoutInflater.from(ChatWindow.getActivity());
					LinearLayout lays = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.chats_display);
					final ScrollView lay = (ScrollView)ChatWindow.getActivity().findViewById(R.id.scrollView1);
					View view = li.inflate(R.layout.sent_audio, lays, false);
					ImageView play_icon = (ImageView)view.findViewById(R.id.play);
					play_icon.setTag(file.getAbsolutePath());
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
								File file = new File((String)v.getTag());
								String name = file.getName();
								int pos = name.indexOf('.');
								String mtp = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(pos+1));
								Intent intent = new Intent();
								intent.setType(mtp);
								intent.setAction(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(file), mtp);
								Toast.makeText(ChatWindow.getActivity(), name.substring(pos+1), Toast.LENGTH_LONG).show();
								ChatWindow.getActivity().startActivity(intent);
								//playAudio(img);
							}
							return true;
						}
					});
					lays.addView(view);
					lay.post(new Runnable()
					{
						@Override
						public void run() 
						{
							// TODO Auto-generated method stub
							lay.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});
					execute(new Object());
					dial.dismiss();
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
			
		});
		dial.show();
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

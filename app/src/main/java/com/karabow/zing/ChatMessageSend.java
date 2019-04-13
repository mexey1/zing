package com.karabow.zing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ChatMessageSend extends Thread
{
	//Client Class
	private final int DEST_PORT = 3500;
	private int id;
	private String text,ip_address;
	private ImageView v;
	private MediaPlayer mp;
	private String myMacAddress;
	
	public ChatMessageSend(String ip,String text,int id, ImageView view)
	{
		ip_address = ip;
		this.text = text;
		this.id = id;
		v = view;
		final SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
    	myMacAddress = spref.getString("mac", "mac");
    	myMacAddress = myMacAddress.replace(":", "");
	}
	
	private void sendChatMessage()
	{
		try
		{
		
			
			Uri sounduri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.pop); 
			mp = MediaPlayer.create(ChatWindow.getActivity(), sounduri);
			
			mp.start();
			
			mp.setOnCompletionListener(new OnCompletionListener()
			{

				@Override
				public void onCompletion(MediaPlayer mp) 
				{
					// TODO Auto-generated method stub
					mp.stop();
					mp.release();
					mp = null;
					System.gc();
					//mp.reset();
				}
				
			});
			//Thread.sleep(3000);
			JSONObject jobj = new JSONObject();
			jobj.put("mac_address", myMacAddress);
			jobj.put("msg", text);
			jobj.put("file_type","message");
			jobj.put("zing_id", DatabaseHelper.getZingID(ChatWindow.getActivity()));
			Log.d("Thread name", Thread.currentThread().getName());
			Socket sock = new Socket(ip_address, DEST_PORT);
			
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			
			dos.writeUTF(jobj.toString());
			
			sock.close();
			//ChatList.getMediaPlayerSend().start();
			//ChatList.getMediaPlayer().prepare();
			/*NotificationManager nm = (NotificationManager)ChatList.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			Uri sounduri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			NotificationCompat.Builder ncb = new NotificationCompat.Builder(ChatList.getActivity());
			ncb.setSmallIcon(R.drawable.ic_launcher);
			ncb.setContentTitle("Zing");
			ncb.setSound(sounduri);
			
			nm.notify(0, ncb.build());*/
			
			Log.d("JUST SENT STUFF", "HI BABY! I JUST SENT " + text);
			
			//if the chat window is active, then update the state of the status image to indicate send successful and update status of sent column to true
			if(ChatWindow.getActivity() != null)
			{
				ChatWindow.getActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,1,"text");	
						//msg_sent
						v.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.send_ok));
						
					}
					
				});
			}
			
			//if the chat window is not active, update the sent column in the database to true
			else if(ChatList.getCurrentActivity() != null)
			{
				ChatWindow.getActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,1,"text");	
						
					}
					
				});
			}
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		//something happened during sending of message
		catch(SocketException e)
		{
			//if the chat window is active, then update the state of the status image to indicate send successful and update status of sent column to true
			if(ChatWindow.getActivity() != null)
			{
				ChatWindow.getActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,0,"text");	
						//msg_sent
						v.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.send_failed));
						
					}
					
				});
			}
			
			//if the chat window is not active, update the sent column in the database to true
			else if(ChatList.getCurrentActivity() != null)
			{
                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,0,"text");	
						
					}
					
				});
			}
			e.printStackTrace();
			
		}
		catch(IOException e)
		{
			//if the chat window is active, then update the state of the status image to indicate send successful and update status of sent column to false
			if(ChatWindow.getActivity() != null)
			{
                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,0,"text");	
						//msg_failed
						v.setImageDrawable(ChatWindow.getActivity().getResources().getDrawable(R.drawable.send_failed));
						
					}
					
				});
			}
			
			//if the chat window is not active, update the sent column in the database to false
			else if(ChatList.getCurrentActivity() != null)
			{
                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable()
				{

					@Override
					public void run() 
					{
						// Add message to database, NOTE: There needs to be reception acknowledgment
						DatabaseHelper.addMessageSent(myMacAddress, ChatWindow.getRecipientMacAddress()/* to whom? */, text,0,"text");	
						
					}
					
				});
			}
			e.printStackTrace();
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		sendChatMessage();
		
	}
	
	
}

package com.karabow.zing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IncomingCallAsyncTask extends AsyncTask<String,String,String>
{
	private final int AUDIO_RECORD_BUFFER_SIZE=10*1024, SAMPLE_RATE=44100,UDP_CALL_PORT=6100;
	private static volatile boolean finish = false;
	private static DatagramSocket dgs;
	private static DataInputStream dist;
	private static DataOutputStream dost;
	private static AudioRecord  aur;
	private static AudioDataReceiveThread aurt;
	private AudioManager aum;
	private volatile int sec = 0,min = 0, hr = 0;
	private volatile String time = null,seconds=null, minutes=null, hour=null;
	private static Thread threads;
	
	public IncomingCallAsyncTask(DataInputStream dis, DataOutputStream dos)
	{
		dist =dis;
		dost =dos;
	}
	public String doInBackground(String...strings)
	{
		
		/* * UDP is used in the call session to make it fast and real time
		 * TCP is first used to initiate connection
		 */
		try
		{
				//!response.equals("end")
				try
				{
					//this portion of code would be used to write audio data from this caller to the receiver.
					//a new thread would be spawned to receive audio data from the receiver to this caller.
					aurt = new AudioDataReceiveThread();
					aurt.start();
					aum = (AudioManager)ChatList.getCurrentActivity().getSystemService(Activity.AUDIO_SERVICE);
					int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
					//aum.setStreamVolume(AudioManager.STREAM_VOICE_CALL, aum.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
					
					aur = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,minBufferSize+AUDIO_RECORD_BUFFER_SIZE);
					
					aur.startRecording();
					
					byte audio_data[] = new byte[8*1024];
					dgs = new DatagramSocket();
					DatagramPacket dgp = new DatagramPacket(audio_data,audio_data.length,InetAddress.getByName(strings[0]),UDP_CALL_PORT);
					
					
					int count=0,data_read=0;
					startTime();
					while(!IncomingCallAsyncTask.finish)
					{
						Log.d("AudioData To Send", "Receive Starte dxgwq wqsqghssq");
						data_read = aur.read(audio_data, count, audio_data.length);
						
						dgp.setData(audio_data);
						dgs.send(dgp);
					}
					dost.writeUTF("ce");//call ended
					cleanUp();
				}
		/*
		 * UnknownHostException is thrown at the initiation of the connection to the receiver of the call
		 */
		catch(UnknownHostException e)
		{
			cleanUp();
			try
			{
				Thread.sleep(300);
			}
			catch(Exception g)
			{
				g.printStackTrace();
			}
			publishProgress("could not be reached...");
		}
		
		catch(IOException e)
		{
			cleanUp();
			try
			{
				Thread.sleep(300);
			}
			catch(Exception g)
			{
				g.printStackTrace();
			}
			publishProgress("call ended");
			e.printStackTrace();
		}
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	@Override
    public void onPreExecute()
    {
        TextView status = (TextView)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.callStatus);
        status.setText("connected...");
    }
	@Override
	public void onProgressUpdate(String...strings)
	{
		TextView tview = (TextView)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.duration);
		tview.setText(strings[0]);
	}
	private static void cleanUp()
	{
		try
		{
			if(threads!=null &&(threads.getState() == Thread.State.TIMED_WAITING))
				threads.interrupt();
			finish = false;
			if(aur != null)
				aur.stop();
			    aur = null;
			if(aurt != null)
            {
                aurt.setEndCall();
                aurt.closePort();
                aurt =null;
            }

			if(dgs !=null && !dgs.isClosed())
			{
				dgs.close();
				dgs = null;
			}
			if(dost != null)
			{
				dost.close();
				dist.close();
				dost = null;
				dist = null;
			}
			//dost.writeUTF("call_ended");
			//dist.close();
			//dost.close();
			Sounds.stopIncomingCallRing();
			Sounds.stopVoipSearching();
			Sounds.stopVoipRinging();
			Sounds.callEndTone();
			CallReceiveThread.decrementCallCount();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * a static method to perform clean up
	 * @param finish
	 */
	public static void doCleanUp()
	{
		cleanUp();
	}
	public static void finish(boolean fin)
	{
		finish = true;
	}

	@Override
	public void onPostExecute(String args)
	{
		final Activity act = ((Activity)ChatList.getCurrentActivity());
		act.findViewById(R.id.call_answer).postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				act.finish();
				FrameLayout pro_pix = (FrameLayout)act.findViewById(R.id.incoming_call_layout);
				BitmapDrawable bmpd = (BitmapDrawable)pro_pix.getBackground();
				Bitmap bmp = bmpd.getBitmap();
				bmp.recycle();
				bmp = null;
				
			}
			
		}, 1500);
	}
	
	private void startTime()
	{
		
		threads = new Thread(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				try
				{
					if(time != null || sec!=0 || min !=0 || hour != null || seconds!=null ||minutes!=null)
					{
						time = null;
						sec = 0;
						min = 0;
						hr = 0;
						time = null;
						seconds=null;
						minutes=null;
						hour=null;
					}
					while(!finish)
					{
						Thread.sleep(1000);
						if(sec < 60)
						{
							seconds = Integer.toString(sec).length() ==1 ?"0"+Integer.toString(sec):Integer.toString(sec);
							minutes = Integer.toString(min).length() ==1 ?"0"+Integer.toString(min):Integer.toString(min);
							hour = Integer.toString(hr).length() ==1 ?"0"+Integer.toString(hr):Integer.toString(hr);
							if(hr == 0)
								time = minutes +":"+seconds;
							else
								time = hour+":"+minutes +":"+seconds;
							sec++;
						}
						
						else if(sec == 60)
						{
							if(min < 60)
							{
								min++;
								sec = 0;
							}
							else if(min == 60)
							{
								hr++;
							}
							
							seconds = Integer.toString(sec).length() ==1 ?"0"+Integer.toString(sec):Integer.toString(sec);
							minutes = Integer.toString(min).length() ==1 ?"0"+Integer.toString(min):Integer.toString(min);
							hour = Integer.toString(hr).length() ==1 ?"0"+Integer.toString(hr):Integer.toString(hr);
							if(hr == 0)
								time = minutes +":"+seconds;
							else
								time = hour+":"+minutes +":"+seconds;
						}
						publishProgress(time);
					}
					
					time = null;
					sec = 0;
					min = 0;
					hr = 0;
					time = null;
					seconds=null;
					minutes=null;
					hour=null;
					Log.d("Call End", "Yeah, call is ended...");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		});
		threads.start();
	}
	
}

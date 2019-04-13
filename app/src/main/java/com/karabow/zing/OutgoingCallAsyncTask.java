package com.karabow.zing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class OutgoingCallAsyncTask extends AsyncTask<String,String,String>
{
	private final int TCP_CALL_PORT = 6000, AUDIO_RECORD_BUFFER_SIZE=10*1024, SAMPLE_RATE=44100,UDP_CALL_PORT=6100;
	private static volatile String response = null;
	private static volatile boolean finish=false, cancel = false;
	private static Socket sock;
	private DatagramSocket dgs;
	private DataOutputStream dos;
	private DataInputStream dis;
	private AudioRecord aur;
	private AudioDataReceiveThread aurt;
	private int current_volume;
	private AudioManager aum;
	private volatile int sec = 0,min = 0, hr = 0;
	private volatile String time = null,seconds=null, minutes=null, hour=null,myMacAddress;
	private Thread threads;
	private static OutgoingCallAsyncTask ocat;
	
	public OutgoingCallAsyncTask()
	{
		ocat = this;
        SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs",Activity.MODE_PRIVATE);
        myMacAddress = spref.getString("mac", "mac");
        myMacAddress = myMacAddress.replace(":", "");
	}
	@Override
	public String doInBackground(String...strings)
	{
		/*
		 * UDP is used in the call session to make it fast and real time
		 * TCP is first used to initiate connection
		 */
		try
		{
			//start playing the voip searching tone.
			/*
			 * User could attempt to cancel this call at any time...we need to be prepared for this...
			 */

			aum = (AudioManager)ChatList.getCurrentActivity().getSystemService(Activity.AUDIO_SERVICE);
			current_volume = aum.getStreamVolume(AudioManager.STREAM_RING);
			//aum.setStreamVolume(AudioManager.STREAM_RING, aum.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
			aum.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			aum.setStreamVolume(AudioManager.STREAM_MUSIC, aum.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
			
			Sounds.startVoipSearching();
			if(cancel)
			{
				publishProgress("Call ended...");
				cleanUp();
				return null;
			}
			sock = new Socket();
            sock.connect(new InetSocketAddress(strings[0],TCP_CALL_PORT),10000);

			Log.d("Thread Name", "Solved");
			dos = new DataOutputStream(sock.getOutputStream());
			Log.d("Thread Name", "Solved");
			if(cancel)
			{
				publishProgress("Call ended...");
				cleanUp();
				return null;
			}
			dos.writeUTF(getCallString());
			/*
			 * we'd like the caller to know when it has begun ringing at the receiver's end
			 * so we'd listen for a message
			 */
			dis = new DataInputStream(sock.getInputStream());
			String call_status = dis.readUTF();
			if(call_status.equals("r"))//r means ringing
			{
				//we end the voip searching tone and begin the ringing tone.
				Sounds.stopVoipSearching();
				Sounds.startVoipRinging();

                DatabaseHelper.addMessageSent(myMacAddress,OutgoingCallActivity.getMacAddress(),"you called",1,"call");
				publishProgress("Ringing...");
			}
			else if(call_status.equals("b"))//b means busy
			{

                DatabaseHelper.addMessageSent(myMacAddress,OutgoingCallActivity.getMacAddress(),"missed a call from you",1,"call");

				Sounds.stopVoipSearching();
				publishProgress("User busy...");
				Sounds.callEndTone();
				cleanUp();
				return null;
			}
			
			if(cancel)
			{
				publishProgress("Call ended...");
				cleanUp();
				return null;
			}
			
			
			
			/*
			 * While doInBackground Thread is waiting to get a response from the receiver of this call
			 * we have to keep checking the value of cancel to see if it changes. If it does, we'd attempt to cancel 
			 * the doInBackground Task by calling the cancel method of this AsyncTask Object.
			 */
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					while(!cancel)
					{
						
					}
					if(response == null)
					{
						try
						{
							dos.writeUTF("ce");//ce means call ended
							publishProgress("Call ended...");
							cancel(true);
							cleanUp();
						}
						catch(IOException e)
						{
							cancel(true);
							cleanUp();
						}
						
					}
						
				}
			});
			thread.start();
			/*
			 * Listen for the status of the call if it was declined or accepted
			 */
			response = dis.readUTF();
			cancel = true;
			/*
			 * We have now read the response string from the receiver, we need two threads to help us know 
			 * when call has ended due to call_end button being pressed or network error...
			 */

            Log.d("respone", response);
            if(response.equals("d"))//d means declined
            {
                Sounds.stopVoipRinging();
                Sounds.callEndTone();
                sock.close();
                publishProgress("Call declined...");
                response = null;
                cleanUp();
                return null;
            }
            else if(response.equals("np"))//np means not picked
            {
                Sounds.stopVoipRinging();
                Sounds.callEndTone();
                sock.close();
                publishProgress("Call not answered...");
                response = null;
                cleanUp();
                return null;
            }
			
			Thread readThread = new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					try
					{
						String msg = dis.readUTF();
						if(msg.equals("ce"))//ce means call ended
						{
							publishProgress("Call ended...");
							finish = true;
						}
					}
					catch(Exception e)
					{
						//finish = true;
					}	
				}
				
			});
			readThread.start();
			
			
			//Toast.makeText(ChatWindow.getActivity(),response, Toast.LENGTH_LONG).show();

			if(response.equals("a"))//a means accepted
			{
				//stop the ringing tone once the call has been picked.
				Sounds.stopVoipRinging();
				response = null;
				publishProgress("Connected...");
				startTime();
				//!response.equals("end")
				try
				{
					//this portion of code would be used to write audio data from this caller to the receiver.
					//a new thread would be spawned to receive audio data from the receiver to this caller.
					aurt = new AudioDataReceiveThread();
					aurt.start();
					int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
                    Log.d("Buffer Size",Integer.toString(minBufferSize));
					//Toast.makeText(ChatWindow.getActivity(), "Buffer Size"+Integer.toString(minBufferSize), Toast.LENGTH_LONG).show();
					aur = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,minBufferSize+AUDIO_RECORD_BUFFER_SIZE);
					//aum.setStreamVolume(AudioManager.STREAM_VOICE_CALL, aum.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
					aur.startRecording();
					
					byte audio_data[] = new byte[8*1024];
					dgs = new DatagramSocket();
					DatagramPacket dgp = new DatagramPacket(audio_data,audio_data.length,InetAddress.getByName(strings[0]),UDP_CALL_PORT);
					int count=0,data_read=0;
					while(!finish)
					{
						//sendAudioData();
						data_read = aur.read(audio_data, count, audio_data.length);
						dgp.setData(audio_data);
						Log.d("AudioData To Send", "Receive Starte dxgwq wqsqghssq");
						//Log.d("AudioData To Sen", new String(audio_data));
						dgs.send(dgp);
					}
					cleanUp();
				}
				catch(Exception e)
				{
					cleanUp();
					e.printStackTrace();
				}
			}
			
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
				e.printStackTrace();
			}
			catch(Exception g)
			{
				cancel = true;
				g.printStackTrace();
			}
			
			publishProgress("could not be reached...");
		}
		


        catch(SocketTimeoutException e)
        {
            e.printStackTrace();
            cancel = true;
            cleanUp();
            try
            {
                Thread.sleep(300);
            }
            catch(Exception g)
            {
                g.printStackTrace();
            }
            publishProgress("could not be reached at this time...");
        }

        catch(IOException e)
        {
            cancel = true;
            cleanUp();
        }

		catch(Exception e)
		{
			e.printStackTrace();
			cancel = true;
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
			
		}
		return null;
		
	}
	
	private String getCallString() 
	{
		// TODO Auto-generated method stub
		Activity act = null;
		JSONObject jobj = new JSONObject();
		if(ChatWindow.getActivity() != null)
			act = ChatWindow.getActivity();
		else if(ChatList.getCurrentActivity() != null)
			act = ((Activity)ChatList.getCurrentActivity());
		/*
		 * we'd like the receiver of this call to know who is calling
		 * as such, we need the current user's zing id
		 */
		//String zing_id = DatabaseHelper.getZingID(act);
		try
		{
			jobj.put("zing_id", DatabaseHelper.getZingID(act));
			final SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
	    	String myMacAddress = spref.getString("mac", "mac");
	    	myMacAddress = myMacAddress.replace(":", "");
			jobj.put("mac_address", myMacAddress);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jobj.toString();
	}
	
	@Override
	public void onProgressUpdate(String... string)
	{
		try
		{
			if(!string[0].equals("time"))
			{
				TextView tview = (TextView)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.callStatus);
				tview.setText(string[0]);
			}
			else if(string[0].equals("time"))
			{
				TextView tview = (TextView)((Activity)ChatList.getCurrentActivity()).findViewById(R.id.duration);
				if(string[1]!=null && tview !=null)
					tview.setText(string[1]);
			}
			
			
		}
		catch(Exception e)
		{
			finish = true;
			e.printStackTrace();
		}
				
	}
	
	/*
	 * this method frees up resources as well as play call end tone
	 */
	private void cleanUp()
	{
		try
		{
			if(threads != null && threads.getState()==Thread.State.TIMED_WAITING)
			{
				threads.interrupt();
			}
			finish = false;
			cancel = false;
			response = null;
			if(aur != null)
				aur.stop();
			if(aurt != null)
            {
                aurt.setEndCall();
                aurt.closePort();
            }

			aur = null;
			aurt = null;
			if(dgs!=null && !dgs.isClosed())
			{
				dgs.close();
				dgs = null;
			}
			Sounds.stopVoipSearching();
			Sounds.stopVoipRinging();
			Sounds.callEndTone();
			aum.setStreamVolume(AudioManager.STREAM_RING, current_volume, 0);
			aum = null;
			if(sock!=null && !sock.isClosed())
			{
				sock.close();
				sock = null;
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void finish(boolean fin)
	{
		finish = fin;
		
	}
	
	/**
	 * this method is called to end the call before the receiver sends a declined or accepted acknowledgement
	 */
	public static void cancelCall()
	{
		cancel = true;
	}
	@Override
	public void onPostExecute(String args)
	{
		try
		{
			final Activity act = ((Activity)ChatList.getCurrentActivity());
			act.findViewById(R.id.endIcon).postDelayed(new Runnable()
			{

				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					act.finish();
					ImageView pro_pix = (ImageView)act.findViewById(R.id.profilePix);
					BitmapDrawable bmpd = (BitmapDrawable)pro_pix.getDrawable();
					Bitmap bmp = bmpd.getBitmap();
					bmp.recycle();
					bmp = null;
				}
				
			}, 1500);
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onCancelled(String res)
	{
		cleanUp();
		final Activity act = ((Activity)ChatList.getCurrentActivity());
		act.findViewById(R.id.endIcon).postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				act.finish();
				ImageView pro_pix = (ImageView)act.findViewById(R.id.profilePix);
				BitmapDrawable bmpd = (BitmapDrawable)pro_pix.getDrawable();
				Bitmap bmp = bmpd.getBitmap();
				bmp.recycle();
				bmp = null;
				//finish = true;
				
			}
			
		}, 1000);
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
						publishProgress("time",time);
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
	/*private void sendAudioData()
	{
		try
		{
			Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
			while(ni.hasMoreElements())
			{
				NetworkInterface net = ni.nextElement();
				net.
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}*/
}

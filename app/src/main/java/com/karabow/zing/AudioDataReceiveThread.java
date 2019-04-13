package com.karabow.zing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class is responsible for receiving audio data during a call session.
 * @author Anthony
 *
 */
public class AudioDataReceiveThread extends Thread//implements Runnable
{
	private static volatile boolean status= false;
	private Socket sock;
	private DatagramSocket dgs;
	private int CALL_DATA_PORT = 6100,AUDIO_RECORD_BUFFER_SIZE=10*1024, SAMPLE_RATE=44100;
	
	public AudioDataReceiveThread()
	{
		
	}
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		init();
		Log.d("AudioData Receive", "working...");
		byte audio_data[] = new byte[8*1024];
		DatagramPacket dgp = new DatagramPacket(audio_data,audio_data.length);
		AudioTrack aut = new AudioTrack(AudioManager.STREAM_VOICE_CALL,SAMPLE_RATE,AudioFormat.CHANNEL_OUT_MONO
				,AudioFormat.ENCODING_PCM_16BIT,8*1024,AudioTrack.MODE_STREAM);
		aut.setStereoVolume(1.0f, 1.0f);
		aut.play();
		while(!status)
		{
			try
			{
				dgs.receive(dgp);
				Log.d("AudioData To Play", new String(audio_data));
				aut.write(dgp.getData(), 0, dgp.getData().length);
			}
			catch(IOException e)
			{
				status = false;
				//dgs.close();
				e.printStackTrace();
				return;
			}
			catch(NullPointerException n)
			{
				status = false;
				n.printStackTrace();
				return;
			}
		}
		
		status = false;
		//dgs.close();
		aut.flush();
		aut.stop();
		aut = null;
	}
	
	public void setEndCall()
	{
		status = true;
	}
	
	private void init()
	{
		try
		{
			if(dgs== null)
			{
				 dgs = new DatagramSocket(CALL_DATA_PORT);
			}
			 status = false;
		}
		catch(SocketException e)
		{
			/*if(dgs!= null)
			   dgs.close();
			   dgs = null;
			try 
			{
				dgs = new DatagramSocket(CALL_DATA_PORT);
				status = false;
			} 
			catch (SocketException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			e.printStackTrace();
		}
	}


    public void closePort()
    {
        if(dgs!= null && dgs.isBound())
            dgs.close();
            dgs = null;
    }
	

}

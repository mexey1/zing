package com.karabow.zing;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.util.Timer;
import java.util.TimerTask;

public class Sounds 
{
	private static Context act;
	private static MediaPlayer voip_search, voip_ring,call_ring,call_drop;
	
	/**
	 * this method is a method to play the call searching audio tone.
	 * it sets the media player to looping.
	 */
	public static void startVoipSearching()
	{
		if(ChatWindow.getActivity() != null)
			act = ChatWindow.getActivity();
		else if(ChatList.getCurrentActivity() != null)
			act = ChatList.getCurrentActivity();
        else
            act = ZingService.getContextFromService();

		
		Uri sound_uri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.voip_searching);
		voip_search = MediaPlayer.create(act, sound_uri);
		voip_search.setLooping(true);
		voip_search.setVolume(1.0f, 1.0f);
		voip_search.start();
	}
	
	/**
	 * This method ends voip searching tone and releases native resources as well as setting 
	 * the media player object to null to mark it for garbage collectin.
	 */
	public static void stopVoipSearching()
	{
		if(voip_search != null)
		{
			voip_search.stop();
			voip_search.reset();
			voip_search.release();
			voip_search = null;
		}
	}
	
	/**
	 * this method is a method to play the call ringing audio tone.
	 * it sets the media player to looping.
	 */
	public static void startVoipRinging()
	{
		if(ChatWindow.getActivity() != null)
			act = ChatWindow.getActivity();
		else if(ChatList.getCurrentActivity() != null)
			act = ChatList.getCurrentActivity();
        else
            act = ZingService.getContextFromService();
		
		Uri sound_uri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.outgoing_call);
		voip_ring = MediaPlayer.create(act, sound_uri);
		voip_ring.setLooping(true);
		voip_ring.setVolume(1.0f, 1.0f);
		voip_ring.start();
	}
	
	/**
	 * This method ends voip ringing tone and releases native resources as well as setting 
	 * the media player object to null to mark it for garbage collectin.
	 */
	public static void stopVoipRinging()
	{
		if(voip_ring != null)
		{
			voip_ring.stop();
			voip_ring.reset();
			voip_ring.release();
			voip_ring = null;
		}
	}
	
	/**
	 * this method is a method to play the incoming call ringing audio tone.
	 * it sets the media player to looping.
	 */
	public static void startIncomingCallRing()
	{
		if(ChatWindow.getActivity() != null)
			act = ChatWindow.getActivity();
		else if(ChatList.getCurrentActivity() != null)
			act = ChatList.getCurrentActivity();
        else
            act = ZingService.getContextFromService();
		
		Uri sound_uri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.incoming_call_new);
		call_ring = MediaPlayer.create(act, sound_uri);
		call_ring.setLooping(true);
		call_ring.setVolume(1.0f, 1.0f);
		call_ring.start();
	}
	
	/**
	 * This method ends voip ringing tone and releases native resources as well as setting 
	 * the media player object to null to mark it for garbage collectin.
	 */
	public static void stopIncomingCallRing()
	{
        try
        {
            if(call_ring != null)
            {

                //call_ring.stop();
                //call_ring.reset();
                call_ring.release();
                call_ring = null;
            }
        }
        catch(NullPointerException e)
        {
            call_ring = null;
            System.gc();
            e.printStackTrace();
        }

	}
	
	/**
	 * This method plays audio tone that signifies call has ended due to network failure or any other
	 * possible exception
	 */
	public static void callEndTone()
	{
        if (act == null)
        {
            if(ChatWindow.getActivity() != null)
                act = ChatWindow.getActivity();
            else if(ChatList.getCurrentActivity() != null)
                act = ChatList.getCurrentActivity();
            else
                act = ZingService.getContextFromService();
        }

		Uri sound_uri = Uri.parse("android.resource://com.karabow.zing/"+R.raw.dropped_call);
		call_drop = MediaPlayer.create(act, sound_uri);
		call_drop.setVolume(1.0f, 1.0f);
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				if(call_drop !=null)
				{
                    try
                    {
                        //call_drop.stop();
                        //call_drop.reset();
                        call_drop.release();
                        call_drop = null;
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
				}
			}
			
		}, call_drop.getDuration()>0?call_drop.getDuration():3000);
        call_drop.start();
		
		
	}
}

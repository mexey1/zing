package com.karabow.zing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiveBroadcastMessage extends Thread //implements Runnable
{
	private static DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private byte [] data;

	@Override
	public void run()
	{
		try
		{
			datagramSocket = new DatagramSocket(3846);
			datagramSocket.setSoTimeout(0);
			data = new byte[512];
			// TODO Auto-generated method stub
			while(true)
			{
				Log.d("about to receive dATA", "hello theree everybody");
				
				datagramPacket = new DatagramPacket(data,data.length);
				datagramSocket.receive(datagramPacket);
				
				/*if(datagramPacket.getAddress().getHostAddress().equals(datagramSocket.getLocalAddress().getHostAddress()))
				{
					
					
				}*/
				
				String dataReceived = new String(data,"UTF-8");
				final JSONObject jobj = new JSONObject(new String(data,"UTF-8"));
                Log.d("What's happening",Boolean.toString(ChatList.getCurrentActivity() instanceof Context));
                SharedPreferences spref = null;
                if(ChatList.getCurrentActivity() != null)
                {
                   spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
                }

                else
                {
                   spref = ZingService.getContextFromService().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
                }
		    	String myMacAddress = spref.getString("mac", "mac");
		    	myMacAddress = myMacAddress.replace(":", "");
				if(myMacAddress != null)
				{
					//Do NOthing if the mac_address is equal to mine
					if(myMacAddress.equals(jobj.getString("mac_address")))
					{
						/*Log.d("Data Received by me", dataReceived);
						
						ChatList.getCurrentActivity().runOnUiThread(new Runnable()
						{
							public void run()
							{
								new ChatPerson().execute(jobj);
							}
						});*/
					}
					else
					{
						Log.d("Data Received", dataReceived);

                        if(ChatList.getCurrentActivity()!= null && ChatList.getCurrentActivity() instanceof Activity)
                        {
                            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    new ChatPerson().execute(jobj);
                                }
                            });
                        }

                        else
                        {

                        }

					}
				}
				else
				{
					
					
				}
				
				/*Log.d("This is my IP",datagramSocket.getLocalAddress().getHostAddress());
				
				Log.d("This is the sender",datagramPacket.getAddress().getHostAddress());
				
				
				String dataReceived = new String(data,"UTF-8");
				Log.d("Data Received", dataReceived);
				final JSONObject jobj = new JSONObject(new String(data,"UTF-8"));
				ChatList.getCurrentActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						new ChatPerson().execute(jobj);
					}
				});*/
				
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}


    /**
     * This method is called to close the port used for sending BroadcstMessages.
     */
    public static void closePort()
    {
        if(datagramSocket!= null && datagramSocket.isBound())
            datagramSocket.close();
            datagramSocket = null;
    }
	

}

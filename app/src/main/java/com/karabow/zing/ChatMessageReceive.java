package com.karabow.zing;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatMessageReceive extends Thread
{
	//Client Class
	private final int HOST_PORT = 3500;
	private Socket socket;
	private void receiveChatMessage()
	{
		Log.d("running receive", "receing messages");
		try
		{	
			ServerSocket sock = new ServerSocket(HOST_PORT);
			while(true) 
			{
				Log.d("ACCEPTING", "I just accepted a connection");
				socket = sock.accept();
				Log.d("This is the sender's IP", socket.getLocalAddress().getHostAddress());
				Log.d("AM I RECEIVING", "I just received a Connection from" + socket.getRemoteSocketAddress());
				if(ChatList.getCurrentActivity() != null && ChatList.getCurrentActivity() instanceof Activity)
				{
                    ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("Transferring Handle ", "I just received a Connection from");
                            new ChatMessageHandler(socket).execute(socket);
                        }

                    });
				}
				else if(ChatWindow.getActivity() != null)
				{
					ChatWindow.getActivity().runOnUiThread(new Runnable()
					{

						@Override
						public void run() 
						{
							// TODO Auto-generated method stub
							Log.d("Transferring Handle", "I just received a Connection from");
							new ChatMessageHandler(socket).execute(new Socket());
						}
						
					});
				}

                else
                {
                    /*
                     * An integer value of Zero is passed to the Handler to indicate reading of data from socket
                     */
                    Log.d("Is Handler null ", Boolean.toString(ZingService.getHandler() instanceof android.os.Handler));
                    Message msg = Message.obtain(ZingService.getHandler(),0,socket);
                    msg.obj = socket;
                    msg.arg1 = 0;
                    msg.sendToTarget();

                }
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void run() 
	{
		receiveChatMessage();
		// TODO Auto-generated method stub
		
	}

    /**
     * This method is called to close the port used for sending BroadcstMessages.
     */
    public void closePort() throws IOException
    {
        if(socket!= null && socket.isBound())
            socket.close();
            socket = null;
    }
}

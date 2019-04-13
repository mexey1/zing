package com.karabow.zing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CallReceiveThread extends  Thread
{
	private final int TCP_CALL_PORT = 6000;
	private Context act;
	private DataOutputStream dos;
	private DataInputStream dis;
	private ServerSocket sersock;
	private Socket sock;
	private static int call_count=0;
	
	public CallReceiveThread()
	{
		try 
		{
			sersock = new ServerSocket(TCP_CALL_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		try
		{
			while(true)
			{
				try
				{
					Thread.currentThread().setName("Receive Thread");
					
					if(sersock == null)
					{
						sersock = new ServerSocket(TCP_CALL_PORT);
						
					}
					if(sersock.isClosed())
					{
						sersock = new ServerSocket(TCP_CALL_PORT);
						Log.d("Thread Name", "SerSock Closed");
					}
						
					sock = sersock.accept();
					Log.d("Thread Name", Thread.currentThread().getName());
					//Toast.makeText(ChatList.getActivity(), "IncomingCall", Toast.LENGTH_LONG).show();	
					
					dis = new DataInputStream(sock.getInputStream());
					dos = new DataOutputStream(sock.getOutputStream());
					
					Log.d("Thread Name", Thread.currentThread().getName());
					Log.d("Socket Closed", Boolean.toString(sersock.isBound()));
					/*
					 * call count is a variable that the app uses to know if there is a call session going on
					 * it has a value of one whenever a call is going on.
					 */
					if(call_count >0)
					{
						dos.writeUTF("b");//b means busy
						dos.close();
						//sock.close();
						//continue;
					}
					
					else if(call_count == 0)
					{
						if(ChatWindow.getActivity() != null)
						{
							act = ChatWindow.getActivity();
						}
						else if(ChatList.getCurrentActivity() != null)
						{
							act = ChatList.getCurrentActivity();
						}
						else
                            act = ZingService.getContextFromService();
						Thread thread = new Thread(new Runnable()
						{

							@Override
							public void run() 
							{
								// TODO Auto-generated method stub
								try
								{
									if(act instanceof Activity)
                                    {

                                        IncomingCallActivity.setData(dis.readUTF(),dis,dos);
                                        Intent intent = new Intent(act, IncomingCallActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        act.startActivity(intent);
                                        Log.d("Call Placed","Yeah true");
                                        call_count++;
                                        dos.writeUTF("r");//r means ringing
                                    }
                                    else
                                    {

                                        Intent intent = new Intent(act, ChatList.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.putExtra("delete",false);
                                        intent.putExtra("type","call");
                                        IncomingCallActivity.setData(dis.readUTF(),dis,dos);
                                        act.startActivity(intent);
                                        Log.d("Call Placed","Yeah false");
                                        call_count++;
                                        dos.writeUTF("r");//r means ringing
                                    }
								}
								catch(Exception e)
								{
									try 
									{
										CallReceiveThread.decrementCallCount();
										sock.close();
										//sersock.close();
									} catch (IOException e1) 
									{
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									e.printStackTrace();
									//sock.close();
								}
							}
							
						});
						thread.start();

					}
				}
				catch(Exception e)
				{
					CallReceiveThread.decrementCallCount();
					e.printStackTrace();
					continue;
				}
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try 
			{
				//sersock.close();
			} 
			catch (Exception e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//new CallReceiveThread().start();
			run();
			decrementCallCount();
			
		}
	}
	
	public static void decrementCallCount()
	{
		if(call_count >0)
			call_count =0;
	}

    /**
     * This method is called to close the port used for sending BroadcstMessages.
     */
    public void closePort() throws IOException
    {
        if(sersock!= null && sersock.isBound())
            sersock.close();
            sersock = null;
    }

}

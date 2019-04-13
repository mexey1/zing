package com.karabow.zing;

import java.net.ServerSocket;
import java.net.Socket;

public class ProfilePictureSend extends Thread 
{
	/**
	 * THIS CLASS LISTENS FOR REQUESTS FOR PROFILE PICTURES
	 */
	private int PIXSERVERPORT = 4500;
	private Socket sock;
	private ServerSocket pix_socket;
	public void run()
	{
		try
		{
			pix_socket = new ServerSocket(PIXSERVERPORT);
			while(true)
			{
				sock = pix_socket.accept();
				ProfilePictureSendHandler ppsh = new ProfilePictureSendHandler(sock);
				ppsh.start();
			}
			
		}
		catch(Exception e)
		{
			
		}
	}

}

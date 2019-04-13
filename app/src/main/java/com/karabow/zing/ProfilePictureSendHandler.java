package com.karabow.zing;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

public class ProfilePictureSendHandler extends Thread
{
	private Socket sock;
	private Activity act;
	public ProfilePictureSendHandler(Socket sock)
	{
		this.sock = sock;
	}
	
	public void run()
	{
		try
		{
			if(ChatList.getCurrentActivity() != null)
				act = (Activity)ChatList.getCurrentActivity();
			else
				act = ChatWindow.getActivity();
			
			byte [] pix_bytes = null;
			/*File dir = android.os.Environment.getExternalStorageDirectory();
			String path = dir.getAbsolutePath()+"/Zing/DP/hello.jpg";
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//Bitmap bmp = PrepareBitmap.resizeImage(pix_bytes, 640, 640);
			//bmp.compress(CompressFormat.JPEG, 100, baos);
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			int writebytes=0,len = 0;
			while((len =fis.read(pix_bytes)) != -1)
			{
				dos.write(pix_bytes,0,len);
				writebytes+=len;
			}*/
			
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			if(ChatList.getCurrentActivity() != null)
				pix_bytes = DatabaseHelper.getProfilePix(ChatList.getCurrentActivity());
			else if(ChatWindow.getActivity() != null)
				pix_bytes = DatabaseHelper.getProfilePix(ChatWindow.getActivity());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//Bitmap bmp = PrepareBitmap.resizeImage(pix_bytes, 640, 640);
			//bmp.compress(CompressFormat.JPEG, 100, baos);
			dos.write(pix_bytes);
			
			dos.close();
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

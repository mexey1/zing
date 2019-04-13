package com.karabow.zing;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ProfilePictureReceive extends Thread
{
	/**
	 * THIS CLASS IS RESPONSIBLE FOR REQUESTING AND RECEIVING PROFILE PICTURES FROM ZINGERS
	 */
	private String ip, mac_address, zing_id;
	private long pix_id;
	private int DESTPORT = 4500,view_id;
	public ProfilePictureReceive(String ip, String mac_address, String zing_id, long pix_id,int view_id)
	{
		this.ip = ip;
		this.mac_address = mac_address;
		this.zing_id = zing_id;
		this.pix_id = pix_id;
		this.view_id = view_id;
	}
	
	public void run()
	{
		try
		{
			Log.d("PROFILE PICTURE BYTES", "ABOUT TO RECEIVE PROFILE PIX");
			Socket sock = new Socket(ip,DESTPORT);
			byte buffer [] = new byte[4*1024];
			DataInputStream is = new DataInputStream(sock.getInputStream());
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				File dir = android.os.Environment.getExternalStorageDirectory();
				String path = dir.getAbsolutePath()+"/Zing/DP/";
				File file = new File(path);
				if(!file.exists())
				{
					file.mkdirs();
				}
				file = new File(file.getAbsolutePath()+"/"+mac_address+".jpg");
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				int len = 0, writebytes = 0;
				while((len = is.read(buffer)) != -1)
				{
					fos.write(buffer,0,len);
					writebytes+=len;
					baos.write(buffer,0,len);
				}
			
				
				Log.d("PROFILE PICTURE BYTES", "CLOSED ALL SOCKETS");
				//file = new File(path+"/"+zing_id+".jpg");
				Log.d("PROFILE PICTURE BYTES", "NO EXE");
				//FileInputStream fis = new FileInputStream(file);
				Log.d("PROFILE PICTURE BYTES", "AHHAAX");
				//while((len = fis.read(buffer))!=-1)
					//baos.write(buffer,0,len);
				
				//Toast.makeText(ChatList.getCurrentActivity(), "Profile Picture Bytes"+Integer.toString(baos.size()), Toast.LENGTH_LONG).show();
				
				Log.d("PROFILE PICTURE BYTES", Integer.toString(baos.size()));
				 DatabaseHelper.storeProfilePix(baos.toByteArray(),mac_address, zing_id, Long.toString(pix_id));
				 final Bitmap bmp = PrepareBitmap.resizeImage(baos.toByteArray(), 180, 180);
				 final Bitmap bmps = PrepareBitmap.drawRoundedRect(bmp,7f);
				 if(ChatList.getCurrentActivity() != null)
				 {
					 Log.d("PROFILE PICTURE BYTES0", Integer.toString(baos.size()));
					 if(Zingers.getZingersView() != null)
					 {
						 Log.d("PROFILE PICTURE BYTES1", Integer.toString(baos.size()));
						 // ChatList.getCurrentActivity().runOnUiThread
                         Handler handler = new Handler(Looper.getMainLooper());
                         handler.post(new Runnable()
                         {
                            @Override
                            public void run()
                            {
                                ViewGroup vg = Zingers.getZingersView();

                                if (vg != null)
                                {
                                    ViewGroup vg1 = (ViewGroup)vg.findViewById(R.id.chats_layout);
                                    View v = vg1.getChildAt(view_id);

                                    if (v != null)
                                    {
                                        final ImageView imgs = (ImageView) v.findViewById(R.id.profile_pix);
                                        imgs.setImageDrawable(null);
                                        imgs.setImageBitmap(bmps);
                                        imgs.invalidate();
                                        Toast.makeText(ChatList.getCurrentActivity(), "Profile Picture Bytes" + Integer.toString(baos.size()), Toast.LENGTH_LONG).show();
                                    }
                                }

                                ViewGroup v = (ViewGroup)Chats.getChatsView();
                                if(v != null)
                                {

                                    Log.d("PROFILE PICTURE BYTES29", Integer.toString(baos.size()));
                                    ViewGroup lay = (ViewGroup)v.findViewById(R.id.chats_layout);
                                    if(lay != null)
                                    {

                                        Log.d("PROFILE PICTURE BYTES2o", Integer.toString(baos.size()));
                                        View lay1 = lay.findViewById(view_id+1);
                                        if(lay1 != null)
                                        {
                                            Log.d("PROFILE PICTURE BYTES2op", Integer.toString(baos.size()));
                                            final ImageView imgs = (ImageView) lay1.findViewById(R.id.profile_pix);
                                            imgs.setImageDrawable(null);
                                            imgs.setImageBitmap(bmps);
                                            imgs.invalidate();
                                        }
                                    }
                                }
                            }

                        });
						
					 }
				 }
				
				 fos.close();
				//is.close();
				is.close();
				//fis.close();
				sock.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

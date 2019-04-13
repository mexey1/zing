package com.karabow.zing;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Anthony on 2/12/15.
 * This Service would allow Zing to still be active even after
 * it has been exited by the user.
 */
public class ZingService extends Service
{
    private static Service service;
    private static ServiceHandler handler;
    private String myMacAddress;
    private CallReceiveThread call_thread;
    private ChatMessageReceive cmr;
    private PowerManager.WakeLock pwl;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.d("Service State","Starting service for the first time");
        cmr = new ChatMessageReceive();
        cmr.start();
        call_thread = new CallReceiveThread();
        call_thread.start();
        //new ReceiveBroadcastMessage().start();;
        Network network = new Network();
        network.startBroadcast();
        PowerManager pw = (PowerManager)getSystemService(Activity.POWER_SERVICE);
        if(Build.VERSION.SDK_INT < 17)
        {
            pwl = pw.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,"Zing");
            pwl.acquire();
        }

        else
        {
            pwl = pw.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,"Zing");
            pwl.acquire();
        }

        final SharedPreferences spref = this.getApplicationContext().getSharedPreferences("sprefs", Context.MODE_PRIVATE);
        myMacAddress = spref.getString("mac", "mac");
        myMacAddress = myMacAddress.replace(":", "");
        service = this;
    }

    @Override
    public int onStartCommand(Intent intent, int arg1, int arg2)
    {
       // if(ChatList.getCurrentActivity() == null)
        //ChatList.setCurrentActivity();

        if(handler == null)
        {
            Thread thread = new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    Looper.prepare();
                    handler = new ServiceHandler();

                    Looper.loop();
                }
            });
            thread.start();
            service = this;
        }
        return Service.START_STICKY;

    }

    public static Context getContextFromService()
    {
        if(service == null)
        {
            service = new ZingService();
        }

        return service.getApplicationContext();
    }

    private class ServiceHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            int arg1 = msg.arg1;
            if(arg1 == 0)//0 means a socket Object was passed in this msg object. Typically this is done by the ChatMessageReceive
                         //thread after it detects that all initial activities are null
            {
                readDataFromSocket((Socket)msg.obj);
            }
        }
    }

    private void showNotification(String... str)
    {
        Log.d("Tag 1","Hello Tag");
        NotificationManager nm = (NotificationManager)this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Tag 1","Hello Tag");
        Uri sound = Uri.parse("android.resource://com.karabow.zing/"+R.raw.purr);
        NotificationCompat.Builder ncb = new NotificationCompat.Builder(service.getApplicationContext());

        Log.d("Tag 2","Hello Tag");
        byte pix[] = DatabaseHelper.getZIngerProfilePix(str[1]);
        if(pix.length == 0)
        {
            DisplayMetrics dmp = new DisplayMetrics();
            ((WindowManager)service.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dmp);
            float dpi = dmp.density;
            int width = (int)(100*dpi);
            Bitmap bmp = PrepareBitmap.resizeImage(service.getApplicationContext().getResources(),
                                                   R.drawable.user,width,width);
            ncb.setLargeIcon(bmp);
        }
        else
        {
            Log.d("Tag 3","Hello Tag");
            DisplayMetrics dmp = new DisplayMetrics();
            ((WindowManager)service.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dmp);
            float dpi = dmp.density;
            int width = (int)(100*dpi);
            Log.d("Tag 4","Hello Tag");
            Bitmap bmp = PrepareBitmap.resizeImage(pix,width,width);
            ncb.setLargeIcon(bmp);
            Log.d("Tag 5","Hello Tag");
        }
        ncb.setLights(0,0,0);

        ncb.setSmallIcon(R.drawable.ic_launcher);
        ncb.setContentTitle(str[0]);
        ncb.setContentText(str[2]);
        ncb.setSound(sound);
        //ncb.setNumber(4);
        /*
         set intent to start activity
         */
        Intent intent = new Intent(getContextFromService(),ChatList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("delete",false);
        intent.putExtra("type","message");
        intent.putExtra("macAddress",str[1]);
        int requestcode = DatabaseHelper.getId(str[1]);
        PendingIntent pintent =  PendingIntent.getActivity(getApplicationContext(),requestcode,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        ncb.setContentIntent(pintent);
        ncb.setDefaults(Notification.VISIBILITY_PUBLIC);
        ncb.setDefaults(Notification.DEFAULT_LIGHTS);
        ncb.setDefaults(Notification.DEFAULT_SOUND);
        ncb.setAutoCancel(true);


        if(nm == null)
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(requestcode, ncb.build());
        Log.d("Tag 6","Hello Tag");
    }

    public static Handler getHandler()
    {
        return handler;
    }

    private void readDataFromSocket(final Socket socket)
    {
        Thread thread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    //byte[] data = new byte[2*1024];
                    //Read the first String data from the stream, this is the json data
                    String data = dis.readUTF();
                    JSONObject jobj = new JSONObject(data);
                    String zing_id = jobj.getString("zing_id");
                    String file_type = jobj.getString("file_type");
                    String macAddress = jobj.getString("mac_address").trim();

                    if(file_type.equals("message"))
                    {
                        String msgReceived = jobj.getString("msg").trim();
                        DatabaseHelper.addMessageReceived(macAddress, myMacAddress, msgReceived,true,"text");
                        showNotification(zing_id,macAddress,msgReceived);
                    }
                    else
                    {
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("off");
                    }

                    socket.close();

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override
    public void onDestroy()
    {
        try
        {
            BroadcastMessage.closePort();
            ReceiveBroadcastMessage.closePort();
            call_thread.closePort();
            cmr.closePort();
        }
        catch(IOException e)
        {
            e.printStackTrace();

        }


    }
}

package com.karabow.zing;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class IncomingCallActivity extends Activity
{
	private static String data, mac_address, zing_id,myMacAddress;
	private static DataOutputStream dost;
	private static DataInputStream dist;
	private static Activity act;
	private ImageView accept,decline;
	private FrameLayout rel;
	private IncomingCallAsyncTask icat;
	private AudioManager aum;
    private Context cont;
    private Timer timer;
    private PowerManager.WakeLock pwl;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		act = this;
		setContentView(R.layout.incoming_phone_call);

        SharedPreferences spref = ChatList.getCurrentActivity().getSharedPreferences("sprefs",Activity.MODE_PRIVATE);
        myMacAddress = spref.getString("mac","mac");
        myMacAddress = myMacAddress.replace(":","");

		this.getActionBar().hide();
		//play incoming call tone
        if(ChatList.getCurrentActivity()!= null)
            cont = ChatList.getCurrentActivity();
        else
            cont = ZingService.getContextFromService();
		aum = (AudioManager)cont.getSystemService(Activity.AUDIO_SERVICE);
		//current_volume = aum.getStreamVolume(AudioManager.STREAM_RING);
		aum.setStreamVolume(AudioManager.STREAM_MUSIC, aum.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		//aum.setStreamVolume(AudioManager.STREAM_RING, aum.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
		aum.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        //create and acquire the wake lock so we can turn on the screen at all cost
        PowerManager pw = (PowerManager)getSystemService(Activity.POWER_SERVICE);
        if(Build.VERSION.SDK_INT < 17)
        {
            pwl = pw.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,"Zing");
            pwl.acquire(30000);
        }

        else
        {
            pwl = pw.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,"Zing");
            pwl.acquire(30000);
        }


        //make the window show up on the lock screen
        WindowManager man = (WindowManager)this.getSystemService(Activity.WINDOW_SERVICE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		Sounds.startIncomingCallRing();
		accept = (ImageView)this.findViewById(R.id.call_answer);
		decline = (ImageView)this.findViewById(R.id.call_decline);
		
		Bitmap bmp1 = PrepareBitmap.drawCircularColoredImage(this.getResources(), R.drawable.call_answer_1, getResources().getColor(R.color.call_green),0);
		Bitmap bmp2 = PrepareBitmap.drawCircularColoredImage(this.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0);
		TextView tview = (TextView)this.findViewById(R.id.caller);
		tview.setTypeface(ChatList.getSegoeTypeface());


        TextView status = (TextView)this.findViewById(R.id.callStatus);
        status.setTypeface(ChatList.getSegoeTypeface());

		char c = Character.toUpperCase(zing_id.charAt(0));
		char[] name = zing_id.toCharArray();
		name[0]=c;
		tview.setText(String.valueOf(name));
        status.setText("is calling you on Zing");
		
		/*ImageView rel = (ImageView)this.findViewById(R.id.incoming_call_background);
		byte pix[] = DatabaseHelper.getZIngerProfilePix(mac_address);
		Bitmap pixes = PrepareBitmap.decodeProfilePix(pix);
		rel.setImageBitmap(pixes);*/
		//rel.setImageBitmap()
		
		rel  = (FrameLayout)this.findViewById(R.id.incoming_call_layout);
		byte pix[] = DatabaseHelper.getZIngerProfilePix(mac_address);
		Bitmap pixes = PrepareBitmap.decodeProfilePix(pix);
		Bitmap bmp = null;
		if(pixes != null)
		{
			bmp =PrepareBitmap.drawShadedBackground(pixes);
		}
			
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			if(bmp == null)
			{
				bmp = PrepareBitmap.resizeImage(act.getResources(), R.drawable.user, 640, 640);
				bmp = PrepareBitmap.drawShadedBackground(bmp);
			}
			rel.setBackgroundDrawable(new BitmapDrawable(act.getResources(),bmp));
			
		}
		else
		{
			if(bmp == null)
			{
				bmp = PrepareBitmap.resizeImage(act.getResources(), R.drawable.user, 640, 640);
				bmp = PrepareBitmap.drawShadedBackground(bmp);
			}
			rel.setBackgroundDrawable(new BitmapDrawable(act.getResources(),bmp));
		}
		
		try
		{
			
		}
		catch(Exception e)
		{
			
		}
		
		
		accept.setImageBitmap(bmp1);
		accept.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_answer_1, getResources().getColor(R.color.call_green),51));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_answer_1, getResources().getColor(R.color.call_green),0));
				}
				return false;
			}
			
		});
		accept.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				try
				{
					Toast.makeText(act,"accepted", Toast.LENGTH_LONG).show();
					dost.writeUTF("a");//accepted
					Sounds.stopIncomingCallRing();
					playAnimator(v);
					icat = new IncomingCallAsyncTask(dist,dost);
					int id = DatabaseHelper.getId(mac_address);
					icat.execute(DatabaseHelper.getIpAddress(id));
                    if(timer != null)
                        timer.cancel();
                        timer = null;


                    DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"received a call from ",true,"call");
					//v.startAnimation(getAnimation());

                    //We release the wake lock here
                    if(pwl.isHeld() && pwl !=null)
                    {
                        pwl.release();
                    }
					
				}
				catch(IOException e)
				{
					Sounds.callEndTone();
					BitmapDrawable bmpd = (BitmapDrawable)rel.getBackground();
					Bitmap bmp = bmpd.getBitmap();
					bmp.recycle();
					bmp = null;
					act.finish();


                    DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"missed a call from ",true,"call");
					e.printStackTrace();
				}
				
			}
			
		});
		
		decline.setImageBitmap(bmp2);
		decline.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),51));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0));
				}
				return false;
			}
			
		});
		
		
		
		decline.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				try
				{
					CallReceiveThread.decrementCallCount();
                    //We release the wake lock here
                    if(pwl.isHeld() && pwl !=null)
                    {
                        pwl.release();
                    }
					dost.writeUTF("d");//declined
					dost.close();

					Sounds.stopIncomingCallRing();
					Sounds.callEndTone();
					accept.setEnabled(false);
                    if(timer != null)
                        timer.cancel();
                        timer = null;
					v.setEnabled(false);
					icat = null;
					v.postDelayed(new Runnable()
					{

						@Override
						public void run() 
						{
							// TODO Auto-generated method stub
							act.finish();
						}
						
					}, 1500);

                    DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"declined a call from ",true,"call");
				}
				catch(IOException e)
				{

                    DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"declined a call from ",true,"call");
					e.printStackTrace();
				}
			}
			
		});
		
		/*
		 * we need a thread to help us know 
		 * when call has been ended by caller at the other end of the line due to call_end button being pressed or network error...
		 */
		
		Thread readThread = new Thread(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				try
				{
					String msg = dist.readUTF();
					if(msg.equals("ce"))//call ended
					{
						IncomingCallAsyncTask.finish(true);
						IncomingCallAsyncTask.doCleanUp();
						if(icat == null)
						{
							Thread.sleep(3000);
							act.runOnUiThread(new Runnable()
							{
								@Override
								public void run() 
								{
									// TODO Auto-generated method stub
									act.finish();
								}
								
							});
						}

                        if(timer != null)
                        {

                            DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"missed a call from ",true,"call");
                            timer.cancel();
                            timer = null;
                        }
					}


				}
				catch(Exception e)
				{
					e.printStackTrace();
					if(icat instanceof IncomingCallAsyncTask)
					   IncomingCallAsyncTask.finish(true);
                    else
                    {
                        Sounds.stopIncomingCallRing();
                        Sounds.callEndTone();
                        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                act.finish();
                            }
                        }, 1500);

                    }

                    if(timer != null)
                    {

                        try
                        {
                            //DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"missed a call from ",true,"call");
                            timer.cancel();
                            timer = null;
                        }
                        catch(NullPointerException g)
                        {
                            e.printStackTrace();
                        }
                    }

				}
			}
			
		});
		readThread.start();

        /*
        * Create an inner class that extends TimerTask, this class would be executed to cut calls when they are not picked.
        */
        class EndCall extends TimerTask
        {
            @Override
            public void run()
            {
                try
                {
                    CallReceiveThread.decrementCallCount();
                    DatabaseHelper.addMessageReceived(mac_address,myMacAddress,"missed a call from ",true,"call");
                    showMissedCallNotification(zing_id,mac_address);
                    dost.writeUTF("np");//np means Not Picked
                    dost.close();
                    Sounds.stopIncomingCallRing();
                    Sounds.callEndTone();
                    icat = null;
                    decline.postDelayed(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            accept.setEnabled(false);
                            decline.setEnabled(false);
                            act.finish();
                        }

                    }, 1500);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

         /*
         * A timer object is created to cut the call in case the user doesn't pick u
         */

        timer = new Timer();
        timer.schedule(new EndCall(),30000);




	}
	/**
	 * method to pass reference to InputStreams to monitor the call
	 * @param dat: JSONObject string containing information about caller
	 * @param dis: DataInputStream
	 * @param dos: DataOutputStream
	 */
	public static void setData(String dat, DataInputStream dis, DataOutputStream dos)
	{
		
		try
		{
			
			JSONObject jobj = new JSONObject(dat);
			mac_address = jobj.getString("mac_address");
			zing_id = jobj.getString("zing_id");
			dost = dos;
			dist = dis;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * method called to cause a translate and rotate animation on a view 
	 * @param v: View to animate
	 */
	private void playAnimator(View v)
	{
		float density = getResources().getDisplayMetrics().density;
		float value = getResources().getDisplayMetrics().widthPixels/2;//act.findViewById(R.id.incoming_call_background).getWidth()/2;
		
		
		View gap = act.findViewById(R.id.gap);
		Rect rect = new Rect();
		gap.getGlobalVisibleRect(rect);
		int gapCenter = rect.centerX();
		v.getGlobalVisibleRect(rect);
		int viewLeft = rect.left;
		float dist = value - (v.getWidth()+(60*density));
		AccelerateDecelerateInterpolator adi = new AccelerateDecelerateInterpolator();
		
		ObjectAnimator translate = ObjectAnimator.ofFloat(v, "x", -(gapCenter-(viewLeft+80*density)));
		translate.setInterpolator(adi);
		ObjectAnimator color = ObjectAnimator.ofFloat(v, "rotation", 0.0f,135f);
		
		color.addListener(new AnimatorListener() 
		{
			
			@Override
			public void onAnimationStart(Animator animation) 
			{
				// TODO Auto-generated method stub
				ObjectAnimator anim = ObjectAnimator.ofFloat(decline,"alpha", 0.0f);
				anim.setDuration(250);
				anim.start();
				resetListeners();
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) 
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) 
			{
				// TODO Auto-generated method stub
				accept.setImageDrawable(null);
				ObjectAnimator color = ObjectAnimator.ofFloat(accept, "rotation", 135f,0.0f);
				color.setDuration(0);
				color.start();
				accept.setImageBitmap(PrepareBitmap.drawCircularColoredImage(getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0));
				
			}
			
			

			@Override
			public void onAnimationCancel(Animator animation) 
			{
				// TODO Auto-generated method stub
				
			}
		});
		AnimatorSet animset = new AnimatorSet();
		animset.setDuration(500);
		
		animset.play(translate).with(color).after(250);
		
		animset.start();
		
		
		
		
	}
	
	private Animation getAnimation()
	{
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		int finalX = disp.getWidth()/2;
		AnimationSet animset = new AnimationSet(false);
		TranslateAnimation tanim = new TranslateAnimation(0.0f,-finalX,0.0f,0.0f);
		tanim.setDuration(5000);
		
		
		AlphaAnimation aanim = new AlphaAnimation(0.5f,1.0f);
		aanim.setDuration(2500);
		aanim.setStartTime(0);
		aanim.setStartOffset(2500);
		aanim.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationRepeat(Animation arg0) 
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation arg0) 
			{
				// TODO Auto-generated method stub
				Bitmap bmp2 = PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0);
				accept.setImageBitmap(bmp2);
			}
			
		});
		
		animset.setFillAfter(true);
		animset.startNow();
		animset.addAnimation(tanim);
		animset.addAnimation(aanim);
		
		return animset;
		
	}
	
	/**
	 * method to change touch and click listeners of the accept button
	 * after it has changed to a decline button
	 */
	private void resetListeners() 
	{
		// TODO Auto-generated method stub
		
		accept.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),51));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					ImageView img = (ImageView)v;
					img.setImageBitmap(PrepareBitmap.drawCircularColoredImage(act.getResources(), R.drawable.call_decline_1, getResources().getColor(R.color.call_red),0));
				}
				return false;
			}
			
		});
		
		accept.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				IncomingCallAsyncTask.finish(true);
				icat = null;
				Toast.makeText(act,"call ended", Toast.LENGTH_LONG).show();
				
				
			}
		});
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
		}
	}

    private void showMissedCallNotification(String ...str)
    {
        Log.d("Tag 1", "Hello Tag");
        NotificationManager nm = (NotificationManager)this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Tag 1","Hello Tag");
        Uri sound = Uri.parse("android.resource://com.karabow.zing/"+R.raw.purr);
        NotificationCompat.Builder ncb = new NotificationCompat.Builder(ChatList.getCurrentActivity());

        Log.d("Tag 2","Hello Tag");
        byte pix[] = DatabaseHelper.getZIngerProfilePix(str[1]);
        if(pix.length == 0)
        {
            DisplayMetrics dmp = new DisplayMetrics();
            ((WindowManager)act.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dmp);
            float dpi = dmp.density;
            int width = (int)(100*dpi);
            Bitmap bmp = PrepareBitmap.resizeImage(act.getResources(),
                    R.drawable.user,width,width);
            ncb.setLargeIcon(bmp);
        }
        else
        {
            Log.d("Tag 3","Hello Tag");
            DisplayMetrics dmp = new DisplayMetrics();
            ((WindowManager)act.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dmp);
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
        ncb.setContentText("missed call on zing");
        ncb.setSound(sound);
        //ncb.setNumber(4);
    /*
     set intent to start activity
     */
        Intent intent = new Intent(act,ChatList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("delete",false);
        intent.putExtra("type","call");
        intent.putExtra("macAddress",str[1]);
        int requestcode = DatabaseHelper.getId(str[1]);
        PendingIntent pintent =  PendingIntent.getActivity(getApplicationContext(),requestcode,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        ncb.setContentIntent(pintent);
        ncb.setDefaults(Notification.VISIBILITY_PUBLIC);
        ncb.setDefaults(Notification.DEFAULT_LIGHTS);
        ncb.setDefaults(Notification.DEFAULT_SOUND);
        ncb.setAutoCancel(true);
        nm.notify(0, ncb.build());
        Log.d("Tag 6","Hello Tag");
      }
}

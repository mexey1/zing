package com.karabow.zing;





import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Animation.AnimationListener
{

	
	private MainActivity act;
	private ImageView img;
	private String mac = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		SharedPreferences spref = this.getSharedPreferences("sprefs", MODE_PRIVATE);
		spref.getString("status", "NotLoggedIn");
		//spref.getString("ChatActivity", "No");
		act = this;
		act.getActionBar().hide();
		Log.d("status",spref.getString("status", "NotLoggedIn"));
		if(spref.getString("status", "NotLoggedIn").equals("NotLoggedIn"))
		{
			setContentView(R.layout.activity_main);
			img = (ImageView)findViewById(R.id.imageView1);
			performAnimation();
			//this.getActionBar().hide();
		}
		
		else 
		{
			Intent chat_list_intent = new Intent(act, Welcome.class);
			act.startActivity(chat_list_intent);
			getActivity().finish();
			//act.finish();
			//Toast.makeText(act, "Hello, am running", Toast.LENGTH_LONG);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void doSignUp(View view)
	{	
		Intent sign_up_intent = new Intent(view.getContext(), SignUpActivity.class);
		act.startActivity(sign_up_intent);
		//act.finish();
		Toast.makeText(act, "Hello, am running", Toast.LENGTH_LONG).show();
	}
	
	public Activity getActivity() {
		return act;
	}
	
	public void login(View view)
	{
		EditText zing_id = (EditText)findViewById(R.id.zing_id);
		EditText pass = (EditText)findViewById(R.id.password);
		
		DatabaseHelper.initializeHelper(new DatabaseHelper(view.getContext()));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Log.d("Typed in id", zing_id.getText().toString().trim());
		Log.d("Password Typed", pass.getText().toString().trim());
		Log.d("Query", "SELECT * FROM user_info WHERE zing_id= '" 
				+ zing_id.getText().toString().trim() + "' AND password= '" + pass.getText().toString() + "'");
		
		
		Cursor cursor = sql.rawQuery("SELECT * FROM user_info WHERE zing_id= '" 
					+ zing_id.getText().toString().trim() + "' AND password= '" + pass.getText().toString() + "'", null);
		
	
		Log.d("Query", "SELECT * FROM user_info WHERE zing_id= '" 
				+ zing_id.getText().toString().trim() + "' AND password= '" + pass.getText().toString() + "'");
		
		cursor.moveToFirst();
		if(cursor.getCount() > 0)
		{
			final SharedPreferences spref = act.getSharedPreferences("sprefs", MODE_PRIVATE);
			
			spref.edit().remove("status").commit();
			spref.edit().putString("status", "LoggedIn").commit();
			
			final WifiManager wi = (WifiManager)act.getSystemService(Activity.WIFI_SERVICE);
			
			if(wi.isWifiEnabled())
			{
				WifiInfo wifiInfo = wi.getConnectionInfo();
				mac = wifiInfo.getMacAddress();
				Log.d("Wifi state n Mac", mac +" Enabled");
                //mac = mac.replace(":","");
				spref.edit().putString("mac", mac).commit();
			}
			else
			{
				final int state = wi.getWifiState();
				Thread thread = new Thread(new Runnable()
				{
					@Override
					public void run() 
					{
						// TODO Auto-generated method stub
						if(state == WifiManager.WIFI_STATE_DISABLED)
						{
							boolean status = wi.setWifiEnabled(true);
							while(!status)
							{
								status = wi.setWifiEnabled(true);
							}
							mac = wi.getConnectionInfo().getMacAddress();
							Log.d("Wifi state n Mac", mac +" Disabled");
						}
						
						else if(state == WifiManager.WIFI_STATE_DISABLING)
						{
							while(state == WifiManager.WIFI_STATE_DISABLING)
							{
								try
								{
									Thread.sleep(3000);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								
							}
							
							boolean status = wi.setWifiEnabled(true);
							while(!status)
							{
								status = wi.setWifiEnabled(true);
							}
							mac = wi.getConnectionInfo().getMacAddress();
							Log.d("Wifi state n Mac", mac +" Disabling");
						}
						
						else if(state == WifiManager.WIFI_STATE_ENABLING)
						{
							while(state == WifiManager.WIFI_STATE_ENABLING)
							{
								try
								{
									Thread.sleep(3000);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								
							}
							
							boolean status = wi.setWifiEnabled(true);
							while(!status)
							{
								status = wi.setWifiEnabled(true);
							}
							mac = wi.getConnectionInfo().getMacAddress();
							Log.d("Wifi state n Mac", mac +" Enabling");
						}
						
						else if(state == WifiManager.WIFI_STATE_ENABLED)
						{
							mac = wi.getConnectionInfo().getMacAddress();
							Log.d("Wifi state n Mac", mac +" Enabled");
						}
						if(mac == null)
						{
							this.run();
						}
						else
							spref.edit().putString("mac", mac).commit();
					}
					
				});
				thread.start();
			}
			Log.d("status here",spref.getString("status", "NotLoggedIn"));
			Intent it = new Intent(getActivity(), Welcome.class);
			getActivity().startActivity(it);
			getActivity().finish();
			DatabaseHelper.updateLoggedIn(act,cursor.getInt(0));
			
			/*if(cursor.getString().equals(zing_id.getText().toString().trim()) && cursor.getString(0).equals(pass.getText().toString()))
			{
				SharedPreferences spref = this.getSharedPreferences("spref", MODE_PRIVATE);
				spref.edit().putString("status", "LoggedIn");
				spref.edit().commit();
				
				Intent it = new Intent(getActivity(), MainActivity.class);
				getActivity().startActivity(it);
				getActivity().finish();
				
			}
			else
			{
				showErrorMessage("Invalid username or password");
			}*/
			
		}
		else 
		{
			showErrorMessage("Zing ID specified does not exist");
		}
		cursor = sql.rawQuery("SELECT * FROM user_info",null);
		cursor.moveToFirst();
		int count=0;
		while(count< cursor.getCount())
		{
			Log.d("name", cursor.getString(cursor.getColumnIndex("zing_id")));
			Log.d("password", cursor.getString(cursor.getColumnIndex("password")));
			cursor.moveToNext();
			count++;
		}
		
		DatabaseHelper.closeDatabase();
	}
	
	public void showErrorMessage(String msg)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				/*
				Intent it = new Intent(getActivity(), MainActivity.class);
				getActivity().startActivity(it);
				getActivity().finish();
				*/
			}
		});
		LinearLayout lay = new LinearLayout(getActivity());
		lay.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView warn = new TextView(getActivity());
		warn.setPadding(10, 10, 10, 10);
		//warn.setTypeface(MainActivity.getLoadedWindowsTypeface());
		
		warn.setText(msg);
		warn.setGravity(Gravity.CENTER);
		warn.setTextSize(16);
		//warn.setWidth()
		//lay.addView(img);
		lay.addView(warn);
		alert.setIcon(R.drawable.ic_launcher);
		alert.setTitle("Zing");
		alert.setView(lay);
		alert.setCancelable(false);
		alert.show();
	}

	private void performAnimation()
	{
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		float y = (float)((disp.getHeight()/2.0));
		float x = (float)((disp.getWidth()/2.0)*0.5);
		
		
		/*Animation tanim = new TranslateAnimation(0.0f,0.0f,0.0f,img.getY()+10);
		tanim.setDuration(1000);
		tanim.setFillAfter(false);*/
		//float y2 = (float)(y/4.25);
		float y2 = (float)(y*0.5);
		Animation tanim1 = new TranslateAnimation(0.0f,0.0f,y,-y/4);
		tanim1.setDuration(2000);
		tanim1.setFillAfter(true);
		//tanim1.setAnimationListener(begin);
		tanim1.setStartOffset(600);
		
		
		/*AnimationSet anim_set = new AnimationSet(false);
		anim_set.addAnimation(tanim1);
		anim_set.addAnimation(tanim);*/
		
		tanim1.setInterpolator(new AnticipateOvershootInterpolator());
		tanim1.setAnimationListener(act);
		//tanim.setInterpolator(new DecelerateInterpolator());
		
		//anim_set.setStartOffset(100);
		//img.startAnimation(anim_set);
		img.startAnimation(tanim1);
	}

	@Override
	public void onAnimationEnd(Animation animation) 
	{
		// TODO Auto-generated method stub
		LinearLayout rel_lay = (LinearLayout)findViewById(R.id.info_layout);
		AlphaAnimation anim = new AlphaAnimation(0.0f,1.0f);
		anim.setDuration(2000);
		anim.setFillAfter(true);
		rel_lay.startAnimation(anim);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) 
	{
		// TODO Auto-generated method stub
		
	}
	

}

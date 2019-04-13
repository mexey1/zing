package com.karabow.zing;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Network 
{
	private final int BROADCAST_INTERVAL =2000;
    private static Timer timer;

	public void enableWifi(Activity act)
	{
		final WifiManager wm = (WifiManager)act.getSystemService(Activity.WIFI_SERVICE);
		if(wm.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
		{
			try
			{
				//turn off hotspot tethering
				Method meth = wm.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,boolean.class);
				meth.invoke(wm, null,false);
				//turn on wifi
				wm.setWifiEnabled(true);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			
		}
			Thread th = new Thread(new Runnable()
			{
				public void run()
				{
					findZingNetworks(wm);
				}
			});
			
			
			th.start();
		
	}
	
	private void findZingNetworks(WifiManager wm)
	{
		try
		{
			Thread.sleep(3000);
		}
		catch(Exception e)
		{
			
		}
		
		wm.startScan();
		List<ScanResult> wifi_result = wm.getScanResults();
		int count = 0;
		int loop = 0;
		while(wifi_result == null)
		{
			wifi_result = wm.getScanResults();
			if(loop++ == 100)
				wm.setWifiEnabled(true);
			//Log.d("Scan", "null");
		}
			
		for(ScanResult sc:wifi_result)
		{
			Log.d("Scan Result", sc.SSID);
			if(sc.SSID.contains("ZNG"))
			{
				List<WifiConfiguration> ls = wm.getConfiguredNetworks();
				for(WifiConfiguration wc:ls)
				{
					if(wc.SSID.equals(sc.SSID))
					{
						wm.removeNetwork(wc.networkId);
						WifiConfiguration  conf = new WifiConfiguration();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            conf.SSID = sc.SSID;
                        else
						conf.SSID ="\""+sc.SSID+"\"";
						conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
						
						int id = wm.addNetwork(conf);
						wm.disconnect();
						wm.enableNetwork(id, true);
						
						final ViewGroup act = Zingers.getZingersView();
                        ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
						{
							public void run()
							{
								TextView scan_text = (TextView)act.findViewById(R.id.scan_text);
								scan_text.setText("Zing group found...Joining group...Please wait ");
							}
						});
						
						wm.reconnect();
						wm.saveConfiguration();
						count++;
						//startBroadcast();
						
						break;
					}
					else
					{
						WifiConfiguration  conf = new WifiConfiguration();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            conf.SSID = sc.SSID;
                        else
                            conf.SSID ="\""+sc.SSID+"\"";
						conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
						int id = wm.addNetwork(conf);
						wm.disconnect();
						wm.enableNetwork(id, true);
						
						final ViewGroup act = Zingers.getZingersView();
                        ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
						{
							public void run()
							{
								TextView scan_text = (TextView)act.findViewById(R.id.scan_text);
								scan_text.setText("Zing group found...Joining group...Please wait ");
							}
						});
						
						wm.reconnect();
						wm.saveConfiguration();
						count++;
						//startBroadcast();
						break;
					}
				}
			}
			else
			{
				Log.d("Network_Name", sc.SSID);
			}
		}
		
		if(count == 0)
		{

            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(ChatList.getCurrentActivity(), "No zing groups were found...click on the wifi button below", Toast.LENGTH_LONG).show();
					
					ProgressBar pb = (ProgressBar)Zingers.getZingersView().findViewById(R.id.scan_progress);
					TextView tv = (TextView)Zingers.getZingersView().findViewById(R.id.scan_text);
					
					final Button jg = (Button)Zingers.getZingersView().findViewById(R.id.join_group);
					final Button cg = (Button)Zingers.getZingersView().findViewById(R.id.create_group);
					jg.setVisibility(Button.VISIBLE);
					cg.setVisibility(Button.VISIBLE);
					pb.setVisibility(ProgressBar.GONE);
					tv.setVisibility(TextView.GONE);
				}
			});
		}
		
	}
	
	public void enableHotspot(Activity act)
	{
		try
		{
			final WifiManager wm = (WifiManager)act.getSystemService(Activity.WIFI_SERVICE);
			WifiConfiguration  conf = new WifiConfiguration();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            conf.SSID ="ZNGHello";
             else
            conf.SSID ="\""+"ZNGHello"+"\"";
			conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			
			wm.setWifiEnabled(false);
			final ViewGroup acts = Zingers.getZingersView();
            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
			{
				public void run()
				{
					TextView scan_text = (TextView)acts.findViewById(R.id.scan_text);
					scan_text.setText("Creating zing group");
				}
			});
			
			Method meth = wm.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,boolean.class);
			meth.invoke(wm, conf,true);
			//startBroadcast();
            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
			{
				public void run()
				{
					TextView scan_text = (TextView)acts.findViewById(R.id.scan_text);
					scan_text.setText("Group created, waiting for friends to join");
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void startBroadcast()
	{
		timer = new Timer(true);
		BroadcastMessage.init();
        new ReceiveBroadcastMessage().start();
        new ProfilePictureSend().start();
		Log.d("CALLED SEND", "I HAVE CALLED SEND");
		

		Log.d("CALLED RECEIVE", "I HAVE CALLED RECEIVE");
		timer.scheduleAtFixedRate(new BroadcastTask(), 4000, BROADCAST_INTERVAL);

		
	}

    public void stopBroadcast()
    {
        timer.cancel();
    }
	
	public List<ScanResult> getAvailableNetworks()
	{
		
				// TODO Auto-generated method stub
				if(ChatList.getCurrentActivity() != null)
				{
					final WifiManager wm = (WifiManager)ChatList.getCurrentActivity().getSystemService(Activity.WIFI_SERVICE);
					if(wm.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
					{
						try
						{
							//turn off hotspot tethering
							Method meth = wm.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,boolean.class);
							meth.invoke(wm, null,false);
							//turn on wifi
							wm.setWifiEnabled(true);
							Thread.sleep(3000);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
					wm.startScan();
					List<ScanResult> wifi_result = wm.getScanResults();
				
					int loop = 0;
					while(wifi_result == null)
					{
						wifi_result = wm.getScanResults();
						if(loop++ == 100)
							wm.setWifiEnabled(true);
						//Log.d("Scan", "null");
					}
					return wifi_result;
				}
				else
					return null;
	  		}
	
}

class BroadcastTask extends TimerTask
{
	public void run()
	{
		BroadcastMessage.sendBroadcast();
	}
}

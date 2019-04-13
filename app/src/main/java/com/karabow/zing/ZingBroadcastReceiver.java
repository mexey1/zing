package com.karabow.zing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * Created by Anthony on 2/18/15.
 */
public class ZingBroadcastReceiver extends BroadcastReceiver
{

    /*
     *This service is called when the device is done booting up. We start our service and also attempt to turn wifi on.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service = new Intent(context,ZingService.class);
        context.startService(service);
        WifiManager wiman = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wiman.setWifiEnabled(true);
    }
}

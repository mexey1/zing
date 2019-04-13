package com.karabow.zing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Anthony on 2/10/15.
 */
public class WifiNetworks extends Activity
{
    private Activity act;
    private Network network;
    private String pass;
    private  Dialog dial;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_popup);
        act = this;
        createWifiPopup();
    }


    private void createWifiPopup()
    {
        // TODO Auto-generated method stub
        final LinearLayout lay = (LinearLayout) act.findViewById(R.id.wifi_layout);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                network = new Network();
                List<ScanResult> scan = network.getAvailableNetworks();
                for (ScanResult sc : scan) {
                    final String name = sc.SSID;
                    String cap = sc.capabilities;
                    if (cap.contains("WEP"))
                    {
                        if (ChatList.getCurrentActivity() != null && ChatList.getCurrentActivity() instanceof Activity)
                        {
                            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.locked);
                                    int[] size = computeSize(20, 20);
                                    d.setBounds(0, 0, size[0], size[1]);
                                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                                    SpannableString ss = new SpannableString("ane " + name);
                                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    tv.setText(ss);
                                    initNetwork(v,name,"locked","wep");
                                    lay.addView(v);
                                }

                            });
                        }
                        else
                        {
                            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.locked);
                                    int[] size = computeSize(20, 20);
                                    d.setBounds(0, 0, size[0], size[1]);
                                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                                    SpannableString ss = new SpannableString("ane " + name);
                                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    tv.setText(ss);
                                    initNetwork(v, name, "locked", "wep");
                                    lay.addView(v);
                                }
                            });
                        }
                    }

                    else if(cap.contains("WPA") || cap.contains("WPA2"))
                    {
                        if (ChatList.getCurrentActivity() != null && ChatList.getCurrentActivity() instanceof Activity)
                        {
                            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable()
                            {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.locked);
                                    int[] size = computeSize(20, 20);
                                    d.setBounds(0, 0, size[0], size[1]);
                                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                                    SpannableString ss = new SpannableString("ane " + name);
                                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    tv.setText(ss);
                                    initNetwork(v,name,"locked","wpa");
                                    lay.addView(v);
                                }

                            });
                        }
                        else
                        {
                            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    Drawable d = ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.locked);
                                    int[] size = computeSize(20, 20);
                                    d.setBounds(0, 0, size[0], size[1]);
                                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                                    SpannableString ss = new SpannableString("ane " + name);
                                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    tv.setText(ss);
                                    initNetwork(v,name,"locked","wpa");
                                    lay.addView(v);
                                }
                            });
                        }
                    }

                    else
                    {
                        if (ChatList.getCurrentActivity() != null && ChatList.getCurrentActivity() instanceof Activity)
                        {
                            ((Activity)ChatList.getCurrentActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    tv.setText(name);
                                    initNetwork(v,name,"open",null);
                                    lay.addView(v);
                                }
                            });
                        }

                        else
                        {
                            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutInflater li = LayoutInflater.from(act);
                                    View v = li.inflate(R.layout.wifi_networks, lay, false);
                                    TextView tv = (TextView) v.findViewById(R.id.network_name);
                                    tv.setText(name);
                                    initNetwork(v, name, "open", null);
                                    lay.addView(v);
                                }
                            });
                        }

                    }
                }

            }

        });
        thread.start();
    }


    private void initNetwork(View v,final String name,final String state,final String type)
    {
        v.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() ==  MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));

                }
                else //if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundColor(0);
                }
                return false;
            }
        });

        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    WifiManager wi = (WifiManager)ChatList.getCurrentActivity().getSystemService(Activity.WIFI_SERVICE);
                    List<WifiConfiguration>configuredNetworks = wi.getConfiguredNetworks();
                    Toast.makeText(ChatList.getCurrentActivity(),name,Toast.LENGTH_LONG).show();
                    int loop = 0;
                    for(WifiConfiguration wifiConfig: configuredNetworks)
                    {
                        if(wifiConfig.SSID.equals("\""+name+"\""))
                        {
                            boolean status = wi.enableNetwork(wifiConfig.networkId,true);
                            if(status)
                            {
                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                if (mWifi.isConnectedOrConnecting())
                                {
                                    // Do whatever
                                    Toast.makeText(ChatList.getCurrentActivity(), "Successfully connected to " + name, Toast.LENGTH_LONG).show();
                                    if(dial != null)
                                        dial.dismiss();
                                }
                                else
                                    Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+name+" failed",Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+name+" failed",Toast.LENGTH_LONG).show();
                            loop++;
                        }
                    }

                    if(loop == 0)
                    {
                        if(state.equals("open"))
                        {
                            WifiConfiguration wifiConfig = new WifiConfiguration();
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                wifiConfig.SSID = name;
                            else
                                wifiConfig.SSID = "\""+name+"\"";
                            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                            int id = wi.addNetwork(wifiConfig);
                            boolean status =  wi.enableNetwork(id,true);

                            if(status)
                            {
                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                if (mWifi.isConnectedOrConnecting())
                                {
                                    // Do whatever
                                    Toast.makeText(ChatList.getCurrentActivity(), "Successfully connected to " + name, Toast.LENGTH_LONG).show();
                                    if(dial != null)
                                        dial.dismiss();
                                }
                                else
                                    Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+name+" failed",Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+name+" failed",Toast.LENGTH_LONG).show();

                        }

                        else
                        {
                            displayPasswordField(name,type);
                        }
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(ChatList.getCurrentActivity(),"Wifi is disabled, please enable wifi",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private int[] computeSize(int... size) {
        WindowManager wm = (WindowManager) act.getSystemService(Activity.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int dpi = dm.densityDpi;
        Log.d("DPI", Integer.toString(dpi));
        size[0] = (int) ((dpi * size[0]) / 160);
        size[1] = (int) ((dpi * size[1]) / 160);
        return size;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if(hasFocus)
        {
            ChatList.setCurrentActivity(act);
        }
    }

    private void displayPasswordField(final String networkName, final String type)
    {
        dial = new Dialog(ChatList.getCurrentActivity());

        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dial.setContentView(R.layout.wifi_password);
        //Configuration config = getResources().getConfiguration();
        DisplayMetrics config = ChatList.getCurrentActivity().getResources().getDisplayMetrics();
        int height = (int)(config.heightPixels/2.5);
        int width = (int)(config.heightPixels/1.9);
        dial.getWindow().setLayout(width,height);
        Button butt = (Button)dial.findViewById(R.id.send);
        butt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText password = (EditText)dial.findViewById(R.id.commentBox);
                pass = password.getText().toString();
                connectToLockedNetwork(networkName,type);

            }

        });

        Button butt2 = (Button)dial.findViewById(R.id.cancel);
        butt2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dial.dismiss();

            }

        });

        dial.show();

    }

    private void connectToLockedNetwork(String networkName,String type)
    {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        WifiManager wi = (WifiManager)ChatList.getCurrentActivity().getSystemService(Activity.WIFI_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            wifiConfig.SSID = networkName;
        else
            wifiConfig.SSID = "\""+networkName+"\"";

        if(type!= null && type.equalsIgnoreCase("wpa"))
        {
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.preSharedKey = "\""+pass+"\"";

            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            int id = wi.addNetwork(wifiConfig);
            wi.disconnect();
            boolean status =  wi.enableNetwork(id,true);
            wi.reconnect();

            if(status)
            {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnectedOrConnecting())
                {
                    // Do whatever
                    Toast.makeText(ChatList.getCurrentActivity(), "Successfully connected to " + networkName, Toast.LENGTH_LONG).show();
                    if(dial != null)
                        dial.dismiss();
                }
                else
                    Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+networkName+" failed",Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+networkName+" failed",Toast.LENGTH_LONG).show();

        }

        else if(type != null && type.equalsIgnoreCase("wep"))
        {
            wifiConfig.wepKeys[0] = pass;
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            int id = wi.addNetwork(wifiConfig);
            wi.disconnect();
            boolean status =  wi.enableNetwork(id,true);
            wi.reconnect();
            //wi.saveConfiguration();


            if(status)
            {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnectedOrConnecting())
                {
                    // Do whatever
                    Toast.makeText(ChatList.getCurrentActivity(), "Successfully connected to " + networkName, Toast.LENGTH_LONG).show();
                    if(dial != null)
                        dial.dismiss();
                }
                else
                    Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+networkName+" failed",Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(ChatList.getCurrentActivity(),"Connection to "+networkName+" failed",Toast.LENGTH_LONG).show();

        }
    }

}

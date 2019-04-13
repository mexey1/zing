package com.karabow.zing;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class BroadcastMessage  implements Timeable 
{
	//declare variables
	//this class sends broadcast messages
        
	
	/*private static InetAddress inetadd,bcast;
	private static InetSocketAddress broadcastAddress;
	private static DatagramSocket datagramSocket;
	private static final int HOST_PORT = 4000,DEST_PORT = 3846;
	
	private static DatagramPacket datagramPacket;*/
	private static String macAddress,ip_address;
	private static JSONObject deviceInfo;
    private  static DatagramSocket datagramSocket=null;
    private static Context cont;
 
	public static void init()
	{
		
	}
    public static void sendBroadcast() 
    {
    	InetAddress inetadd = null,bcast=null;
    	InetSocketAddress broadcastAddress=null;
        //datagramSocket=null;
    	final int HOST_PORT = 4000,DEST_PORT = 3846;
    	DatagramPacket datagramPacket=null;
    	int ap_state = 0;
    	WifiManager wifiManager = null;
    	WifiInfo wi = null;//AccessPoint
        SharedPreferences spref = null;
        if(ChatList.getCurrentActivity() != null)
            cont = ChatList.getCurrentActivity();
        else
            cont = ZingService.getContextFromService();

    	if(macAddress ==  null)
    	{
            if(cont != null)
            {
                spref = cont.getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
            }

            else
            {
                spref = ZingService.getContextFromService().getSharedPreferences("sprefs", Activity.MODE_PRIVATE);
            }
        	macAddress = spref.getString("mac", "mac");
        	macAddress = macAddress.replace(":", "");
    	}
    	
    		
	     //Log.d("IP Address of host PC",Integer.toString(wi.getIpAddress()));
		 /*
		  * To send broadcast packets, we need to know the broadcast address for the currently connected network
		  * First, we get all the available network interfaces on the device.
		  * Next, we loop through each of them getting all the available InetAddresses on the interface
		  * We then check to make sure it is not loopback and it starts with either wlan0 or ap0
		  * if these conditions hold true, then we have gotten the InetAddress we are looking for
		  */
		 
		 //if(wifiManager.getWifiState() == WifiManager.)
		 try 
		 {
			wifiManager = (WifiManager) cont.getSystemService(Activity.WIFI_SERVICE);
			wi =  wifiManager.getConnectionInfo();
			Method meth = wifiManager.getClass().getDeclaredMethod("getWifiApState");
			meth.setAccessible(false);
			ap_state =(Integer)meth.invoke(wifiManager, (Object[])null);
			if(ap_state > 10)
			{
				ap_state-=10;
			}
		}
		 catch (Exception e1) 
		 {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 ConnectivityManager con =null;
		 NetworkInfo ni =null;
		 if(cont != null)
		 {
			 con =(ConnectivityManager)cont.getSystemService(Activity.CONNECTIVITY_SERVICE);
			 ni= con.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		 }
		 else 
			 return;
		 
		 if(ni.getState() == NetworkInfo.State.CONNECTED || ap_state==3)
		 {
			 try
			 {
				try
				{
					// macAddress = wi.getMacAddress();
					 //Log.d("Mac Adress from WifiInfo",macAddress);
					 
		              Enumeration<NetworkInterface> neti = NetworkInterface.getNetworkInterfaces();
		              for(NetworkInterface e : Collections.list(neti))
		              {
		            	  //Log.d("This is the display name:",e.getDisplayName());
		            	  //Log.d("This is the name:",e.getName());
		                  Enumeration<InetAddress> inet = e.getInetAddresses();
		                  for(InetAddress add : Collections.list(inet))
		                  {
		                	  if(!add.isLoopbackAddress() &&(e.getDisplayName().contains("wlan0") || e.getDisplayName().contains("ap0")))
		                	  {
		                		  	//Log.d("InetAddress",add.getHostName());
		                		  	inetadd = add;
		                	  }
		                  }
		              }
		              
		              /*
		               * Just in case the device doesn't have its wifi network labelled by wlan or ap0
		               * we have to use another means.
		               * We query WifiManger and get the mac address of the currently used interface
		               * Do some computations....
		               * Then get the inetaddresses associated with the interface
		               * make sure the address is not loopback and it is reachable
		               */
		              
		              if(inetadd == null)
		              {
		            	  String wiadd = wi.getMacAddress();
		            	  byte macBytes [] = macAddressToByteArray(wiadd);
		            	  BigInteger big1 = new BigInteger(macBytes);
		            	  for(NetworkInterface currentInterface: Collections.list(neti))
		            	  {
		            		  byte [] hardwareAddress = currentInterface.getHardwareAddress();
		            		  if(hardwareAddress != null)
		            		  {
		            			  BigInteger big2 = new BigInteger(hardwareAddress);
		            			  if(big1.equals(big2))
		            			  {
		            				  Enumeration<InetAddress> inet = currentInterface.getInetAddresses();
		            				  for(InetAddress add : Collections.list(inet))
		            				  {
		            					  if(!add.isLoopbackAddress() && add.isReachable(5000))
		            					  {
		            						  inetadd = add;
		            					  }
		            				  }
		            			  }
		            		  }
		            	  }
		              }
		              
	              //Log.d("This is the interface", g);
	         }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			 
			 if(inetadd != null)
			 {
				 NetworkInterface neti = NetworkInterface.getByInetAddress(inetadd);
	    		 List<InterfaceAddress> ls = neti.getInterfaceAddresses();
	    		 for(InterfaceAddress e : ls)
	    		 {
	    			 try
	    			 {
	    				
	    				 bcast = e.getBroadcast();
	    				 if(bcast != null)
	    				 {
	    					// Log.d("broadcast",bcast.toString());
	    					 ip_address = inetadd.getHostAddress();
	    				 }
	    				   
	    			 }
	    			 catch(Exception g)
	    			 {
	    				 g.printStackTrace();
	    			 }
	    			 
	    		 }
			 }
			 //bcast = null;
			 broadcastAddress = new InetSocketAddress(bcast,DEST_PORT);
			 if(datagramSocket == null)
				 datagramSocket = new DatagramSocket(HOST_PORT);
			 datagramSocket.connect(broadcastAddress);
			 deviceInfo = getDeviceInfo();
			 byte [] bytMacAddress = NetworkInterface.getByInetAddress(inetadd).getHardwareAddress();//get's device mac_address
			 //Log.d("Mac Address Byte Array Length", Integer.toString(bytMacAddress.length));
    		 StringBuilder sb = new StringBuilder();
    		 int counts=0;
    		 
    		 while(counts < bytMacAddress.length)
    		 {
    			 String a = Integer.toHexString(Integer.parseInt(Byte.toString(bytMacAddress[counts++])));
    			 if(a.length() == 8)
    			 {
    				 a = a.substring(6);
    				 sb.append(a);
    			 }
    			 
    			 else if(a.length() == 1)
    			 {
    				 a = a.replace(a, "0"+a);
				 sb.append(a);
			 }
			 
			 else
			 {
				 sb.append(a);
			 }
				
			// Log.d("The value of byte array is " ,);
			 //Log.d("Mac Address", a);
			 //sb.append(Byte.toHexString(bytMacAddress[counts++]));
    		}
		
		// Log.d("The value of 234 is " ,sb.toString());
		 
		 //macAddress = sb.toString();
    	
		
		 
		 deviceInfo.put("mac_address",macAddress);
		 Log.d("Mac Address Computation",macAddress);
		
		 //this code is to make sure every packet sent is 8*1024 bytes long
		 byte dataToSend[] = null;
		 dataToSend = new byte[512];
		 byte data[] = deviceInfo.toString().getBytes();
		 int count = data.length;
		 int loop = 0;
		 while(loop<count && count<dataToSend.length)
		 {
			 dataToSend[loop] = data[loop];
			 loop++;
		 }
		 datagramPacket = new DatagramPacket(dataToSend,dataToSend.length);
		 Log.d("Data Sent", new String(dataToSend));
		 datagramSocket.setBroadcast(true);
		 datagramSocket.send(datagramPacket);
		 //datagramSocket.close();
		 }
		
		catch(JSONException e)
    	{
    		e.printStackTrace();
    	}
    	catch(NullPointerException e)
    	{
    		e.printStackTrace();
    	}
    	catch(SocketException e)
    	{
    		
    		e.printStackTrace();
    		//datagramSocket.close();
    	}
    	catch(UnknownHostException e)
    	{
    		e.printStackTrace();
    	}
    	catch(IOException e)
    	{
    		//datagramSocket.close();
    		e.printStackTrace();
    	}
	}
		 
		 else
		 {
			 macAddress = null;
		 }
    }
   
    protected static byte[] macAddressToByteArray(String macString) 
    {
    	
        String[] mac = macString.split("[:\\s-]");
        byte[] macAddress = new byte[6];
        for (int i = 0; i < mac.length; i++)
        {
            macAddress[i] = Integer.decode("0x" + mac[i]).byteValue();
        }

        return macAddress;
    }
    //get device and user info from database
    private static JSONObject getDeviceInfo() throws JSONException,SocketException,NullPointerException //for datagram packet
    {
    	JSONObject jobj = new JSONObject();
    	String zing_id = DatabaseHelper.getZingID(cont);
    	String status = DatabaseHelper.getStatus(cont);
    	
    	//correct this
    	long profile_pix_count = DatabaseHelper.getProfilePixCount();
    	
    	jobj.put("zing_id", zing_id);
    	jobj.put("profile_pix_count", profile_pix_count);
    	jobj.put("ip_address",ip_address); //datagramSocket.getInetAddress().getHostAddress());
    	//Log.d("Ip To Send", jobj.getString("ip_address"));
    	//jobj.put("status", DatabaseHelper.getStatus(ChatList.getActivity()));
    	jobj.put("status", status);
    	
    	return jobj;
    }
    
    /*public static String getMacAddress()
    {
    	return macAddress;
    }*/


    /**
     * This method is called to close the port used for sending BroadcastMessages.
     */
    public static void closePort()
    {
        if(datagramSocket!= null && datagramSocket.isBound())
            datagramSocket.close();
            datagramSocket = null;
    }
    
	
     
}

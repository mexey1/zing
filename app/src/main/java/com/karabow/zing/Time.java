/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.karabow.zing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 *
 * @author ANTHONY
 */
public class Time 
{
         
	private static String s,s1,time;
   // private static Calendar c= Calendar.getInstance();
        
        
    public static String getTime()
    {
    	Calendar c= Calendar.getInstance();
   
	    int n=c.get(Calendar.SECOND);
	    int h,g=0; 
	    
	    
	    s=Integer.toString(c.get(Calendar.HOUR));
	    s1=Integer.toString(c.get(Calendar.MINUTE));
	    int r=c.get(Calendar.MINUTE);
	    h=c.get(Calendar.HOUR);
	    if(r<10)
	    {
	            s1="0"+s1;
	
	    }
	
	    if (h<10)
	    {
	            s="0"+s;
	    }
	    
	    if(h == 0)
	    	s="12";
	    
	    if(c.get(Calendar.AM_PM) == Calendar.AM) 
	       time = s+":"+s1+" AM";
	    else
	    	time = s+":"+s1+" PM";
	        
	       return time;
    }
    
    public static String getTime(Timestamp ts)
    {
    	Calendar cd = Calendar.getInstance();
		cd.setTimeInMillis(ts.getTime());
		int hour = cd.get(Calendar.HOUR);
		int minute = cd.get(Calendar.MINUTE);
		StringBuilder time = new StringBuilder();
		if(hour < 10)
		{
			if(hour == 0)
			{
				time.append("12");
			}
			
			else
			{
				time.append("0"+hour);
			}
		}
		
		else
		{
			time.append(hour);
		}
		
		if(minute < 10)
		{
			time.append(":"+"0"+minute+(cd.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM"));
		}
		else
		{
			time.append(":"+minute+(cd.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM"));
		}
		
			
		
		/*String time = /*Hour*String.format("%d:%d %s", 
											cd.get(Calendar.HOUR),
											cd.get(Calendar.MINUTE),
											(cd.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM"));*/
		return time.toString();
    }
    
    public static String getDayOfWeek(Calendar cal)
    {
    	return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    }
    
    public static String getDateAsString(Calendar cal)
    {
    	DateFormat df = DateFormat.getDateInstance();
    	
    	return df.format(cal.getTime());
    }
    
    public static Timestamp getTimeStamp()
    {
    	Calendar cal = Calendar.getInstance();
    	Timestamp ts = new Timestamp(cal.getTimeInMillis());
    	return ts;
    }
        
}








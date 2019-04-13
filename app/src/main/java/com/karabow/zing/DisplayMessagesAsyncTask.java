/*package com.karabow.zing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessagesAsyncTask extends AsyncTask<ArrayList<JSONObject>,String,String>
{
	private boolean hasBeenOpened = false;
	private static Activity act;
	private static String ip_address,mac_address;
	private static int id;
	private int lastMessageId;
	private ScrollView lays;
	private LinearLayout main_lay;
	private int count;
	public DisplayMessagesAsyncTask(Activity act)
	{
		this.act = act;
	}
	@Override
	protected String doInBackground(ArrayList<JSONObject>... params) 
	{
		// TODO Auto-generated method stub
		
	}
	
	
	
	private int[] computeSize(int... size)
	{
		WindowManager wm = (WindowManager)act.getSystemService(Activity.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int dpi = dm.densityDpi;
		Log.d("DPI", Integer.toString(dpi));
		size[0] = (int)((dpi*size[0])/160);
		size[1] = (int)((dpi*size[1])/160);
		return size;
	}
	
	private int getMaxWidth()
	{
		WindowManager wm = (WindowManager)act.getSystemService(Activity.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int dpi = dm.densityDpi;
		int size = width - (int)((dpi*40)/160);
		return size;
	}

}*/

package com.karabow.zing;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


public class SignUpActivity extends Activity 
{
	private static Activity act;
	private String pixPath;
	private int count;
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter,R.anim.animation_leave);
		act = this;
		act.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		setContentView(R.layout.sign_up);
		//showWelcomePopup();
		
		//Uri imguri = Uri.parse("android.resource://com.karabow.zing/"+R.drawable.user); 
		//pixPath = imguri.toString();
	}
	
	public void choosePix(View view)
	{
		Intent sign_up_intent = new Intent();
		sign_up_intent.setType("image/*");
		sign_up_intent.setAction(Intent.ACTION_GET_CONTENT);
		act.startActivityForResult(Intent.createChooser(sign_up_intent, "Select Picture"),5);
		Toast.makeText(act, "Hello, am running", Toast.LENGTH_LONG).show();
	}
	
	protected void onActivityResult(int requestcode, int resultcode, Intent data)
	{
		if(resultcode == Activity.RESULT_CANCELED)
		return;
		
		else if (resultcode == Activity.RESULT_OK)
		{
			Uri uri = data.getData();
			String path = getPath(uri);
			
			
			
			if(isFileSizeOk(path))
			{
				pixPath = path;
				ImageView img = (ImageView)act.findViewById(R.id.profile_pix);
				final Bitmap bmps = PrepareBitmap.resizeImage(path, img.getWidth(), img.getHeight());
				final Bitmap output = PrepareBitmap.drawRoundedRect(bmps,7f);
				img.setImageBitmap(output);
				img.invalidate();
			}
			
			else
				showAlertDialog("Selected file is small");
			
			
			
			//img.invalidate();
		}
		
	}
	
	//called to retrieve the path to the file selected
	private String getPath(Uri uri)
	{
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = act.managedQuery(uri,proj,null,null,null);
		
		int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		if(column_index == -1)
			return "invalid_column";
		else
		{
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		
	}
	
	
	//returns an activity object to any user
	public static Activity getActivity()
	{
		return act;
	}
	
	//called when a user has entered all necessary fields
	public void done(View view)
	{
		EditText zing_id = (EditText)findViewById(R.id.zing_id);
		EditText pass = (EditText)findViewById(R.id.password);
		EditText con_pass = (EditText)findViewById(R.id.confirm_password);
		ImageView img = (ImageView)act.findViewById(R.id.profile_pix);
		
		DatabaseHelper.initializeHelper(new DatabaseHelper(img.getContext()));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cursor = sql.rawQuery("SELECT zing_id FROM user_info WHERE zing_id='" + zing_id.getText().toString().trim() + "'", null);		
		cursor.moveToFirst();
		
		// Check if any of the fields is empty
		if(zing_id.getText().toString().trim().isEmpty() || pass.getText().toString().isEmpty() || con_pass.getText().toString().isEmpty())
		{
			//emptyFiledAlertDialog();
			showAlertDialog("You have one or more empty fields...");
		}
		
		//  Password and "confirm password" are different
		else if(!pass.getText().toString().equals(con_pass.getText().toString())) 
		{
			showAlertDialog("Password fields are not the same.");
		}
		
		// Check if user name exists already
		else if(cursor.getCount() > 0) 
		{
			showAlertDialog("The Zing ID has already been taken...");
			//usernameTaken();
		}
		 // Save profile picture
		else 
		{
			DatabaseHelper.createUser(pixPath, zing_id, pass);
			returnToLogin();
		}
		
		DatabaseHelper.closeDatabase();
	}
	
	public void cancel(View view)
	{
		Intent it = new Intent(getActivity(), MainActivity.class);
		getActivity().startActivity(it);
		getActivity().finish();
	}
	//called if nothing went wrong during account creation
	private void returnToLogin() 
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				/*Intent it = new Intent(getActivity(), MainActivity.class);
				getActivity().startActivity(it);*/
				getActivity().finish();
			}
		});
		LinearLayout lay = new LinearLayout(getActivity());
		lay.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView warn = new TextView(getActivity());
		warn.setPadding(10, 10, 10, 10);
		//warn.setTypeface(MainActivity.getLoadedWindowsTypeface());
		warn.setText("Your Zing account is ready to go!");
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
	
	private void showAlertDialog(String msg)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				
				//alert.
				//progress.dismiss();
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
	
	//called to reduce image quality to prevent OutOfMemoryError
	
	private boolean isFileSizeOk(String path)
	{
		BitmapFactory.Options bo = new BitmapFactory.Options();
		bo.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bo);
		
		final int REQUIRED_SIZE =70;
		
		//int scale =1;
		
		if(bo.outWidth >= REQUIRED_SIZE && bo.outHeight >=REQUIRED_SIZE)
			return true;
		else
			return false;
	}
	
	private void showWelcomePopup()
	{
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		FrameLayout fl = (FrameLayout)getActivity().findViewById(R.id.anchorlay);
		View v = li.inflate(R.layout.guide_welcome,fl,false);
		final LinearLayout lin = (LinearLayout)v.findViewById(R.id.welcome_guide);
				//s.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,-1.0f,Animation.RELATIVE_TO_PARENT,0.0f
				  ,Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
		ta.setDuration(1000);
		/*
		*Start entry animation 
		*/
		lin.startAnimation(ta);
		/*
		* Wait 2.5seconds before starting exit animation
		*/
		lin.postDelayed(new Runnable()
		{
		
			@Override
			public void run() 
			{
				TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,0.0f,Animation.RELATIVE_TO_PARENT,1.0f
										  ,Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
				ta.setDuration(1000);
				ta.setAnimationListener(new AnimationListener()
				{
				@Override
					public void onAnimationEnd(Animation animation)
					{
					// TODO Auto-generated method stub
						pop.dismiss();
						showTapPicturePopup();
					}
				
				@Override
					public void onAnimationRepeat(Animation animation) 
					{
					// TODO Auto-generated method stub
					
					}
			
				@Override
					public void onAnimationStart(Animation animation) 
					{
					// TODO Auto-generated method stub
					
					}
			
			});
			lin.startAnimation(ta);
		}
		
		}, 3000);
		
		
		pop.setWidth(LayoutParams.MATCH_PARENT);
		//pop.setWidth(120);
		pop.setHeight(160);
		pop.setContentView(v);
		pop.showAtLocation(fl, Gravity.TOP, 0, 80);
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus && count==0)
			showWelcomePopup();
		count++;
	}
	
	private void showTapPicturePopup()
	{
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		FrameLayout fl = (FrameLayout)getActivity().findViewById(R.id.anchorlay);
		View v = li.inflate(R.layout.tap_picture,fl,false);
		final LinearLayout lin = (LinearLayout)v.findViewById(R.id.welcome_guide);
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		//pop.setWidth(120);
		pop.setHeight(140);
		pop.setContentView(v);
		v.postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				pop.dismiss();
				zingIDPopup();
			}
			
		}, 3000);
			
		
		pop.showAtLocation(fl, Gravity.TOP, 0, 50);
	}
	
	private void zingIDPopup()
	{
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		FrameLayout fl = (FrameLayout)getActivity().findViewById(R.id.anchorlay);
		EditText zing = (EditText)getActivity().findViewById(R.id.zing_id);
		View v = li.inflate(R.layout.tap_picture,fl,false);
		TextView tv = (TextView)v.findViewById(R.id.textView1);
		tv.setText("Enter a cute zing_id e.g doro-hot");
		//final LinearLayout lin = (LinearLayout)v.findViewById(R.id.welcome_guide);
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		//pop.setWidth(120);
		pop.setHeight(140);
		pop.setContentView(v);
		Rect rect = new Rect();
		zing.getLocalVisibleRect(rect);
		
		v.postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				pop.dismiss();
				passwordPopup();
			}
			
		}, 3000);
		
		pop.showAsDropDown(zing, 0, rect.top-2*rect.height());
		//pop.showAtLocation(zing, Gravity.TOP, 0, rect.top);
	}
	
	private void passwordPopup()
	{
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		FrameLayout fl = (FrameLayout)getActivity().findViewById(R.id.anchorlay);
		EditText zing = (EditText)getActivity().findViewById(R.id.password);
		View v = li.inflate(R.layout.tap_picture,fl,false);
		TextView tv = (TextView)v.findViewById(R.id.textView1);
		tv.setText("Enter a password e.g ****...and then re-enter it below");
		//final LinearLayout lin = (LinearLayout)v.findViewById(R.id.welcome_guide);
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		//pop.setWidth(120);
		pop.setHeight(140);
		pop.setContentView(v);
		Rect rect = new Rect();
		zing.getLocalVisibleRect(rect);
		
		v.postDelayed(new Runnable()
		{

			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				pop.dismiss();
				//donePopup();
			}
			
		}, 3000);
		pop.showAsDropDown(zing, 0, rect.top-2*rect.height());
		//pop.showAtLocation(zing, Gravity.TOP, 0, rect.top);
	}
	
	private void donePopup()
	{
		final PopupWindow pop = new PopupWindow();
		LayoutInflater li = LayoutInflater.from(act);
		FrameLayout fl = (FrameLayout)getActivity().findViewById(R.id.anchorlay);
		Button zing = (Button)getActivity().findViewById(R.id.button1);
		View v = li.inflate(R.layout.tap_picture,fl,false);
		TextView tv = (TextView)v.findViewById(R.id.textView1);
		tv.setText("Told ya we'd be done in a min...");
		//final LinearLayout lin = (LinearLayout)v.findViewById(R.id.welcome_guide);
		pop.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.empty));
		pop.setWidth(LayoutParams.WRAP_CONTENT);
		//pop.setWidth(120);
		pop.setHeight(140);
		pop.setContentView(v);
		Rect rect = new Rect();
		zing.getLocalVisibleRect(rect);
		pop.showAsDropDown(zing, 0, rect.top-2*rect.height());
		//pop.showAtLocation(zing, Gravity.TOP, 0, rect.top);
	}
}

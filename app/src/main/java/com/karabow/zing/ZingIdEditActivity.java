package com.karabow.zing;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import github.ankushsachdeva.emojicon.EmojiconEditText;

public class ZingIdEditActivity extends Activity
{
	private Activity act;
	private int count;
	public static boolean keyboardVisible=false;
	private EmojiDisplay emd;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter,R.anim.animation_leave);
		act = this;
		act.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		setContentView(R.layout.zing_id_activity);
		
		final EmojiconEditText zing_id = (EmojiconEditText)act.findViewById(R.id.zingIdEditText);
		final ImageView smiley = (ImageView)act.findViewById(R.id.smiley1);
		zing_id.setText(DatabaseHelper.getZingID(act));
		
		/*
		 * attach Keyboardlisteners to handle callback due to back-key being pressed
		 */
		zing_id.setKeyboardListener(new KeyboardListener()
		{
			@Override
			public void isBackKeyPressed(boolean yes) 
			{
				// TODO Auto-generated method stub
				if(yes)
				{
					showEmoji(smiley);
				}
			}
		});
		
		zing_id.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				keyboardVisible = true;
				if(emd != null)
				{
					emd.setEmojiEditText(zing_id);
					//keyboardVisible = true;
				}
			}
			
		});
		
		smiley.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					smiley.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					smiley.setBackgroundColor(0);
					
				}
				return false;
			}
		});
		smiley.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				if(emd == null)
				{
					LinearLayout lay = (LinearLayout)act.findViewById(R.id.smiley_edittext);
					emd = new EmojiDisplay(lay, (Activity)ChatList.getCurrentActivity(),zing_id);
				}
				
				InputMethodManager im = (InputMethodManager)ChatList.getCurrentActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
				if(keyboardVisible)
				{
					showEmoji(v);
					Toast.makeText(ChatList.getCurrentActivity(), "Keyboard Visible", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(ChatList.getCurrentActivity(), "Toggle Called", Toast.LENGTH_LONG).show();
					im.toggleSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED,0);
					showEmoji(v);
				}
					//im.sh
			}
			
		});
		
		final Button save = (Button)act.findViewById(R.id.saveButton);
		
		save.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					save.setBackgroundColor(getResources().getColor(R.color.baby_blue));
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					save.setBackgroundColor(0);
					
				}
				return false;
			}
		});
		
		save.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				DatabaseHelper.updateUserId(zing_id.getText().toString());
				Toast.makeText(ChatList.getCurrentActivity(), "Zing ID update successful", Toast.LENGTH_LONG).show();
				act.finish();
			}
			
		});
	}
	
	public void showEmoji(View v)
	{
		LinearLayout lay = (LinearLayout)act.findViewById(R.id.smiley_edittext);
		if(count == 0)
		{
			
			
			/*EditText msg = (EditText)act.findViewById(R.id.editText1);
			msg.performClick();*/
			
			EmojiDisplay.showEmoji(lay);
			EmojiDisplay.setEmojiVisibile(true);
			((ImageView)v).setImageDrawable(act.getResources().getDrawable(R.drawable.smiley2));
			count++;
		}
		
		else 
		{
			count--;
			((ImageView)v).setImageDrawable(act.getResources().getDrawable(R.drawable.smiley));
			EmojiDisplay.closeEmoji();
			EmojiDisplay.setEmojiVisibile(false);
		}
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
		{
			ChatList.setCurrentActivity(act);
		}
	}
	
	/*final EmojiconEditText status = (EmojiconEditText)view.findViewById(R.id.statusEditText);
	
	zing_id.setText(DatabaseHelper.getZingID(getActivity()));
	status.setText(DatabaseHelper.getStatus(getActivity()));
	
	
	
	status.setKeyboardListener(new KeyboardListener()
	{
		@Override
		public void isBackKeyPressed(boolean yes) 
		{
			// TODO Auto-generated method stub
			if(yes)
			{
				showEmoji(smiley);
			}
		}
	});
	
	
	
	
	status.setOnFocusChangeListener(new OnFocusChangeListener()
	{

		@Override
		public void onFocusChange(View arg0, boolean arg1) 
		{
			// TODO Auto-generated method stub
			if(arg1)
			{
				keyboardVisible = true;
				if(emd != null)
					emd.setEmojiEditText(status);
			}
			
		}
		
	});
	
	smiley.setOnClickListener(new OnClickListener()
	{

		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			if(emd == null)
			{
				LinearLayout lay = (LinearLayout)view.findViewById(R.id.bottomLayout);
				emd = new EmojiDisplay(lay, ChatList.getCurrentActivity(),zing_id);
			}
			
			InputMethodManager im = (InputMethodManager)ChatList.getCurrentActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
			if(keyboardVisible)
			{
				showEmoji(v);
			}
			else
			{
				im.toggleSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_NOT_ALWAYS);
				showEmoji(v);
			}
				//im.sh
		}
		
	});*/
	
	
	
	
	
	

}

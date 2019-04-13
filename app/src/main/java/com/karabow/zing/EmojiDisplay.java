package com.karabow.zing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup.*;
import github.ankushsachdeva.emojicon.emoji.Emojicon;



public class EmojiDisplay 
{
	private LinearLayout lay;
	private Activity act;
	private EmojiconEditText emojiconEditText;
	private static EmojiconsPopup popup;
	private static boolean vis;
	public EmojiDisplay(LinearLayout lay, Activity act)
	{
		this.lay = lay;
		this.act = act;
		emojiconEditText = (EmojiconEditText)act.findViewById(R.id.editText1);
		initEmoji();
	}
	
	public EmojiDisplay(LinearLayout lay, Activity act, EmojiconEditText ed)
	{
		this.lay = lay;
		this.act = act;
		emojiconEditText = ed;
		initEmoji();
	}
	
	private void initEmoji()
	{
		// Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
		//if(popup == null)
		popup = new EmojiconsPopup(lay, act);

		//Will automatically set size according to the soft keyboard size        
		popup.setSizeForSoftKeyboard();

		//Set on emojicon click listener
		popup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() 
		{
					

		            @Override
		            public void onEmojiconClicked(Emojicon emojicon) 
		            {
		                emojiconEditText.append(emojicon.getEmoji());
		            }

					
		        });

		//Set on backspace click listener
		
		
		popup.setOnEmojiconBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() 
		{

		    @Override
		    public void onEmojiconBackspaceClicked(View v) 
		    {
		        KeyEvent event = new KeyEvent(
		                 0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
		                 emojiconEditText.dispatchKeyEvent(event);
		    }
		});

		//Set listener for keyboard open/close
		/*popup.setOnSoftKeyboardOpenCloseListener(new OnSoftKeyboardOpenCloseListener() {

		            @Override
		            public void onKeyboardOpen(int keyBoardHeight) {
		                if(!popup.isShowing()){
		                    popup.showAtBottom();
		                }
		            }

		            @Override
		            public void onKeyboardClose() {
		                if(popup.isShowing())
		                    popup.dismiss();
		            }
		        });*/

		//popup.showAtBottom();
		//popup.
		//To show popup manually you can call popup.showAtBottom();
		//To show popup when the soft keyboard is not already visible, use popup.showAtBottomPending()
	}
	
	public static void closeEmoji()
	{
		try
		{
			popup.dismiss();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//popup = null;
	}
	
	public void setEmojiEditText(EmojiconEditText ed)
	{
		emojiconEditText = ed;
	}
	
	public static void showEmoji(View v)
	{
		Log.d("True/ False", Boolean.toString(popup instanceof EmojiconsPopup));
		
		if(popup != null)
		{
			if(popup.isKeyBoardOpen())
				popup.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			else
				popup.showAtBottomPending();
		}
		
		//popup.showAtBottom();
	}

	public static void setEmojiVisibile(boolean b) 
	{
		// TODO Auto-generated method stub
		vis = b;
	}
	
	public static boolean isEmojiVisible()
	{
		return vis;
	}
}

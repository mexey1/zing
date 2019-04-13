package com.karabow.zing;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import github.ankushsachdeva.emojicon.EmojiconEditText;

/**
 * Created by Anthony on 2/10/15.
 */
public class Status extends Activity
{
    private Activity act;
    private int count;
    public static boolean keyboardVisible = false;
    private EmojiDisplay emd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.animation_enter,R.anim.animation_leave);
        act = this;
        act.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        setContentView(R.layout.status);

        final EmojiconEditText status = (EmojiconEditText)act.findViewById(R.id.statusEditText);
        final ImageView smiley = (ImageView)act.findViewById(R.id.smiley1);
        status.setText(DatabaseHelper.getStatus(act));

		/*
		 * attach Keyboardlisteners to handle callback due to back-key being pressed
		 */
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

        status.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                keyboardVisible = true;
                if(emd != null)
                {
                    emd.setEmojiEditText(status);
                    //keyboardVisible = true;
                }
            }

        });

        smiley.setOnTouchListener(new View.OnTouchListener()
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
        smiley.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                if(emd == null)
                {
                    LinearLayout lay = (LinearLayout)act.findViewById(R.id.smiley_edittext);
                    emd = new EmojiDisplay(lay, (Activity)ChatList.getCurrentActivity(),status);
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

        save.setOnTouchListener(new View.OnTouchListener()
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

        save.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                DatabaseHelper.updateUserStatus(status.getText().toString());
                Toast.makeText(ChatList.getCurrentActivity(), "Status update successful", Toast.LENGTH_LONG).show();
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

}








package com.karabow.zing;

import android.app.Activity;
import android.os.Bundle;


/**
 * Created by Anthony on 2/10/15.
 */
public class About extends Activity
{
    private Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        act = this;
        //this.getActionBar().hide();
        this.setContentView(R.layout.about);
        act.getActionBar().hide();
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

package com.karabow.zing;




import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Settings extends Fragment
{
    private View view;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	
	{
		if(container == null)
		{
			Log.d("This Is Null", "Container");
			return null;
		}
		Log.d("This Is Null", "Containerfggf");
		view =  inflater.inflate(R.layout.settings, container, false);

        aboutInit();
        statusInit();
        wifiNetworksInit();


		
		return view;


	}

    private void aboutInit()
    {
        LinearLayout about = (LinearLayout)view.findViewById(R.id.aboutLayout);
        about.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(ChatList.getCurrentActivity().getResources().getColor(R.color.baby_blue));

                }

                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundColor(0);
                }
                return false;
            }
        });

        about.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ChatList.getCurrentActivity(),About.class);
                ChatList.getCurrentActivity().startActivity(intent);
            }
        });
    }

    private void statusInit()
    {
        LinearLayout status = (LinearLayout)view.findViewById(R.id.status);
        status.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(ChatList.getCurrentActivity().getResources().getColor(R.color.baby_blue));

                }

                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundColor(0);
                }
                return false;
            }
        });

        status.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ChatList.getCurrentActivity(),Status.class);
                ChatList.getCurrentActivity().startActivity(intent);
            }
        });
    }

    private void wifiNetworksInit()
    {
        LinearLayout wifi = (LinearLayout)view.findViewById(R.id.wifi);
        wifi.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(ChatList.getCurrentActivity().getResources().getColor(R.color.baby_blue));

                }

                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundColor(0);
                }
                return false;
            }
        });

        wifi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ChatList.getCurrentActivity(),WifiNetworks.class);
                ChatList.getCurrentActivity().startActivity(intent);
            }
        });

    }
}

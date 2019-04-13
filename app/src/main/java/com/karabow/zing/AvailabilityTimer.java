package com.karabow.zing;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class AvailabilityTimer extends TimerTask
{
	private Timer timer;
	private boolean isAvailable = true;
	private int id;
	private String mac_address;
	public AvailabilityTimer(int id,String mac)
	{
		this.id = id;
		this.mac_address =mac;
		timer = new Timer();
		timer.schedule(this, 6000);
	}
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		if(Zingers.getZingersView() != null)
		{
			View zingers_layout = Zingers.getZingersView().findViewById(R.id.chats_layout);
			
			View view  = zingers_layout.findViewById(id);
			Log.d("ID",Integer.toString(id));
			Log.d("Is view null",Boolean.toString(view instanceof View));
            if(view != null)
            {
                final ImageView img = (ImageView)view.findViewById(R.id.indicator);
                img.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        img.setImageResource(0);
                        img.setImageResource(R.drawable.zing_offline);
                        if(ChatWindow.getActivity() != null && ChatWindow.getRecipientMacAddress().equals(mac_address))
                        {
                            LinearLayout bl = (LinearLayout)ChatWindow.getActivity().findViewById(R.id.bottomm_layout);
                            bl.setEnabled(false);
                            ImageView call_icon = (ImageView)ChatWindow.getActivity().findViewById(R.id.call_icon);
                            call_icon.setEnabled(false);
                            ImageView add_icon = (ImageView)ChatWindow.getActivity().findViewById(R.id.add_icon);
                            add_icon.setEnabled(false);
                        }
                    }

                });
            }

		}
		isAvailable = false;
	}
	
	public boolean getAvailability()
	{
		return isAvailable;
	}
	
	public void setAvailabilityOn()
	{
		if(Zingers.getZingersView() != null)
		{
			View zingers_layout = Zingers.getZingersView().findViewById(R.id.chats_layout);
			View view  = zingers_layout.findViewById(id);
			Log.d("ID",Integer.toString(id));
			Log.d("Is view null",Boolean.toString(view instanceof View));
			final ImageView img = (ImageView)view.findViewById(R.id.indicator);
			img.setImageResource(0);
			img.setImageResource(R.drawable.zing_online);
		}
	}
	
	public Timer getTimer()
	{
		return timer;
	}

}

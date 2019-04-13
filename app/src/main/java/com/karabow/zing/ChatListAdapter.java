package com.karabow.zing;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;



public class ChatListAdapter extends FragmentStatePagerAdapter
{
	private final int COUNT=4;
	private  List <Fragment> list;
	private  FragmentManager fm;
	private Fragment fragment;
	private ViewPager vpager;
  public ChatListAdapter(FragmentManager fm)
  {
	 super(fm); 
	 this.fm = fm;
	
  }
  
  public int getCount()
  {
	  return COUNT;
  }
  
  public Fragment getItem(int pos)
  {
	 if(pos == 0) 
		 return Fragment.instantiate(ChatList.getCurrentActivity(),Profile.class.getName());
	 else if(pos == 1)
		return Fragment.instantiate(ChatList.getCurrentActivity(),Zingers.class.getName());
	 else if(pos == 2)
			return Fragment.instantiate(ChatList.getCurrentActivity(),Chats.class.getName());
	 else 
			return Fragment.instantiate(ChatList.getCurrentActivity(),Settings.class.getName());
	 
	
	
	// else return null;
  }
  
@Override
public void destroyItem(View container, int position, Object object) 
{
	super.destroyItem(container, position, object);
	View view = (View)object;
	ViewPager vp = (ViewPager)container;
	
	ViewGroup vg = (ViewGroup)view;
	int count = vg.getChildCount();
	for(int c=0;c<count;c++)
	{
		View v = vg.getChildAt(c);
		v=null;
	}
		
	vp.removeView(view);
	view = null;
	Log.d("this is happening","Destroying items");
	Toast.makeText(container.getContext(), "Destroyed", Toast.LENGTH_LONG).show();
    //super.destroyItem(container, position, object);
   //System.gc();
}

/*public void finishUpdate(ViewGroup g)
{
	super.finishUpdate(g);
	if(g != null)
	{
		Toast.makeText(g.getContext(), "Destroyed...about to", Toast.LENGTH_LONG).show();
		int pos = ((ViewPager)g).getCurrentItem();
		
		if(pos>0)
		{
			View v = ((ViewPager)g).getChildAt(pos-1);
			if(v != null)
				//destroyItem(g,pos,((ViewPager)g).getChildAt(pos-1));
		}
			
		else{
			View v = ((ViewPager)g).getChildAt(3);
			if(v != null)
				//destroyItem(g,pos,((ViewPager)g).getChildAt(3));
		}
	}
		
}*/

/*public void recreatePageThree(ViewPager vpager)
{
		list = fm.getFragments();
		//destroyItem(vpager,2,list.get(2).getView());
		vpager.removeViewAt(2);
		fragment = Fragment.instantiate(ChatList.getActivity(),Chats.class.getName());
		this.vpager = vpager;
		//vpager.addView(fragment.getView(), 2);
	
	
}

public void show()
{
	vpager.addView(fragment.getView(),2);
}*/

	public int getItemPosition(Object obj)
	{
		return POSITION_UNCHANGED; 
	}

}
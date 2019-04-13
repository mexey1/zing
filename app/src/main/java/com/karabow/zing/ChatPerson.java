package com.karabow.zing;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import github.ankushsachdeva.emojicon.EmojiconTextView;

public class ChatPerson extends AsyncTask<JSONObject, Integer, Integer>
{
	//private static HashMap<Key, JSONObject>per1sonsTable;
	private static JSONObject jobj;
	private Context cont = null;
	@Override
	protected Integer doInBackground(JSONObject... arg0) 
	{
		synchronized(ChatPerson.class)
		{
			jobj = arg0[0];
			return Integer.valueOf(DatabaseHelper.addZinger(arg0[0]));
		}
		
	}
	
	public void onPostExecute(final Integer id)
	{
		synchronized(ChatPerson.class)
		{
			if(id > 0)
			{
				final ViewGroup act = Zingers.getZingersView();
				
				if(act != null)
				{
					try
					{
						/*
						 * If this Zinger has not been added to the hashmap, we add him/her
						 */
						HashMap<String,AvailabilityTimer>hmap = ChatList.getHashMap();
						
						if(!hmap.containsKey(jobj.getString("mac_address")))
						{
							hmap.put(jobj.getString("mac_address"), new AvailabilityTimer(id,jobj.getString("mac_address")));
						}
						ProgressBar pb = (ProgressBar)act.findViewById(R.id.scan_progress);
						TextView tv = (TextView)act.findViewById(R.id.scan_text);

                        Button cg = (Button)act.findViewById(R.id.create_group);
                        Button jg = (Button)act.findViewById(R.id.join_group);

                        cg.setVisibility(Button.GONE);
                        jg.setVisibility(Button.GONE);
						pb.setVisibility(ProgressBar.GONE);
						tv.setVisibility(TextView.GONE);
						
						ScrollView sv = (ScrollView)act.findViewById(R.id.scrollView1);
						sv.setVisibility(ScrollView.VISIBLE);
						LinearLayout zingers_layout = (LinearLayout)act.findViewById(R.id.chats_layout);
						
						if(ChatList.getCurrentActivity() != null)
						  cont = ChatList.getCurrentActivity();
						else if(ChatWindow.getActivity() != null)
							cont = ChatWindow.getActivity();
						LayoutInflater li = LayoutInflater.from(cont);
						
						final View view = li.inflate(R.layout.chat_person_status, zingers_layout,false);
						TextView name = (TextView)view.findViewById(R.id.name_status);
						TextView status = (TextView)view.findViewById(R.id.status);
						status.setTextColor(ChatList.getCurrentActivity().getResources().getColor(R.color.battleship_grey));
						status.setTextSize(14);
						status.setEllipsize(TextUtils.TruncateAt.END);
						ImageView img = (ImageView)view.findViewById(R.id.indicator);
						//JSONObject jobj = (JSONObject)personsTable.get(file);
						name.setText(jobj.getString("zing_id"));
						status.setText(jobj.getString("status"));
						img.setImageDrawable(ChatList.getCurrentActivity().getResources().getDrawable(R.drawable.zing_online));
						Toast.makeText(ChatList.getCurrentActivity(), "ID Value"+Integer.toString(id), Toast.LENGTH_LONG).show();
						view.setId(id);
						
						view.setOnTouchListener(new OnTouchListener()
						{

							@Override
							public boolean onTouch(View v, MotionEvent event) 
							{
								
								if(event.getAction() ==  MotionEvent.ACTION_DOWN)
								{
									v.setBackgroundColor(act.getResources().getColor(R.color.baby_blue));
								}
								else if(event.getAction() == MotionEvent.ACTION_UP)
								{
									
									v.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.linear_border));
									
								}
								return false;
							}
						});
						
						view.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View v) 
							{
								// TODO Auto-generated method stub
								ChatWindow.setProperties(v.getId(),
														 DatabaseHelper.getIpAddress(v.getId()),
														 DatabaseHelper.getMacAddress(v.getId())
														,DatabaseHelper.getZingerId(v.getId()));
								Intent intent = new Intent(cont,ChatWindow.class);
								cont.startActivity(intent);
							}
							
						});
						
						/**
						 * Check if we had a copy of this profile pix stored before
						 */
						long pix_id = DatabaseHelper.getZIngerProfilePixID(jobj.getString("mac_address"));
						/**
						 * -1 means we dont have a copy of a picture for this zinger
						 */
						if(pix_id != -1)
						{
							 if(ChatList.getCurrentActivity() != null)
							 {
								 if(Zingers.getZingersView() != null )
								 {
									 final ImageView imgs = (ImageView)view.findViewById(R.id.profile_pix);
									 imgs.postDelayed(new Runnable()
									 {

										@Override
										public void run() 
										{
											// TODO Auto-generated method stub
											 //imgs.setImageDrawable(null);
											 byte [] pix = DatabaseHelper.getZIngerProfilePix(DatabaseHelper.getMacAddress(id));
											 if(pix != null && pix.length > 0)
											 {
												 Bitmap bmp = PrepareBitmap.resizeImage(pix, 180, 180);
												 if(bmp != null)
												 {
													 imgs.setImageBitmap(PrepareBitmap.drawRoundedRect(bmp,7f));
													 imgs.invalidate(); 
												 }
												 else
												 {
													 try
													 {
														 if(jobj.getLong("profile_pix_count") >0)
														 {
															 ProfilePictureReceive ppr = new ProfilePictureReceive(jobj.getString("ip_address"),jobj.getString("mac_address"),
																	  jobj.getString("zing_id"),jobj.getLong("profile_pix_count"),id);
														 	 ppr.start();
														 }
													 }
													 catch(JSONException e)
													 {
														 
													 }
												 }
												 
											 }
										}
										 
									 }, 3000);
									 
									
								 }
							 }
						}
						/**
						 * Request and receive picture because we dont have it
						 */
						else if(pix_id == -1 && jobj.getLong("profile_pix_count") > 0)
						{
							Log.d("START TRCEIVING PIX", "YEAH");
							//String ip, String mac_address, String zing_id, int pix_id
							ProfilePictureReceive ppr = new ProfilePictureReceive(jobj.getString("ip_address"),jobj.getString("mac_address"),
																				  jobj.getString("zing_id"),jobj.getLong("profile_pix_count"),id);
							ppr.start();
						}
						
						else if(jobj.getLong("profile_pix_count") == 0)
						{
							DatabaseHelper.storeProfilePix(new byte[0],jobj.getString("mac_address"), jobj.getString("zing_id"), "0");
							/*final ImageView imgs = (ImageView)view.findViewById(R.id.profile_pix);
							imgs.postDelayed(new Runnable()
							{

								@Override
								public void run() 
								{
									// TODO Auto-generated method stub
									imgs.setImageDrawable(C)
									
								}
								
							}, 3000);*/
							
						}
					
						zingers_layout.addView(view);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			/*
			 * This section handles situations in whic the zinger has been added to our table
			 * but changes might have occurred and we need to reflect them...
			 */
			else
			{
				try
				{
					/*
					 * Check if profile picture has changed...
					 */
					HashMap<String,AvailabilityTimer>hmap = ChatList.getHashMap();
					
					if(hmap.containsKey(jobj.getString("mac_address")))
					{
						int ids = DatabaseHelper.getId(jobj.getString("mac_address"));
						AvailabilityTimer timer = hmap.put(jobj.getString("mac_address"), new AvailabilityTimer(ids,jobj.getString("mac_address")));
						if(!timer.getAvailability())
							timer.setAvailabilityOn();
						timer.cancel();
						timer=null;
					}
					long pix_id = DatabaseHelper.getZIngerProfilePixID(jobj.getString("mac_address"));
					if(pix_id != -1)
					{
						if(pix_id != jobj.getLong("profile_pix_count") && jobj.getLong("profile_pix_count") > 0)
						{
                            int position = DatabaseHelper.getId(jobj.getString("mac_address"));
							ProfilePictureReceive ppr = new ProfilePictureReceive(jobj.getString("ip_address"),jobj.getString("mac_address"),
									  jobj.getString("zing_id"),jobj.getLong("profile_pix_count"),position-1);
                            Log.d("PROFILE PICTURE",Integer.toString(position-1));
							ppr.start();
						}
						else if(jobj.getLong("profile_pix_count") == 0)
						{
							DatabaseHelper.storeProfilePix(new byte[0],jobj.getString("mac_address"), jobj.getString("zing_id"), "0");
						}
					}
					
					/*
					 * Check if status or Zing_Id has changed
					 */
					String status[] = DatabaseHelper.getZingStatus(jobj.getString("mac_address")); 
					
					if(!status[0].equals(jobj.getString("zing_id")))
					{
						ViewGroup vg = Zingers.getZingersView();
						if(vg != null)
						{
							int position = DatabaseHelper.getId(jobj.getString("mac_address"));
							View child = vg.getChildAt(position-1);
							EmojiconTextView name  = (EmojiconTextView)child.findViewById(R.id.name_status);
							if(name != null)
							{
								name.setText(jobj.getString("zing_id"));
							}
						}
					}
					if(!status[1].equals(jobj.getString("status")))
					{
						ViewGroup vg = Zingers.getZingersView();
						if(vg != null)
						{
							int position = DatabaseHelper.getId(jobj.getString("mac_address"));
							View child = vg.getChildAt(position-1);
							EmojiconTextView stat = (EmojiconTextView)child.findViewById(R.id.status);
							if(stat != null)
							{
								stat.setText(jobj.getString("status"));
							}
						}
					}
					
					if(!status[1].equals(jobj.getString("status")) || !status[0].equals(jobj.getString("zing_id")))
					{
						DatabaseHelper.updateZingerStatus(jobj.getString("zing_id"), jobj.getString("status"), jobj.getString("mac_address"));
					}
					
					/*
					 * Check if IP address has changed
					 * 
					 */
					int idz = DatabaseHelper.getId(jobj.getString("mac_address"));
					if(!DatabaseHelper.getIpAddress(idz).equals(jobj.getString("ip_address")))
					{
						DatabaseHelper.updateIpAddress(jobj.getString("mac_address"),jobj.getString("ip_address"));
						
					}
					
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}

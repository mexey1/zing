package com.karabow.zing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private String create_user_info = "Create TABLE IF NOT EXISTS user_info (id INTEGER PRIMARY KEY AUTOINCREMENT, zing_id VARCHAR(30) UNIQUE,status VARCHAR(100), password VARCHAR(100), profile_pix BLOB, profile_pix_count VARCHAR(70), logged_in INTEGER)";
	private static String create_zingers = "Create TABLE IF NOT EXISTS zingers (id INTEGER PRIMARY KEY AUTOINCREMENT, mac_address VARCHAR(100) NOT NULL UNIQUE, zing_id VARCHAR(30), status VARCHAR(100),ip_address VARCHAR(100))";
	private static String create_messages = "CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, from_id VARCHAR(30), to_id VARCHAR(30), message VARCHAR(400)  NOT NULL, created DATETIME NOT NULL, unread INTEGER DEFAULT 10, sent INTEGER,type VARCHAR(10)) ";
	private static String create_profile_pix_table = "CREATE TABLE IF NOT EXISTS profile_pictures (id INTEGER PRIMARY KEY AUTOINCREMENT, zing_id VARCHAR(100), mac_address VARCHAR(100) UNIQUE, pix_data BLOB, pix_id VARCHAR(70))";
	private static DatabaseHelper dbhelper = null;
	private static int db_count;
	private static SQLiteDatabase sqldb;
	private static final int MSG_LIMIT = 20;
	
	public static synchronized void initializeHelper(DatabaseHelper helper)
	{
		/**
		 * Due to SQLite DB Locking, 
		 * This method makes sure that only one DatabaseHelper object is used
		 * Across multiple threads
		 */
		if(dbhelper == null)
		{
			dbhelper = helper;
		}
	}
	
	public static synchronized SQLiteDatabase openDatabase()
	{
		try
		{
			if (dbhelper == null)
			{
				throw new IllegalStateException("There must be a ");
			}
			
			db_count++;
			
			if(db_count == 1)
			{
				sqldb = dbhelper.getWritableDatabase();
			}
		}
		
		catch(NullPointerException e)
		{
			if(!(dbhelper instanceof DatabaseHelper))
			{
				dbhelper = new DatabaseHelper(SignUpActivity.getActivity());
				sqldb = dbhelper.getWritableDatabase();
				return sqldb;
			}
		}
		
		return sqldb;
		
	}
	
	public static synchronized void closeDatabase()
	{
		
		if(--db_count == 0)
			dbhelper.close();
		else
		{
			
		}
	}
	
	public DatabaseHelper(Context context)
	{
		super(context, "Zing", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		
		db.execSQL(create_user_info);
		db.execSQL(create_zingers);
		db.execSQL(create_messages);
		db.execSQL(create_profile_pix_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO Auto-generated method stub
		
	}
	
	public static String getZingID(Context cont)
	{
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cursor = sql.rawQuery("SELECT zing_id,status FROM user_info WHERE logged_in=1", null);
		cursor.moveToFirst();
		String g = cursor.getString(0);
		cursor.close();
		DatabaseHelper.closeDatabase();
		return g;
	}
	
	public static String getStatus(Context cont)
	{
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cursor = sql.rawQuery("SELECT status FROM user_info WHERE logged_in=1", null);
		cursor.moveToFirst();
		String g = cursor.getString(0);
		cursor.close();
		DatabaseHelper.closeDatabase();
		return g;
	}
	
	public static byte[] getProfilePix(Context cont)
	{
		try
		{
			DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
			SQLiteDatabase sql = DatabaseHelper.openDatabase();
			Cursor cursor = sql.rawQuery("SELECT profile_pix FROM user_info WHERE logged_in=1", null);
			cursor.moveToFirst();
			byte[] g = cursor.getBlob(0);
			//Log.d(tag, msg)
			DatabaseHelper.closeDatabase();
			cursor.close();
			return g;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void updateLoggedIn(Context cont, int id)
	{
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		sql.execSQL("UPDATE user_info SET logged_in=1 WHERE id="+id);
		DatabaseHelper.closeDatabase();
	}
	
	//add a new Zinger to our database
	public static int addZinger(JSONObject jobj)
	{
		Context cont = ChatList.getCurrentActivity();
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		try
		{
			String macAddress = jobj.getString("mac_address");
			//String varArgs[] = {macAddress};
			Cursor cur = sql.rawQuery("SELECT * FROM zingers WHERE mac_address = "+"'"+macAddress+"'", null);
			
			// If the MAC address does not exist in the table...
			if(cur.getCount() == 0)
			{
				String bindValues[] = {jobj.getString("mac_address"),
									   jobj.getString("zing_id"),
									   jobj.getString("status"),
									   jobj.getString("ip_address")};
				
				//Look into the string to see if it contains ' or "
				if(bindValues[0].contains("\'") ||bindValues[0].contains("\""))
				{
					bindValues[0] = bindValues[0].replace("\'", "\''");
					
					//bindValues[0] = bindValues[0].replace("\"", "\\\"");
				}
				
				//Look into the string to see if it contains ' or "
				if(bindValues[1].contains("\'") ||bindValues[1].contains("\""))
				{
					bindValues[1] = bindValues[1].replace("\'", "\''");
					
					//bindValues[1] = bindValues[1].replace("\"", "\\\"");
				}
				
				//Look into the string to see if it contains ' or "
				if(bindValues[2].contains("\'") ||bindValues[2].contains("\""))
				{
					bindValues[2] = bindValues[2].replace("\'", "\''");
					
					//bindValues[2] = bindValues[2].replace("\"", "\\\"");
				}
				
				//sql.execSQL("INSERT INTO zingers(mac_address, zing_id, status, ip_address) VALUES("+"'"+bindValues[0]+"' ,"+"'"+bindValues[0]+"', "+"'"+bindValues[0]+"', "+"'"+bindValues[0]+"'"+")",null);
				ContentValues cv = new ContentValues();
				cv.put("mac_address", bindValues[0]);
				cv.put("zing_id", bindValues[1]);
				cv.put("status", bindValues[2]);
				cv.put("ip_address", bindValues[3]);
				sql.insert("zingers", null, cv);
			//	sql.insert("", nullColumnHack, values)
				// Get last inserted id
				cur.close();
				Cursor c = sql.rawQuery("SELECT MAX(id) FROM zingers", null);
				c.moveToFirst();
				int id = c.getInt(0);
				c.close();
				DatabaseHelper.closeDatabase();
				
				return id;
			}
			else
				return 0; // ID already exists, so return zero
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			closeDatabase();
			return 0;
		}
	}
	
	public static JSONObject[] getZingers()
	{
		Context cont = ChatList.getCurrentActivity();
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		try
		{
			
			Cursor cursor  =sql.rawQuery("SELECT * FROM ZINGERS", null);
			cursor.moveToFirst();
			int count =0;
			JSONObject [] result = new JSONObject[cursor.getCount()];
			while(count<cursor.getCount())
			{
				JSONObject jobj = new JSONObject();
				jobj.put("id", cursor.getInt(cursor.getColumnIndex("id")));
				jobj.put("zing_id", cursor.getString(cursor.getColumnIndex("zing_id")));
				jobj.put("status", cursor.getString(cursor.getColumnIndex("status")));
				jobj.put("mac_address", cursor.getString(cursor.getColumnIndex("mac_address")));
				result[count] = jobj;
				count++;
				cursor.moveToNext();
			}
			cursor.close();
			closeDatabase();
			return result;
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getIpAddress(int id)
	{
		Context cont = ChatList.getCurrentActivity();
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cursor  =sql.rawQuery("SELECT ip_address FROM zingers where id="+id, null);
		cursor.moveToFirst();
		String ip = cursor.getString(0);
		cursor.close();
		closeDatabase();
		return ip;
	}
	
	public static void createZingers()
	{
		Context cont = ChatList.getCurrentActivity();
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		sql.execSQL(create_zingers);
		
		closeDatabase();
	}
	
	/**
	 * This method should be called with caution. Call this method and all ZIngers are LOST...
	 */
	public static void dropZingers()
	{
		Context cont = ChatList.getCurrentActivity();
		DatabaseHelper.initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		sql.execSQL("DROP TABLE IF EXISTS zingers");
		closeDatabase();
	}
	
	/**
	 * method to add a new message to the database
	 * @param from: who sent the message. This is usually a mac address
	 * @param to: who was the message sent to. This is usually a mac address
	 * @param msg: message content
	 * @param unread: a boolean indicating if message has been read. true if unread, false read
	 */
	public static void addMessageSent(String from, String to, String msg, int sent,String type)
	{
		if(from.length() < 0 || to.length() < 0 || msg.trim().isEmpty())
			return;
		
		Context cont = ChatList.getCurrentActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		//Look into the string to see if it contains ' or "
		if(msg.contains("\'") ||msg.contains("\""))
		{
			msg = msg.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		
		Log.d("message", msg);
		
		String[] listOfArgs = {from, to, msg.trim(), Time.getTimeStamp().toString(),
				Integer.toString(sent), type};

		sql.execSQL("INSERT INTO messages(from_id, to_id, message, created, sent, type) VALUES(?, ?, ?, ?, ?, ?)", listOfArgs);
		closeDatabase();
	}
	
	/**
	 * method to add a new message to the database
	 * @param from: who sent the message. This is usually a mac address
	 * @param to: who was the message sent to. This is usually a mac address
	 * @param msg: message content
	 * @param unread: a boolean indicating if message has been read. true if unread, false read
	 * @param sent: an int value that indicates if the message has been sent. 0 for unsent, 1 for sent.
	 */
	public static void addMessageReceived(String from, String to, String msg, boolean unread, String type)
	{
		if(from.length() < 0 || to.length() < 0 || msg.trim().isEmpty())
			return;
		
		/************************** NOTES ON "sent" ******************************/
		// When sent is -1, the message is in the "sending" state
		// When sent is 0, the message is in the "send failed" state
		// When sent is 1, the message is in the "send succeeded" state
		/************************************************************************/
		
		Context cont = ChatList.getCurrentActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		//Look into the string to see if it contains ' or "
		if(msg.contains("\'") ||msg.contains("\""))
		{
			msg = msg.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		
		// Convert arguments to string and store in string array
		String[] listOfArgs = {from, to, msg.trim(), Time.getTimeStamp().toString(),
								(unread ? Integer.toString(1) : Integer.toString(0)),
								 type};
		
		sql.execSQL("INSERT INTO messages(from_id, to_id, message, created, unread, type) VALUES(?, ?, ?, ?, ?, ?)", listOfArgs);
		closeDatabase();
	}
	
	/**
	 * retrieve ID of a zinger from Zingers table 
	 * @param macAddress: mac address of zinger
	 * @return
	 */
	public static int getId(String macAddress) 
	{
		Context cont = ChatList.getCurrentActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur = sql.rawQuery("SELECT id FROM zingers WHERE mac_address = '" + macAddress + "'", null);
		cur.moveToFirst();
		
		int id = -1;
		if(cur.getCount() > 0) 
		{
			id = cur.getInt(0);
		}
		
		closeDatabase();
		cur.close();
		return id;
	}
	
	/**
	 * get the mac address of this ZInger 
	 * @param id: the View ID from Zingers table 
	 * @return mac address as STring
	 */
	public static String getMacAddress(int id)
	{
		Context cont = ChatList.getCurrentActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur = sql.rawQuery("SELECT mac_address FROM zingers WHERE id = " + id, null);
		cur.moveToFirst();
		
		String macAddress = "";
		if(cur.getCount() > 0) 
		{
			macAddress = cur.getString(0);
		}
		
		closeDatabase();
		cur.close();
		return macAddress;
	}
	
	public static String getZingerId(int id)
	{
		
		Context cont = ChatList.getCurrentActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur = sql.rawQuery("SELECT zing_id FROM zingers WHERE id = " + id, null);
		cur.moveToFirst();
		
		String macAddress = "";
		if(cur.getCount() > 0) 
		{
			macAddress = cur.getString(0);
		}
		
		closeDatabase();
		cur.close();
		return macAddress;
	}
	
	public static ArrayList<JSONObject> getMessages(String id)
	{
		return getMessages(id, -1);
	}
	
	/**
	 * method to retrieve messages from a conversation
	 * @param id: mac address of the zinger whose messages are to be retrieved
	 * @param msgIdToStartFrom: where retrival should start
	 * @return
	 */
	public static ArrayList<JSONObject> getMessages(String id, int msgIdToStartFrom)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
        else
            cont = ZingService.getContextFromService();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur;
		if(msgIdToStartFrom > -1)
			cur = sql.rawQuery("SELECT * FROM messages WHERE (from_id = " + "'"+id+"'" + " OR to_id = " + "'"+id+"'" 
					+ ") AND (id < " + msgIdToStartFrom + ") ORDER BY id DESC LIMIT 20", null);
					
		else
			cur = sql.rawQuery("SELECT * FROM messages WHERE from_id = " + "'"+id+"'" + " OR to_id = " + "'"+id+"'" + " ORDER BY id DESC LIMIT 20", null);
		
		if(cur.getCount() == 0)
			return null;

		// Get the last "message ID" queried from the database
		cur.moveToLast();
		int leastRecentMsgId = cur.getInt(cur.getColumnIndex("id"));
		Log.d("msgIdToStartFrom", Integer.toString(msgIdToStartFrom));
		Log.d("leastRecentId", Integer.toString(leastRecentMsgId));
		
		cur.moveToPosition(-1); // Go to the position before the first record
		ArrayList<JSONObject> idMsgList = new ArrayList<JSONObject>();

		try {
			while(cur.moveToNext())
			{
				Log.d("otherIds", cur.getString(cur.getColumnIndex("id")));
				String fromId = cur.getString(cur.getColumnIndex("from_id"));
				String msg = cur.getString(cur.getColumnIndex("message"));
				String createdTime = cur.getString(cur.getColumnIndex("created"));
				int sent = cur.getInt(cur.getColumnIndex("sent"));
				String type = cur.getString(cur.getColumnIndex("type"));
				JSONObject jobj = new JSONObject();
				jobj.put("least_recent_msg_id", leastRecentMsgId);
				jobj.put("from", fromId);
				jobj.put("message", msg);
				jobj.put("created", createdTime);
				jobj.put("sent", sent);
				jobj.put("type", type);
				//Toast.makeText(ChatList.getCurrentActivity(), Integer.toString(sent), Toast.LENGTH_LONG).show();
				idMsgList.add(jobj);
			}
			cur.close();
			closeDatabase();
			return idMsgList;		
		}
		catch(JSONException e) 
		{
			e.printStackTrace();
		}
		
		cur.close();
		closeDatabase();
		return null;
	}
	
	/**
	 * Method to check if more messages are available for this chat
	 * @param id: mac address of the zinger who the currently logged in user had this chat with
	 * @param msgIdToStartFrom
	 * @return: returns either true or false
	 */
	public static boolean canFetchMessages(String id, int msgIdToStartFrom)
	{
		if(msgIdToStartFrom < 0)
			return false;
		
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur = sql.rawQuery("SELECT * FROM messages WHERE (from_id = " + "'"+id+"'" + " OR to_id = " + "'"+id+"'"
				+ ") AND (id < " + msgIdToStartFrom + ") ORDER BY id DESC", null);
		boolean hasMore = cur.getCount() > 0;
		cur.close();
		closeDatabase();
		return hasMore;
	}

	/**
	 * call this method to create a new user account
	 * @param img: path to a profile picture image
	 * @param zing_id: the user specified zing id
	 * @param pass: user specified zing id
	 */
	public static void createUser(String img, EditText zing_id, EditText pass) 
	{
		// TODO Auto-generated method stub
		Context cont = pass.getContext();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put("status", "Hey there, I am using Zing");
		cv.put("zing_id", zing_id.getText().toString().trim());
		cv.put("password", pass.getText().toString().trim());
		byte img_byte[] = getImageByte(img);
		if(img_byte.length > 0)
		{
			cv.put("profile_pix_count", Long.toString(Time.getTimeStamp().getTime()));
		}
		else
		{
			cv.put("profile_pix_count", "0");
		}
		
		cv.put("profile_pix", img_byte);
		
		sql.insert("user_info", null, cv);
		Cursor cur =sql.rawQuery("select * from user_info", null);
		cur.moveToFirst();
		int count=0;
		while(count< cur.getCount())
		{
			Log.d("name", cur.getString(cur.getColumnIndex("zing_id")));
			cur.moveToNext();
			count++;
		}
	}
	
	/**
	 * a private method to read profile picture when the file path is specified.
	 * @param img: path to the image
	 * @return: a byte array of the specified image..
	 * a byte array of length 0 is returned if image path is null, possibly meaning no profile
	 * picture was selected during account creation.
	 */
	private static byte[] getImageByte(String img)
	{
		try
		{
			if(img != null)
			{
				File file = new File(img);
				FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte buffer[] = new byte[8*1024];
				int len =0;
				while((len = fis.read(buffer))!= -1)
				{
					baos.write(buffer,0,len);
				}
				
				fis.close();
				byte imgbytes [] = baos.toByteArray();
				baos.close();
				return imgbytes;
			}
			else
				return new byte[0];
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * This method returns last messages of all conversations
	 * @return: a JSONObject array containing all last messages . The array contains id, zing_id, message,time,mac address, type and either
	 * send status or unread_status
	 */
	public static JSONObject[] getLastMessages()
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		JSONObject zingers[] = getZingers();
		ArrayList<JSONObject> result = new ArrayList<JSONObject>();
		
		int count =0;
		while(count < zingers.length)
		{
			try
			{
				int id = zingers[count].getInt("id");
				String mac_address = getMacAddress(id);
				Cursor cur = sql.rawQuery("SELECT * FROM messages WHERE from_id = " + "'"+mac_address+"'" + " OR to_id = " + "'"+mac_address+"'" + " ORDER BY id DESC LIMIT 1",null);
				cur.moveToFirst();
				if(cur.getCount() > 0)
				{
					
					JSONObject jobj = new JSONObject();
					jobj.put("id", id);
					jobj.put("zing_id", zingers[count].getString("zing_id"));
					jobj.put("message", cur.getString(cur.getColumnIndex("message")));
					jobj.put("time", cur.getString(cur.getColumnIndex("created")));
					jobj.put("mac_address", mac_address);
					jobj.put("type", cur.getString(cur.getColumnIndex("type")));
					if(cur.getInt(cur.getColumnIndex("unread")) == 10)
					{
						jobj.put("status", "sent");
						jobj.put("send_status", cur.getInt(cur.getColumnIndex("sent")));
					}
					else// if(cur.getInt(cur.getColumnIndex("unread")) == 1)
					{
						jobj.put("status", "unread");
						jobj.put("unread_status", cur.getInt(cur.getColumnIndex("unread")));
					}
						
					result.add(jobj);
				}
				
				count++;
				cur.close();
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				return null;
			}
			
			
		}
		JSONObject [] res = new JSONObject[result.size()];
		
		closeDatabase();
		return result.toArray(res);
	}
	
	/**
	 * This method allows the app to store profile pictures of other Zingers
	 * @param pix: profile picture to be stored as a byte array 
	 * @param mac_address: mac address of zinger whos profile picture is to be stored
	 * @param zing_id: this variable is not used in this current implementation, but just pass in the Zing ID
	 * @param pix_id: picture identifier for this for this picture. Its an integer value stored in the JSON broadcast by every zinger
	 */
	public static void storeProfilePix(byte [] pix, String mac_address, String zing_id, String timestamp)
	{
		Log.d("DATA INSERTED", "STOR PROFILE PIX");
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cur = sql.rawQuery("SELECT * FROM profile_pictures WHERE mac_address = "+"'"+mac_address+"'", null);
		
		if(cur.getCount() == 0 )
		{
			ContentValues cv = new ContentValues();
			cv.put("mac_address", mac_address);
			cv.put("zing_id", zing_id);
			cv.put("pix_id", timestamp);
			cv.put("pix_data", pix);
			long i = sql.insert("profile_pictures", null, cv);
			Log.d("DATA INSERTED", Long.toString(i));
		}
		else
		{
			ContentValues cv = new ContentValues();
			cv.put("pix_data", pix);
			cv.put("pix_id", timestamp);
			sql.update("profile_pictures", cv, "mac_address = "+"'"+mac_address+"'", null);
			//sql.execSQL("UPDATE profile_pictures SET pix_data = "+ pix+" , pix_id = "+"'"+timestamp+"'"+" WHERE mac_address = "+"'"+mac_address+"'");
		}
		cur.close();
		closeDatabase();
	}
	
	/**
	 * This method is called when the currently logged in user changes the profile picture.
	 * @param pix: the picture to store in the database as a byte array.
	 */
	public static void updateProfilePix(byte[] pix, String timestamp)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cur = sql.rawQuery("SELECT profile_pix_count FROM user_info WHERE logged_in = 1",null);
		cur.moveToFirst();
		ContentValues cv = new ContentValues();
		cv.put("profile_pix", pix);
		cv.put("profile_pix_count", timestamp);
		sql.update("user_info", cv, "logged_in = 1", null);
		//sql.execSQL("UPDATE user_info SET profile_pix = " +pix+ " , profile_pix_count= "+"'"+timestamp+"'"+" WHERE logged_in = 1");
		cur.close();
		closeDatabase();
	}
	
	/**
	 * This method returns the profile pix count of the currently logged in user.
	 * Profile Pix Count is just a means for the app to know when a user has changed his profile pix
	 * @return: profile pix count
	 */
	public static long getProfilePixCount()
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cur = sql.rawQuery("SELECT profile_pix_count FROM user_info WHERE logged_in = 1",null);
		cur.moveToFirst();
		long count = cur.getLong(cur.getColumnIndex("profile_pix_count"));
		cur.close();
		closeDatabase();
		return count;
	}
	
	/**
	 * This method helps retrieve a particular Zinger's profile pix from the database.
	 * The Zinger is identified by the mac address.
	 * @param mac_address: The Zinger whose profile picture is to be retrieved.
	 * @return: profile picture as a byte array...Note that a byte array of length 0 means the Zinger 
	 * specified by the mac address was not found.
	 */
	public static byte[] getZIngerProfilePix(String mac_address)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
        else
            cont = ZingService.getContextFromService();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cur = sql.rawQuery("SELECT * FROM profile_pictures WHERE mac_address = "+"'"+mac_address+"'", null);
		cur.moveToFirst();
		int count = cur.getCount();
		byte [] data = null;
		if(count > 0)
		{
			data  = cur.getBlob(cur.getColumnIndex("pix_data"));
			
		}
		
		closeDatabase();
		cur.close();
		if(count > 0)
		{
			return data;
		}
		
		else 
			return new byte[0];
	}
	
	/**
	 * This method takes a mac address and returns an integer value.
	 * 0 is returned if the Zinger didn't set a profile pix, so the app uses the default profile pix
	 * -1 means this mac address has not inserted into the table possibly due to 
	 * profile picture not available at the time of this method call
	 * @param mac: mac address of the Zinger 
	 * @return: pix ID
	 * 
	 */
	public static long getZIngerProfilePixID(String mac)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		Cursor cur = sql.rawQuery("SELECT * FROM profile_pictures WHERE mac_address = "+"'"+mac+"'", null);
		if(cur.getCount() > 0)
		{
			cur.moveToFirst();
			long data  = cur.getLong(cur.getColumnIndex("pix_id"));
			cur.close();
			closeDatabase();
			return data;
		}
		else
		{
			cur.close();
			closeDatabase();
			return -1;
		}
	}
	
	public static void updateMessages(String mac)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		sql.execSQL("UPDATE messages SET unread=0 WHERE from_id = '"+mac+"' AND unread=1");
		closeDatabase();
		Toast.makeText(cont, "Updating messages", Toast.LENGTH_LONG).show();
	}

	public static void updateUserId(String zing_id) 
	{
		// TODO Auto-generated method stub
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		if(zing_id.contains("\'") ||zing_id.contains("\""))
		{
			zing_id = zing_id.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		
		
		sql.execSQL("UPDATE user_info SET zing_id = '"+zing_id+"'"+" WHERE logged_in=1");
		closeDatabase();
	}
	
	public static void updateUserStatus(String status) 
	{
		// TODO Auto-generated method stub
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		if(status.contains("\'") ||status.contains("\""))
		{
			status = status.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		
		
		sql.execSQL("UPDATE user_info SET status = '"+status+"'"+" WHERE logged_in=1");
		closeDatabase();
	}
	
	public static String[] getZingStatus(String mac)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		
		Cursor cur = sql.rawQuery("SELECT zing_id, status FROM zingers WHERE mac_address='"+mac+"'",null);
		String [] result = new String[2];
		
		if(cur.getCount() > 0)
		{
			cur.moveToFirst();
			result[0] = cur.getString(0);
			result[1] = cur.getString(1);
			
		}
		cur.close();
		closeDatabase();
		return result;
	}
	
	public static void updateZingerStatus(String zing_id, String status, String mac)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		if(zing_id.contains("\'") ||zing_id.contains("\""))
		{
			zing_id = zing_id.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		
		if(status.contains("\'") ||status.contains("\""))
		{
			status = status.replace("\'", "\''");
			
			//msg = msg.replace("\"", "\\\"");
		}
		sql.execSQL("UPDATE zingers SET zing_id = '"+zing_id+"', status = '"+status+"' WHERE mac_address= '"+mac+"'");
		closeDatabase();
	}
	
	public static void updateIpAddress(String mac, String ip)
	{
		Context cont = null;
		if(ChatList.getCurrentActivity() != null)
			cont = ChatList.getCurrentActivity();
		else if(ChatWindow.getActivity() != null)
			cont = ChatWindow.getActivity();
		
		initializeHelper(new DatabaseHelper(cont));
		SQLiteDatabase sql = DatabaseHelper.openDatabase();
		sql.execSQL("UPDATE zingers SET ip_address = '"+ip+"' WHERE mac_address= '"+mac+"'");
		closeDatabase();
	}
}

package com.karabow.zing;

import java.io.Serializable;
import java.util.Random;

class Key implements Serializable {

	private final byte[] macAddress;


	public Key(byte[] b)
	{
		macAddress = b;
	}

	public Key()
	{
		macAddress = new byte[]{3,4,5};
	}
	public byte[] getmacAddress()
	{
		return macAddress;
	}

	public boolean equals(Object o)
	{
		int i = 0;
                if(o instanceof Key)
                {
               	   for(byte b : ((Key)o).getmacAddress())
		  {
			if(macAddress[i++] != b)
                        {
				return false;
                        }
		  }
		  return true;
                }
                else
                    return false;
                
	}

	public int hashCode()
	{
		String strinBinary = "";
		int count = 0;
		for(int b: macAddress)
		{
			if(count > 0)
			{
				if( b < 0)
				{
					b += 256;
				}
				strinBinary += Integer.toBinaryString(b);
				count++;
			}
		}
		return BinToDec(strinBinary);
	}

private static int BinToDec(String inBinary)
    {
    	long ans = 0;
    	for (int i = 0, j = inBinary.length() - 1 ; i < inBinary.length(); i++, j--)
    	{

    		ans += Character.getNumericValue(inBinary.charAt(i)) * Math.pow(2,j);
    	}
    	return (int)ans;
    }
@Override
	public String toString()
	{
		return new Random(10000).toString();
		//return Long.toString((System.currentTimeMillis()));
	}

//	public void setHashcode(byte[] mac)

}

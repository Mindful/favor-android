package com.favor.util;

import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;

import com.favor.util.*;

public class Algorithms {
  private static ArrayList<Long> sentTimes = new ArrayList<Long>();
  private static ArrayList<Long> recTimes = new ArrayList<Long>();
  private static double avgSentTime;
  private static double avgRecTime;
  private static long[] sentCharCount;
  private static long[] recCharCount;


  
  DataHandler db = DataHandler.get();
  
  public long getRecCount (String address) {
	  long totalRecChar = 0;
	  ArrayList<textMessage> list = db.queryFromAddress(address, -1, -1);
	  for (textMessage t : list) {
		  totalRecChar += t.charCount();
	  }
	  return totalRecChar;
  }
  
  public long getSentCount (String address) {
	  long totalSentChar = 0;
	  ArrayList<textMessage> list = db.queryToAddress(address, -1, -1);
	  for (textMessage t : list) {
		  totalSentChar += t.charCount();
	  }
	  return totalSentChar;
  }
  
  private long[] friendCount (String address) {
	  long [] mmsChar = {0,0};
	  ArrayList<textMessage list> = db.query 
	  
	  
  }

  private void responseTime (LinkedList<textMessage> list)
	{
		//well, averages are obv wrong, but the consecutive stripping works like a charm
		Log.v("size", ""+list.size());
		long sendTotal = 0;
		long receiveTotal = 0;
		textMessage temp, prev = null;
		Long time;
		while(list.peekLast()!=null)
		{
		    temp = list.pollLast(); //removes from queue
			if (prev!= null)
			{
				time = prev.rawDate() - temp.rawDate(); //make time negative, because it will be. also consider switch ifs?
				if (temp.received()){ sentTimes.add(time); sendTotal += time; } //our response time
				else {recTimes.add(time); receiveTotal += time;}
				
			}
			while(list.peekLast() != null && list.peekLast().received() == temp.received()) //short circuits
			{
				if (list.peekLast().received())	Log.v("look", "received:"+list.peekLast().textDate()+" temp:"+temp.textDate());
				else Log.v("look", "sent:"+list.peekLast().textDate()+" temp:"+temp.textDate());
	
				list.pollLast();
			}
			prev = temp;
		}
		Log.v("sendsize", ""+sentTimes.size());
		Log.v("recsize", ""+recTimes.size());
		avgSentTime = sendTotal/sentTimes.size();
		Log.v("sendavg:", ""+avgSentTime);
		avgRecTime = receiveTotal/recTimes.size();
		Log.v("receiveavg:", ""+avgRecTime);
	}
}

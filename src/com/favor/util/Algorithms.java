package com.favor.util;

import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;


public class Algorithms {

  
  public static long[] charCount (String address, long fromDate, long untilDate) {
	  DataHandler db = DataHandler.get();
	  long [] values = {0,0};
	  ArrayList <textMessage> sent = db.queryToAddress(address, fromDate, untilDate);
	  ArrayList <textMessage> rec = db.queryFromAddress(address, fromDate, untilDate);
	  for (textMessage t : sent) {
		  values[0] += t.charCount();
	  }
	  for (textMessage t : rec) {
		  values[1] += t.charCount();
	  }
	  return values;  
  }
  
  public static double charRatio (String address, long fromDate, long untilDate) {
	  long [] values = charCount(address, fromDate, untilDate);
	  double ratio = values[0]/values[1];
	  return ratio;
  }

  public static double[] responseTime (String address, long fromDate, long untilDate)
	{
	  DataHandler db = DataHandler.get();
	  LinkedList<textMessage> list = db.queryConversation(address, fromDate, untilDate);
		//well, averages are obv wrong, but the consecutive stripping works like a charm
		long sendTotal = 0;
		long receiveTotal = 0;
		double avgSentTime;
		double avgRecTime;
		double [] ratios = {(Double) null, (Double) null};
		ArrayList<Long> sentTimes = new ArrayList<Long>();
		ArrayList<Long> recTimes = new ArrayList<Long>();
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
		avgSentTime = sendTotal/sentTimes.size();
		avgRecTime = receiveTotal/recTimes.size();
		ratios[0] = avgSentTime;
		ratios[1] = avgRecTime;
		return ratios;
	}
  
  	public static double responseRatio (String address, long fromDate, long untilDate) {
	  double [] times = responseTime(address, fromDate, untilDate);
	  double ratio = times[0]/times[1];
	  return ratio;
  	}
  	
  	public static double friendScore (String address) {
  		DataHandler db = DataHandler.get();
  		LinkedList<textMessage> convo = db.queryConversation(address, -1, -1);
  		double score = 0;
  		return score;
  	}
}

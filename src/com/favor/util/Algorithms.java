package com.favor.util;

import java.util.ArrayList;
import java.util.LinkedList;

import com.favor.util.*;

public class Algorithms {

  DataHandler db = DataHandler.get();
  // All Ratios are received over sent because then values over 1 indicate people responding more, under
  // 1 indicates you respond more.
  
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
	  long [] values= charCount(address, fromDate, untilDate);
	  Debug.log(values[1] + "");
	  Debug.log(values[0] + "");
	  double ratio = values[1]/(float)values[0];
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
		double [] ratios = {0, 0};
		ArrayList<Long> sentTimes = new ArrayList<Long>();
		ArrayList<Long> recTimes = new ArrayList<Long>();
		textMessage temp, prev = null;
		Long time;
		int tempSentCount = 0;
		int tempRecCount = 0;
		while(list.peekLast()!=null)
		{
		    temp = list.pollLast(); //removes from queue
			if (prev!= null)
			{
				time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
				if (temp.received()){ sentTimes.add(time); sendTotal += time; tempSentCount++; } //our response time
				else {recTimes.add(time); receiveTotal += time; tempRecCount++;}
				
			}
			while(list.peekLast() != null && list.peekLast().received() == temp.received()) //short circuits
			{
				if (list.peekLast().received())	Debug.log("received:"+list.peekLast().textDate()+" temp:"+temp.textDate());
				else Debug.log("sent:"+list.peekLast().textDate()+" temp:"+temp.textDate());
	
				list.pollLast();
			}
			prev = temp;
		}
		Debug.log("sentTimes"+sentTimes.size()+"tempSent"+tempSentCount);
		Debug.log("recTimes"+recTimes.size()+"tempRec"+tempRecCount);
		avgSentTime = sendTotal/(1000*tempSentCount); //sentTimes.size();
		avgRecTime = receiveTotal/(1000*tempRecCount); //recTimes.size();
		ratios[0] = avgSentTime;
		ratios[1] = avgRecTime;
		return ratios;
	}
  
  	public static double responseRatio (String address, long fromDate, long untilDate) {
	  double [] times = responseTime(address, fromDate, untilDate);
	  double ratio = times[1]/times[0];
	  return ratio;
  	}
  	
  	
  	//Will perform separate queries, doesn't call other algos
  	public static double friendScore (String address) {
  		DataHandler db = DataHandler.get();
  		LinkedList<textMessage> convo = db.queryConversation(address, -1, -1);
  		double score = 100;
  		
  		return score;
  	}
}

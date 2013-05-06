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

  public static long[] responseTime (String address, long fromDate, long untilDate)
 	{
 	  DataHandler db = DataHandler.get();
 	  LinkedList<textMessage> list = db.queryConversation(address, fromDate, untilDate);
 		//well, averages are obi wrong, but the consecutive stripping works like a charm
 		long sendTotal = 0;
 		long receiveTotal = 0;
 		long avgSentTime;
 		long avgRecTime;
 		long [] ratios = {0, 0};
 		ArrayList<Long> sentTimes = new ArrayList<Long>();
 		ArrayList<Long> recTimes = new ArrayList<Long>();
 		textMessage temp, prev = null;
 		Long time;
 		int tempSentCount = 0;
 		int tempRecCount = 0;
 		ArrayList<Long> checkSentTimes = new ArrayList<Long>();
 		ArrayList<Long> checkRecTimes = new ArrayList<Long>();
 		while(list.peekLast()!=null)
 		{
 		    temp = list.pollLast(); //removes from queue
 			if (prev!= null)
 			{
 				time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
 				if (temp.received()){ checkSentTimes.add(time); tempSentCount++; } //our response time
 				else {checkRecTimes.add(time); tempRecCount++;}
 				
 			}
 			while(list.peekLast() != null && list.peekLast().received() == temp.received()) //short circuits
 			{
 				if (list.peekLast().received())	Debug.log("received:"+list.peekLast().textDate()+" temp:"+temp.textDate());
 				else Debug.log("sent:"+list.peekLast().textDate()+" temp:"+temp.textDate());
 	
 				list.pollLast();
 			}
 			prev = temp;
 		}
 		int n1 = checkSentTimes.size(); //n for sent
 		int n2 = checkRecTimes.size(); //n for received
 		for (int i=0;i<n1;i++) Debug.log(" THIS IS A CHECK OF SENT TIMES" + checkSentTimes.get(i)+ " "  + i);
 		for (int i=0;i<n2;i++) Debug.log(checkRecTimes.get(i) + " This is a check of received times" + i);
 		double meanSent = 0; // Y-bar for the sent times
 		double meanRec = 0; // Y-bar for the received times
 		double sentStdDev = 0;
 		double recStdDev = 0;
 		for (int i = 0; i < n1; i++) meanSent += checkSentTimes.get(i);
 		for (int i = 0; i < n2; i++) meanRec += checkRecTimes.get(i);
 		meanSent = meanSent/n1;
 		meanRec = meanRec/n2;
 		for (int i = 0; i < n1; i++) sentStdDev += (checkSentTimes.get(i) - meanSent)*(checkSentTimes.get(i) - meanSent);
 		for (int i = 0; i < n2; i++) recStdDev += (checkRecTimes.get(i) - meanRec)*(checkRecTimes.get(i) - meanRec);
 		sentStdDev = Math.sqrt((1/(n1))*sentStdDev);
 		recStdDev = Math.sqrt((1/(n2))*recStdDev);
 		for (int i = 0; i < n1; i++) {
 			if (checkSentTimes.get(i) > (meanSent + (2*sentStdDev)) || checkSentTimes.get(i) < (meanSent - (2*sentStdDev)))
 				checkSentTimes.remove(i);
 		}
 		for (int i = 0; i < n2; i++) {
 			if (checkRecTimes.get(i) > (meanRec + (2*recStdDev)) || checkRecTimes.get(i) < (meanRec - (2*recStdDev)))
 				checkRecTimes.remove(i);
 		}
 		sentTimes = checkSentTimes;
 		recTimes = checkRecTimes;
 		for (int i=0;i<n1;i++) Debug.log(sentTimes.get(i) + " THIS IS A CHECK OF SENT TIMES after clean" + i);
 		for (int i=0;i<n2;i++) Debug.log(recTimes.get(i) + " This is a check of received times after clean" + i);
 		
 		Debug.log("sentTimes"+sentTimes.size()+"tempSent"+tempSentCount);
 		Debug.log("recTimes"+recTimes.size()+"tempRec"+tempRecCount);
 		avgSentTime = sendTotal/(1000*tempSentCount); //sentTimes.size();
 		avgRecTime = receiveTotal/(1000*tempRecCount); //recTimes.size();
 		ratios[0] = avgSentTime;
 		ratios[1] = avgRecTime;
 		return ratios;
 	}

  
  	public static double responseRatio (String address, long fromDate, long untilDate) {
	  long[] times = responseTime(address, fromDate, untilDate);
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

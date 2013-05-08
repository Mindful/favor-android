package com.favor.util;

import java.util.ArrayList;
import java.util.LinkedList;



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
 		LinkedList<Long> sentTimes = new LinkedList<Long>();
 		LinkedList<Long> recTimes = new LinkedList<Long>();
 		textMessage temp, prev = null;
 		Long time;
 		int tempSentCount = 0;
 		int tempRecCount = 0;
 		LinkedList<Long> checkSentTimes = new LinkedList<Long>();
 		LinkedList<Long> checkRecTimes = new LinkedList<Long>();
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
 		int n1 = tempSentCount; //n for sent
 		int n2 = tempRecCount; //n for received
 	
 		sentTimes = checkSentTimes;
 		recTimes = checkRecTimes;
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
  	public static long friendScore (String address) {
  		DataHandler db = DataHandler.get();
  		LinkedList<textMessage> convo = db.queryConversation(address, -1, -1);
  		for (textMessage t : convo) ;
  		long score = 100;
  		
  		return score;
  	}
  	
  	// 
  	public static LinkedList<Long> finiteMixture (LinkedList<Long> totalData) {
  		LinkedList<Long> A = new LinkedList<Long>();
  		LinkedList<Long> B = new LinkedList<Long>();
  		LinkedList<Long> temp = totalData;
  		LinkedList<Long> check = new LinkedList<Long>();
  		

	
  		//Standard Deviation of the total dataset
  		double mean = 0;
  		long stdDev = 0;
  		int totalN = totalData.size();
 		for (int i = 0; i < totalN; i++) mean += temp.get(i);
 		mean = mean/totalN;
 		while(temp.peekLast() != null) {
 			check.add(temp.peek());
 			stdDev += (temp.peek() - meanSent)*(temp.poll() - meanSent);	
 		}
 		stdDev = (long) (Math.sqrt(1/(totalN))*stdDev);
 		while (totalData.peekLast() != null) {
 			if (totalData.peek() > mean+stdDev) A.add(totalData.poll());
 			else B.add(totalData.poll());
 		}
 		int aN = A.size();
  		int bN = B.size();
 		
 		double aMean;
 		for (long l : A) aMean += l;
 		aMean = aMean/aN;
  		double bMean;
  		for (long l : B) bMean += l;
  		bMean = bMean/bN;
  		double aVar = 0;
  		double bVar = 0;
  		for (long l : A) aVar += (l - aMean)*(l - aMean);
		for (long l : B) bVar += (l - bMean)*(l - bMean);
  		aVar = aVar/aN;
  		bVar = bVar/bN;
 		double likelihood = 0;
 		double margin = 1;
 		int counter = 0;
 		double [] pAs = new double[aN];
 		double [] pBs = new double[bN];
 		while (counter < 30 || margin > 0.000000001) {
 		
 			for (int i =0;i<check.size();i++) {
 				pAs[i] = (1/Math.sqrt(2*Math.PI*Math.sqrt(aVar)))*Math.exp(((A.get(i)-aMean)*(A.get(i)-aMean))/(2*aVar));
 				pBs[i] = (1/Math.sqrt(2*Math.PI*Math.sqrt(bVar)))*Math.exp(((B.get(i)-bMean)*(B.get(i)-bMean))/(2*bVar));
 			}
 			counter++;
 		}
 	

  		
  		
  		return totalData;
   	}
}

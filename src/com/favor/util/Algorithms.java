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
 		sentTimes = Algorithms.finiteMixture(sentTimes);
 		recTimes = Algorithms.finiteMixture(recTimes);
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
  		for (long l : totalData) Debug.log("stuff in the data at first: " + l);
  		

	
  		//Standard Deviation of the total dataset
  		double mean = 0;
  		double stdDev = 0;
  		int totalN = totalData.size();
 		for (int i = 0; i < totalN; i++) mean += temp.get(i);
 		mean = mean/totalN;
 		Debug.log("The initial mean = " + mean);
 		for (int i=0;i<totalN;i++) {
 			check.add(temp.get(i));
 			stdDev += (temp.get(i) - mean)*(temp.get(i) - mean);	
 			Debug.log("seeing if std dev adds = " + stdDev);
 		}
 		stdDev = (Math.sqrt((1/(totalN))*stdDev));
 		Debug.log("Initial stdDev = " + stdDev);
 		while (totalData.peekLast() != null) {
 			if (totalData.peek() > mean+stdDev) B.add(totalData.poll());
 			else A.add(totalData.poll());
 		}
 		int aN = A.size();
  		int bN = B.size();
  		Debug.log("A size: " + aN + " B size: " + bN);
 		
 		double aMean = 0;
 		for (long l : A) aMean += l;
 		aMean = aMean/aN;
  		double bMean = 0;
  		for (long l : B) bMean += l;
  		bMean = bMean/bN;
  		double aVar = 0;
  		double bVar = 0;
  		for (long l : A) aVar += (l - aMean)*(l - aMean);
		for (long l : B) bVar += (l - bMean)*(l - bMean);
  		aVar = aVar/aN;
  		bVar = bVar/bN;
  		Debug.log("Initial A and B means" + aMean + "//" + bMean);
  		Debug.log("Initial A and B vars" + aVar + "//" + bVar);
 		double likelihood = 0;
 		double margin = 1;
 		int counter = 0;
 		double [] pAs = new double[totalN];
 		double [] pBs = new double[totalN];
 		double [] pAsTemp = new double[totalN];
 		double [] pBsTemp = new double[totalN];
 		double sumOfAWeights = 0;
 		double sumOfBWeights = 0;
 		
 		
 		//Calculating mixture
 		while (counter < 30 || margin > 0.000000001) {
 		
 			//Calculates un-normalized probabilities (weights) for each point
 			for (int i =0;i<totalN;i++) {
 				pAsTemp[i] = (1/Math.sqrt(2*Math.PI*Math.sqrt(aVar)))*Math.exp(((check.get(i)-aMean)*(check.get(i)-aMean))/(2*aVar))*(aN/totalN);
 				Debug.log("pAs before normalize ["+i+"] - " + pAsTemp[i]);
 				pBsTemp[i] = (1/Math.sqrt(2*Math.PI*Math.sqrt(bVar)))*Math.exp(((check.get(i)-bMean)*(check.get(i)-bMean))/(2*bVar))*(bN/totalN);
 				Debug.log("pBs before normalize ["+i+"] - " + pBsTemp[i]);
 			}
 			
 			//Normalizes and sums weights
 			for (int i=0;i<totalN;i++) {
 				pAs[i] = pAsTemp[i]/(pAsTemp[i] + pBsTemp[i]);
 				Debug.log("pAs after normalize ["+i+"] - " + pAs[i]);
 				pBs[i] = pBsTemp[i]/(pAsTemp[i] + pBsTemp[i]);
 				Debug.log("pBs after normalize ["+i+"] - " + pBs[i]);
 				sumOfAWeights += pAs[i];
 				sumOfBWeights += pBs[i];		
 			}
 			Debug.log("Sum of A and B weights = " + sumOfAWeights + "//" + sumOfBWeights);
 			//new means
 			for (int i=0;i<totalN;i++) {
 				aMean += pAs[i]*check.get(i);
 				bMean += pBs[i]*check.get(i);
 			}
 			aMean = aMean/sumOfAWeights;
 			Debug.log("new a mean = " + aMean);
 			bMean = bMean/sumOfBWeights;
 			Debug.log("new b mean = " + bMean);
 		
 			//new vars
 			for (int i=0;i<totalN;i++) {
 				aVar += pAs[i]*((check.get(i) - aMean)*(check.get(i) - aMean));
 				bVar += pBs[i]*((check.get(i) - bMean)*(check.get(i) - bMean));
 			}
 			aVar = aVar/sumOfAWeights;
 			bVar = bVar/sumOfBWeights;
 			Debug.log("new a var = " + aVar);
 			Debug.log("new b var = " + bVar);
 			
 			
 			//set new aN and bN
 			aN = 0;
 			bN = 0;
 			for (int i=0;i<totalN;i++) {
 				if (pAs[i] > 0.5) aN++;
 				else bN++;
 			}
 			Debug.log("new number of a's and b's" + aN + "//" + bN);
 			
 			//likelihood
 			for (int i=0;i<check.size();i++) {
 				likelihood += Math.log(((aN/totalN)*pAs[i]) + (bN/totalN)*pBs[i]);
 			}
 			Debug.log("likelihood = " + likelihood);
 			//check if first time through the loop, otherwise find the diff between this calc and the last.
 			if (counter == 0) margin = likelihood;
 			else margin = Math.abs(likelihood - margin);
 			Debug.log("margin = " + margin);
 			//loop counter
 			counter++;
 			break;
 		
 		}
 		for (int i=0;i<totalN;i++) if (check.contains(pAs[i])) check.remove(pAs[i]);
 		while (check.peekLast() != null)
 			totalData.remove(check.poll());
 		for (long l : totalData) Debug.log("stuff after clean: " + l);
  		return totalData;
   	}
  	
}

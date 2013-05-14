package com.favor.util;

import java.util.ArrayList;
import java.util.LinkedList;




public class Algorithms {
	

  DataHandler db = DataHandler.get();

  //weightings for proprietary scoring
  private static final double CHAR_WEIGHT = 0.45;
  private static final double COUNT_WEIGHT = 0.15;
  private static final double MEDIA_WEIGHT = 0.15;
  private static final double RESPONSE_WEIGHT = 0.25;
  
  //character counting
  public static long[] charCount (String address, long fromDate, long untilDate) {
	  DataHandler db = DataHandler.get();
	  long [] values = {0,0};
	  
	  //only gets character count, all other fields are null
	  String[] keys = new String[] {DataHandler.KEY_CHARCOUNT};
	  
	  //queries take an address, keys, and dates
	  ArrayList <textMessage> sent = db.queryToAddress(address, keys, fromDate, untilDate);
	  ArrayList <textMessage> rec = db.queryFromAddress(address, keys, fromDate, untilDate);
	  
	  //counting sent values, stored at index 0 in the array values
	  for (textMessage t : sent) {
		  values[0] += t.charCount();
	  }
	  //couting received values, stored at index 1 in the array values
	  for (textMessage t : rec) {
		  values[1] += t.charCount();
	  }
	  return values;  
  }
  
  public static double charRatio (String address, long fromDate, long untilDate) {
	  
	  //calls character count
	  long [] values= charCount(address, fromDate, untilDate);
	  Debug.log(values[1] + "");
	  Debug.log(values[0] + "");
	  
	  //some kewl casting here jk
	  double ratio = values[1]/(float)values[0];
	  return ratio;
  }

  //response time calculator
  public static double[] responseTime (String address, long fromDate, long untilDate) {
 	  DataHandler db = DataHandler.get();
 	 String[] keys = new String[] {DataHandler.KEY_DATE};
 	  LinkedList<textMessage> list = db.queryConversation(address, keys, fromDate, untilDate);
 		long sendTotal = 0;
 		long receiveTotal = 0;
 		double [] averages = {0, 0};
 		
 		// creates lists of longs
 		LinkedList<Long> sentTimes = new LinkedList<Long>();
 		LinkedList<Long> recTimes = new LinkedList<Long>();
 		textMessage temp, prev = null;
 		long time;
 		
 		//too many variables, don't think we need these
 		int tempSentCount = 0;
 		int tempRecCount = 0;
 		
 		//too many intermediary list objects?
 		LinkedList<Long> checkSentTimes = new LinkedList<Long>();
 		LinkedList<Long> checkRecTimes = new LinkedList<Long>();
 		
 		/*this is the stripping algorithm, it takes out consecutive messages from the same person
 		 *the check is performed on whether there is anything left in the list, this method
 		 *dequeues the list, it will be empty after this loop runs.
 		 *IMPORTANT: IT GOES IN REVERSE. NOTE THAT IT DOES NOT POLL, IT POLLS LAST
 		 * 
 		 */
 		while(list.peekLast()!=null) {
 			//makes temp the end of the queue, removes end of the queue
 		    temp = list.pollLast(); 		    
 		    //checks if there is a value in prev
 			if (prev!= null)
 			{
 				//time is positive, temp minus prev is equivalent to 
 				//newest message minus last message from the other party
 				time = temp.rawDate() - prev.rawDate();
 				//adding times to one of two lists, not textMessage objects any more
 				if (temp.received()){ checkSentTimes.add(time); tempSentCount++; } //our response time
 				else {checkRecTimes.add(time); tempRecCount++;}
 				
 			}
 			//strips consecutive texts from the same person (no response counted for those)
 			while(list.peekLast() != null && list.peekLast().received() == temp.received()) //short circuits
 			{
 				if (list.peekLast().received())	Debug.log("received:"+list.peekLast().textDate()+" temp:"+temp.textDate());
 				else Debug.log("sent:"+list.peekLast().textDate()+" temp:"+temp.textDate());
 	
 				list.pollLast();
 			}
 			//current selection is now the previous selection, lets get a new current selection
 			prev = temp;
 		}
 		//runs finite mixture on the check lists
 		sentTimes = Algorithms.finiteMixture(checkSentTimes);
 		recTimes = Algorithms.finiteMixture(checkRecTimes);
 		Debug.log("sentTimes"+sentTimes.size()+"tempSent"+tempSentCount);
 		Debug.log("recTimes"+recTimes.size()+"tempRec"+tempRecCount);
 		//total the response times
 		for (long l : sentTimes) sendTotal += l;
 		for (long l : recTimes) receiveTotal += l;
 		
 
 		//set the array equal to the averages
 		averages[0] = sendTotal/(float)(1000*sentTimes.size());
 		averages[1] = receiveTotal/(float)(1000*recTimes.size());
 		return averages;
 	}

  	//ratio of averages currently, consider moving to mean ratio between each X-i and Y-i
  	public static double responseRatio (String address, long fromDate, long untilDate) {
  		//calls
  		double[] times = Algorithms.responseTime(address, fromDate, untilDate);
  		double ratio = times[1]/times[0];
  		return ratio;
  	}
  	
  	
  	/* Will perform separate queries, doesn't call other algos, friendscore
  	 * This is a score that will represent their interest in you, less so 
  	 * your overall relationship
  	 */
  	public static double friendScore (String address) {
  		DataHandler db = DataHandler.get();
  		String[] keys = DataHandler.KEYS_PUBLIC; 
  		//TODO: REBAR - READ THIS COMMENT BLOCK:
  		//DataHandler.KEYS_PUBLIC accounts for all public keys
  		//I'm assuming you'll use charCount, date, media, and address. If there are any
  		//that you don't use, just build your own array with the keys excluding the one you don't need
  		LinkedList<textMessage> convo = db.queryConversation(address, keys, -1, -1);
  		long sentChar = 0;
  		long recChar = 0;
  		double charRatio = 0;
  		double sentCount = 0;
  		double recCount = 0;
  		double countRatio = 0;
  		double sentMedia = 0;
  		double recMedia = 0;
  		double mediaRatio = 0;
  		double responseRatio = 0;
  		
  		for (textMessage t : convo) {
  			if (t.received()) { 
  				recChar += t.charCount(); 
  				recCount++;	
  				if (t.multimedia()) recMedia++;
  			}
  			else {
  				sentChar += t.charCount();
  				sentCount++;
  				if (!t.multimedia()) sentMedia++;
  			}
  			
  		}
  		charRatio = recChar/sentChar;
  		countRatio = recCount/sentCount;
  		mediaRatio = recMedia/sentMedia;
  		
  		//response time calc
  		LinkedList<Long> sentTimes = new LinkedList<Long>();
  		LinkedList<Long> recTimes = new LinkedList<Long>();
  		textMessage prev = null;
  		long time = 0;
  		while(convo.peekLast()!=null)
 		{
 		    textMessage temp = convo.pollLast(); //removes from queue
 			if (prev!= null)
 			{
 				time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
 				if (temp.received()) sentTimes.add(time); //our response time
 				else recTimes.add(time);
 				
 			}
 			while(convo.peekLast() != null && convo.peekLast().received() == temp.received()) //short circuits
 			{
 				convo.pollLast();
 			}
 			prev = temp;
 		}
  		double avgSent = 0;
  		double avgRec = 0;
  		for (long l : sentTimes) avgSent += l;
  		for (long l : recTimes) avgRec += l;
  		avgSent /= (double) sentTimes.size();
  		avgRec /= (double) recTimes.size();
  		responseRatio = avgRec/avgSent;
  		double score = 100;
  		score = (CHAR_WEIGHT * charRatio) + (COUNT_WEIGHT * countRatio) + (MEDIA_WEIGHT * mediaRatio) + (RESPONSE_WEIGHT * responseRatio);
  		
  		return score;
  	}
  	
  	
  	
  	/* relationship score
  	 * uses same weights as friend score - considering diff ways of executing this.
  	 */
  	
  	public static double relationshipScore (String address) {
  		DataHandler db = DataHandler.get();
  		String[] keys = DataHandler.KEYS_PUBLIC; 
  		LinkedList<textMessage> convo = db.queryConversation(address, keys, -1, -1);
  		double score = 0;
  		return score;
  	}
  	
  	// gaussian mixtures
  	public static LinkedList<Long> finiteMixture (LinkedList<Long> totalData) {
  		LinkedList<Long> A = new LinkedList<Long>();
  		LinkedList<Long> B = new LinkedList<Long>();
  		LinkedList<Long> temp = totalData;
  		LinkedList<Long> check = new LinkedList<Long>();
  		for (long l : totalData) Debug.log("stuff in the data at first: " + l);
  		

	
  		//Standard Deviation of the total dataset
  		double mean = 0;
  		double stdDev = 0;
  		double totalN = totalData.size();
 		for (int i = 0; i < totalN; i++) mean += temp.get(i);
 		mean = mean/totalN;
 		Debug.log("The initial mean = " + mean);
 		for (int i=0;i<totalN;i++) {
 			check.add(temp.get(i));
 			stdDev += (temp.get(i) - mean)*(temp.get(i) - mean);	
 			Debug.log("seeing if std dev adds = " + stdDev);
 		}
 		Debug.log("Initial stdDev = " + stdDev);
 		stdDev = Math.sqrt(stdDev);
 		Debug.log("totalN " + totalN);
 		Debug.log("" + 1/totalN);
 		Debug.log("  " +(1/totalN)*stdDev);
 		stdDev = (Math.sqrt((1/(totalN))*stdDev));
 		Debug.log("stdDev = " + stdDev);
 		while (totalData.peekLast() != null) {
 			if (totalData.peek() > mean+stdDev) B.add(totalData.poll());
 			else A.add(totalData.poll());
 		}
 		double aN = A.size();
  		double bN = B.size();
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
 		double [] pAs = new double[(int)totalN];
 		double [] pBs = new double[(int)totalN];
 		double [] pAsTemp = new double[(int)totalN];
 		double [] pBsTemp = new double[(int)totalN];
 		double sumOfAWeights = 0;
 		double sumOfBWeights = 0;
 		
 		
 		//Calculating mixture
 		while (counter < 30 || margin > 0.000000001) {
 		
 			//Calculates un-normalized probabilities (weights) for each point
 			double pA = aN/totalN;
 			double pB = bN/totalN;
 			Debug.log("A prob: " + pA + " B prob: " + pB);
 			double otherTermA = (1/Math.sqrt(2*Math.PI*Math.sqrt(aVar)));
 			double otherTermB = (1/Math.sqrt(2*Math.PI*Math.sqrt(bVar)));
 			Debug.log("otherTerms: " + otherTermA + " // " + otherTermB);
 			for (int i =0;i<totalN;i++) {
 				pAsTemp[i] = (otherTermA*Math.exp(((check.get(i)-aMean)*(check.get(i)-aMean))/(2*aVar)))*pA;
 				Debug.log("pAs before normalize ["+i+"] - " + pAsTemp[i]);
 				pBsTemp[i] = (otherTermB*Math.exp(((check.get(i)-bMean)*(check.get(i)-bMean))/(2*bVar)))*pB;
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

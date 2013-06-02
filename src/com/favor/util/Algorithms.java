package com.favor.util;

import jMEF.BregmanSoftClustering;
import jMEF.MixtureModel;
import jMEF.PVector;
import jMEF.UnivariateGaussian;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class Algorithms {

  DataHandler db = DataHandler.get();

  //weightings for proprietary scoring
  private static final double CHAR_WEIGHT = 0.45;
  private static final double COUNT_WEIGHT = 0.15;
  private static final double MEDIA_WEIGHT = 0.15;
  private static final double RESPONSE_WEIGHT = 0.25;
  private static final long DISTANCE_VALUE = 900000l;
  //TODO: constant for upper limit on response times
  
  
  public static long[] messageCount(String address, long fromDate, long untilDate)
  {
	  DataHandler db = DataHandler.get();
	  long [] values = {0,0};

	  //Have to grab a random field. Character count seems as good as any
	  String[] keys = new String[] {DataHandler.KEY_CHARCOUNT};
	  ArrayList <textMessage> sent = db.queryToAddress(address, keys, fromDate, untilDate);
	  ArrayList <textMessage> rec = db.queryFromAddress(address, keys, fromDate, untilDate);

	  values[0] = sent.size();
	  values[1] = rec.size();
	  return values;  
  }
  
  public static double messageRatio (String address, long fromDate, long untilDate) {
	  long [] values= messageCount(address, fromDate, untilDate);
	  double ratio = (values[1]/(double)values[0]);
	  return ratio;
  }
  
  /**
   * Calculates the total characters sent and received for a contact and period of time,
   * returns an array of longs with sent characters in the 0th index and received
   * characters in the 1st index.
   * @param address
   * @param fromDate
   * @param untilDate
   * @return
   */
  
  
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
	  values[0] /= sent.size();
	  values[1] /= rec.size();
	  return values;  
  }
  
  /**
   * Calculates a ratio of characters, calls the charCount method
   * @param address
   * @param fromDate
   * @param untilDate
   * @return
   */
  public static double charRatio (String address, long fromDate, long untilDate) {

	  //calls character count
	  long [] values= charCount(address, fromDate, untilDate);
	  Debug.log(values[1] + "");
	  Debug.log(values[0] + "");

	  //some kewl casting here jk
	  double ratio = (values[1]/(double)values[0]);
	  return ratio;
  }

  /**
   * calculates response time by stripping consecutive messages,from last sent by each party, 
   * averages calc'd values, returns an array of the average response times with the 0th index 
   * being the user's average response time and the 1st index being the contact's response time
   * @param address
   * @param fromDate
   * @param untilDate
   * @return
   */
  public static long[] responseTime (String address, long fromDate, long untilDate) {
 	  DataHandler db = DataHandler.get();
 	  String[] keys = new String[] {DataHandler.KEY_DATE};
 	  LinkedList<textMessage> list = db.queryConversation(address, keys, fromDate, untilDate);
 	  double avgSent = 0;
 	  double avgRec = 0;
 	  long [] averages = {0, 0};
 	  textMessage temp, prev = null;
 	  long time;
 		
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
 			  if (temp.received() && time < 172800000l) checkSentTimes.add(time); //our response time
 			  else if (time < 172800000l) checkRecTimes.add(time);
 				
 		  }
 		  //strips consecutive texts from the same person (no response counted for those)
 		  while(list.peekLast() != null && list.peekLast().received() == temp.received()) //short circuits
 		  {
 				list.pollLast();
 		  }
 		  //current selection is now the previous selection, lets get a new current selection
 		  prev = temp;
 	  }
 	  long maxSent = Long.MIN_VALUE;
 	  long minSent = Long.MAX_VALUE;
 	  long maxRec = Long.MIN_VALUE;
 	  long minRec = Long.MAX_VALUE;
 	  
 	  for (long l : checkSentTimes) {
 		  if (l > maxSent) maxSent = l;
 		  if (l < minSent) minSent = l;
 	  }
 	  for (long l : checkRecTimes) {
 		  if (l > maxRec) maxRec = l;
 		  if (l < minRec) minRec = l;
 	  }
 	  
 	  avgSent = density(checkSentTimes);
 	  avgRec = density(checkRecTimes);
 	  
 	  avgSent = avgSent/(1000);
 	  avgRec = avgRec/(1000);
 	  
 	  averages[0] = (long) avgSent;
 	  averages[1] = (long) avgRec;
 	  return averages;
  	}

  	/**
  	 * calculates ratio of the mean response times for user and contact, contact/user
  	 * if the user is liked, then it will be over 1 - if the user likes more, it will be under 1
  	 * 
  	 * @param address
  	 * @param fromDate
  	 * @param untilDate
  	 * @return
  	 */
  	public static double responseRatio (String address, long fromDate, long untilDate) {
  		//calls
  		long[] times = Algorithms.responseTime(address, fromDate, untilDate);
  		double ratio = (times[1]/(double)times[0]);
  		return ratio;
  	}
  	
  	
  	/** Will perform separate queries, doesn't call other algos, friendscore
  	 *  This is a score that will represent their interest in you, less so 
  	 *  your overall relationship
  	 *  @param address
  	 */
  	public static long[] relationshipScore (String address) {
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
  			if (!t.received()) { 		
  				sentChar += t.charCount();
  				sentCount++;
  				if (t.multimedia()) sentMedia++;
  			}
  		}
  		sentChar /=sentCount;
  			
  		//response time calc
  		LinkedList<Long> sentTimes = new LinkedList<Long>();
  		textMessage prev = null;
  		long time = 0;
  		while(convo.peekLast()!=null)
 		{
 		    textMessage temp = convo.pollLast(); //removes from queue
 			if (prev!= null)
 			{
 				time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
 				if (temp.received() && time < 172800000l) sentTimes.add(time); //our response time
 			}
 			while(convo.peekLast() != null && convo.peekLast().received() == temp.received()) //short circuits
 			{
 				convo.pollLast();
 			}
 			prev = temp;
 		}
  		double avgSent = density(sentTimes);
  		
  		avgSent = avgSent/3456l;
  
  		long [] score = {0,0};
  		
  		score[0] = (long)(10*((CHAR_WEIGHT * sentChar) + (COUNT_WEIGHT * sentCount) + (MEDIA_WEIGHT * sentMedia) - (RESPONSE_WEIGHT * avgSent)));
  		Debug.log("My score : : : : " + score[0]);
  		score[1] = friendScore(address);
  		Debug.log("Their score : : : : " + score[1]);
  		return score;
  	}
  	
  	
  	
  	/**friend score
  	 * uses same weights as friend score - considering diff ways of executing this.
  	 * 
  	 */
  	
  	public static long friendScore (String address) {
  		DataHandler db = DataHandler.get();
  		String[] keys = DataHandler.KEYS_PUBLIC; 
  		LinkedList<textMessage> convo = db.queryConversation(address, keys, -1, -1);
  		int media = 0;
  		int messages = 0;
  		long charCount = 0;
  		double responseAvg = 0;
  		double score = 0;
  		long maxChar = Long.MIN_VALUE;
  		long minChar = Long.MAX_VALUE;
  		LinkedList<Long> recTimes = new LinkedList<Long>();
  		
  		for (textMessage t: convo) {
  				messages++;
  				charCount += t.charCount();
  				if (t.multimedia()) media++;
  		}
  		
  		textMessage prev = null;
  		long maximum = Long.MIN_VALUE;
  		long minimum = Long.MAX_VALUE;
  		while(convo.peekLast()!=null)
 		{
 		    textMessage temp = convo.pollLast(); //removes from queue
 			if (prev!= null)
 			{
 				long time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
 				if (!temp.received()) {
 					recTimes.add(time);
 				}
 			}
 			while(convo.peekLast() != null && convo.peekLast().received() == temp.received()) //short circuits
 			{
 				convo.pollLast();
 			}
 			prev = temp;
 		}
  		responseAvg = density(recTimes);
 		//calculating totals from the returned values
  		for (long l : recTimes) {
  			if (l > maximum) maximum = l;
  			if (l < minimum) minimum = l;
  		}
  		
 		responseAvg = responseAvg/3456l;
 		if (responseAvg < 1) responseAvg = 1;
 		//responseAvg = 1/responseAvg;
 		charCount = charCount/messages;
 		
 		Debug.log("Charcounts : " + charCount);
 		Debug.log("inverse response avg : " + responseAvg*RESPONSE_WEIGHT);
 		
  		score = 10*(CHAR_WEIGHT * charCount) + (COUNT_WEIGHT * messages) + (MEDIA_WEIGHT * media) - (RESPONSE_WEIGHT * responseAvg);
  		if (score < 1) score = 1;
  		return (long)score;

  	}
  	
  	/**
  	 * Density calculation method
  	 */
  	private static long density (List<Long> list) {
  		long priorAvg = 0;
  		long average = 0;
  		double meanDensity = 0.0;
  		double stdDevDensity = 0.0;
  		//15 minutes as a long
  	
  		ArrayList<densityPoint> points = new ArrayList<densityPoint>();
  		
  		for (int j = 0; j < list.size(); j++) {
  			int i = 0;
  			for(int k = 0; k < list.size(); k++)
  				if (Math.abs(list.get(j) - list.get(k)) <= DISTANCE_VALUE && list.get(j) != list.get(k)) i++;
  			points.add(j, new densityPoint(list.get(j), i));
  			meanDensity += points.get(j).density;
  			Debug.log("values : " + points.get(j).value + " densities : " + points.get(j).density);
  			priorAvg += points.get(j).value;
  		}
  		if (points.size() != 0) {
  			meanDensity = meanDensity/points.size();
  			priorAvg = priorAvg/points.size();
  		}
  		for (densityPoint d : points)
  			stdDevDensity += (Math.pow(d.density - meanDensity, 2));
  		if (stdDevDensity != 0) stdDevDensity = Math.sqrt((stdDevDensity)/points.size());
  		
  		Debug.log("Points uncleaned : " + list.toString());
  		Debug.log("original average : " + priorAvg);
  		Debug.log("Mean density : " + meanDensity);
  		Debug.log("Std dev density : " + stdDevDensity);
  		
  		double test = (meanDensity - stdDevDensity);
  		for (densityPoint d : points) 
  			if (d.density < test) list.remove(d.value);
  		Debug.log("Points cleaned : " + list.toString());
  		
  		for (long l : list)
  			average += l;
  		if (list.size() != 0) average /= list.size();
  		
  		Debug.log("average : " + average);
  		return average;
  	}  	
  	
}


class densityPoint {
	public long value;
	public int density;

	private densityPoint () {}

	public densityPoint (long l, int i) {
		value = l;
		density = i;
	}

}
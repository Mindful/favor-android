package com.favor.util;

import jMEF.BregmanSoftClustering;
import jMEF.MixtureModel;
import jMEF.PVector;
import jMEF.UnivariateGaussian;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;


public class Algorithms {
	
  DataHandler db = DataHandler.get();

  //weightings for proprietary scoring
  private static final double CHAR_WEIGHT = 0.45;
  private static final double COUNT_WEIGHT = 0.15;
  private static final double MEDIA_WEIGHT = 0.15;
  private static final double RESPONSE_WEIGHT = 0.25;
  
  
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
	  return new long[] {25, 35};
	  /*
 	  DataHandler db = DataHandler.get();
 	 String[] keys = new String[] {DataHandler.KEY_DATE};
 	  LinkedList<textMessage> list = db.queryConversation(address, keys, fromDate, untilDate);
 		double sentTotal = 0;
 		double receiveTotal = 0;
 		long [] averages = {0, 0};
 	
 		textMessage temp, prev = null;
 		long time;
 		
 		//too many variables, don't think we need these
 		int tempSentCount = 0;
 		int tempRecCount = 0;
 		
 		//too many intermediary list objects?
 		LinkedList<Long> checkSentTimes = new LinkedList<Long>();
 		LinkedList<Long> checkRecTimes = new LinkedList<Long>();
 		
 		*this is the stripping algorithm, it takes out consecutive messages from the same person
 		 *the check is performed on whether there is anything left in the list, this method
 		 *dequeues the list, it will be empty after this loop runs.
 		 *IMPORTANT: IT GOES IN REVERSE. NOTE THAT IT DOES NOT POLL, IT POLLS LAST
 		 * 
 		 *
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
 		
 		//calculate initial clusters
 		Vector<PVector>[] sentClusters = toPVectors(checkSentTimes);
 		Vector<PVector>[] recClusters = toPVectors(checkRecTimes);
 		PVector[] sentPoints = toPoints(checkSentTimes);
 		for (PVector v : sentPoints) Debug.log("uncleaned sent points --" + v.array[0]);
 		PVector[] recPoints = toPoints(checkRecTimes);
 		for (PVector v : recPoints) Debug.log("uncleaned rec points --" + v.array[0]);
 		
 		//Making mixture models
 		MixtureModel sentTimes;
 		sentTimes = BregmanSoftClustering.initialize(sentClusters, new UnivariateGaussian());
 		MixtureModel recTimes;
 		recTimes = BregmanSoftClustering.initialize(recClusters, new UnivariateGaussian());
 		sentTimes = BregmanSoftClustering.run(sentPoints, sentTimes);
 		recTimes = BregmanSoftClustering.run(recPoints, recTimes);
 		Debug.log(sentTimes.toString());
 		Debug.log(recTimes.toString());
 		
 		//retrieving parameter ranges
 		PVector temp1 = (PVector) sentTimes.param[0];
 		PVector temp2 = (PVector) recTimes.param[0];
 		double sentMax = temp1.array[1];
 		if (sentMax > 28800000 && temp1.array[0] < 28800000) sentMax = 28800000.0;
 		Debug.log("sentMax   " + sentMax);
 		double recMax = temp2.array[1];
 		if (recMax > 28800000 && temp2.array[0] < 28800000) recMax = 28800000.0;
 		Debug.log("recMax   " + recMax);
 		LinkedList<Double> cleanSent = new LinkedList<Double>();
 		LinkedList<Double> cleanRec = new LinkedList<Double>();
 		//calculating totals from the returned values
 		for (int i = 0;i< sentPoints.length; i++) {
 			if (sentPoints[i].array[0] <= sentMax) {
 				sentTotal += sentPoints[i].array[0];
 				cleanSent.add(sentPoints[i].array[0]);
 			}	
 		}
 		for (int i = 0;i < recPoints.length; i++) {
 			if(recPoints[i].array[0] <= recMax) {
 				receiveTotal += recPoints[i].array[0];
 				cleanRec.add(recPoints[i].array[0]);
 			}	
 		}
 		Debug.log("Cleaned sent ---- " + cleanSent.toString());
 		Debug.log("Cleaned Rec ---- " + cleanRec.toString());
 		
 		//set the array equal to the averages (which are in seconds not milliseconds)
 		averages[0] = (long) (sentTotal/(float)(1000*sentPoints.length));
 		averages[1] = (long) (receiveTotal/(float)(1000*recPoints.length));
 		return averages;*/
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
  		return new long[] {25, 35};
  		/*
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
  	//calculate initial clusters
 		Vector<PVector>[] sentClusters = toPVectors(sentTimes);
 		Vector<PVector>[] recClusters = toPVectors(recTimes);
 		PVector[] sentPoints = toPoints(sentTimes);
 		for (PVector v : sentPoints) Debug.log("uncleaned sent points --" + v.array[0]);
 		PVector[] recPoints = toPoints(recTimes);
 		for (PVector v : recPoints) Debug.log("uncleaned rec points --" + v.array[0]);
 		
 		//Making mixture models
 		MixtureModel sentTimesMM;
 		sentTimesMM = BregmanSoftClustering.initialize(sentClusters, new UnivariateGaussian());
 		MixtureModel recTimesMM;
 		recTimesMM = BregmanSoftClustering.initialize(recClusters, new UnivariateGaussian());
 		sentTimesMM = BregmanSoftClustering.run(sentPoints, sentTimesMM);
 		recTimesMM = BregmanSoftClustering.run(recPoints, recTimesMM);
 		Debug.log(sentTimes.toString());
 		Debug.log(recTimes.toString());
 		
 		//retrieving parameter ranges
 		PVector temp1 = (PVector) sentTimesMM.param[0];
 		PVector temp2 = (PVector) recTimesMM.param[0];
 		double sentMax = temp1.array[1];
 		if (sentMax > 28800000 && temp1.array[0] < 28800000) sentMax = 28800000.0;
 		Debug.log("sentMax   " + sentMax);
 		double recMax = temp2.array[1];
 		if (recMax > 28800000 && temp2.array[0] < 28800000) recMax = 28800000.0;
 		Debug.log("recMax   " + recMax);
 		LinkedList<Double> cleanSent = new LinkedList<Double>();
 		LinkedList<Double> cleanRec = new LinkedList<Double>();
 		//calculating totals from the returned values
 		for (int i = 0;i< sentPoints.length; i++) {
 			if (sentPoints[i].array[0] <= sentMax) {
 				avgSent += sentPoints[i].array[0];
 				cleanSent.add(sentPoints[i].array[0]);
 			}	
 		}
 		for (int i = 0;i < recPoints.length; i++) {
 			if(recPoints[i].array[0] <= recMax) {
 				avgRec += recPoints[i].array[0];
 				cleanRec.add(recPoints[i].array[0]);
 			}	
 		}
  		
  		avgSent /= (double) cleanSent.size();
  		avgRec /= (double) cleanRec.size();
  		long [] score = {0,0};
  		score[1] = (long) ((CHAR_WEIGHT * recChar) + (COUNT_WEIGHT * recCount) + (MEDIA_WEIGHT * recMedia) + (RESPONSE_WEIGHT * avgRec));
  		score[0] = (long) ((CHAR_WEIGHT * sentChar) + (COUNT_WEIGHT * sentCount) + (MEDIA_WEIGHT * sentMedia) + (RESPONSE_WEIGHT * avgSent));
  		
  		return score;*/
  	}
  	
  	
  	
  	/** relationship score
  	 * uses same weights as friend score - considering diff ways of executing this.
  	 * 
  	 */
  	
  	public static long friendScore (String address) {
  		return 25;
  		/*
  		DataHandler db = DataHandler.get();
  		String[] keys = DataHandler.KEYS_PUBLIC; 
  		LinkedList<textMessage> convo = db.queryConversation(address, keys, -1, -1);
  		int media = 0;
  		int messages = 0;
  		long charCount = 0;
  		double responseAvg = 0;
  		double score = 0;
  		double numResponse = 0;
  		double receiveTotal = 0.0;
  		LinkedList<Long> recTimes = new LinkedList<Long>();
  		for (textMessage t: convo) {
  			if (t.received()) {
  				messages++;
  				charCount += t.charCount();
  				if (t.multimedia()) media++;
  			}
  		}
  		textMessage prev = null;
  		while(convo.peekLast()!=null)
 		{
 		    textMessage temp = convo.pollLast(); //removes from queue
 			if (prev!= null)
 			{
 				long time = temp.rawDate() - prev.rawDate(); //make time negative, because it will be. also consider switch ifs?
 				if (!temp.received()) {
 					responseAvg += time;
 					numResponse++;
 				}
 			}
 			while(convo.peekLast() != null && convo.peekLast().received() == temp.received()) //short circuits
 			{
 				convo.pollLast();
 			}
 			prev = temp;
 		}
  	//calculate initial clusters

 		Vector<PVector>[] recClusters = toPVectors(recTimes);
 		PVector[] recPoints = toPoints(recTimes);
 		for (PVector v : recPoints) Debug.log("uncleaned rec points --" + v.array[0]);
 		
 		//Making mixture models
 		
 		MixtureModel recTimesMM;
 		recTimesMM = BregmanSoftClustering.initialize(recClusters, new UnivariateGaussian());
 		recTimesMM = BregmanSoftClustering.run(recPoints, recTimesMM);
 		
 		Debug.log(recTimes.toString());
 		
 		//retrieving parameter ranges

 		PVector temp = (PVector) recTimesMM.param[0];
 		double recMax = temp.array[1];
 		if (recMax > 28800000 && temp.array[0] < 28800000) recMax = 28800000.0;
 		Debug.log("recMax   " + recMax);
 		LinkedList<Double> cleanSent = new LinkedList<Double>();
 		LinkedList<Double> cleanRec = new LinkedList<Double>();
 		
 		//calculating totals from the returned values
 		for (int i = 0;i < recPoints.length; i++) {
 			if(recPoints[i].array[0] <= recMax) {
 				receiveTotal += recPoints[i].array[0];
 				cleanRec.add(recPoints[i].array[0]);
 			}	
 		}
  		responseAvg = responseAvg/numResponse;
  		score = (CHAR_WEIGHT * charCount) + (COUNT_WEIGHT * messages) + (MEDIA_WEIGHT * media) + (RESPONSE_WEIGHT * responseAvg);
  		return (long)score;*/
  	}
  	
  	
  	/**
  	 * utility method for calculating a split value that is mean + 1 standard deviation
  	 * @param list
  	 * @return
  	 */
  	public static double calcInitialSplit (LinkedList<Long> list) {
  		double mean = 0;
  		double stdDev = 0;
  		double totalN = list.size();
 		for (int i = 0; i < totalN; i++) mean += list.get(i);
 		mean = mean/totalN;
 		for (int i=0;i<totalN;i++) {
 			stdDev += (list.get(i) - mean)*(list.get(i) - mean);	
 		}
 		stdDev = Math.sqrt(stdDev);
 		Debug.log("mean plus standard dev   " + ((mean + stdDev)/1000));
 		return (mean + stdDev)/1000;
  	}
  	
  	/**
  	 * utility method that turns a linked list of longs into two clusters of 
  	 * PVectors based on an initial split value. returns an array of Vectors of
  	 * PVectors for use in the Bregman Soft Clustering algo.
  	 * @param list
  	 * @return
  	 */
  	public static Vector<PVector>[] toPVectors (LinkedList<Long> list) {
  		double split = calcInitialSplit(list);
  		
  		//divide list based on split
  		ArrayList<Double> under = new ArrayList<Double>(), over = new ArrayList<Double>();
  		for (long l : list) {
  			if (l > split) over.add((double)l);
  			else under.add((double)l);
  		}
  		Vector<PVector> unders = new Vector<PVector>();
  		Vector<PVector> overs = new Vector<PVector>();
  		
  		//adds a new PVector for each value, then makes the PVector contain that value
  		for (int i = 0;i < under.size(); i++) {
  			unders.add(i, new PVector(1));
  			unders.get(i).array[0] = under.get(i);
  		}
  		for (int i = 0;i < over.size(); i++) {
  			overs.add(i, new PVector(1));
  			overs.get(i).array[0] = over.get(i);
  		}
  		
  		Vector<PVector>[] result = (Vector<PVector>[]) new Vector[2];
  		result[0] = unders;
  		result[1] = overs;
  		Debug.log("0    " + result[0].toString());
  		Debug.log("1    " + result[1].toString());
  		return result;
  		
  	}
  	/**
  	 * Utility method turns a linked list into a list of points for use in
  	 * Bregman soft clustering, returns an array of PVectors.
  	 * @param list
  	 * @return
  	 */
  	public static PVector[] toPoints (LinkedList<Long> list) {
  		PVector[] points = new PVector[list.size()];
  		for (int i = 0;i < points.length;i++) {
  			points[i] = new PVector(1);
  			points[i].array[0] = (double) list.get(i);	
  		}
  		return points;
  	}
  	
}

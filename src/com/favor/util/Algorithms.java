package com.favor.util;

import java.util.ArrayList;
import com.favor.util.*;

/*public class queryBeast {
	messageCounter(queryFromAll(currentTimeMillis(), currentTimeMillis() - 31557600000));
	
	
}
*/




public class Algorithms {
  private int mmsCount = 0;
  private int smsCount = 0;
  private int totalCount = 0; 
  private long totalCharCount = 0;
  private double avgCharCount;
  private double avgPerYear;
  private double avgPerMonth;
  private double avgPerWeek;
  private double avgPerDay; 
  private double timeSpan;


/*
* isMMS returns 0/1
* isSMS returns 1/0
* charCount returns int
* rawdate returns long
*/

//count mms/sms, produce total count
  public void messageCounter (ArrayList<textMessage> a) {
	  for (textMessage t : a) {
		  if(t.mms()) mmsCount++;
		  else smsCount++;
	  }
	  totalCount = mmsCount + smsCount;
  }

//getters for counting messages
  public int getMMS () {
	  return mmsCount;
  }

  public int getSMS () {
	  return smsCount;
  }

  public int getTotal () {
	  return totalCount;
  }

//date calcs
  //public long getTimeSpan {return long 1;}

/*Count characters if part of same array, likely N/A
  public void charCounter (ArrayList<textMessage> a) {
  for (textMessage t : a) {
    if (fromYou) yourCharCount += t.charCount();
    else theirCharCount += t.charCount();
  }
  totalCharCount = yourCharCount + theirCharCount;
  }
*/

// counts total characters in message array 
  public void charCounter (ArrayList<textMessage> a) {
	  for (textMessage t : a)
		  totalCharCount += t.charCount();
  }

//getter 4 Character Count
  public long getCharCount () {
	  return totalCharCount;
  }

  public void CharCountPerDay () {
	  avgCharCount = getCharCount()/totalCount;
  }

  public void averageCharCountPerMonth (double months) {
	  avgCharCount = getCharCount()/months;
  }
  


// Getter for average character count
  public double getAvgCharCount () {
	  return avgCharCount;
  }

// Getter for average # sms per day
  public double getAvgPerDay () {
	  return avgPerDay;
  }
}

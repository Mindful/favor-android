package com.favor.develop;

import java.lang.reflect.Method;

import com.favor.util.Contact;
import com.favor.util.DataHandler;
import com.favor.util.Debug;


public class Benchmark{
	
	private static DataHandler db;
	private static Contact[] allContacts;
	private static Contact[] threeContacts;
	
	public static enum databaseBenchmarks {MULTIQUERY_DATABASE_ALL, MULTIQUERY_DATABASE_THREE, MULTIQUERY_JAVA_ALL, MULTIQUERY_JAVA_THREE}
	
	public static void setUpDatabase(){
		db  = DataHandler.get();
		allContacts = (Contact[])db.contacts().toArray(new Contact[db.contacts().size()]);
		threeContacts = new Contact[3];
		int offset = (int) Math.random() * (allContacts.length-4);
		for(int i = 0; i < 3; ++i){
			threeContacts[i]=allContacts[offset+i];
		}
		Method databaseMethod;
		Method javaMethod;
		try {
			databaseMethod = db.getClass().getMethod("multiQueryDatabase");
			javaMethod = db.getClass().getMethod("multiQuery");
			javaMethod.setAccessible(true);
			databaseMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			Debug.log("Reflection setup failed.");
			e.printStackTrace();
		}
	}
	
	/*
	public static void multiQueryBenchmarks(int reps){
		timeMultiQueryJavaAll(reps);
		timeMultiQueryJavaThree(reps);
		timeMultiQueryDatabaseAll(reps);
		timeMultiQueryDatabaseThree(reps);
		Debug.log("Finished database benchmarks");
	}
	
	
	public static void timeMultiQueryDatabaseThree(int reps){
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < reps; ++i){
			db.multiQueryDatabase(threeContacts, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
			db.multiQueryDatabase(threeContacts, DataHandler.KEYS_PUBLIC, -1, -1, "received");
		}
		long totalTime = System.currentTimeMillis() - startTime;
		Debug.log("MultiQueryDatabase with 3 took "+totalTime/1000+" seconds total.");
		Debug.log("MultiQueryDatabase with 3 took "+totalTime/reps+" milliseconds per pair.");
	}

	public static void timeMultiQueryDatabaseAll(int reps){
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < reps; ++i){
			db.multiQueryDatabase(allContacts, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
			db.multiQueryDatabase(allContacts, DataHandler.KEYS_PUBLIC, -1, -1, "received");
		}
		long totalTime = System.currentTimeMillis() - startTime;
		Debug.log("MultiQueryDatabase with all took "+totalTime/1000+" seconds total.");
		Debug.log("MultiQueryDatabase with all took "+totalTime/reps+" milliseconds per pair.");
	}
	
	public static void timeMultiQueryJavaThree(int reps){
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < reps; ++i){
			db.multiQuery(threeContacts, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
			db.multiQuery(threeContacts, DataHandler.KEYS_PUBLIC, -1, -1, "received");
		}
		long totalTime = System.currentTimeMillis() - startTime;
		Debug.log("MultiQueryJava with 3 took "+totalTime/1000+" seconds total.");
		Debug.log("MultiQueryJava with 3 took "+totalTime/reps+" milliseconds per pair.");
	}
	
	public static void timeMultiQueryJavaAll(int reps){
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < reps; ++i){
			db.multiQuery(allContacts, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
			db.multiQuery(allContacts, DataHandler.KEYS_PUBLIC, -1, -1, "received");
		}
		long totalTime = System.currentTimeMillis() - startTime;
		Debug.log("MultiQueryJava with all took "+totalTime/1000+" seconds total.");
		Debug.log("MultiQueryJava with all took "+totalTime/reps+" milliseconds per pair.");
	}
	*/
}
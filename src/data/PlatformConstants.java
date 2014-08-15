package data;

import com.favor.util.Logger;

public class PlatformConstants {
	//TODO: this may need to actually be initialized, in which case its variables can't be final, but we can make them all
	//private and just use getters
	
	static private String dbName;
	static private boolean initialized = false;
	
	
	public static String getDbName(){
		if(!initialized) throw new RuntimeException("Must initialize platform specific constants.");
		return dbName;
	}
	
	public static boolean getInitialized(){
		return initialized;
	}
	
	
	public static void initialize(){
		if(!initialized){
			//File file = new File(context.getFilesDir(), filename);
			dbName = "favor.db";
		} else Logger.warn("Duplicate initialization of platform constants.");
		
	}
	
	

}

package aiman.pacs.behaviours;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import net.jalone.jul4j.logging.Logger;

/** log utility class. logs are printed in home directory */
public class LearningLogger extends Logger{
	
	//SINGLETON
	private static LearningLogger instance = null;
	public static Logger getInstance(){
		if(instance == null){
			instance = new LearningLogger();
		}
		return instance;
	}
	
	private boolean enabled = true;
	
	PrintWriter		logger; 
	
	protected LearningLogger(){
		super();
    	
    	StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName ();
		try {
			logger = new PrintWriter(System.getProperty("user.home") + "/" + mainClass +"learn.log", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void log(){
		if(enabled)
	        logger.println("");  
			logger.flush();
	}
	public void log(String s){
		if(enabled)
	        logger.println(s);  
			logger.flush();
	}
	
}

package base.log;

import java.io.File;

import org.apache.log4j.Level;

/**
 * The Class Log.
 */
public class Log{
	
	/** The log file's path */
	public static final String CLIENT_LOG_FILE_PATH = "log" + File.separatorChar + "Client.log";
	/** The log level. */
	public static final Level CLIENT_LOG_LEVEL = Level.ERROR;
	
	/** The log file's path */
	public static final String PERFORMANCE_LOG_FILE_PATH = "log" + File.separatorChar + "Performance.log";
	/** The log level. */
	public static final Level PERFORMANCE_LOG_LEVEL = Level.ALL;
	
	public static final Level NODE_ID_LOG_LEVEL = Level.ALL;
	
}
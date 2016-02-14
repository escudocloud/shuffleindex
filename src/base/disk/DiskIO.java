package base.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The abstract class DiskIO
 * Implements the main I/O operations
 * @author Tommaso
 *
 */
public abstract class DiskIO {

	/** The disk configuration file path */
	final static public String CONF_FILE = "disk" + File.separator + "disk_conf.xml"; 	
	/** The disks file path */
	protected String[] disks;
	
	protected File[] files;		
	protected RandomAccessFile[] rafs;
	/** 
	 * The class constructor 
	 * 
	 * @param disk the disk file path 
	 */
	public DiskIO(String[] disks){
		
		this.disks = disks;
		
		if(disks != null)
			openDisk();
		
	}
	 
	protected void openDisk() {
		
		try {
			
			files = new File[4];
			rafs = new RandomAccessFile[4];
			
    		for( int i=0 ; i<4 ; i++ ) {
    			files[i] = new File(disks[i]);	
    			rafs[i] = new RandomAccessFile(files[i], "rw");
    		}
	    	
    	} catch(IOException ioe){
    		
    		ioe.printStackTrace();
    		
    	}  catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
	}
	
	/**
	 * Writes the bytes vector starting from the given offset position
	 * 
	 * @param buffer the bytes vector
	 * @param offset on the disk
     * @param i the disk number
	 */
    public void writeBytes(byte[] buffer, long offset, int i) {
    	try {
    		
    		rafs[i].seek(offset);
	    	rafs[i].write(buffer);
	    	
    	} catch(IOException ioe){
    		
    		ioe.printStackTrace();
    		System.err.println(offset);
    		
    	}  catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }
    
    /**
     * Reads a given number of bytes starting from a specified position
     * 
     * @param offset 
     * @param length number of bytes
     * @param i the disk number
     * 
     * @return the readed bytes vector
     */
    public byte[] readBytes(long offset, int length, int i) {
    	
    	byte[] bytes = new byte[length];
    	
    	try {
    		
    		rafs[i].seek(offset);
	    	rafs[i].read(bytes,0,length);
	    	
    	}  catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}  
    	
    	return bytes;
    	
    }
   
    /**
     * Closes all the opened disks
     */
    public void closeDisks() {
    	
    	try { 
    		
    		for( int i=0 ; i<4 ; i++ ) {
    			rafs[i].close();
    		}
    		
    	} catch(Exception e) { 
    		
    		e.printStackTrace(); 
    		
    	}
    		
    }
	
}

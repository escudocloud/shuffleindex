package base.disk;

import base.bptree.Node;

import java.io.*;

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
	 * @param disks the disk file path
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

		} catch(IOException ioe) {

			//ioe.printStackTrace();
			//System.err.println(offset);
			System.err.println("===== STREAM  "+ i +" CLOSED  =====");

		//} catch (NullPointerException npe) {
		//	System.err.println("===== DISK EMPTY - PLEASE RUN \" create \" COMMAND ON CLIENT=====");

		}  catch(Exception e) {

			e.printStackTrace();

		}

	}


	/**
	 * Write a node to the file
	 * */

	public void writeBytes(Node node, int i){
		try {
			OutputStream file = new FileOutputStream(files[i]);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
//			output.
//			
			output.writeObject(node);

		} catch (Exception e) {
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

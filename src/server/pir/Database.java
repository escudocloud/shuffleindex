package server.pir;

import base.bptree.Bptree;
import base.disk.Disk;

/**
 * The class Database
 * @author Tommaso
 *
 */
public class Database {

	/**
	 * Creates the b+tree and saves it on the disk, all the informations used during the creation are stored in the file disk_conf.xml 
	 */
	public static void create() {
		
		Disk<Long, Long> disk = new Disk<Long, Long>(true);	
		Bptree<Long, Long> bptree = new Bptree<Long, Long>(disk);
				
		for(long i = 0 ; i < disk.getDiskSuperBlock().getKeyNum() ; i++)
			bptree.insert(i+1, i+1);
				
		bptree.close();
		
	}

}

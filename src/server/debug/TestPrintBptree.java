package server.debug;

import base.bptree.Bptree;
import base.disk.Disk;

public class TestPrintBptree {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Disk<Long, Long> disk = new Disk<Long, Long>(false);	
		
		Bptree<Long, Long> bptree = new Bptree<Long, Long>(disk);
		
		bptree.printAll(bptree.getRoot(), "");
		
		bptree.close();

	}

}

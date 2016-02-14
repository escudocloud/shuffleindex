package server.debug.performance;

import base.bptree.Bptree;
import base.disk.Disk;

public class Search {

	public static void main(String[] args) {

		Disk<Long, Long> disk = new Disk<Long, Long>(false);	
		
		Bptree<Long, Long> bptree = new Bptree<Long, Long>(disk);
		
		long start, end, time;
				
		start = System.currentTimeMillis();
		
		System.err.println(bptree.find(500L));
		
		end = System.currentTimeMillis();	
		time = end - start;	
		
		System.err.println("Time in [ms] for the traditional search algorithm (Executed on server): " +time);
	}

}

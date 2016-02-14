package server.debug.performance;

import java.io.File;

import base.bptree.Bptree;
import base.disk.Disk;

public class CreateBptree {

	public static void main(String[] args) {
		
		create();
		
	}
	
	public static void create() {
		
		File f = new File("disk/disk");
		if( f.exists() )
			f.delete();
		
		Disk<Long, Long> disk = new Disk<Long, Long>(true);	
		
		Bptree<Long, Long> bptree = new Bptree<Long, Long>(disk);
				
		long start, end, time;
		start = System.currentTimeMillis();		
				
		for( long i = 0 ; i < disk.getDiskSuperBlock().getKeyNum() ; i++ )
			bptree.insert(i+1, i+1);
		
		end = System.currentTimeMillis();	
		time = end - start;	
		
		System.out.println("Time : " + time/1000 + "[s] "+ time +"[ms] Key number: "+ disk.getDiskSuperBlock().getKeyNum());
		long time_estimated;
		
		time_estimated = ( 16777216L * time) / (long)(disk.getDiskSuperBlock().getKeyNum());	
		System.out.println("Time estimated for 1 GiB : " + (int)(time_estimated/(3600000L*24L)) +"[days] "+ (int)(time_estimated/3600000L)%24 +"[hours] "+ (int)(time_estimated/60000)%60 + "[min]" );
		
		time_estimated = ( 17179869184L * time) / (long)(disk.getDiskSuperBlock().getKeyNum());	
		System.out.println("Time estimated for 1 TiB : " + (int)(time_estimated/(3600000L*24L)) +"[days] "+ (int)(time_estimated/3600000L)%24 +"[hours] "+ (int)(time_estimated/60000)%60 + "[min]" );

		System.out.println("");
		
		bptree.printAll(bptree.getRoot(), "");
		
		bptree.close();
		
	}

}

package base.debug;

import base.disk.Disk;

public class TestDiskCacheBlock {

	public static void main(String[] args) {
		
		Disk<Long, Long> disk = new Disk<Long, Long>(false);
		
//		for( long i = 0 ; i < (((disk.getDiskSuperBlock().getHeight()-1)*disk.getDiskSuperBlock().getNumLvlEle())+1) ; i++ ) {
//			
//			disk.getDiskCacheBlock().cachePids.set((int)i,262144L);
//			disk.getDiskCacheBlock().cacheWeights.set((int)i,new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});
//			
//		}
//		
//		disk.getDiskCacheBlock().save();
		
		for( int i = 0 ; i < (((disk.getDiskSuperBlock().getHeight()-1)*disk.getDiskSuperBlock().getNumLvlEle())+1) ; i++ ) {
			
			System.out.println(disk.getDiskCacheBlock().cachePids.get(i));
			System.out.println(disk.getDiskCacheBlock().cacheWeights.get(i));
			
		}
		
	}

}

package base.disk;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.LinkedList;

/**
 * The DiskCacheBlock class
 * @author Tommaso
 *
 */
public class DiskCacheBlock extends DiskIO{
	
	/** The super block associated with the cache block */
	private DiskSuperBlock diskSuperBlock; 
	/** The cache elements' PIDs */
	public LinkedList<Long> cachePids;
	/** The cache elements' weights */
	public LinkedList<byte[]> cacheWeights;
	
	/**
	 * The constructor of the class
	 * 
	 * @param disks the disks file path
	 * @param diskSuperBlock the super block associated with the cache block
	 */
	public DiskCacheBlock(String[] disks, DiskSuperBlock diskSuperBlock) {
			
		super(disks);
		this.cachePids = new LinkedList<Long>();
		this.cacheWeights = new LinkedList<byte[]>();
		this.diskSuperBlock = diskSuperBlock;	
		
	}
	
	/**
	 * Fills all the cache block with zeros
	 */
	public void init() {
		reset();
	}
	
	/**
	 * Fills all the cache block with zeros
	 */
	public void reset() {
		
		byte[] cacheSectorBytes = new byte	[	
		                                  	 	(int) (((diskSuperBlock.getHeight()-1)* diskSuperBlock.getNumLvlEle())+1)*8 +
		                                  	 	(int) (((diskSuperBlock.getHeight()-1)* diskSuperBlock.getNumLvlEle())+1)*16 
		           							];
		
		for( int i = 0 ; i < cacheSectorBytes.length ; i++ )
			cacheSectorBytes[i] = 0;
		
		save(cacheSectorBytes);
		
	}
	
	/**
	 * Initializes the cache block loading the informations from the disk
	 * 
	 * @param superBlockBytes the bytes vector containing the super block informations
	 */
	public void load(){
		
		this.cachePids = new LinkedList<Long>();
		this.cacheWeights = new LinkedList<byte[]>();
		
		ByteBuffer bBuffer;
		LongBuffer lBuffer;
		
		for( int i = 0; i < (((diskSuperBlock.getHeight()-1)* diskSuperBlock.getNumLvlEle())+1) ; i++) {
			
			bBuffer = ByteBuffer.wrap(readBytes(diskSuperBlock.getCOffset()+(i*24), 8, 0) , 0, 8);
			lBuffer = bBuffer.asLongBuffer();
			
			cachePids.add(lBuffer.get(0));
			cacheWeights.add(readBytes(diskSuperBlock.getCOffset()+(i*24)+8, 16, 0));
			
		}
		
	}
	
	/**
	 * Collects all the cache block's information into a bytes vector and returns it
	 * 
	 * @return the bytes vector containing the cache block informations
	 */
	public byte[] getBytes() {
		
		byte[] cacheSectorBytes = new byte	[	
		                                  	 	(int) (((diskSuperBlock.getHeight()-1)* diskSuperBlock.getNumLvlEle())+1)*8 +
		                                  	 	(int) (((diskSuperBlock.getHeight()-1)* diskSuperBlock.getNumLvlEle())+1)*16 
		           							];
		
		ByteBuffer bBuffer;
		LongBuffer lBuffer;
		
		for( int i = 0 ; i < cachePids.size() ; i++ ) {

			bBuffer = ByteBuffer.wrap(cacheSectorBytes, (i*24), 8);
			lBuffer = bBuffer.asLongBuffer();
			lBuffer.put(cachePids.get(i));
			
			bBuffer = ByteBuffer.wrap(cacheSectorBytes, ((i*24)+8), 16);
			bBuffer.put(cacheWeights.get(i));
		}	
		
		return cacheSectorBytes;
		
	}
	
	/**
	 * Saves the cache block 
	 */
	public void save() {   	
		
		writeBytes(getBytes(), diskSuperBlock.getCOffset(), 0);		
		
    }
	
	/**
	 * Saves the cache block 
	 *
	 * @param cacheSectorBytes the vector containing the information to save
	 */
	public void save(byte[] cacheSectorBytes) {   	
		
		writeBytes(cacheSectorBytes, diskSuperBlock.getCOffset(), 0);		
		
    }
	
}

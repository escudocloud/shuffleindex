package base.disk;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * The DiskSuperBlock class
 * @author Tommaso
 *
 */
public class DiskSuperBlock extends DiskIO {

	/** The disk date of creation */
    private long disksCreationDate;
    /** The boot sector offset on the disk */
    private long sbOffset;
    /** The boot sector size on the disk */
    private long sbSize;    
    /** The cache offset on the disk */
    private long cOffset;
    /** The cache size on the disk */
    private long cSize; 
    /** Number of element for every cache level */
    private long numLvlEle;
    /** The nodes offset on the disk */
    private long[] nOffsets;   
    /** The new node offset */
    private long[] newNodeOffsets;
    /** Number of leaf node in the bptree */
    private long leafNodeNum;  
    /** Number of inner node in the bptree */
    private long innerNodeNum;  
    /** The bptree height */
    private long height;
    /** Key domain start */
    private long keyDomainStart;
    /** Key domain end */
    private long keyDomainEnd;
    /** The root pid */
    private long rootPid;   
    /** Number of keys in the bptree */
    private int keyNum;   
    /** The maximum number of keys in the leaf node, M must be > 0 */
    private int m;
    /** The maximum number of keys in inner node, the number of pointer is N+1, N must be > 2 */
    private int n; 
    /** The minimum number of keys in the root node, the number of pointer is minnumkeys+1 */
    private int minnumkeys;
    
    
    /**
     * The class constructor invoked if the disk doesn't exists on the disk, it would be initialized using the info contained in the configuration file
     * 
     * @param disks the disks file path
     * @param diskConfFile the disk configuration file path
     */
	public DiskSuperBlock(	String[] disks,
							String diskConfFile) {
		
		super(disks);
		
		this.disksCreationDate	= System.currentTimeMillis();
		this.sbOffset			= DiskConfFile.getInstance(diskConfFile).getSbOffset();
		this.sbSize 			= DiskConfFile.getInstance(diskConfFile).getSbSize();
	    this.cOffset 			= DiskConfFile.getInstance(diskConfFile).getCOffset();
		this.cSize 				= DiskConfFile.getInstance(diskConfFile).getCSize();
		this.numLvlEle 			= DiskConfFile.getInstance(diskConfFile).getNumLvlEle();
		this.nOffsets 			= DiskConfFile.getInstance(diskConfFile).getNOffsets();
		this.newNodeOffsets 	= DiskConfFile.getInstance(diskConfFile).getNewNodeOffsets();
		this.leafNodeNum		= 0;
		this.innerNodeNum		= 0;
		this.height 			= DiskConfFile.getInstance(diskConfFile).getHeight();
		this.keyDomainStart 	= DiskConfFile.getInstance(diskConfFile).getKeyDomainStart();
		this.keyDomainEnd 		= DiskConfFile.getInstance(diskConfFile).getKeyDomainEnd();
		this.rootPid 			= DiskConfFile.getInstance(diskConfFile).getRootPid();
		this.keyNum 			= DiskConfFile.getInstance(diskConfFile).getKeyNum();
		this.m 					= DiskConfFile.getInstance(diskConfFile).getM();
		this.n 					= DiskConfFile.getInstance(diskConfFile).getN();
		this.minnumkeys         = DiskConfFile.getInstance(diskConfFile).getMinNumKeys();
	}
	
	/**
	 * The class constructor invoked if the super block already exists on the disk
	 * 
	 * @param disk the disk file path
	 * @param sbOffset the super block start offset on the disk
	 * @param sbSize the super block size in bytes on the disk
	 */
	public DiskSuperBlock(	String[] disks, long sbOffset, int sbSize ) {
		
		super(disks);
		this.minnumkeys         = DiskConfFile.getInstance(CONF_FILE).getMinNumKeys();
		load(readBytes(sbOffset, sbSize, 0));
		
		// System.err.println(this);
	}
	
	public String toString() {
		String msg = "";
		msg += "height = "+this.height;
		msg += " disks = [";
		for (String s: this.disks) msg+=" "+s;
		msg += " ]";
		return msg;
	}
	/**
	 * The class constructor invoked if the disk doesn't exists on the disk, it would be initialized using the info contained in the given bytes vector
	 * 
	 * @param superBlockBytes the bytes vector containing the super block informations
	 */
	public DiskSuperBlock(	byte[] superBlockBytes ) {
		
		super(null);
		this.minnumkeys         = DiskConfFile.getInstance(CONF_FILE).getMinNumKeys();
		load(superBlockBytes);
	}
	
	/**
	 * Initializes the super block loading the informations from the given bytes vector
	 * 
	 * @param superBlockBytes the bytes vector containing the super block informations
	 */
	public void load(	byte[] superBlockBytes ) {

		ByteBuffer bBuffer;
		LongBuffer lBuffer;
		IntBuffer iBuffer;

		bBuffer = ByteBuffer.wrap(superBlockBytes, 0, (20*8));
		lBuffer = bBuffer.asLongBuffer();
				
		this.disksCreationDate = lBuffer.get();
		this.sbOffset = lBuffer.get();
		this.sbSize = lBuffer.get();
		this.cOffset = lBuffer.get();
		this.cSize = lBuffer.get();
		this.numLvlEle = lBuffer.get();
		
		this.nOffsets = new long[4];
		this.newNodeOffsets = new long[4];
		for( int i = 0 ; i < 4 ; i++) {
			this.nOffsets[i] = lBuffer.get();
			this.newNodeOffsets[i] = lBuffer.get();
		}
		
		this.leafNodeNum = lBuffer.get();
		this.innerNodeNum = lBuffer.get();
		this.height = lBuffer.get();
		this.keyDomainStart = lBuffer.get();
		this.keyDomainEnd = lBuffer.get();
		this.rootPid = lBuffer.get();
		
		bBuffer = ByteBuffer.wrap(superBlockBytes, (20*8), (3*4));
		iBuffer = bBuffer.asIntBuffer();
		
		this.keyNum = iBuffer.get();
		this.m = iBuffer.get();
		this.n = iBuffer.get();
        
	}
	
	/**
	 * Saves the super block 
	 */
	public void save() {   	
		
		writeBytes(getBytes(), getSbOffset(), 0);		
		
    }
	
	/**
	 * Collects all the super block's information into a bytes vector and returns it
	 * 
	 * @return the bytes vector containing the super block informations
	 */
	public byte[] getBytes() {
		
		byte[] bootSectorBytes = new byte[(20*8)+(3*4)];
		
		ByteBuffer bBuffer;
		LongBuffer lBuffer;
		IntBuffer iBuffer;
		
		bBuffer = ByteBuffer.wrap(bootSectorBytes, 0, 20*8);
		lBuffer = bBuffer.asLongBuffer();
		
		lBuffer.put(disksCreationDate);
		lBuffer.put(sbOffset);
		lBuffer.put(sbSize);
		lBuffer.put(cOffset);
		lBuffer.put(cSize);
		lBuffer.put(numLvlEle);
		
		for( int i = 0 ; i < 4 ; i++) {
			lBuffer.put(nOffsets[i]);
			lBuffer.put(newNodeOffsets[i]);
		}
		
		lBuffer.put(leafNodeNum);
		lBuffer.put(innerNodeNum);
		lBuffer.put(height);
		lBuffer.put(keyDomainStart);
		lBuffer.put(keyDomainEnd);
		lBuffer.put(rootPid);
		
		bBuffer = ByteBuffer.wrap(bootSectorBytes, 20*8, 3*4);
		iBuffer = bBuffer.asIntBuffer();	
		
		iBuffer.put(keyNum);
		iBuffer.put(m);
		iBuffer.put(n);
		
		return bootSectorBytes;
		
	}

	/**
	 * Gets the creation date of the disks in millisecond
	 * @return
	 */
	public long getDisksCreationDate() {
		return disksCreationDate;
	}
	
	/**
	 * Gets the super block offset.
	 * 
	 * @return the super block  offset
	 */
	public long getSbOffset() {
		return sbOffset;
	}

	/**
	 * Gets the super block  size.
	 * 
	 * @return the super block  size
	 */
	public long getSbSize() {
		return sbSize;
	}

	/**
	 * Gets the cache offset.
	 * 
	 * @return the cache offset
	 */
	public long getCOffset() {
		return cOffset;
	}

	/**
	 * Gets the cache size.
	 * 
	 * @return the cache size
	 */
	public long getCSize() {
		return cSize;
	}
	
	/**
	 * Gets the minnumkeys in the root node
	 * The root node will include a number of keys greater or equal to minnumkeys
	 * @return the min num keys in the root node
	 */
	public int getMinNumKeys() {
		return minnumkeys;
	}
	
	/**
	 * Gets the number of element for cache level.
	 * 
	 * @return the number of element for cache level
	 */
	public long getNumLvlEle() {
		return numLvlEle;
	}
	
	/**
	 * Gets the nodes block offset of the disks.
	 * 
	 * @return the nodes block offset of the disks
	 */
	public long[] getNOffsets() {
		return nOffsets;
	}

	/**
	 * Gets the new node offset of the given disk.
	 * 
	 * @param i the disk number
	 * 
	 * @return the new node offset of the given disk
	 */
	public long getNewNodeOffsets( int i) {
		return newNodeOffsets[i];
	}



	/**
	 * Gets the leaf node number.
	 * 
	 * @return the leaf node number
	 */
	public long getLeafNodeNum() {
		return leafNodeNum;
	}

	/**
	 * Gets the inner node number.
	 * 
	 * @return the inner node number
	 */
	public long getInnerNodeNum() {
		return innerNodeNum;
	}
	
	/**
	 * Gets the b+tree height.
	 * 
	 * @return the b+tree height
	 */
	public long getHeight() {
		return height;
	}

	/**
	 * Gets the key domain start.
	 * 
	 * @return the key domain start
	 */
	public long getKeyDomainStart() {
		return keyDomainStart;
	}

	/**
	 * Gets the key domain end.
	 * 
	 * @return the key domain end
	 */
	public long getKeyDomainEnd() {
		return keyDomainEnd;
	}

	/**
	 * Gets the root pid.
	 * 
	 * @return the root pid
	 */
	public long getRootPid() {
		return rootPid;
	}

	/**
	 * Gets the key num.
	 * 
	 * @return the key num
	 */
	public int getKeyNum() {
		return keyNum;
	}

	/**
	 * Gets the leaf node number of key.
	 * 
	 * @return the leaf node number of key.
	 */
	public int getM() {
		return m;
	}

	/**
	 * Gets the inner node number of key.
	 * 
	 * @return the inner node number of key
	 */
	public int getN() {
		return n;
	}

	/**
	 * Sets the root pid
	 * 
	 * @param rootPid
	 */
	public void setRootPid(long rootPid) {
		this.rootPid = rootPid;
	}

	/**
	 * Sets the new node offset.
	 * 
	 * @param newNodeOffset the new node offset
	 * @param i disk index
	 */
	public void setNewNodeOffset(int i, long newNodeOffset) {
		this.newNodeOffsets[i] = newNodeOffset;
	}

	/**
	 * Sets the number of element per cache's level.
	 * 
	 * @param numLvlEle the number of element per cache's level
	 */
	public void setNumLvlEle(long numLvlEle) {
		this.numLvlEle = numLvlEle;
	}
	
	public void addLeafNode() {
		leafNodeNum = leafNodeNum + 1; 
	}
	
	public void addInnerNode() {
		innerNodeNum = innerNodeNum + 1; 
	}
	
	public void setHeight(long h) {
		height = h;
	}
	public void updateKeyNumber(){
		keyNum++;
	}
	public int getKeyNumber(){	
		return keyNum;
	}
	public void setKeyNumber(int k){
		this.keyNum =  k;
	}
	
	/**
	 * Gets the super block information.
	 * 
	 * @return the string containing the super block information
	 */
	public String getInfo() {
		
		return 		"disksCreationDate: "	+ this.disksCreationDate + " \n" +
					"sbOffset: "			+ this.sbOffset + " \n" +
					"sbSize: " 				+ this.sbSize + " \n" +
					"cOffset: " 			+ this.cOffset + " \n" +
					"cSize: " 				+ this.cSize + " \n" +
					"numLvlEle: " 			+ this.numLvlEle + " \n" +
					"nOffset1: " 			+ this.nOffsets[0] + " \n" +
					"newNodeOffset1: " 		+ this.newNodeOffsets[0] + " \n" +
					"nOffset2: " 			+ this.nOffsets[1] + " \n" +
					"newNodeOffset2: " 		+ this.newNodeOffsets[1] + " \n" +
					"nOffset3: " 			+ this.nOffsets[2] + " \n" +
					"newNodeOffset3: " 		+ this.newNodeOffsets[2] + " \n" +
					"nOffset4: " 			+ this.nOffsets[3] + " \n" +
					"newNodeOffset4: " 		+ this.newNodeOffsets[3] + " \n" +
					"leafNodeNum: " 		+ this.leafNodeNum + " \n" +
					"innerNodeNum: " 		+ this.innerNodeNum + " \n" +
					"height: " 				+ this.height + " \n" +
					"keyDomainStart: " 		+ this.keyDomainStart + " \n" +
					"keyDomainEnd: " 		+ this.keyDomainEnd + " \n" +
					"rootPid: " 			+ this.rootPid + " \n" +
					"keyNum: " 				+ this.keyNum + " \n" +
					"m: " 					+ this.m + " \n" +
					"n: " 					+ this.n;
		
	}
	

}

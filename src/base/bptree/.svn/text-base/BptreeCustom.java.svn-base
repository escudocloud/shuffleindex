package base.bptree;

import java.util.LinkedList;

import base.disk.Disk;

/**
 * The BptreeCustom class
 * @author tommaso
 *
 * LEAF_NODE_SIZE		= 29 + 16 * LEAF_NODE_FAN_OUT [byte]
 * INNER_NODE_SIZE		= 37 + 16 * INNER_NODE_FAN_OUT [byte]
 *
 * MAX_LEAF_NODE_NUMBER	= (M+1)^(LEVEL_NUMBER-1)
 * MAX_BPTREE_SIZE		= MAX_LEAF_NODE_NUMBER * LEAF_NODE_SIZE
 *
 */
public class BptreeCustom{

	private int diskNumber; 
	
	private int levelNumber;
	private int mfo;
	private long leafNodeNumber;
	private long nodeNumber;
	
	/** The disk object used to write and read nodes from disk */
    private Disk<Long, Long> disk;
	
	public BptreeCustom(Disk<Long, Long> disk, int diskNumber) {
		
		this.disk 		= disk;
		this.diskNumber = diskNumber;
		
		levelNumber 	= (int) disk.getDiskSuperBlock().getHeight();
		mfo 			= disk.getDiskSuperBlock().getM(); //M==N
		leafNodeNumber 	= (int)Math.pow((mfo+1), (levelNumber-1));
		
		nodeNumber= 0;
		for( int i = 0 ; i < levelNumber-1 ; i++) {
			nodeNumber += (long)Math.pow((double)(mfo+1), i);
		}
		nodeNumber += leafNodeNumber;
	}
	
	public void create() {
		
		long currentNodeNumber = 0;
		int currentDiskNumber = 0;
		
		INode<Long , Long> root = new INode<Long, Long>(mfo);
		root.num = mfo;	
		for( int i = 0 ; i < mfo ; i ++ ){
				root.setKey(i, (long)((i+1)*( Math.pow((mfo+1), (levelNumber-2))) +1 ));
		}
		
		/** WRITE NEW NODE */
		currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
		disk.writeNode(root, true, currentDiskNumber);
		currentNodeNumber++;
		
		disk.getDiskSuperBlock().setRootPid(root.pid);
		
		LinkedList<INode<Long, Long>> innerNodeList =  new LinkedList<INode<Long, Long>>();
		for( int lev = 0 ; lev < levelNumber - 2 ; lev++) {
			innerNodeList.add(new INode<Long, Long>(mfo));				
		}
		
		
		for( long lnn = 0 ; lnn < leafNodeNumber ; lnn++) {
			
			for( int lev = 0 ; lev < levelNumber-1 ; lev++) {
				
				if( lnn % Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) ) == 0) {
					if(lev != levelNumber-2) {
						
						if(lnn != 0) {
							//Saves node on the disk
							disk.writeNode(innerNodeList.get(lev), false);
						}
						
						INode<Long, Long> in = new INode<Long, Long>(mfo);
						in.num = 0;
						
						/** WRITE NEW NODE */
						currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
						disk.writeNode(in, true, currentDiskNumber);
						currentNodeNumber++;
						
						innerNodeList.set(lev, in);
						
						if(lev < levelNumber-2){
							in.num = mfo;
							for( int i = 0 ; i < mfo ; i ++) {
								in.setKey	(i, (long)
										
												( 
														(
															(lnn / Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) ))
															*
															Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) )
														)
														+
														1
														+
														(
																(i+1) * Math.pow( (mfo+1),((levelNumber-2)-(lev+1)))
														)
												)
											);
							}
						}
						
						//Update parent
						if(lev != 0) {
							innerNodeList.get(lev-1).children[innerNodeList.get(lev-1).getLoc(lnn+1)] = in.pid;
						} else {
							root.children[root.getLoc(lnn+1)] = in.pid;
						}
						
					}
				}
				
				if(lev == levelNumber-2) {
					
					for(int a = 0 ; a < mfo+1 ; a++) {
						
						LNode<Long, Long> ln = new LNode<Long, Long>(mfo, mfo);
						ln.setKey(0, lnn+1);
						ln.setValue(0, lnn+1);
						ln.num = 1;
						
						/** WRITE NEW NODE */
						currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
						disk.writeNode(ln, true, currentDiskNumber);
						currentNodeNumber++;
						
						
						//Update parent
						if(lev != 0) {
							innerNodeList.get(lev-1).children[innerNodeList.get(lev-1).getLoc(lnn+1)] = ln.pid;
							if( a < mfo ) {
								innerNodeList.get(lev-1).setKey(a, lnn+2);
							}
						} else {
							root.children[root.getLoc(lnn+1)] = ln.pid;
						}
						
						if( a < mfo ) {
							lnn++;
						}
						
					}
				
				}
									
			}
			
		}
		
		for( int lev = 0 ; lev < levelNumber - 2 ; lev++) {
			disk.writeNode(innerNodeList.get(lev), false);
		}
		
		disk.writeNode(root, false);
		
	}
	
	/**
	 * Closes the disk associated with the b+ tree
	 */
	public void close(){
		
		disk.close();
		
	}
	
}

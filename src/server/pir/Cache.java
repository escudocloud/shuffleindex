package server.pir;

import java.util.LinkedList;

import base.disk.Disk;

/** 
 * The ServerCache class
 * @author Tommaso
 *
 */
public class Cache {

	/** The disk instance */
	private Disk<Long, String> disk;
	/** The cache's levels list */
	private LinkedList<Level> levels;
	
	/**
	 * The class constructor
	 * 
	 * @param disk
	 * @param diskSuperBlock
	 * @param diskCacheBlock
	 */
	public Cache(Disk<Long, String> disk){
		
		this.disk = disk;
		
		levels = new LinkedList<Level>(); 
		//TODO
		//PROBLEM WITH SERIALIZABLE
		Level level = new Level(1);
		level.addNode(	disk.getDiskCacheBlock().cachePids.get(0), 
						disk.readNodeBytes(disk.getDiskCacheBlock().cachePids.get(0)), 
						disk.getDiskCacheBlock().cacheWeights.get(0) );
		
		levels.add(level);
		
		for( int i = 0 ; i < disk.getDiskSuperBlock().getHeight()-1 ; i++ ) {
			
			level = new Level((int)disk.getDiskSuperBlock().getNumLvlEle());
			
			for( int a = 0 ; a < disk.getDiskSuperBlock().getNumLvlEle() ; a++) {
				
				int index = (1+(i*(int)disk.getDiskSuperBlock().getNumLvlEle()))+a;
				
				level.addNode(	disk.getDiskCacheBlock().cachePids.get(index), 
								disk.readNodeBytes(disk.getDiskCacheBlock().cachePids.get(index)), 
								disk.getDiskCacheBlock().cacheWeights.get(index) );
				
			} 
		
			levels.add(level);
			
		}
		
		
	}
	
	/**
	 * Return the specified level
	 * 
	 * @param index the level (starting from 0)
	 * 
	 * @return the level object
	 */
	public Level getLevel(int index) {
		
		return levels.get(index);
		
	}
	
	/**
	 * Returns the number of levels
	 * 
	 * @return the number of levels
	 */
	public int getLevelNumber() {
		
		return levels.size();
		
	}
	
	/**
	 * Saves all the cache informations to the DiskCacheBlock
	 */
	public void save() {
		
		//Saves the nodes on the disk
		for( int l = 0 ; l < levels.size() ; l++ ) {
			for( int i = 0 ; i < levels.get(l).getSize() ; i++) {
				
				long offset = levels.get(l).pids.get(i) & 0x1FFFFFFFFFFFFFFFL;
		    	int diskNumber = (int)( (levels.get(l).pids.get(i) & 0x6000000000000000L) >> 61 );
				
				disk.writeBytes(levels.get(l).nodes.get(i), offset, diskNumber);
				
			}
		}
		
		//Saves the cache block
		LinkedList<Long> pids = new LinkedList<Long>();
		LinkedList<byte[]> weights = new LinkedList<byte[]>();
		
		for( int i = 0 ; i < levels.size() ; i++ ) {
			pids.addAll(levels.get(i).pids);
			weights.addAll(levels.get(i).weights);
		}
		
		disk.getDiskCacheBlock().cachePids = pids;
		disk.getDiskCacheBlock().cacheWeights = weights;
		
		disk.getDiskCacheBlock().save();
		
	}
	
	public boolean isEmpty() {
		
		if(getLevel(0).pids.get(0) == 0)
			return true;
		
		return false;
		
	}
	
}

/**
 * The level class
 * @author Tommaso
 *
 */
class Level {
	
	/** The max number of element in the level */
	int maxSize;
	/** The cached nodes' pids list */
	LinkedList<Long> pids;
	/** The cached nodes list */
	LinkedList<byte[]> nodes;
	/** The list of weights associated with nodes */
	LinkedList<byte[]> weights;
	
	/**
	 * The class constructor
	 * 
	 * @param levelEleNum the number of element
	 */
	public Level(int levelEleNum) {
		
		maxSize = levelEleNum;
		pids = new LinkedList<Long>();
		nodes = new LinkedList<byte[]>();
		weights = new LinkedList<byte[]>();
		
	}
	
	/**
	 * Adds a new node to the level with the specified weight and pid
	 * 
	 * @param pid the node pid
	 * @param node the byte vector containing the node info
	 * @param weight the node's weight
	 */
	public void addNode( Long pid, byte[] node, byte[] weight ) {
		
		pids.add(pid);
		nodes.add(node);
		weights.add(weight);
		
	}
	
	/**
	 * Sets node to the level with the specified index
	 * 
	 * @param pid the node pid
	 * @param node the byte vector containing the node info
	 * @param weight the node's weight
	 */
	public void setNode( int index, Long pid, byte[] node, byte[] weight ) {
		
		pids.set(index, pid);
		nodes.set(index, node);
		weights.set(index, weight);
		
	}
	
	/**
	 * Returns the number of nodes actually in the level
	 * 
	 * @return the current size
	 */
	public int getSize() {
		
		return pids.size();
		
	}
}
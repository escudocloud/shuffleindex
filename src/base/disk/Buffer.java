package base.disk;

import base.bptree.Node;

/**
 * The buffer class
 * @author Tommaso
 *
 * @param <Key> the bptree key type
 * @param <Value> the bptree value type
 */
public class Buffer<Key extends Comparable<? super Key>, Value> {
	
	/** The disk */
	private Disk<Key, Value> disk;
	/** The bufferd nodes vector */
	private Node<Key, Value> nodes[];
	/** The bufferd nodes' weight vector */
	private int weights[];
	/** The maximum number of node in the buffer */
	private int nodeNumber;
	/** The number of nodes currently in the buffer */
	private int num;
	
	/**
	 * The class constructor
	 * 
	 * @param d the disk
	 * @param h the bptree height
	 */
	@SuppressWarnings("unchecked")
	public Buffer( Disk<Key, Value> d, int h ) {
		
		disk = d;
		num = 0;
		nodeNumber = 4*h;
		
		nodes = new Node[nodeNumber];
		weights = new int[nodeNumber];
		
		for( int i = 0 ; i < nodeNumber ; i++ ) {
			nodes[i] = null;
			weights[i] = 0;
		}		
	}	
	
	/** 
	 * Puts a new node in the buffer, if the buffer is full removes the most used node
	 * 
	 * @param node to add
	 */
	public void put(Node<Key, Value> node) {
		
		if( num < nodeNumber) {
			
			nodes[num] = node;
			weights[num] = 0;
			num++;
			
		} else {
			
			int index = getNodeToRemoveIndex();
			Node<Key, Value> temp = nodes[index];
			nodes[index] = node;
			weights[index] = 0;
			disk.writeNode(temp, false);
			
		}
			
		
	}

	/**
	 * Returns the index of the most used node
	 * 
	 * @return the index of the most used node
	 */
	private int getNodeToRemoveIndex() {
		
		int index = 0;
		int weight = 0;
		
		for( int i = 0 ; i < nodeNumber ; i++ )  {
			if( weights[i] <= weight ){
				weight = weights[i];
				index = i;
			}	
		}
				
		return index;
		
	}
	
	/**
	 * Returns the node with the given pid and update its weight
	 * 
	 * @param pid
	 * 
	 * @return the specified node, returns null if the node isn't in the buffer
	 */
	public Node<Key, Value> get( long pid ) {
		
		for( int i = 0 ; i < nodeNumber ; i++ ){
			if( nodes[i] != null )
				if( nodes[i].pid == pid ) {
					weights[i] = weights[i] - 1;
					return nodes[i];
				}
		}
		
		return null;
		
	}
	
	/**
	 * Checks if a node is in the buffer
	 * 
	 * @param pid
	 * 
	 * @return true if the node is in the buffer, otherwise false
	 */
	public boolean isInBuffer( long pid ) {
		
		for( int i = 0 ; i < nodeNumber ; i++ ){
			if( nodes[i] != null ) {
				if( nodes[i].pid == pid ) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	/**
	 * Empties the buffer and writes all the nodes to the disk
	 */	
	public void emptyBuffer() {
		
		for( int i = 0 ; i < num ; i++ ){
			Node<Key, Value> temp = nodes[i];
			nodes[i] = null;
			disk.writeNode(temp, false);
		}
		
		num = 0;
		
	}
	
}

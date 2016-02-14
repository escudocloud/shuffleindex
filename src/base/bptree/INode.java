package base.bptree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import base.disk.Disk;
import base.disk.DiskSuperBlock;

/**
 * The class implementing the inner node
 * @author Tommaso
 *
 * @param <Key>
 * @param <Value>
 */
public class INode<Key extends Comparable<? super Key>, Value> extends Node<Key, Value> {
	
	/** The maximum number of keys in inner node, the number of pointer is N+1, N must be > 2 */
    public final int N;
	/** The vector containing the children pointers */
	public final long[] children;
 
	/**
	 * The class constructor
	 * 
	 * @param n the maximum number of keys in inner node
	 */
	@SuppressWarnings("unchecked")
	public INode(int n) { 
		super((Disk<Key, Value>)null);
		pid = -1;
		vid = -1;
        N = n;
        num = 0;
        
        keys = (Key[]) new Comparable[N];
        
        for(int i=0 ; i<N; i++)
        	keys[i] = (Key) Long.valueOf(0);
        
        children = new long[N+1];    
        for(int i=0 ; i<N+1; i++)
        	children[i] = Long.valueOf(0);
    }
	
	public INode(INode<Key,Value> a) { 
		super(a);
		this.N = a.N;
		this.children = a.children.clone();
    }
	
	public int lenChildren() {
		return children.length;
	}
	
	/**
	 * The class constructor
	 * 
	 * @param disk the object used to write and read nodes from disk
	 */
	@SuppressWarnings("unchecked")
	public INode(Disk<Key, Value> disk) { 
		super(disk);
		pid = -1;
		vid = -1;
        N = disk.getDiskSuperBlock().getN();
        num = 0;
        
        keys = (Key[]) new Comparable[N];
        for(int i=0 ; i<N; i++)
        	keys[i] = (Key) Long.valueOf(0);
        
        children = new long[N+1];    
        for(int i=0 ; i<N+1; i++)
        	children[i] = Long.valueOf(0);
    }
	
	/**
	 * The class constructor
	 * 
	 * @param d the object used to write and read nodes from disk
	 * @param n the maximum number of keys in inner node
	 * @param nodeBytes the bytes vector containing the node info
	 */
	@SuppressWarnings("unchecked")
	public INode(Disk<Key, Value> d, int n, byte[] nodeBytes) { 
		disk = d;
		N = n;
        keys = (Key[]) new Comparable[N];
        children = new long[N+1];
        
		ByteBuffer bBuffer;
		
		bBuffer = ByteBuffer.wrap(nodeBytes,0,4);
		IntBuffer iBuffer = bBuffer.asIntBuffer();
		num = iBuffer.get();
        
		bBuffer = ByteBuffer.wrap(nodeBytes,4,(2*N+4)*8);
		LongBuffer lBuffer = bBuffer.asLongBuffer();
		
		nonce = lBuffer.get();
		pid = lBuffer.get();
		vid = lBuffer.get();
		
		for(int i = 0 ; i < N; i++) 
			keys[i] = (Key) new Long(lBuffer.get());
		
		for(int i = 0 ; i < N+1; i++) 
			children[i] = lBuffer.get();
		
    }
	
	public void insertForSplit(Key promoted_Key, Node<Key,Value> a) {
		int ins = this.getLoc(promoted_Key);
		for(int i=this.num; i>ins; i--) {
			this.keys[i]=this.keys[i-1];
			this.children[i+1] = this.children[i];
		}	
        this.keys[ins] = promoted_Key;
        this.children[ins+1] = a.getPid();
        this.num += 1;
	}	
	
	public boolean hasChild(Node<Key,Value> c) {
		for (long mmm : this.children) 
			if (c.getPid() == mmm) return true;
		return false;
	}
	
	
	/**
	 * Returns the position where 'key' should be inserted in an inner node
	 * that has the given keys.
	 * 
	 * @param key
	 */
	public int getLoc(Key key) {
	    // Simple linear search. Faster for small values of N or M
	    for (int i = 0; i < num; i++) {
		  if (keys[i].compareTo(key) > 0) 
		    return i;
	    }
	    return num;
	    // Binary search is faster when N or M is big,
	}
	
	public Key getFirstKey() {
		return keys[0];
	}
	
	public long getSubTreePid(Key key) {
	    
	    return this.children[this.getLoc(key)];
	}
	
	
	public Key splitNodeInMemory(ArrayList<Node<Key, Value>> ns, DiskSuperBlock sup) {
		
		int mid = this.num/2;
		Key promoted_value = this.keys[mid];
		int i = 0;
		
		ns.add(new INode<Key, Value>(this.N));
		ns.add(new INode<Key, Value>(this.N));
		
		INode<Key, Value> tref = (INode<Key, Value>) ns.get(0);
		tref.pid = this.pid; tref.vid = this.vid;
		for (i = 0; i<mid; i++) tref.keys[i] = this.keys[i];
		tref.num = i;
		for (i = 0; i<=mid; i++) tref.children[i] = this.children[i];
		
		tref = (INode<Key, Value>) ns.get(1);
		tref.pid = sup.getNewNodeOffsets(0); tref.vid = tref.pid;
		for (i = mid+1; i<num; i++) tref.keys[i-mid-1] = this.keys[i];
		tref.num = num-mid-1;
		for (i = mid+1; i<=num; i++) tref.children[i-mid-1] = this.children[i];
		
		sup.setNewNodeOffset(0, tref.pid+this.getBytes().length);
		
		return promoted_value;
	}
	
	public INode<Key,Value> splitInMemoryRoot(LinkedList<INode<Key,Value>> nn, DiskSuperBlock sup, int numChildren) {
		 int i;
		 INode<Key,Value> newRoot = new INode<Key,Value>(this.N);
		 newRoot.num = 0;
		 newRoot.pid = this.pid;
		 INode<Key,Value> t = null;
		 int num_keys_per_child = (this.keys.length - numChildren-1)/ numChildren;
		 for (i = 0; i < this.N-(this.N % (num_keys_per_child+1)); i+=num_keys_per_child+1) {
			 t = new INode<Key,Value>(this.N);
			 t.pid = sup.getNewNodeOffsets(0)+(i/(num_keys_per_child+1))*this.getBytes().length;
			 t.vid = t.pid;
			 t.changeNonce();
			 System.arraycopy(this.keys, i, t.keys, 0, num_keys_per_child); 
			 System.arraycopy(this.children, i, t.children, 0, num_keys_per_child+1);
			 t.num = num_keys_per_child;
			 newRoot.keys[i/(num_keys_per_child+1)] = this.keys[i+num_keys_per_child];
			 newRoot.children[i/(num_keys_per_child+1)] = t.pid;
			 newRoot.num++;
			 
			 nn.add(t);
		 }
		 long lastPid = t.pid;
		 if (this.N % (num_keys_per_child+1) > 0) {
			 t = new INode<Key,Value>(this.N);
			 t.pid = lastPid+this.getBytes().length;
			 t.vid = t.pid;
			 t.changeNonce();
			 
			 System.arraycopy(this.keys, i, t.keys, 0, this.N % (num_keys_per_child+1)); 
			 System.arraycopy(this.children, i, t.children, 0, (this.N % (num_keys_per_child+1))+1);
			 t.num = this.N % (num_keys_per_child+1);
			 newRoot.children[newRoot.num] = t.pid;
			 nn.add(t);
		 }
		 sup.setNewNodeOffset(0, t.pid+this.getBytes().length); // ger... ci deve essere un solo disco 
		 return newRoot;
	}
	
	
	/**
	 * Early split if node is full.
	 * This is not the canonical algorithm for B+ trees,
	 * but it is simpler and it does break the definition
	 * which might result in immature split, which might not be desired in database
	 * because additional split lead to tree's height increase by 1, thus the number of disk read
	 * so first search to the leaf, and split from bottom up is the correct approach.
	 * 
	 * @param key
	 * @param value
	 */
	public Split<Key, Value> insert(Key key, Value value) {
	    
		if (this.num == N) { // Split
	    	
			int mid = (N+1)/2;
			int sNum = this.num - mid;
			INode<Key, Value> sibling = new INode<Key, Value>(disk);
			sibling.num = sNum;
			System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
			System.arraycopy(this.children, mid, sibling.children, 0, sNum+1);
	 
			this.num = mid-1;//this is important, so the middle one elevate to next depth(height), inner node's key don't repeat itself
	 
			// Set up the return variable
			Split<Key, Value> result = new Split<Key, Value>(this.keys[mid-1],
						 this,
						 sibling);
	 
			// Now insert in the appropriate sibling
			if (key.compareTo(result.key) < 0) {
			    this.insertNonfull(key, value);
			} else {
			    sibling.insertNonfull(key, value);
			}
			
		    //WRITE NODE "sibling"
			disk.writeNode(sibling, true);
		    //WRITE NODE "this"
			disk.writeNode(this, false);
			
			return result;
	 
	    } else {// No split
			this.insertNonfull(key, value);
			
			//WRITE NODE "this"
			disk.writeNode(this, false);
			
			return null;
	    }
	}
 
	/**
	 * Inserts the pair (key, value) into the node which is not full
	 * 
	 * @param key
	 * @param value
	 */
	private void insertNonfull(Key key, Value value) {
	    // Simple linear search
	    int idx = getLoc(key);
	    
	    //READ NODE "children[idx]"   
	    Node<Key, Value> child = disk.readNode(children[idx]);
	    
	    Split<Key, Value> result = child.insert(key, value);
 
	    if (result != null) {
			if (idx == num) {
			    // Insertion at the rightmost key
			    keys[idx] = result.key;
			    children[idx] = result.left.pid;
			    children[idx+1] = result.right.pid;
			    num++;
			} else {
			    // Insertion not at the rightmost key
			    //shift i>idx to the right
			    System.arraycopy(keys, idx, keys, idx+1, num-idx);
			    System.arraycopy(children, idx, children, idx+1, num-idx+1);
	 
			    children[idx] = result.left.pid;
			    children[idx+1] = result.right.pid;
			    
			    keys[idx] = result.key;
			    num++;
			}
	    } // else the current node is not affected
	    
	    /** WRITE NODE "children[idx]" */
	    disk.writeNode(child, false);
	    
	}
	
	/**
	 * Prints all the information about the node
	 */
	public void printInfo(){
		
		System.out.println("Pid:"+ pid + " Vid:" + vid + " Nonce:" + nonce  + " Key_number:" + num);
		
		System.out.print(" Keys: ");
		for(int i=0 ; i < num ; i++) {				
			System.out.print(keys[i] + " ");
		}
		for(int i=num ; i < N ; i++) {				
			System.out.print("- ");
		}
		System.out.print("\n");
		
		System.out.print(" Pointers: ");
		for(int i=0 ; i < num + 1 ; i++) {				
			System.out.print(children[i] + " ");
		}
		for(int i=num+1 ; i < N + 1  ; i++) {				
			System.out.print("- ");
		}
		System.out.print("\n");
		
	}
	
	/**
	 * Print pid, vid and the key list of the node
	 */
	public void print(){
		
		System.out.print("Pid:"+ pid + " Vid:" + vid +" Keys: ");
		for(int i=0 ; i < num ; i++) {				
			System.out.print(keys[i] + " ");
		}
		for(int i=num ; i < N ; i++) {				
			System.out.print("- ");
		}
		System.out.print("\n");
		
	}
	
	/**
	 * Returns the bytes vector containing the node information
	 * 
	 * @return byte[] the bytes vector containing the node information
	 */
	public byte[] getBytes(){
		
		byte[] nodeBytes = null;
    	
		try{
			
		    /** 
			* 1*8B 	: pid
			* 1*8B 	: vid
			* 1*8B 	: nonce
			* N*8B		: keys
			* (N+1)*8B : pointers 
			* 1*4B		: number of keys
			*/
	    		
			nodeBytes = new byte[1+(2*N+4)*8+4];
				
			ByteBuffer bBuffer;
			LongBuffer lBuffer;
			IntBuffer iBuffer;
			
			bBuffer = ByteBuffer.wrap(nodeBytes, 1+4, 3*8);
			lBuffer = bBuffer.asLongBuffer();			
				
			lBuffer.put(nonce);
			lBuffer.put(pid);
			lBuffer.put(vid);
				
			bBuffer = ByteBuffer.wrap(nodeBytes, 1+4+(3*8), N*8);
			lBuffer = bBuffer.asLongBuffer();
			for( int i=0 ; i<num ; i++)
				lBuffer.put(Long.parseLong(keys[i].toString()));
			
			bBuffer = ByteBuffer.wrap(nodeBytes, 1+4+(3+N)*8, (N+1)*8);		
			lBuffer = bBuffer.asLongBuffer();
			for( int i=0 ; i<num+1 ; i++)
				lBuffer.put(children[i]);
			
			bBuffer = ByteBuffer.wrap(nodeBytes, 1, 4);
			iBuffer = bBuffer.asIntBuffer();			
			iBuffer.put(num);
				
			bBuffer = ByteBuffer.wrap(nodeBytes, 0, 1);
			bBuffer.put((byte) 128);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return nodeBytes;
	}
	
	/**
	 * Returns a string containing the xml code that represent the node's information
	 * 
	 * @return a string containing the xml code that represent the node's information
	 */
	public String getXml() {
		
		String nodeString = "<node ";
		
		nodeString = nodeString +  "pid=\"" + pid + "\" " ;
		nodeString = nodeString +  "vid=\"" + vid + "\" " ;
		nodeString = nodeString +  "num=\"" + num + "\" " ;
		
		nodeString = nodeString +  "N=\"" + N + "\" " ;
		
		nodeString = nodeString + "leaf=\"false\" >" ; 
		
		for( int c = 0 ; c < num ; c++ ) {
			nodeString = 	nodeString + "<key>" + keys[c] + "</key>";
		} 
		
		for(int c = 0 ; c < num + 1 ; c++) {		
			nodeString = nodeString + "<child>" + children[c] + "</child>";		
		}
		
		nodeString = nodeString +  "</node>" ;	
		
		return nodeString;
		
	}
	
	/**
	 * Sets the key at the given index
	 * 
	 * @param index of the keys vector
	 * @param k the new key value
	 */
	public void setKey(int index, Key k){
		keys[index]= k;
	}
	
}

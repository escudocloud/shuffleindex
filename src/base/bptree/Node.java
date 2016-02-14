package base.bptree;

import java.util.ArrayList;
import java.util.Random;

import base.disk.Disk;
import base.disk.DiskSuperBlock;


abstract public class Node<Key extends Comparable<? super Key>, Value> {
	
	/** Numbers of key. */
	public int num;
	/** Keys vector */
	public Key[] keys;
	/** Physical IDentifier. */
	public long pid;
	/** Virtual(Content) IDentifier. */
	public long vid;
	
	public long nonce;

    public Disk<Key, Value> disk;
 
    public Node(){
    	
	}
    
    public Node(Disk<Key, Value> disk){
		this.disk = disk;
		pid = 0;
		vid = 0;
		nonce = new Random().nextLong();
	}
	
    public Node(Node<Key,Value> that) {
        this.num = that.num;
        this.keys = that.keys.clone();
        this.pid = that.pid;
        this.vid = that.vid;
        this.nonce = that.nonce;
        this.disk = that.disk;
    }
    
	public void changeNonce() {
		nonce = new Random().nextLong();
	}
	
	public int getKeysSize() {
		return num;
	}
	public long getPid() {
		return pid;
	}
	public boolean isFull() {
		return (num == keys.length);
	}
	abstract public Key splitNodeInMemory(ArrayList<Node<Key, Value>> ns, DiskSuperBlock sup);
	
	abstract public int getLoc(Key key);
	
	/** Returns null if no split, otherwise returns split info */
	abstract public Split<Key, Value> insert(Key key, Value value);
	
	abstract public void printInfo();
	
	abstract public void print();
	
	abstract public byte[] getBytes();
	
	abstract public String getXml();
	
	abstract public void setKey(int index, Key k);
	
	public int lenKeys(){ return keys.length; }
	
	abstract public Key getFirstKey();
	abstract public boolean hasChild(Node<Key,Value> c); 
	
}

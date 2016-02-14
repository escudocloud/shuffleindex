package base.bptree;

import base.disk.Disk;

/**
 * The B+ tree class
 * @author Tommaso
 *
 * @param <Key> the key type
 * @param <Value> the value type
 */
public class Bptree<Key extends Comparable<? super Key>, Value>
{
	
	/** Pointer to the root node. It may be a leaf or an inner node, but it is never null. */
    public Node<Key, Value> root;
    /** The maximum number of keys in the leaf node, M must be > 0 */
    public final int M;
    /** The maximum number of keys in inner node, the number of pointer is N+1, N must be > 2 */
    public final int N;
    /** The disk object used to write and read nodes from disk */
    private Disk<Key, Value> disk;
    
    /**
     * The class constructor
     * 
     * @param disk the object used to write and read nodes from disk
     */
    public Bptree(Disk<Key, Value> disk) {
    	
    	this.disk = disk;
    	this.M = disk.getDiskSuperBlock().getM();
    	this.N = disk.getDiskSuperBlock().getN();
        
        root = new LNode<Key, Value>(disk);
        	
        //WRITE NODE "ROOT"
        disk.writeNode(root, true);
        
    }
 
    /**
     * Inserts a new pair, key and value, into the b+ tree
     * 
     * @param key
     * @param value
     */
    public void insert(Key key, Value value) {
    	
    	//READ NODE "ROOT"
    	root = disk.readNode(disk.getDiskSuperBlock().getRootPid());
		
		Split<Key, Value> result = root.insert(key, value);
		
        if (result != null) {
        	
	    // The old root was splitted in two parts.
	    // We have to create a new root pointing to them
            INode<Key, Value> _root = new INode<Key, Value>(disk);
            _root.num = 1;
            _root.keys[0] = result.key;
            _root.children[0] = result.left.pid;
            _root.children[1] = result.right.pid;
            root = _root;
            
            /** WRITE NODE "ROOT" */
            disk.writeNode(root, true);
            
            disk.getDiskSuperBlock().setRootPid(root.pid);
            
        }
        
    }
 
    /** 
     * Looks for the given key. If it is not found, it returns null.
     * If it is found, it returns the associated value.
     * 
     * @param key to search
     * 
     * @return the value associated with the key or null if the key is not found
     */
    public Value find(Key key) {
    	
    	//READ NODE "ROOT"   	
        Node<Key, Value> node = disk.readNode(disk.getDiskSuperBlock().getRootPid());
        
        while (node instanceof INode<?,?>) { // need to traverse down to the leaf
        	
        	INode<Key, Value> inner = (INode<Key, Value>) node;
            int idx = inner.getLoc(key);
            
            //READ NODE "inner.children[idx]"
            Node<Key, Value> child = disk.readNode(inner.children[idx]);
            
            node = child;
            
        }
 
        //We are @ leaf after while loop
        LNode<Key, Value> leaf = (LNode<Key, Value>) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
        	return leaf.values[idx];
        } else {
        	return null;
        }
        
    }

    /**
	 * Print the B-tree.
	 * 
	 * @param node
	 * @param tab used to indent
	 */
	public void printAll(Node<Key, Value> node, String tab) {
		
		if(node.num != 0) {
			System.out.print(tab);
			node.print();	
		}	
		
		if(node instanceof INode<?,?>) {		
			for(int i=0 ; i < node.num+1 ; i++) {	
				
				//READ NODE "node.children[i]"
				Node<Key, Value> child = disk.readNode(((INode<Key, Value>)node).children[i]);
				
				printAll(child, tab + "    ");
			}
		}
		
	}
	
	/**
	 * Returns the b+ tree root
	 * 
	 * @return the root node
	 */
	public Node<Key, Value> getRoot(){
		return disk.readNode(disk.getDiskSuperBlock().getRootPid());
	}
    
	/**
	 * Closes the disk associated with the b+ tree
	 */
	public void close(){
		
		disk.close();
		
	}
	
}

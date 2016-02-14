package client.pir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import base.bptree.INode;
import base.bptree.LNode;
import base.bptree.Node;

import base.disk.DiskSuperBlock;

/** 
 * The ClientSideCache class
 * @author Tommaso
 *
 */
public class ClientSideCache {
	
	/** The cache's levels list */
	private LinkedList<ServerSideCacheLevel> levels;
	/** The cache creation date */
	private long creationDate;
	/** Path of the file where is saved the cache */
	private static final String CACHE_FILE = "cache"  + File.separatorChar + "cache.xml";
	
	/**
	 * The class constructor
	 * 
	 * @param levelNum the number of cache levels
	 * @param levelEleNum the number of element in every level
	 */
	public ClientSideCache( long levelNum , long levelEleNum ) {
		
		creationDate = System.nanoTime();
		levels = new LinkedList<ServerSideCacheLevel>();
		
		for ( int i = 0 ; i < levelNum ; i++ ) {
			levels.add( new ServerSideCacheLevel((int)levelEleNum) );
		}
		//System.err.println("CLASS ClientSideCache USED");
	}
	
	/**
	 * The class constructor
	 * 
	 * @param levelNum the number of cache levels
	 * @param levelEleNum the number of element in every level
	 * @param load if true load the cache from disk
	 */
	public ClientSideCache( long levelNum , long levelEleNum, boolean load) {
			
		load(levelNum, levelEleNum);
			
	}
	
	/**
	 * Return the specified level
	 * 
	 * @param (int)(level the level
	 * 
	 * @return the level object
	 */
	public ServerSideCacheLevel getLevel( long level ) {
		
		return levels.get((int)(level - 1));
		
	}
	
	/**
	 * Returns the number of levels
	 * 
	 * @return the number of levels
	 */
	public int getLevelNumber() {
		
		return levels.size();
		
	}
	
	public void pushLevel(INode<Long,Long> newroot, long weight) {
		this.levels.add(0, new ServerSideCacheLevel(levels.getFirst().getMaxSize(), newroot, weight));
	}
	

	/** 
	 * Saves the cache on the disk
	 */
	public void save() {
		
		try{
			
			FileWriter fw = new FileWriter(CACHE_FILE);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
			bw.write("<cache creationDate=\"" + creationDate + "\">");
			
			for( int i = 0 ; i < levels.size() ; i++ ) {
				
				bw.write("<level number=\"" + (i+1) + "\" element=\"" + levels.get(i).getSize() +"\">");
				
				for( int a = 0 ; a < levels.get(i).getSize() ; a++ ) {
					
					bw.write(levels.get(i).nodes.get(a).getXml());
					bw.write("<weight value=\"" + levels.get(i).weights.get(a) + "\"/>");
					
				} 
				bw.write("</level>");
			}
			
			bw.write("</cache>");
						
			bw.close();
			fw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Loads cache from disk
	 * 
	 * @param levelNum
	 * @param levelEleNum
	 * 
	 */
	public void load( long levelNum , long levelEleNum) {

		try {
			
	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (new File(CACHE_FILE));
            
	        NamedNodeMap nnmAttributes;
	        
	        levels = new LinkedList<ServerSideCacheLevel>();
	        
	        for(int i = 0 ; i < levelNum ; i++) {
	        
	        	//Loads the cache creation date
	        	nnmAttributes = doc.getElementsByTagName("cache").item(0).getAttributes();
	        	creationDate = Long.valueOf(nnmAttributes.getNamedItem("creationDate").getNodeValue());
	        	
	        	NodeList elementList = doc.getElementsByTagName("level").item(i).getChildNodes();	
	        	
	        	ServerSideCacheLevel level = new ServerSideCacheLevel((int)levelEleNum);
	        	
	        	for(int a = 0 ; a < elementList.getLength() ; a++) {
	        		
	        		Node<Long, Long> node;
	        		long weight;
	        		
	        		//Reads node
	        		nnmAttributes = elementList.item(a).getAttributes();
	        		NodeList nodeList = elementList.item(a).getChildNodes();
	        		if(Boolean.parseBoolean(nnmAttributes.getNamedItem("leaf").getNodeValue())) {
	        			node = new LNode<Long, Long>(Integer.valueOf(nnmAttributes.getNamedItem("M").getNodeValue()),Integer.valueOf(nnmAttributes.getNamedItem("N").getNodeValue()));
	        			node.num = Integer.valueOf(nnmAttributes.getNamedItem("num").getNodeValue());
	        			for(int v = 0 ; v < node.num ; v++) {
	        				((LNode<Long, Long>)node).setValue(v, Long.valueOf(nodeList.item(v+node.num).getTextContent()));
	        			}
	        		} else {
	        			node = new INode<Long, Long>(Integer.valueOf(nnmAttributes.getNamedItem("N").getNodeValue()));
	        			node.num = Integer.valueOf(nnmAttributes.getNamedItem("num").getNodeValue());
	        			for(int v = 0 ; v < node.num+1 ; v++) {
	        				((INode<Long, Long>)node).children[v] = Long.valueOf(nodeList.item(v+node.num).getTextContent());
	        			}
	        		}
        			node.pid = Long.valueOf(nnmAttributes.getNamedItem("pid").getNodeValue());
        			node.vid = Long.valueOf(nnmAttributes.getNamedItem("vid").getNodeValue());
        			for(int v = 0 ; v < node.num ; v++) {
        				node.setKey(v,Long.valueOf(nodeList.item(v).getTextContent()));
        			}
        				
	        		//Reads weight
	        		a++;
	        		nnmAttributes = elementList.item(a).getAttributes();
	        		weight = Long.valueOf(nnmAttributes.getNamedItem("value").getNodeValue());
	        		
	        		level.addNode(node, weight);
	        	}
	        	
	        	levels.add(level);
	        	
	        }
		
		}catch (Exception e) {
        	e.printStackTrace();
        }
        
	}
	
	/**
	 * Checks if the cache exists on file
	 * 
	 * @return true if the cache exists on file, false otherwise
	 */
	public static boolean exist() {
		
		File cache = new File(CACHE_FILE);
		return cache.exists();
		
	} 
	
	/**
	 * Delete the cache saved on the file
	 */
	public static void delete() {
		File cache = new File(CACHE_FILE);
		if(cache.exists())
			cache.delete();
	}
	
	/**
	 * Print the nodes and their weights stored into the cache
	 */
	public void printInfo(){
		
		for( int i = 0 ; i < levels.size() ; i++ ) {
			for( int c = 0 ; c < levels.get(i).getSize() ; c++) {
				levels.get(i).nodes.get(c).printInfo();
				System.out.println(levels.get(i).weights.get(c));
			}
		}
		
	}
	
	public long getCreationDate() {
		
		return creationDate;
		
	}
	
}


/**
 * The ServerSideCacheLevel class
 * @author Tommaso
 *
 */
class ServerSideCacheLevel {
	
	/** The max number of element in the level */
	int maxSize;
	/** The cached nodes list */
	LinkedList<Node<Long,Long>> nodes;
	/** The list of weights associated with nodes */
	LinkedList<Long> weights;
	
	/**
	 * The class constructor
	 * 
	 * @param levelEleNum the number of element
	 */
	public ServerSideCacheLevel(int levelEleNum) {
		
		maxSize = levelEleNum;
		nodes = new LinkedList<Node<Long,Long>>();
		weights = new LinkedList<Long>();
		//System.err.println("CLASS ServerSideCacheLevel USED");
		
	}
	
	public ServerSideCacheLevel(int levelEleNum, Node<Long,Long> n, Long w ) {
		this(levelEleNum);
		nodes.add(n);
		weights.add(w);
	}
	
	/** 
	 * Update the weight of all the nodes in the level (weight = weight -1)
	 */
	public void updateWeights() {
		
		for( int i = 0 ; i < weights.size() ; i++) {
			weights.set(i, weights.get(i) - 1);			
		}
		
	}
	
	/**
	 * Update the weight of the specified node
	 * 
	 * @param pid
	 */
	public void updateWeight( long pid , long weight) {
		
		for( int i = 0 ; i < nodes.size() ; i++ ) {
			if( nodes.get(i).pid == pid )
				weights.set(i, weight);
		}
		
	}
	
	/**
	 * Returns the list of the cached nodes' pid
	 * @return
	 */
	public LinkedList<Long> getPids() {
		
		LinkedList<Long> pids = new LinkedList<Long>();
		
		for( Node<Long,Long> n : nodes )
			pids.add(n.pid);
		
		return pids;
		
	}
	
	/**
	 * Returns the number of nodes actually in the level
	 * 
	 * @return the current size
	 */
	public int getSize() {
		
		return nodes.size();
		
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public LinkedList<Node<Long,Long>> getNodeList() {
	    return  nodes;
	}
	public LinkedList<Long> getWeightsList() {
	   return  weights;
	}
	
	/**
	 * Adds a new node to the level with the specified weight
	 * 
	 * @param node
	 * @param weight
	 */
	public void addNode( Node<Long,Long> node, long weight ) {
		
		nodes.add(node);
		weights.add(weight);
		
	}	
	
	public long removeNode(long pid) {
		int index;
		for (index = 0; index < nodes.size(); index++)  
			if (nodes.get(index).getPid() == pid) break;
		nodes.remove(index);
		return weights.remove(index);
	}
	
	/**
	 * Adds a list of new nodes to the level with the specified weight, that is the same for all the nodes
	 * 
	 * @param toAdd the list of nodes
	 * @param weight
	 */
	public void addNodes( LinkedList<Node<Long,Long>> toAdd, long weight ) {
		
		for( int i = 0 ; i < toAdd.size() ; i++  )
			addNode(toAdd.get(i), weight);
		
	}
	
	/**
	 * Add a new nodes to the level with the specified weight, in the specified position: index 
	 * 
	 * @param toAdd the list of nodes
	 * @param weight
	 */
	public void addNode(int index, Node<Long,Long> toAdd, long weight ) {	
		nodes.add(index, toAdd);
		weights.add(index, weight);
	}
	
	
	/** 
	 * Checks if exist a node in the level with the given pid
	 * 
	 * @param pid
	 * 
	 * @return true if the node exist otherwise false
	 */
	public boolean contains( long pid ) {
		
		for( Node<Long,Long> n : nodes ) {
			if( n.pid == pid )
				return true;
		}
					
		return false;
		
	}
	
	public boolean contains( long[] pids ) {
		
		for( long n : pids ) {
			if (this.contains(n))
				return true;
		}
					
		return false;
		
	}

	
	
	
	/**
	 * Returns and removes from the level the specified node and his weight
	 *  
	 * @param pid
	 * 
	 * @return the removed node
	 */
	public Node<Long,Long> getAndRemove( long pid ) {
		
		Node<Long,Long> node = null;
		
		for( int i = 0 ; i < nodes.size() ; i++) {
			if( nodes.get(i).pid == pid ) {
				node = nodes.get(i);
				nodes.remove(i);
				weights.remove(i);
			}
		}
		
		return node;
		
	}
	
	/**
	 * Returns and removes from the level the oldest node
	 *  
	 * @param maxValue maximum weight that a node can have
	 * 
	 * @return the removed node
	 */
	public Node<Long,Long> getAndRemoveOldestNode( long maxValue ) {
		
		Node<Long,Long> node = null;
		
		long min = maxValue;
		int index = 0;
		
		for( int i = 0 ; i < weights.size() ; i++) {
			if( weights.get(i) < min ) {
				min = weights.get(i);
				index = i;
			}
		}
		
		node = nodes.get(index);
		nodes.remove(index);
		weights.remove(index);
		
		return node;
		
	}
	
	/**
	 * Returns from the level the specified node
	 *  
	 * @param pid
	 */
	public Node<Long,Long> get( long pid ) {
		
		for( int i = 0 ; i < nodes.size() ; i++) {
			if( nodes.get(i).pid == pid ) {
				return nodes.get(i);
			}
		}
		
		return null;
		
	}
	
	/**
	 * Update all the nodes' pid using the given associative array
	 * 
	 * @param associativeArray
	 */
	public void updatePids(HashMap<Long, Long> associativeArray) {
		
		for( int i = 0 ; i < nodes.size() ; i++) {
			if( associativeArray.containsKey(nodes.get(i).pid) ) {
				nodes.get(i).pid = associativeArray.get(nodes.get(i).pid);
			}
		}
		
	}
	
	/**
     * Updates all the nodes' pointers with the given associative array
     * 
     * @param nodeList
     * @param associativeArray
     */
    public void updatePointers(HashMap<Long, Long> associativeArray) {

    	for( int n = 0 ; n < nodes.size() ; n++ ) {
    		for( int i = 0 ; i < ((INode<Long,Long>)nodes.get(n)).num+1 ; i++ ) {
        		if(associativeArray.containsKey(((INode<Long,Long>)nodes.get(n)).children[i])) {
        			((INode<Long,Long>)nodes.get(n)).children[i] = associativeArray.get(((INode<Long,Long>)nodes.get(n)).children[i]);
        		} 		
        	}
    	}
    	
    }
	
	/**
	 * Update the cache replacing with the given node the one with the minimum weight 
	 * 
	 * @param node to add to the level
	 */
	public void replaceOldestNode( Node<Long,Long> node) {
		
		long min = maxSize;
		int index = 0;
		
		for( int i = 0 ; i < weights.size() ; i++) {
			if( weights.get(i) < min ) {
				min = weights.get(i);
				index = i;
			}
		}
		
		nodes.set(index, node);
		weights.set(index, (long)maxSize);
		
	}
	
	
	

}


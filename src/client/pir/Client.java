package client.pir;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;


import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import base.bptree.INode;
import base.bptree.LNode;
import base.bptree.Node;
import base.communication.Protocol;
import base.conversion.Conversion;
import base.crypto.Crypto;
import base.disk.DiskConfFile;
import base.disk.DiskSuperBlock;
import base.log.Log;
import client.test.EsteemedAccessProfile;
import client.test.NodeCoverageItem;
import client.test.NodeIdLog;

/**
 * The client class
 * @author Tommaso
 * 
 */
public class Client {

	private static final double FILL_FACTOR = 0.6;  
	private static final double SPLIT_ALPHA_FACTOR = 1.0;
	/** Logger for this class */
	private static final Logger logger = Logger.getLogger(Client.class);	
	/** The client configuration file path */
	private final static String CLIENT_CONF_FILE = "conf"  + File.separator + "config.xml";
	
	private final static String CONF_FILE = "disk" + File.separator + "disk_conf.xml"; 	
	/** Object stream */
    private ObjectInputStream ois = null;
    /** Object stream */
    private ObjectOutputStream oos = null;   
    /** The socket */
    private Socket socket = null;	
    /** The superblock sent by the server */
    private DiskSuperBlock superBlock = null;
    /** The local cache */
    public ClientSideCache clientSideCache = null;

    
    
    /** The class constructor */
	public Client() {

		try{
			logger.addAppender( new FileAppender(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"), Log.CLIENT_LOG_FILE_PATH) );
		}catch(Exception e){ e.printStackTrace(); }

		logger.setLevel(Log.CLIENT_LOG_LEVEL);
		
	}
	
	/**
	 * Opens a socket connection with the server
	 */
	public void openConnection() {
				
		try {
			
			socket = new Socket( ClientConfigFile.getInstance(CLIENT_CONF_FILE).getServerAddress(),
				ClientConfigFile.getInstance(CLIENT_CONF_FILE).getSocketPort() );
			socket.setTcpNoDelay(true);
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		
		}catch(Exception e){
			e.printStackTrace();
		}
			
	}
	
	/**
	 * Closes the socket connection
	 */
	public void closeConnection() {
		
		try {

			ois.close();
			oos.close();
			socket.close();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		socket = null;
		
	}
	
	/**
	 * Gets the superblock, if doesn't exists downloads it from the server
	 * 
	 * @return the superblock
	 */
	public DiskSuperBlock getSuperBlock() {
		
		if( superBlock == null )
			downloadSuperBlock();
		
		return superBlock;
		
	}
		
	/**
	 * Downloads the superblock from the server
	 */
	private void downloadSuperBlock() {
		
		if(socket != null) {
		
			try {
				
				oos.writeObject(Protocol.GET_SUPERBLOCK_CODE);
				oos.flush();	
				
				superBlock = Conversion.stringToSuperBlock((String)ois.readObject());
				
				oos.reset();
				
			}catch(ConnectException ce){
				ce.printStackTrace();
		
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
			catch(NullPointerException npe){
				npe.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		} else {
			
			System.err.println("Connection with the server not opened, impossible download the super block");
			System.exit(0);
			
		}
	}
	
	
	/**
	 * Downloads the nodes from the server
	 * 
	 * @param pids the pids list of the nodes to retrieve
	 * 
	 * @return the node list, the list is empty if no node is find. The null value is returned if occur an error.
	 */
	public  LinkedList<Node<Long,Long>> getNodes(LinkedList<Long> pids) {
		
		if(socket != null) {
			// long s1,e1,v1;

			LinkedList<Node<Long,Long>> nodes = new LinkedList<Node<Long,Long>>();
			
			String request = Protocol.GET_NODES_CODE + " ";
			
			for( long p : pids)
				request = request + p + " ";
			
			try {
				
				oos.writeObject(request);
				oos.flush();		
				
				socket.getOutputStream().flush();

				
				for( int i = 0 ; i < pids.size() ; i++ ) {
				
					//s1 = System.currentTimeMillis();
					String node = (String)getObject();
					//e1 = System.currentTimeMillis();
					//v1 = e1-s1;
					//System.err.println(" String node = (String)getObject();: "+v1);
					
					if(DiskConfFile.getInstance(CONF_FILE).isEncrypted()) {
						
						byte[] recieved = Conversion.stringToNodeBytes(node);
						//System.err.println(" length = "+recieved.length+" Received bytes ... node = "+recieved[0]+" "+recieved[1]+"..."+recieved[15]);
						byte[] encrypted = new byte[recieved.length -1];
						System.arraycopy(recieved, 1, encrypted, 0, recieved.length -1);
						byte[] decrypted = Crypto.decryptBytes(Crypto.loadKey(), encrypted);
						byte[] toInstance = new byte[decrypted.length+1];
						toInstance[0] = recieved[0];
		    			System.arraycopy(decrypted, 0, toInstance, 1, decrypted.length);
						node = Conversion.nodeBytesToString(toInstance);
						
					}
					int m = superBlock.getM();
					int n = superBlock.getN();
					nodes.add(Conversion.stringToNode(node, m, n));
					
				}
				
			}catch(ConnectException ce){
				ce.printStackTrace();
				return new LinkedList<Node<Long,Long>>();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
				return new LinkedList<Node<Long,Long>>();
			}
			catch(NullPointerException npe){
				npe.printStackTrace();
				return new LinkedList<Node<Long,Long>>();
			}
			catch(Exception e){
				e.printStackTrace();
				return new LinkedList<Node<Long,Long>>();	
			}
				
			return nodes;
			
		} else {

			System.err.println("Connection with the server not opened, impossible download the nodes");
			System.exit(0);
			
		}
		
		return null;
	}
    
	/**
	 * Sends to the server a list of nodes to store them.
	 * 
	 * @param nodes to store on the server
	 * 
	 * @throws Exception if an error occur and the store can't be performed
	 */
	private void storeNodes( LinkedList<Node<Long,Long>> nodes ) throws Exception {
		
		if( socket != null ) {

			oos.writeObject( Protocol.STORE_NODES_CODE + " " + nodes.size() );
			oos.flush();
						
			for( int i = 0 ; i < nodes.size() ; i++ ) {
				
				//Changes the nonce before encrypt the node
				nodes.get(i).changeNonce();
				byte[] nodeBytes = nodes.get(i).getBytes();
				
				if(DiskConfFile.getInstance(CONF_FILE).isEncrypted()) {
		    		//System.err.println(" node w/ pid = "+nodes.get(i).getPid());
		    		
					byte[] toEncrypt = new byte[nodeBytes.length -1];
		    		System.arraycopy(nodeBytes, 1, toEncrypt, 0, nodeBytes.length-1);
		    		byte[] encrypted = Crypto.encryptBytes(Crypto.loadKey(), toEncrypt);
		    		
		    		byte[] toStore = new byte[encrypted.length+1];
		    		toStore[0] = nodeBytes[0];
		    		System.arraycopy(encrypted,0,toStore,1,encrypted.length);
		    		//System.err.println("length = "+toStore.length+" byte[] to be stored =  "+toStore[0]+" "+toStore[1]+"..."+toStore[15]);
		    		nodeBytes = toStore;
		    		
		    	}
				
				String storeRequest = nodes.get(i).pid + " " + Conversion.nodeBytesToString(nodeBytes);
					
				oos.writeObject(storeRequest);
				oos.flush();	
				
			}
			
			oos.reset();
				
			if (!((String)getObject()).equals(Protocol.CONFIRM_NODES_STORE))
				throw new Exception("Confirm message not recieved");
		
		} else {
			
			System.err.println("Connection with the server not opened, impossible send the nodes");
			System.exit(0);
			
		}
	}
	
    private void storeSuperBlock() throws Exception {
		
		if( socket != null ) {
		
			oos.writeObject(Protocol.SAVE_SB_CODE+" "+superBlock.getHeight()+" "+
			                                          superBlock.getNewNodeOffsets(0)+" "+
			                                          superBlock.getNewNodeOffsets(1)+" "+
			                                          superBlock.getNewNodeOffsets(2)+" "+
			                                          superBlock.getNewNodeOffsets(3)+" "+
			                                          superBlock.getKeyNum()
					       );
			oos.flush();
			
			oos.reset();
			
			if (!((String)getObject()).equals(Protocol.CONFIRM_SB_STORE))
				throw new Exception("Confirm message not received");
			
		} else {
			
			System.err.println("Connection with the server not opened, impossible send the nodes");
			System.exit(0);
			
		}
	}

	
	/**
	 * Reads an object on the ObjectInputStream 
	 * 
	 * @return the read object
	 * 
	 * throws Exception is the reading fail
	 */
    private Object getObject() throws Exception {
    	Object obj = null;
    	try{
            //Checks that the connection is established  and ready      	
            if (isConnected() == false){ throw new IOException(); }
            obj = ois.readObject();
        }
        catch(Exception e){ throw e; }
        return obj;
    }
		
    /**
     * Returns true if the connection is working
     * 
     * @return true if all is correctly working
     */
    private boolean isConnected(){
    	
        return ((!socket.isClosed()) && (socket.isBound()));
        
    }

    /**
     * Returns a fake target key which has a target node different from tid
     * 
     * @param node
     * @param tid the target node pid
     * 
     * @return the fake target key
     */
    private long searchId( Node<Long,Long> node, long tid ) {   	
    	
    	long index = (long)(superBlock.getKeyNum()*Math.random()) + 1;
    	
    	while( ((INode<Long,Long>)node).children[node.getLoc(index)] == tid ||
    			clientSideCache.getLevel(2).contains(((INode<Long,Long>)node).children[node.getLoc(index)]))
    		
    		index = (long)(superBlock.getKeyNum()*Math.random()) + 1;
    	
    	return index;
    	
    }
    
    /**
     * Returns a fake target key which has a target node different from tid and fid1
     * 
     * @param node
     * @param tid the target node pid
     * @param fid1 the first fake node pid
     * 
     * @return the fake target key
     */
    private long searchId( Node<Long,Long> node, long tid, long fid1 ) {
    	
    	long index = (long)(superBlock.getKeyNum()*Math.random()) + 1;
    	
    	while(	((INode<Long,Long>)node).children[node.getLoc(index)] == tid ||
    			((INode<Long,Long>)node).children[node.getLoc(index)] == fid1 ||
    			clientSideCache.getLevel(2).contains(((INode<Long,Long>)node).children[node.getLoc(index)]) )
    		
    		index = (long)(superBlock.getKeyNum()*Math.random()) + 1;
    	
    	return index;
    	
    }
    
    /**
     * Puts all the given nodes in a unique list and call the method to store it on the server
     * 
     * @param tparent the target's parent pid
     * @param CParents the cache nodes pids
     * @param FParents the fake nodes pids
     */
    private void storeNodes(Node<Long,Long> tparent, LinkedList<Node<Long,Long>> CParents, LinkedList<Node<Long,Long>> FParents ) {
    	
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
    	toStore.add(tparent);
    	toStore.addAll(CParents);
    	toStore.addAll(FParents);
    	
    	try{
    		storeNodes(toStore);
    	}catch(Exception e){
    		e.printStackTrace();
    		System.err.println(e.getMessage());
    	}
    	
    }
    
    /**
     * Calls the function to download nodes from the server for just one node
     * 
     * @param pid of the node
     * 
     * @return the downloaded node
     */
    private Node<Long,Long> getNode( long pid ) {
    	
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	LinkedList<Long> pids = new LinkedList<Long>();
    	pids.add(pid);
    	return getNodes( pids ).get(0);
    	
    }
       
    /**
     * Retrieves a pid among the node children' s pid, this pid would be used to fill the cache
     * 
     * @param node
     * 
     * @return the retrieved pid
     */
    private long searchCacheId(Node<Long,Long> node) {
    	
    	return ((INode<Long,Long>)node).children[0];
    	
    }
    
    /**
     * Retrieves the given number of pids among the node children' s pid other than tid and fid1, 
     * these pids would be used to fill the cache
     * 
     * @param node
     * @param tid the target node pid
     * @param fid1 the first fake node pid
     * @param num of pids to retrieve
     * 
     * @return the list of pids
     */
    private LinkedList<Long> searchCacheIds( Node<Long,Long> node, long tid, long fid1, int num ) {
    	
    	LinkedList<Long> pids = new LinkedList<Long>();
    	
    	for( int i = 0 ; i < node.num+1 ; i++) {
    		if(	((INode<Long,Long>)node).children[i] != tid &&
    			((INode<Long,Long>)node).children[i] != fid1 ) {
    			
    			pids.add(((INode<Long,Long>)node).children[i]);
    			
    		}
    	}
    	
    	LinkedList<Long> results = new LinkedList<Long>();
    	results.addAll(pids.subList(0, num));
    	
    	return results;
    }
    
    /**
     * Returns the node contained in the list with the given pid
     * 
     * @param nodes the node list
     * @param pid 
     * 
     * @return the node with the given pid
     */
    private Node<Long,Long> extractNode( LinkedList<Node<Long,Long>> nodes, long pid ) {
    	
    	Node<Long,Long> extractedNode = null;
    	for( int i = 0 ; i < nodes.size() ; i++) {
    		if( nodes.get(i).pid == pid)
    			extractedNode = nodes.get(i);
    	}
    	return extractedNode;
    	
    }
    
    /**
     * Returns the node list containing all the nodes with the pid specified in the pids list
     * 
     * @param nodes the node list to filter
     * @param pids of the nodes to extract from the nodes list
     * 
     * @return the list with the filtered nodes
     */
    private LinkedList<Node<Long,Long>> extractNodes( LinkedList<Node<Long,Long>> nodes, LinkedList<Long> pids ) {
    	
    	LinkedList<Node<Long,Long>> extractedNodes = new LinkedList<Node<Long,Long>>();
	    for( int a = 0 ; a < pids.size() ; a++ ) {
    		for( int i = 0 ; i < nodes.size() ; i++) {
	    		if( nodes.get(i).pid == pids.get(a)) {
	    			
	    			extractedNodes.add(nodes.get(i));
	    			break;
	    			
	    		}
	    	}
	    }
    	return extractedNodes;
    	
    }
    
    /**
     * Creates an associative array where the keys are in IdSet and the associated objects are the result of the IdSet shuffle
     * 
     * @param IdSet
     * 
     * @return an HashMap object that must be used to update all the nodes and parents
     */
    @SuppressWarnings("unchecked")
	private HashMap<Long, Long> createAssociativeArray(LinkedList<Long> IdSet) {
    	
    	HashMap<Long, Long> associativeArray = new HashMap<Long, Long>();

		//Shuffle contents
		LinkedList<Long> ShuffledIdSet = (LinkedList<Long>)IdSet.clone();

		for( int i = 0 ; i < 3 ; i++ ) {
			Collections.shuffle(ShuffledIdSet, new Random());
		}
		
		for( int i = 0 ; i < IdSet.size() ; i++ ) {
			associativeArray.put(IdSet.get(i), ShuffledIdSet.get(i));
		}
		
		return associativeArray;
		
    }
    
    /**
     * Updates all the nodes' pid with the given associative array
     * 
     * @param tnode the target node
     * @param CNodes the cache nodes
     * @param FNodes the fakes node
     * @param associativeArray
     */
    private void updateIds(Node<Long,Long> tnode, LinkedList<Node<Long,Long>> CNodes, LinkedList<Node<Long,Long>> FNodes, HashMap<Long, Long> associativeArray) {
    	
    	tnode.pid = associativeArray.get(tnode.pid);
    	
    	for( int i = 0 ; i < CNodes.size() ; i++) {
    		CNodes.get(i).pid = associativeArray.get(CNodes.get(i).pid);
    	}
    	
    	for( int i = 0 ; i < FNodes.size() ; i++) {
    		FNodes.get(i).pid = associativeArray.get(FNodes.get(i).pid);
    	}

    }
    
    /**
     * Updates all the nodes' pid with the given associative array
     * 
     * @param nodeList
     * @param associativeArray
     */
    private void updateIds(LinkedList<Node<Long,Long>> nodeList, HashMap<Long, Long> associativeArray) {
    	
    	for( int i = 0 ; i < nodeList.size() ; i++) {
    		nodeList.get(i).pid = associativeArray.get(nodeList.get(i).pid);
    	}

    }
    
    /**
     * Updates all the nodes' pointers with the given associative array
     * 
     * @param tparent the target node's parent
     * @param CParents the cache nodes' parents
     * @param FParents the fakes nodes' parents
     * @param associativeArray
     */
    private void updatePointers(Node<Long,Long> tparent, LinkedList<Node<Long,Long>> CParents, LinkedList<Node<Long,Long>> FParents, HashMap<Long, Long> associativeArray) {

    	for( int i = 0 ; i < ((INode<Long,Long>)tparent).num+1 ; i++ ) {
    		if(associativeArray.containsKey(((INode<Long,Long>)tparent).children[i])) {
    			((INode<Long,Long>)tparent).children[i] = associativeArray.get(((INode<Long,Long>)tparent).children[i]);
    		} 		
    	}
    	
    	for( int n = 0 ; n < CParents.size() ; n++ ) {
    		for( int i = 0 ; i < ((INode<Long,Long>)CParents.get(n)).num+1 ; i++ ) {
        		if(associativeArray.containsKey(((INode<Long,Long>)CParents.get(n)).children[i])) {
        			((INode<Long,Long>)CParents.get(n)).children[i] = associativeArray.get(((INode<Long,Long>)CParents.get(n)).children[i]);
        		} 		
        	}
    	}
    	
    	for( int n = 0 ; n < FParents.size() ; n++ ) {
    		for( int i = 0 ; i < ((INode<Long,Long>)FParents.get(n)).num+1 ; i++ ) {
        		if(associativeArray.containsKey(((INode<Long,Long>)FParents.get(n)).children[i])) {
        			((INode<Long,Long>)FParents.get(n)).children[i] = associativeArray.get(((INode<Long,Long>)FParents.get(n)).children[i]);
        		} 		
        	}
    	}
    	
    }
    
    /**
     * Updates all the nodes' pointers with the given associative array
     * 
     * @param nodeList
     * @param associativeArray
     */
    private void updatePointers(LinkedList<Node<Long,Long>> nodeList, HashMap<Long, Long> associativeArray) {

    	for( int n = 0 ; n < nodeList.size() ; n++ ) {
    		for( int i = 0 ; i < ((INode<Long,Long>)nodeList.get(n)).num+1 ; i++ ) {
        		if(associativeArray.containsKey(((INode<Long,Long>)nodeList.get(n)).children[i])) {
        			((INode<Long,Long>)nodeList.get(n)).children[i] = associativeArray.get(((INode<Long,Long>)nodeList.get(n)).children[i]);
        		} 		
        	}
    	}
    	
    }
    
    /**
     * Retrieves the value associated at the given key using the private information retrieval algorithm
     * 
     * @param target key
     * 
     * @return the value associated at the target key
     */
    public long pirCSC( long target ) {
    	
    	//Tuple associated with target
    	long tuple = -1L;
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	//Init the cache
    	if( clientSideCache == null )
    		clientSideCache = new ClientSideCache( superBlock.getHeight(), superBlock.getNumLvlEle() );
    	 	
    	//Retrieve root node
    	Node<Long,Long> root;
    	if(clientSideCache.getLevel(1).contains(superBlock.getRootPid())){
    		root = clientSideCache.getLevel(1).get(superBlock.getRootPid());
    	} else {
    		root = getNode(superBlock.getRootPid());
    		clientSideCache.getLevel(1).addNode(root, superBlock.getNumLvlEle()-1);
    	}
    	
    	//Finds the root children target
    	long tid = ((INode<Long,Long>)root).children[root.getLoc(target)];
    	   	
    	//Inits fakes
    	long fake1 = searchId(root, tid);
    	long fid1 = ((INode<Long,Long>)root).children[root.getLoc(fake1)];
    	long fake2 = searchId(root, tid, fid1);
    	long fid2 = ((INode<Long,Long>)root).children[root.getLoc(fake2)];
    	
    	Node<Long,Long> tparent = root;
    	LinkedList<Node<Long,Long>> CParents = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> FParents = new LinkedList<Node<Long,Long>>();
    	int l = 2;
    	
    	while( l <= superBlock.getHeight() ) {
    		
    		//Inits fake id list
    		LinkedList<Long> Fid = new LinkedList<Long>();
    		Fid.add(fid1);
    		
    		LinkedList<Node<Long,Long>> nodes = new LinkedList<Node<Long,Long>>();
    		
    		if( clientSideCache.getLevel(l).getSize() == 0 ) {
    			if( l == 2 ) {
    				LinkedList<Node<Long,Long>> cachedNodes = getNodes(searchCacheIds( root, tid, fid1, (int)superBlock.getNumLvlEle()));
    				for( int i = 0; i < cachedNodes.size() ; i++ )
    					clientSideCache.getLevel(l).addNode(cachedNodes.get(i), i);
    			} else { //l>2
    				for( int i = 0 ; i < CParents.size() ; i++) {
    					clientSideCache.getLevel(l).addNode(getNode(searchCacheId(CParents.get(i))), (int)superBlock.getNumLvlEle()-1);
    				}
    			}
    		} else { //Cache is not empty
    			clientSideCache.getLevel(l).updateWeights();
    			if(clientSideCache.getLevel(l).contains(tid)) { //Cache hit   		
    				nodes.add(clientSideCache.getLevel(l).getAndRemove(tid));
    				Fid.add(fid2);
    			}
    		}
    		
    		LinkedList<Long> Cid = clientSideCache.getLevel(l).getPids();
    		LinkedList<Long> IdSet = new LinkedList<Long>();
    		
    		if( Fid.size() == 2) {
    			
    			//Log when the error occur
    			if(Fid.contains(-1L))
    				logger.error("LinkedList<Node<Long,Long>> getNodes(LinkedList<Long> pids)", new Exception("Target: " + target + " " + Fid.toString()));
    				
    			IdSet.addAll(Fid);
    			//Query to the server
        		nodes.addAll(getNodes(IdSet));
        		IdSet.add(tid);
        		IdSet.addAll(Cid);
    			
    		} else {
    			
    			IdSet.add(tid);
    			IdSet.addAll(Fid);
    			//Query to the server
    			nodes.addAll(getNodes(IdSet));
        		IdSet.addAll(Cid);
    			
    		}
    		
    		nodes.addAll(clientSideCache.getLevel(l).nodes);	
    		
    		Node<Long,Long> tnode = extractNode(nodes, tid);
    		LinkedList<Node<Long,Long>> CNodes = extractNodes(nodes, Cid);
    		LinkedList<Node<Long,Long>> FNodes = extractNodes(nodes, Fid);
    		 
    		HashMap<Long, Long> associativeArray = createAssociativeArray(IdSet);
    			
    		updateIds(tnode, CNodes, FNodes, associativeArray);
    		updatePointers(tparent, CParents, FParents, associativeArray);
    	
    		//Stores tparent, CParents and FParents
        	storeNodes(tparent, CParents, FParents);
        	//Update cache
        	//Not needed because the updateIds() method just update the cache level elements //cache.getLevel(l).updatePids(associativeArray);
    		
        	if( CNodes.size() == superBlock.getNumLvlEle() ){ //Cache miss
        		clientSideCache.getLevel(l).replaceOldestNode(tnode);
        	} else { //Cache hit
        		clientSideCache.getLevel(l).addNode(tnode, (int)superBlock.getNumLvlEle());
        	}	

        	//Choose tid, fake1 and fake2 for level l+1
        	if( l<superBlock.getHeight() ) {
        		tid = ((INode<Long,Long>)tnode).children[tnode.getLoc(target)];
        		fid1 = ((INode<Long,Long>)FNodes.get(0)).children[FNodes.get(0).getLoc(fake1)];
        		if( CNodes.size() == superBlock.getNumLvlEle() ) { //Cache miss
        			fid2 = -1;
        		} else { //Cache hit
        			fid2 = ((INode<Long,Long>)FNodes.get(1)).children[FNodes.get(1).getLoc(fake2)];
        		}
        	} else { //Leaf

        		tuple = ((LNode<Long, Long>) tnode).getValue(target);
        	}
        	tparent = tnode; CParents = CNodes; FParents = FNodes; l=l+1;
    	}
    	
    	//Stores tparent, CParents and FParents
    	storeNodes(tparent, CParents, FParents);
    	
    	return tuple;
    	
    }
    
    /**
     * Initialize the cache 
     */
    public void initClientSideCache( int cacheElementNumber ) {
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	if(superBlock.getNumLvlEle() != cacheElementNumber) {  //The superblock on the server must be updated  		
    		changeClientSideCacheElementNumber(cacheElementNumber);
    		superBlock.setNumLvlEle(cacheElementNumber);
    		ClientSideCache.delete();
    	}
    		
    	initClientSideCache();
    	
    }
    
 
    
    /**
     * Checks if the nodes stored into the cache are valid nodes
     *
     * @return true if the cache is valid, false otherwise
     */
    private boolean isClientSideCacheValid() {
    	
    	String errorMessage = "Old cache file deleted";
    	
    	if(ClientSideCache.exist()) {
    		
    		if(clientSideCache.getCreationDate() < superBlock.getDisksCreationDate() ) {

    			System.err.println(errorMessage);
    			ClientSideCache.delete();
    			return false;
    			
    		}
    			    	
    		for( int i = 1 ; i <= clientSideCache.getLevelNumber() ; i++ ) {
    			
    			LinkedList<Node<Long,Long>> cachedNodes = clientSideCache.getLevel(i).nodes;
    			LinkedList<Long> cachedPids = clientSideCache.getLevel(i).getPids();
    			
    			LinkedList<Node<Long,Long>> nodes = getNodes(cachedPids);
    			
    			if( nodes.size() != cachedNodes.size() ) {
    				
    				System.err.println(errorMessage);
	    			ClientSideCache.delete();
		    		return false;
		    		
    			}		
    			
    			for( int n = 0 ; n < nodes.size() ; n++ ) {
    				
    				if(cachedNodes.get(n).vid != nodes.get(n).vid) {
    					System.err.println(errorMessage);
    	    			ClientSideCache.delete();
    		    		return false;
    				}
    				
    			}
    			
    		}
    		
    	}
    	
    	return true;
    	
    }
    
    /**
     * Changes the number of element per cache's level stored on the server
     * 
     * @param cacheElementNumber
     */
    private void changeClientSideCacheElementNumber(int cacheElementNumber) {
    	if( socket != null ) {
    		
    		try{
    			oos.writeObject( Protocol.CHANGE_CLIENT_SIDE_CACHE_ELEMENT_NUMBER + " " + cacheElementNumber );
    			oos.flush();
    			
    			if (!((String)getObject()).equals(Protocol.CONFIRM_CHANGE_CACHE_ELEMENT_NUMBER))
    				throw new Exception("Confirm message not recieved");
    			
    		} catch(Exception e) {
    			
    		}
    	}
    }
    
   
    /**
     * Changes the number of element per cache's level stored on the server with server side cache
     * 
     * @param cacheElementNumber
     */
    /**** 
    private void changeServerSideCacheElementNumber(int cacheElementNumber) {
    	if( socket != null ) {
    		
    		try{
			
    			oos.writeObject( Protocol.CHANGE_SERVER_SIDE_CACHE_ELEMENT_NUMBER + " " + cacheElementNumber );
    			oos.flush();
    			
    			if (!((String)getObject()).equals(Protocol.CONFIRM_CHANGE_CACHE_ELEMENT_NUMBER))
    				throw new Exception("Confirm message not recieved");
    			
    		} catch(Exception e) {
    			
    		}
    	
    	}
    }
    *****/
 
 
    /**
     * Initialize the cache
     */
    private void initClientSideCache() {
    	
    	try {
	    	
	    	//If doesn't exist download the superblock
	    	if( superBlock == null )
	    		downloadSuperBlock();
	    	
	    	if(!ClientSideCache.exist()) {
	    		
	    		//Init the cache
		    	clientSideCache = new ClientSideCache( superBlock.getHeight(), superBlock.getNumLvlEle() );
		    	
		    	//Puts the root node into the cache first level 
		    	Node<Long,Long> root = getNode(superBlock.getRootPid());
		    	clientSideCache.getLevel(1).addNode(root, System.currentTimeMillis()); Thread.sleep(1);
		    	
		    	LinkedList<Long> CPid = new LinkedList<Long>();
		    	
		    	for( int i = 0 ; i < superBlock.getNumLvlEle() ; i++ ) {
		    		long ckey;		
		    		do {
		    			ckey = (long)(superBlock.getKeyNum()*Math.random()) + 1;
		    		} while(CPid.contains(((INode<Long,Long>)root).children[root.getLoc(ckey)])); 	
		    		CPid.add(((INode<Long,Long>)root).children[root.getLoc(ckey)]);
		    	}
		    	
		    	int l;
		    	LinkedList<Node<Long,Long>> CNodes;
		    	
		    	for( l = 2 ; l < superBlock.getHeight() ; l++ ) {
		    		CNodes = getNodes(CPid);
		    		for( int i = 0 ; i < CNodes.size() ; i++ ) {
		    			clientSideCache.getLevel(l).addNode(CNodes.get(i) , System.currentTimeMillis());  
		    			Thread.sleep(1);
		    		}
		    		CPid = new LinkedList<Long>();
		    		for(Node<Long,Long> n : CNodes) {
		    			CPid.add(((INode<Long,Long>)n).children[0]);
		    		}			
		    	}
		    	
		    	CNodes = getNodes(CPid);
		    	for( int i = 0 ; i < CNodes.size() ; i++ ) {
	    			clientSideCache.getLevel(l).addNode(CNodes.get(i) , System.currentTimeMillis());  
	    			Thread.sleep(1);
	    		}
		    	
	    	} else {
	    		
	    		//Load from disk
	    		clientSideCache = new ClientSideCache( superBlock.getHeight(), superBlock.getNumLvlEle(), true);
	    		
		    	if(!isClientSideCacheValid()) {
		    		initClientSideCache();
		    	}
	    		
	    	}
    	
    	} catch ( Exception e ) {
    		e.printStackTrace();
    	}
		
    }
    
  
    
    /**
     * Save the cache on the disk
     */
    public void saveClientSideCache() {
    	clientSideCache.save();
    }
    
    /**
     * Save the cache on the server
     */
    public void saveServerSideCache() {
    	if( socket != null ) {
    		
    		try{
			
    			oos.writeObject( Protocol.SAVE_CACHE_CODE );
    			oos.flush();
    			
    			if (!((String)getObject()).equals(Protocol.CONFIRM_CACHE_SAVE))
    				throw new Exception("Confirm message not recieved");
    			
    		} catch(Exception e) {
    			
    		}
    	
    	}
    }
    
    public void deleteServerSideCache() {
    	if( socket != null ) {
    		
    		try{
			
    			oos.writeObject( Protocol.DELETE_CACHE_CODE );
    			oos.flush();
    			
    			if (!((String)getObject()).equals(Protocol.CONFIRM_CACHE_DELETE))
    				throw new Exception("Confirm message not recieved");
    			
    		} catch(Exception e) {
    			
    		}
    	
    	}
    }
    
    /**
     * Returns the children of the node with the given pid to follow to search key
     * 
     * @param pid
     * @param key
     * @param l current level
     * @param CachedNodesParents
     * 
     * @return the children pid
     */
    private long followChildCSC( long pid, long key, int l, LinkedList<Node<Long,Long>> CachedNodesParents) {
    	
    	long child = 1L;
    	// Il livello deve essere l >=1 
    	/*
    	if (l == 1) 
    	  if (clientSideCache.getLevel(1).contains(pid))
    		return ((INode<Long,Long>)clientSideCache.getLevel(1).get(pid)).children[clientSideCache.getLevel(1).get(pid).getLoc(key)];
    	*/
    	/*if(l>1)*/
    		if(clientSideCache.getLevel(l-1).contains(pid))
    			return ((INode<Long,Long>)clientSideCache.getLevel(l-1).get(pid)).children[clientSideCache.getLevel(l-1).get(pid).getLoc(key)];
    	/*
    	if(clientSideCache.getLevel(l).contains(pid))
    		return ((INode<Long,Long>)clientSideCache.getLevel(l).get(pid)).children[clientSideCache.getLevel(l).get(pid).getLoc(key)];
    	*/
    	for( int i=0 ; i < CachedNodesParents.size() ; i++ ) {
    		if( CachedNodesParents.get(i).pid == pid)
    			return ((INode<Long,Long>)CachedNodesParents.get(i)).children[CachedNodesParents.get(i).getLoc(key)];
    	} 
    	System.err.println("PDDDDDDDDDDDDDD");
    	return child;
    	
    }
    
 
    
    /**
     * Builds a list of key to use for the cover searches 
     * 
     * @param root the root node
     * @param targetKey the searched value
     * @param coverNum the number of keys for the cover searches to return
     * 
     * @return the list of key to use for the cover searches 
     */
    private LinkedList<Long> buildCoverKeysList( Node<Long, Long> root, long targetKey, int coverNum, LinkedList<Long> cachedPids ) {
    		
    	LinkedList<Long> coverKeys = new LinkedList<Long>();
    	LinkedList<Integer> coverRootChild = new LinkedList<Integer>();
    	
    	long tid = ((INode<Long, Long>)root).children[root.getLoc(targetKey)];
    	
    	for( int i = 0 ; i < coverNum ; i++ ) {
    		long cover;
    		do {
    			cover = (long)(superBlock.getKeyNum()*Math.random()) + 1;
    		} while( ((INode<Long, Long>)root).children[root.getLoc(cover)] == tid || 
    				   coverRootChild.contains(root.getLoc(cover)) || 
    				   cachedPids.contains(((INode<Long, Long>)root).children[root.getLoc(cover)]) );
    	
    		coverRootChild.add(root.getLoc(cover));
    		coverKeys.add(cover);
    	}
    	return coverKeys;
    }
    
    /**
     * Builds a list of key to use for the cover searches.
     * 
     * @param root the root node
     * @param targetKey the searched value
     * @param coverNum the number of keys for the cover searches to return
     * @param eap the eap
     * @param cachedPids the cached pids
     * 
     * @return the list of key to use for the cover searches
     */
    private LinkedList<Long> buildCoverKeysListWithEsteemedAccessprofile( EsteemedAccessProfile eap, Node<Long, Long> root, long targetKey, int coverNum, LinkedList<Long> cachedPids ) {
    	   	
    	LinkedList<Long> coverKeys = new LinkedList<Long>();
    	LinkedList<Integer> coverRootChild = new LinkedList<Integer>();
    	
    	long tid = ((INode<Long, Long>)root).children[root.getLoc(targetKey)];
    	
    	for( int i = 0 ; i < coverNum ; i++ ) {
    	
    		long cover;
    		
    		do {
    			long  r = eap.extrRand();
    			cover = (r + ((superBlock.getM()+1)*(coverKeys.size()+1))) % superBlock.getKeyNum();
    			eap.assessFdf(r);
    			eap.computePdfCdf();
    			
//    			System.err.println(cover);
    			
    		} while( ((INode<Long, Long>)root).children[root.getLoc(cover)] == tid || coverRootChild.contains(root.getLoc(cover)) || cachedPids.contains(((INode<Long, Long>)root).children[root.getLoc(cover)]) );
    	
//    		System.err.println(cover);
    		coverRootChild.add(root.getLoc(cover));
    		coverKeys.add(cover);
    		
    	}
    	
    	return coverKeys;
    	
    }
    public long pirCSC( long targetKey, int coverNum) throws Exception {
    	return pirCSC(targetKey, coverNum, -1L);
    }
   
// //////////////////////////////////////////////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Retrieves the value associated at the given key using the private information retrieval algorithm
     * 
     * @param targetKey value to search in the B+ tree
     * @param coverNum the maximum number of cover search
     * 
     * @return the value associated at the target key
     */
    public long pirCSC( long targetKey, int coverNum, long insertValue) throws Exception {

    	//If doesn't exist download the superblock
    	if( superBlock == null ) downloadSuperBlock();
    	// System.err.println(superBlock.getInfo());
    	
    	//Initialization
    	boolean cacheHit = true;
    	long target = superBlock.getRootPid();
    	long newtarget = 0;
    	LinkedList<Long> cover = new LinkedList<Long>();
    	//coverNum+=1; 
    	for(int i=0; i<coverNum; i++) cover.add(target);
    	
    	LinkedList<Long> coverKeys = null;
    	LinkedList<Long> cachedKeys = new LinkedList<Long>();
    	
    	LinkedList<Long> ToRead_Pids = new LinkedList<Long>();
    	LinkedList<Node<Long,Long>> Read = new LinkedList<Node<Long,Long>>();
    	
    	LinkedList<Node<Long,Long>> Non_Cached_Parents = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> Non_Cached;
    	
    	LinkedList<Node<Long,Long>> ReadUpdateForShuffling = new LinkedList<Node<Long,Long>>(); 
    	LinkedList<Node<Long,Long>> temp_for_swap; 
    	
    	LinkedList<Long> IdSet = new LinkedList<Long>();
    	LinkedList<Node<Long,Long>> Parents = new LinkedList<Node<Long,Long>>();
    	
//		TreeViewClient<Long, Long> twc = inOrderVisit();
//		twc.dump("tree.dot");
//		System.err.println("level = 1, Tree: "+twc.isConsistent());
		
    	//newtarget = 
    	splitRootEvaluation(targetKey);
        
//		twc = inOrderVisit();
//		twc.dump("tree_1.dot");
//		System.err.println("level = 1, Tree: "+twc.isConsistent());
		
    	// Choose cover keys
		if (clientSideCache.getLevel(1).get(superBlock.getRootPid()).num < coverNum+clientSideCache.getLevel(2).getPids().size()) {
			System.err.println("clientSideCache.getLevel(1).get(superBlock.getRootPid()).num = "+clientSideCache.getLevel(1).get(superBlock.getRootPid()).num);
			System.err.println("coverNum = "+coverNum);
			System.err.println("clientSideCache.getLevel(2).getPids().size() = "+clientSideCache.getLevel(2).getPids().size());
					
		  throw new Exception("Too few children in the root node to manage num_cover+num_cache+1 paths!!!");
		}
    	coverKeys = buildCoverKeysList( 
                                     clientSideCache.getLevel(1).get(superBlock.getRootPid()), 
                                     targetKey, 
                                     coverNum, 
                                     clientSideCache.getLevel(2).getPids()
                                     );
    	// Retrieve Cached Keys 
    	LinkedList<Node<Long, Long>> tempRef = clientSideCache.getLevel((int)superBlock.getHeight()).getNodeList();
		for (int i= 0; i < tempRef.size(); ++i) 
			cachedKeys.add(tempRef.get(i).getFirstKey());
		
    	HashMap<Long, Long> pidsShuffle = new HashMap<Long, Long>();
		

    	for( int l = 2, e = (int) superBlock.getHeight(); l <= e; l++ ) {
             
    		//Identify the blocks to read from the server
    		target = followChildCSC(pidsShuffle.get(target) == null ? target : pidsShuffle.get(target), 
    				                targetKey, 
    				                l, 
    				                Non_Cached_Parents
    				               );
    		
    		if(!clientSideCache.getLevel(l).contains(target)) {
    			ToRead_Pids.add(target);
    			if(cacheHit) { 
    				cacheHit = false; 
    				coverKeys.remove(coverNum-1);
    				coverNum -= 1;
    			}	
    		}
    		for( int i = 0 ; i < coverNum ; i++ ) {
    			cover.set(i, 
    					  followChildCSC(pidsShuffle.get(cover.get(i)) == null ? cover.get(i) : pidsShuffle.get(cover.get(i)), 
    					                 coverKeys.get(i), 
    					                 l, 
    					                 Non_Cached_Parents
    					                )
    					 );
    			ToRead_Pids.add(cover.get(i));
    		}
    		
    		//Get blocks
    		Read = getNodes(ToRead_Pids);

    		/* Evaluate Splitting of Non Cached Nodes == cover nodes + 
    		 * the current target node (if not already in cache) 
    		 * */
    		//LinkedList<Node<Long,Long>> ReadUpdateForShuffling = new LinkedList<Node<Long,Long>>(); 
    		for (Node<Long,Long> r : Read) {
        		if (Math.random() < splitProbability(l, r)) {
        			long keyInLeaf = targetKey;
        			int ind = 0; 
        			long temp_target;
   
        			if (r.getPid() != target) {
        				ind = cover.indexOf(r.getPid());
        				keyInLeaf = coverKeys.get(ind);
        			}
	        		temp_target = split_NonCachedNode(l, keyInLeaf, r.getPid(),
	        		    		            		  r, 
	        		    		            		  ToRead_Pids, 
	        		    		            		  ReadUpdateForShuffling,
	        		    		            		  Non_Cached_Parents
	        		    		           		     );
	        		 if (r.getPid() == target) target = temp_target;
	        		 else cover.set(ind, temp_target);
        		} 
        		else {
        			ReadUpdateForShuffling.add(r);
        		} // end if Math.random 
        	} // end for r: Read
    		/* end evaluation of the splitting of Non Cached Nodes */
    		// Exchange the pointers to the containers "Read" and "ReadUpdateForShuffling" 
    		// to update the former and reuse the latter
    		temp_for_swap = Read; 
    		Read = ReadUpdateForShuffling; 
    		ReadUpdateForShuffling = temp_for_swap; 
    		ReadUpdateForShuffling.clear(); 
    		
    		/* Evaluate Splitting of Cached Nodes including the  
    		 * the current target node (if already in cache) 
    		 * --- qui non c'e' un targetId o targetKey se non davvero quello voluto, 
    		 *     perché gli altri nodi in cache li riscrivo comunque tutti sul server
    		 * 
    		 * */
    		LinkedList<Node<Long,Long>> copyCacheLevelContainer = new LinkedList<Node<Long,Long>>(clientSideCache.getLevel(l).getNodeList());
    		for (Node<Long,Long> c : copyCacheLevelContainer) {
    			if (Math.random() < splitProbability(l, c)) {
    				long keyInLeaf = targetKey;
        			long temp_target;
        			if (c.getPid() != target) {
        				int ind = clientSideCache.getLevel(l).getNodeList().indexOf(c);
        				keyInLeaf = cachedKeys.get(ind);
        			}
    				temp_target = split_CachedNode(l, keyInLeaf, c.getPid(),
	        		          		               c, 
	        		    		                   ToRead_Pids, 
	        		    		                   ReadUpdateForShuffling,
	        		    		                   Non_Cached_Parents
	        		    		           	      );
	        		 if (c.getPid() == target) target = temp_target;
    			} // end if 
    		} // end for c: current cache level
    		/* end evaluation of the splitting of Cached Nodes */
            Read.addAll(ReadUpdateForShuffling); 
            ReadUpdateForShuffling.clear();
    		
    
    	    //Create permutation between blocks pid in cache_l and Read 
    		IdSet.clear(); 
    		IdSet.addAll(ToRead_Pids); 
    		IdSet.addAll(clientSideCache.getLevel(l).getPids());
    		pidsShuffle = createAssociativeArray(IdSet);  
    		
    		//Shuffle blocks in Read and Cache at level l
    		updateIds(Read, pidsShuffle);
    		clientSideCache.getLevel(l).updatePids(pidsShuffle);
    		
    		//Determine effects on parents and Stores blocks at level l-1
    		clientSideCache.getLevel(l-1).updatePointers(pidsShuffle);
    		updatePointers(Non_Cached_Parents, pidsShuffle);
    		
    		Parents.clear();
    		Parents.addAll(clientSideCache.getLevel(l-1).nodes);
    		Parents.addAll(Non_Cached_Parents);
   			storeNodes(Parents); 
    			    		
    		//Update cache
    		Non_Cached = Read;
    		if(cacheHit) {
    			clientSideCache.getLevel(l).updateWeight(pidsShuffle.get(target) == null ? 
    					                                 target : 
    					                                 pidsShuffle.get(target), 
    					                                 System.currentTimeMillis()
    					                                );
    		} else {
    			// aggiunge il nodo eliminato dalla cache (che è stato pero' acceduto in questo livello) 
    			// alla lista dei padri dei nodi acceduti in questo livello
    			Non_Cached.add(clientSideCache.getLevel(l).getAndRemoveOldestNode(System.currentTimeMillis()+1)); 
    			
    			long targetShuffled = (pidsShuffle.get(target) == null ? target : pidsShuffle.get(target));
    			for (Node<Long,Long> r : Read) {
    				if (r.getPid() == targetShuffled) {
    					clientSideCache.getLevel(l).addNode(r, System.currentTimeMillis());
    	    			// toglie il nodo ricevuto dal server da Read (e quindi anche da Non_Cached...) 
    	    			// perché è stato inserito in Cache
    	    			Read.remove(r);
    	    			break;
    				}
    			}
    		}
    		Non_Cached_Parents = Non_Cached;
    		ToRead_Pids.clear();
    		
    	} // end for 
    	
    	// Insertion or Update 
    	if (insertValue != -1L ) { // insert a new value in the target Leaf ... per questo prototipo conta inserire solo la chiave in realtà
    		LNode<Long,Long> lastTempRef = (LNode<Long,Long>)clientSideCache.getLevel((int)superBlock.getHeight()).get(pidsShuffle.get(target) == null ? target : pidsShuffle.get(target));
    		if (!lastTempRef.contains(targetKey)) {
    			lastTempRef.insertNewValue(targetKey,insertValue);
    			superBlock.updateKeyNumber();
    		} else  // Update value
    			lastTempRef.updateKeyValue(targetKey, insertValue);
    	}
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
		toStore.addAll(clientSideCache.getLevel((int)superBlock.getHeight()).nodes);
		toStore.addAll(Non_Cached_Parents); // ger .... c'era Read 
		storeNodes(toStore);
		this.storeSuperBlock();
		long tuple = ((LNode<Long,Long>)extractNode(toStore, pidsShuffle.get(target) == null ? target : pidsShuffle.get(target))).getValue(targetKey);
    	
//		twc = inOrderVisit();
//		twc.dump("tree_end.dot");
//		System.err.println("At the end,  Tree: "+twc.isConsistent());
    	
    	return tuple;
    	
    }
        
    public long split_NonCachedNode(int level,
    								long target_key,  
    								long targetId, 
    								Node<Long,Long> r,
    								LinkedList<Long> ToRead_Pids,
    								LinkedList<Node<Long,Long>> ReadUpdateForShuffling,
    								LinkedList<Node<Long,Long>> Non_Cached_Parents
    								) {
    	long newTargetId = targetId;
    	ArrayList<Node<Long, Long>> ns = new ArrayList<Node<Long, Long>>();
    	
    	long promoted_Key = r.splitNodeInMemory(ns, this.superBlock);
    	
    	if (target_key >= promoted_Key)  newTargetId = ns.get(1).getPid();
    
    	ReadUpdateForShuffling.add(ns.get(0));
    	ReadUpdateForShuffling.add(ns.get(1));
    	ToRead_Pids.add(ns.get(1).getPid());
    		
		// update the pointers of the parent node of r, according to the split of r into r1 and r2
		Node<Long,Long> parent = null;
		LinkedList<Node<Long, Long>> tempRefList = Non_Cached_Parents;
		for (int i = 0 ; parent == null && i < tempRefList.size() ; i++) {
			Node<Long,Long> tempRefNode = tempRefList.get(i);
			if (tempRefNode.hasChild(r)) parent = tempRefNode; 
		}
		// cerco anche in cache di level-1
		tempRefList = clientSideCache.getLevel(level-1).getNodeList();
	    for (int i = 0 ; parent == null && i < tempRefList.size(); i++) {
	    	Node<Long,Long> tempRefNode = tempRefList.get(i);
			if (tempRefNode.hasChild(r)) parent = tempRefNode;	
	    }    
	    
	    ((INode<Long,Long>) parent).insertForSplit(promoted_Key, ns.get(1));	
		
    	return newTargetId;
	} // end split_NonCachedNode
   
    public long split_CachedNode(int level,
								 long target_key,  
								 long targetId, 
								 Node<Long,Long> c,
								 LinkedList<Long> ToRead_Pids,
								 LinkedList<Node<Long,Long>> ReadUpdateForShuffling,
								 LinkedList<Node<Long,Long>> Non_Cached_Parents
								) {
    	long newTargetId = targetId;
    	ArrayList<Node<Long, Long>> ns = new ArrayList<Node<Long, Long>>();
    	long promoted_Key = c.splitNodeInMemory(ns, this.superBlock);

    	// remove n from Cache_level
    	int index = clientSideCache.getLevel(level).getNodeList().indexOf(c);
    	long weight = clientSideCache.getLevel(level).getWeightsList().get(index);
    	clientSideCache.getLevel(level).getNodeList().remove(index);
    	clientSideCache.getLevel(level).getWeightsList().remove(index);
    	
    	if (target_key >= promoted_Key)  {
    		newTargetId = ns.get(1).getPid();
    		// insert into Cache_level the node w/ a child in Cache_level+1 or the correct node if it is a leaf 
			clientSideCache.getLevel(level).addNode(index, ns.get(1), weight);
			ReadUpdateForShuffling.add(ns.get(0));
			ToRead_Pids.add(ns.get(0).getPid());
    	} else {
    		// insert into Cache_level the node w/ a child in Cache_level+1
			clientSideCache.getLevel(level).addNode(index, ns.get(0), weight);
			ReadUpdateForShuffling.add(ns.get(1));
			ToRead_Pids.add(ns.get(1).getPid()); // to add also the other node to the shuffling operation in the main algorithm
    	}
    	// Ricordarsi di modificare per valutare lo split delle foglie .....
    	
    	// update the pointers of the parent node of r, according to the split of r into r1 and r2
		Node<Long,Long> parent = null;
		LinkedList<Node<Long, Long>> tempRefList = Non_Cached_Parents;
		for (int i = 0 ; parent == null && i < tempRefList.size() ; i++) {
			Node<Long,Long> tempRefNode = tempRefList.get(i);
			if (tempRefNode.hasChild(c)) parent = tempRefNode;
		}
		// cerco anche in cache di level-1
		tempRefList = clientSideCache.getLevel(level-1).getNodeList();
	    for (int i = 0 ; parent == null && i < tempRefList.size(); i++) {
	    	Node<Long,Long> tempRefNode = tempRefList.get(i);
	    	if (tempRefNode.hasChild(c)) parent = tempRefNode;
	    }    
	    
		((INode<Long,Long>) parent).insertForSplit(promoted_Key, ns.get(1));
    	
        return newTargetId;
    } // end split_CachedNode
    
    
    public long splitRootEvaluation(long target_key) throws Exception {
	    long newTargetId = 0;
	    long level = 1;
	    Node<Long,Long> n = clientSideCache.getLevel(1).get(superBlock.getRootPid());
		if (n.isFull()) {
	
			// lo split della root è diverso.....
    		// split solo quando è piena davvero e lo split va fatto in minnumkeys+1 FIGLI
    		// VINCOLI sulla definizione dell'albero ... root con almeno minnumkeys+1 FIGLI 
    		// e ogni figlio def tradizionale del nodo di un BTREE
		   
			LinkedList<INode<Long,Long>> nn = new LinkedList<INode<Long,Long>>();
			// split the root in minnumkeys+1 CHILDREN + the PARENT
			// pointers and keys are correctly assigned by the method, the pid of the current node is not changed
			INode<Long,Long> newroot = ((INode<Long,Long>)n).splitInMemoryRoot(nn, superBlock, superBlock.getMinNumKeys());
	    	// remove current root from Cache
	    	int index = clientSideCache.getLevel(level).getNodeList().indexOf(n);
	    	long weight = clientSideCache.getLevel(level).getWeightsList().get(index);
	    	clientSideCache.getLevel(level).getNodeList().remove(index);
	    	clientSideCache.getLevel(level).getWeightsList().remove(index);
	      	// increase tree height 
	    	superBlock.setHeight(superBlock.getHeight()+1);
	    	this.storeSuperBlock();
	    	// insert in Cache the new root with the same timestamp 
	    	for (INode<Long,Long> u : nn) {
	    		if (clientSideCache.getLevel(level+1).contains(u.children)){
	    			clientSideCache.getLevel(level).addNode(u, weight);
	    		}
	    	}
	    	clientSideCache.pushLevel(newroot, weight);  			
	    	LinkedList<Node<Long,Long>> nodes = new LinkedList<Node<Long,Long>>(nn); 
            nodes.add(newroot);
	    	storeNodes(nodes);
	    	newTargetId = newroot.getSubTreePid(target_key);
	    		
		}
		return newTargetId;
    } // end splitRootEvaluation
   
    
    public double splitProbability(long level, Node<Long, Long> n) {
    	
    	if (level == 1) { 
    		if (n.isFull()) return 1.0;
    	    else return 0.0;
    	}	
//    	if (n.isFull()) return 1.0; // da togliere ... solo per i test
//    	else return 0.0;
    	
    	double threshold = FILL_FACTOR*(superBlock.getN());
    	double ks = n.getKeysSize() - threshold;
    	if (ks <= 0.0) return 0.0;
    	
    	double x = Math.exp(SPLIT_ALPHA_FACTOR*ks)-1/(Math.exp(SPLIT_ALPHA_FACTOR*(superBlock.getN()-threshold))-1);
    	return x; 
    }


    public TreeViewClient<Long, Long> inOrderVisit() {
    	
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	Stack<Node <Long,Long>> s = new Stack<Node <Long,Long>>();
    	HashMap<Long, TreeViewClient<Long, Long>> prg = new HashMap<Long, TreeViewClient<Long, Long>>();
    	
    	long target = superBlock.getRootPid();
    	LinkedList<Long> ToGet = new LinkedList<Long>();
    	ToGet.add(target);
    	LinkedList<Node <Long,Long>> Read = getNodes(ToGet);
    	s.push(Read.getFirst());
    	
    	prg.put(target, new TreeViewClient<Long, Long>(target));
    	
    	while (!s.empty()) {
    		Node<Long, Long> temp = s.pop();
    		
    		
    		if (temp instanceof INode<?, ?>) {    			
    			
    		  INode<Long, Long> Itemp = (INode<Long,Long>) temp;
    		  
    		  TreeViewClient<Long, Long> currNode = prg.get(temp.pid);
    		  LinkedList<TreeViewClient<Long, Long>> currChildren = new LinkedList<TreeViewClient<Long, Long>>();
        	  for (Long i : Itemp.children) { 
        		  if (i != 0) {
        		    prg.put(i, new TreeViewClient<Long, Long>(i));
                    currChildren.add(prg.get(i));   
        		  }
        	  }

        	  currNode.setChildrenNodes(Itemp.num, Itemp.keys, currChildren); 
    		  
    		  ToGet.clear();
    		  for (long i : Itemp.children) { if (i != 0) ToGet.add(i);  }
    		  Read = getNodes(ToGet);
    		  for (Node<Long,Long> n : Read) { s.push(n);  }
    		}
    		else 
    		{
    			LNode<Long, Long> Ltemp = (LNode<Long,Long>) temp; 
    			
    			TreeViewClient<Long, Long> currNode = prg.get(temp.pid);
    			currNode.setLeafValues(Ltemp.num, Ltemp.keys, Ltemp.values);
    		}
    	}
    	
    	return prg.get(target);
    }
    
    
    
    public long pirCSCNodeIdLog( long targetKey, int coverNum, NodeIdLog nil) {
    	
    	//Tuple associated with target
    	long tuple = -1L;
    	
    	HashMap<Long, Long> associativeArray = new HashMap<Long, Long>();
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	//Initialization
    	boolean cacheHit = true;
    	long target = superBlock.getRootPid();
    	
    	LinkedList<Long> cover = new LinkedList<Long>();
    	for( int i = 0 ; i < coverNum ; i++ ) {
    		cover.add(superBlock.getRootPid());
    	}
    	
    	//Choose cover keys
    	LinkedList<Long> coverKeys = buildCoverKeysList( clientSideCache.getLevel(1).get(superBlock.getRootPid()), targetKey, coverNum, clientSideCache.getLevel(2).getPids());
    	
    	LinkedList<Node<Long,Long>> Read = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> CachedNodes = new LinkedList<Node<Long,Long>>();
		LinkedList<Node<Long,Long>> CachedNodesParents = new LinkedList<Node<Long,Long>>();
    			
    	for( int l = 2 ; l <= superBlock.getHeight() ; l++ ) {

    		//Identify blocks
    		CachedNodesParents = CachedNodes;
    		CachedNodes = new LinkedList<Node<Long,Long>>();  		
    		target = followChildCSC(associativeArray.get(target) == null ? target : associativeArray.get(target), targetKey, l, CachedNodesParents);

    		LinkedList<Long> Cached = clientSideCache.getLevel(l).getPids();
    		LinkedList<Long> ToGet = new LinkedList<Long>();
    		
    		if(!clientSideCache.getLevel(l).contains(target)) {
    			ToGet.add(target);
    			if(cacheHit) {
    				cacheHit = false;
    				coverNum = coverNum -1;
    			}	
    		}
    		
    		for( int i = 0 ; i < coverNum ; i++ ) {
    			cover.set(i, followChildCSC(associativeArray.get(cover.get(i)) == null ? cover.get(i) : associativeArray.get(cover.get(i)), coverKeys.get(i), l, CachedNodesParents));
    			ToGet.add(cover.get(i));
    		}
    		
    		//Get blocks
    		Read = getNodes(ToGet);
    		//Permute blocks
    		LinkedList<Long> IdSet = new LinkedList<Long>(); IdSet.addAll(ToGet); IdSet.addAll(Cached);
    		associativeArray = createAssociativeArray(IdSet);  
    		//Determine effects on parents
    		clientSideCache.getLevel(l-1).updatePointers(associativeArray);
    		updatePointers(CachedNodesParents, associativeArray);
    		//Stores blocks at level l-1
    		LinkedList<Node<Long,Long>> Parents = new LinkedList<Node<Long,Long>>();
    		Parents.addAll(clientSideCache.getLevel(l-1).nodes);Parents.addAll(CachedNodesParents);
    		try { storeNodes(Parents); } catch (Exception e) { e.printStackTrace(); }   		
    		//Shuffle blocks at level l
    		updateIds(Read, associativeArray);
    		clientSideCache.getLevel(l).updatePids(associativeArray);
    		//Update cache
    		CachedNodes = Read;
    		
    		/** START LOG */
    		if(l!=2) {
	    		for( Node<Long,Long> n : Parents) {
	    			nil.addPid(n.pid, l-1);
	    		}	
	    	}
    		if(!cacheHit) {
    			nil.addVid((extractNode(Read, associativeArray.get(target) == null ? target : associativeArray.get(target))).vid, l);
    		} else {
    			nil.addVid(clientSideCache.getLevel(l).get(associativeArray.get(target) == null ? target : associativeArray.get(target)).vid, l);
       		}
    		/** END LOG */
    			
    		if(cacheHit) {
    			clientSideCache.getLevel(l).updateWeight(associativeArray.get(target) == null ? target : associativeArray.get(target), System.currentTimeMillis());
    		} else {
    			Node<Long,Long> deleted = clientSideCache.getLevel(l).getAndRemoveOldestNode(System.currentTimeMillis()+1);
    			int i;
    			for (i = 0 ; i < Read.size() ; i++) {
    				if(Read.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target)))
    					break;
    			}
    			
    			//Log when the error occur
    			if( i == Read.size()){
    				logger.error("public long pir( long targetKey, int coverNum)", new Exception("Target key: " + targetKey  + " Target: " + (associativeArray.get(target) == null ? target : associativeArray.get(target))+ " Level l: " + l) );
    				if(!clientSideCache.getLevel(l).contains(associativeArray.get(target) == null ? target : associativeArray.get(target))) {
    					logger.error("public long pir( long targetKey, int coverNum)", new Exception("Cache hit"));
    				}
    			}
    				
    			clientSideCache.getLevel(l).addNode(Read.get(i), System.currentTimeMillis());
    			Read.remove(i);
    			CachedNodes.add(deleted);
    		}
    		
    	}
    	
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
		toStore.addAll(clientSideCache.getLevel((int)superBlock.getHeight()).nodes);toStore.addAll(Read);
    	
		try { storeNodes(toStore); } catch (Exception e) { e.printStackTrace(); }
    	
		/** START LOG */
		for( Node<Long,Long> n : toStore) {
			nil.addPid(n.pid, (int)superBlock.getHeight());
		}
		/** END LOG */
    	
    	tuple = ((LNode<Long,Long>)extractNode(toStore, associativeArray.get(target) == null ? target : associativeArray.get(target))).getValue(targetKey);

    	return tuple;
   
    }
    
    public long pirCSCNodeCoverage( long targetKey, int coverNum, NodeCoverageItem nci) {
    	
    	//Tuple associated with target
    	long tuple = -1L;
    	
    	HashMap<Long, Long> associativeArray = new HashMap<Long, Long>();
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	//Initialization
    	boolean cacheHit = true;
    	long target = superBlock.getRootPid();
    	
    	LinkedList<Long> cover = new LinkedList<Long>();
    	for( int i = 0 ; i < coverNum ; i++ ) {
    		cover.add(superBlock.getRootPid());
    	}
    	
    	//Choose cover keys
    	LinkedList<Long> coverKeys = buildCoverKeysList( clientSideCache.getLevel(1).get(superBlock.getRootPid()), targetKey, coverNum, clientSideCache.getLevel(2).getPids());
    	
    	LinkedList<Node<Long,Long>> Read = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> CachedNodes = new LinkedList<Node<Long,Long>>();
		LinkedList<Node<Long,Long>> CachedNodesParents = new LinkedList<Node<Long,Long>>();
    			
    	for( int l = 2 ; l <= superBlock.getHeight() ; l++ ) {

    		//Identify blocks
    		CachedNodesParents = CachedNodes;
    		CachedNodes = new LinkedList<Node<Long,Long>>();  		
    		target = followChildCSC(associativeArray.get(target) == null ? target : associativeArray.get(target), targetKey, l, CachedNodesParents);

    		LinkedList<Long> Cached = clientSideCache.getLevel(l).getPids();
    		LinkedList<Long> ToGet = new LinkedList<Long>();
    		
    		if(!clientSideCache.getLevel(l).contains(target)) {
    			ToGet.add(target);
    			if(cacheHit) {
    				cacheHit = false;
    				coverNum = coverNum -1;
    			}	
    		}
    		
    		for( int i = 0 ; i < coverNum ; i++ ) {
    			cover.set(i, followChildCSC(associativeArray.get(cover.get(i)) == null ? cover.get(i) : associativeArray.get(cover.get(i)), coverKeys.get(i), l, CachedNodesParents));
    			ToGet.add(cover.get(i));
    		}
    		
    		//Get blocks
    		Read = getNodes(ToGet);
    		//Permute blocks
    		LinkedList<Long> IdSet = new LinkedList<Long>(); IdSet.addAll(ToGet); IdSet.addAll(Cached);
    		associativeArray = createAssociativeArray(IdSet);  
    		//Determine effects on parents
    		clientSideCache.getLevel(l-1).updatePointers(associativeArray);
    		updatePointers(CachedNodesParents, associativeArray);
    		//Stores blocks at level l-1
    		LinkedList<Node<Long,Long>> Parents = new LinkedList<Node<Long,Long>>();
    		Parents.addAll(clientSideCache.getLevel(l-1).nodes);Parents.addAll(CachedNodesParents);
    		try { storeNodes(Parents); } catch (Exception e) { e.printStackTrace(); }   		
    		//Shuffle blocks at level l
    		updateIds(Read, associativeArray);
    		clientSideCache.getLevel(l).updatePids(associativeArray);
    		//Update cache
    		CachedNodes = Read;
    		
    		/** START LOG */
    		if(l!=2) {
	    		for( Node<Long,Long> n : Parents) {
	    			nci.addPid(n.pid, l-1);
	    		}	
	    	}
    		if(!cacheHit) {
    			nci.addVid((extractNode(Read, associativeArray.get(target) == null ? target : associativeArray.get(target))).vid, l);
    		} else {
    			nci.addVid(clientSideCache.getLevel(l).get(associativeArray.get(target) == null ? target : associativeArray.get(target)).vid, l);
       		}
    		/** END LOG */
    			
    		if(cacheHit) {
    			clientSideCache.getLevel(l).updateWeight(associativeArray.get(target) == null ? target : associativeArray.get(target), System.currentTimeMillis());
    		} else {
    			Node<Long,Long> deleted = clientSideCache.getLevel(l).getAndRemoveOldestNode(System.currentTimeMillis()+1);
    			int i;
    			for (i = 0 ; i < Read.size() ; i++) {
    				if(Read.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target)))
    					break;
    			}
    			
    			//Log when the error occur
    			if( i == Read.size()){
    				logger.error("public long pir( long targetKey, int coverNum)", new Exception("Target key: " + targetKey  + " Target: " + (associativeArray.get(target) == null ? target : associativeArray.get(target))+ " Level l: " + l) );
    				if(!clientSideCache.getLevel(l).contains(associativeArray.get(target) == null ? target : associativeArray.get(target))) {
    					logger.error("public long pir( long targetKey, int coverNum)", new Exception("Cache hit"));
    				}
    			}
    				
    			clientSideCache.getLevel(l).addNode(Read.get(i), System.currentTimeMillis());
    			Read.remove(i);
    			CachedNodes.add(deleted);
    		}
    		
    	}
    	
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
		toStore.addAll(clientSideCache.getLevel((int)superBlock.getHeight()).nodes);toStore.addAll(Read);
    	
		try { storeNodes(toStore); } catch (Exception e) { e.printStackTrace(); }
    	
		/** START LOG */
		for( Node<Long,Long> n : toStore) {
			nci.addPid(n.pid, (int)superBlock.getHeight());
		}
		/** END LOG */
    	
    	tuple = ((LNode<Long,Long>)extractNode(toStore, associativeArray.get(target) == null ? target : associativeArray.get(target))).getValue(targetKey);

    	return tuple;
   
    }
          
    public long pirCSCLeafsPidLog( long targetKey, int coverNum, HashMap<Long, String> leafsReadPid, HashMap<Long, String> leafsWritePid) {
    	
    	//Tuple associated with target
    	long tuple = -1L;
    	
    	HashMap<Long, Long> associativeArray = new HashMap<Long, Long>();
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	//Initialization
    	boolean cacheHit = true;
    	long target = superBlock.getRootPid();
    	
    	LinkedList<Long> cover = new LinkedList<Long>();
    	for( int i = 0 ; i < coverNum ; i++ ) {
    		cover.add(superBlock.getRootPid());
    	}
    	
    	//Choose cover keys
    	LinkedList<Long> coverKeys = buildCoverKeysList( clientSideCache.getLevel(1).get(superBlock.getRootPid()), targetKey, coverNum, clientSideCache.getLevel(2).getPids());
    	
    	LinkedList<Node<Long,Long>> Read = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> CachedNodes = new LinkedList<Node<Long,Long>>();
		LinkedList<Node<Long,Long>> CachedNodesParents = new LinkedList<Node<Long,Long>>();
    			
    	for( int l = 2 ; l <= superBlock.getHeight() ; l++ ) {

    		//Identify blocks
    		CachedNodesParents = CachedNodes;
    		CachedNodes = new LinkedList<Node<Long,Long>>();  		
    		target = followChildCSC(associativeArray.get(target) == null ? target : associativeArray.get(target), targetKey, l, CachedNodesParents);

    		LinkedList<Long> Cached = clientSideCache.getLevel(l).getPids();
    		LinkedList<Long> ToGet = new LinkedList<Long>();
    		
    		if(!clientSideCache.getLevel(l).contains(target)) {
    			ToGet.add(target);
    			if(cacheHit) {
    				cacheHit = false;
    				coverNum = coverNum -1;
    			}	
    		}
    		
    		for( int i = 0 ; i < coverNum ; i++ ) {
    			cover.set(i, followChildCSC(associativeArray.get(cover.get(i)) == null ? cover.get(i) : associativeArray.get(cover.get(i)), coverKeys.get(i), l, CachedNodesParents));
    			ToGet.add(cover.get(i));
    		}
    		
    		if(l == superBlock.getHeight()){
    			for( int i=0 ; i < ToGet.size() ; i++){
    				if(ToGet.get(i) == target){
    					leafsReadPid.put(ToGet.get(i), "target");
    				}else{
    					leafsReadPid.put(ToGet.get(i), "cover");
    				}
    			}
    		}
    		
    		//Get blocks
    		Read = getNodes(ToGet);
    		//Permute blocks
    		LinkedList<Long> IdSet = new LinkedList<Long>(); IdSet.addAll(ToGet); IdSet.addAll(Cached);
    		associativeArray = createAssociativeArray(IdSet);  
    		//Determine effects on parents
    		clientSideCache.getLevel(l-1).updatePointers(associativeArray);
    		updatePointers(CachedNodesParents, associativeArray);
    		//Stores blocks at level l-1
    		LinkedList<Node<Long,Long>> Parents = new LinkedList<Node<Long,Long>>();
    		Parents.addAll(clientSideCache.getLevel(l-1).nodes);Parents.addAll(CachedNodesParents);
    		try { storeNodes(Parents); } catch (Exception e) { e.printStackTrace(); }
    		//Shuffle blocks at level l
    		updateIds(Read, associativeArray);
    		clientSideCache.getLevel(l).updatePids(associativeArray);
    		//Update cache
    		CachedNodes = Read;
    		if(cacheHit) {
    			clientSideCache.getLevel(l).updateWeight(associativeArray.get(target) == null ? target : associativeArray.get(target), System.currentTimeMillis());
    		} else {
    			Node<Long,Long> deleted = clientSideCache.getLevel(l).getAndRemoveOldestNode(System.currentTimeMillis()+1);
    			int i;
    			for (i = 0 ; i < Read.size() ; i++) {
    				if(Read.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target)))
    					break;
    			}
    			
    			//Log when the error occur
    			if( i == Read.size()){
    				logger.error("public long pir( long targetKey, int coverNum)", new Exception("Target key: " + targetKey  + " Target: " + (associativeArray.get(target) == null ? target : associativeArray.get(target))+ " Level l: " + l) );
    				if(!clientSideCache.getLevel(l).contains(associativeArray.get(target) == null ? target : associativeArray.get(target))) {
    					logger.error("public long pir( long targetKey, int coverNum)", new Exception("Cache hit"));
    				}
    			}
    				
    			clientSideCache.getLevel(l).addNode(Read.get(i), System.currentTimeMillis());
    			Read.remove(i);
    			CachedNodes.add(deleted);
    		}
    		
    	}
    	
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
		toStore.addAll(clientSideCache.getLevel((int)superBlock.getHeight()).nodes);toStore.addAll(Read);
    	
		try { storeNodes(toStore); } catch (Exception e) { e.printStackTrace(); }
    	
		//Logs the pids written at the last level
		for( int i = 0 ; i <  clientSideCache.getLevel((int)superBlock.getHeight()).nodes.size() ; i++){
			if(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target))){
				leafsWritePid.put(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid, "target");
			}else{
				leafsWritePid.put(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid, "cache");
			}
		}
		for( int i = 0 ; i < CachedNodes.size() ; i++){
			if(CachedNodes.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target))){
				leafsWritePid.put(CachedNodes.get(i).pid, "target");
			}else{
				leafsWritePid.put(CachedNodes.get(i).pid, "cover");
			}
		}
		
    	tuple = ((LNode<Long,Long>)extractNode(toStore, associativeArray.get(target) == null ? target : associativeArray.get(target))).getValue(targetKey);

    	return tuple;
   
    }
    
    public long pirCSCLeafsPidLogWithEsteemedAccessprofile( long targetKey, int coverNum, EsteemedAccessProfile eap, HashMap<Long, String> leafsReadPid, HashMap<Long, String> leafsWritePid) {
    	
    	//Tuple associated with target
    	long tuple = -1L;
    	
    	HashMap<Long, Long> associativeArray = new HashMap<Long, Long>();
    	
    	//If doesn't exist download the superblock
    	if( superBlock == null )
    		downloadSuperBlock();
    	
    	//Initialization
    	boolean cacheHit = true;
    	long target = superBlock.getRootPid();
    	
    	LinkedList<Long> cover = new LinkedList<Long>();
    	for( int i = 0 ; i < coverNum ; i++ ) {
    		cover.add(superBlock.getRootPid());
    	}
    	
    	//Choose cover keys
    	LinkedList<Long> coverKeys = buildCoverKeysListWithEsteemedAccessprofile( eap, clientSideCache.getLevel(1).get(superBlock.getRootPid()), targetKey, coverNum, clientSideCache.getLevel(2).getPids());
    	
    	LinkedList<Node<Long,Long>> Read = new LinkedList<Node<Long,Long>>();
    	LinkedList<Node<Long,Long>> CachedNodes = new LinkedList<Node<Long,Long>>();
		LinkedList<Node<Long,Long>> CachedNodesParents = new LinkedList<Node<Long,Long>>();
    			
    	for( int l = 2 ; l <= superBlock.getHeight() ; l++ ) {

    		//Identify blocks
    		CachedNodesParents = CachedNodes;
    		CachedNodes = new LinkedList<Node<Long,Long>>();  		
    		target = followChildCSC(associativeArray.get(target) == null ? target : associativeArray.get(target), targetKey, l, CachedNodesParents);

    		LinkedList<Long> Cached = clientSideCache.getLevel(l).getPids();
    		LinkedList<Long> ToGet = new LinkedList<Long>();
    		
    		if(!clientSideCache.getLevel(l).contains(target)) {
    			ToGet.add(target);
    			if(cacheHit) {
    				cacheHit = false;
    				coverNum = coverNum -1;
    			}	
    		}
    		
    		for( int i = 0 ; i < coverNum ; i++ ) {
    			cover.set(i, followChildCSC(associativeArray.get(cover.get(i)) == null ? cover.get(i) : associativeArray.get(cover.get(i)), coverKeys.get(i), l, CachedNodesParents));
    			ToGet.add(cover.get(i));
    		}
    		
    		if(l == superBlock.getHeight()){
    			for( int i=0 ; i < ToGet.size() ; i++){
    				if(ToGet.get(i) == target){
    					leafsReadPid.put(ToGet.get(i), "target");
    				}else{
    					leafsReadPid.put(ToGet.get(i), "cover");
    				}
    			}
    		}
    		
    		//Get blocks
    		Read = getNodes(ToGet);
    		//Permute blocks
    		LinkedList<Long> IdSet = new LinkedList<Long>(); IdSet.addAll(ToGet); IdSet.addAll(Cached);
    		associativeArray = createAssociativeArray(IdSet);  
    		//Determine effects on parents
    		clientSideCache.getLevel(l-1).updatePointers(associativeArray);
    		updatePointers(CachedNodesParents, associativeArray);
    		//Stores blocks at level l-1
    		LinkedList<Node<Long,Long>> Parents = new LinkedList<Node<Long,Long>>();
    		Parents.addAll(clientSideCache.getLevel(l-1).nodes);Parents.addAll(CachedNodesParents);
    		try { storeNodes(Parents); } catch (Exception e) { e.printStackTrace(); }
    		//Shuffle blocks at level l
    		updateIds(Read, associativeArray);
    		clientSideCache.getLevel(l).updatePids(associativeArray);
    		//Update cache
    		CachedNodes = Read;
    		if(cacheHit) {
    			clientSideCache.getLevel(l).updateWeight(associativeArray.get(target) == null ? target : associativeArray.get(target), System.currentTimeMillis());
    		} else {
    			Node<Long,Long> deleted = clientSideCache.getLevel(l).getAndRemoveOldestNode(System.currentTimeMillis()+1);
    			int i;
    			for (i = 0 ; i < Read.size() ; i++) {
    				if(Read.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target)))
    					break;
    			}
    			
    			//Log when the error occur
    			if( i == Read.size()){
    				logger.error("public long pir( long targetKey, int coverNum)", new Exception("Target key: " + targetKey  + " Target: " + (associativeArray.get(target) == null ? target : associativeArray.get(target))+ " Level l: " + l) );
    				if(!clientSideCache.getLevel(l).contains(associativeArray.get(target) == null ? target : associativeArray.get(target))) {
    					logger.error("public long pir( long targetKey, int coverNum)", new Exception("Cache hit"));
    				}
    			}
    				
    			clientSideCache.getLevel(l).addNode(Read.get(i), System.currentTimeMillis());
    			Read.remove(i);
    			CachedNodes.add(deleted);
    		}
    		
    	}
    	
    	LinkedList<Node<Long,Long>> toStore = new LinkedList<Node<Long,Long>>();
		toStore.addAll(clientSideCache.getLevel((int)superBlock.getHeight()).nodes);toStore.addAll(Read);
    	
		try { storeNodes(toStore); } catch (Exception e) { e.printStackTrace(); }
    	
		//Logs the pids written at the last level
		for( int i = 0 ; i <  clientSideCache.getLevel((int)superBlock.getHeight()).nodes.size() ; i++){
			if(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target))){
				leafsWritePid.put(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid, "target");
			}else{
				leafsWritePid.put(clientSideCache.getLevel((int)superBlock.getHeight()).nodes.get(i).pid, "cache");
			}
		}
		for( int i = 0 ; i < CachedNodes.size() ; i++){
			if(CachedNodes.get(i).pid == (associativeArray.get(target) == null ? target : associativeArray.get(target))){
				leafsWritePid.put(CachedNodes.get(i).pid, "target");
			}else{
				leafsWritePid.put(CachedNodes.get(i).pid, "cover");
			}
		}
		
    	tuple = ((LNode<Long,Long>)extractNode(toStore, associativeArray.get(target) == null ? target : associativeArray.get(target))).getValue(targetKey);

    	return tuple;
   
    }
    
    /**
     * Retrieves the value associated with the key using the tradition search algorithm
     * 
     * @param key
     * 
     * @return the value associated with the key  
     */
    public long find( long key) {
		
    	if(clientSideCache == null)
    		initClientSideCache(1);
    	
		Node<Long,Long> node = clientSideCache.getLevel(1).nodes.get(0);
	        
	    while (node instanceof INode<?,?>) { // need to traverse down to the leaf
	        	
	        	INode<Long, Long> inner = (INode<Long, Long>) node;
	            int idx = inner.getLoc(key);
	            
	            /** READ NODE "inner.children[idx]" */
	            Node<Long, Long> child = getNode(inner.children[idx]);
	            
	            node = child;
	            
	   }
	 
	   //After the while loop we are at the leaf
	   LNode<Long, Long> leaf = (LNode<Long, Long>) node;
	   
	   return leaf.getValue(key);
	        
	}
    
}

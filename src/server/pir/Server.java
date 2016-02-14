package server.pir;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import server.gui.ServerGUI;
import base.communication.Protocol;
import base.conversion.Conversion;
import base.disk.Disk;
import base.disk.DiskSuperBlock;

/**
 * The server class
 * @author Tommaso
 *
 */
public class Server implements Runnable{
	
	/** The server config file path */
	private final static String SERVER_CONF_FILE = "conf/config.xml";
	/** Checked to log or not all the request made to the server from the client */
	private final boolean LOG = false;	
    
	/** Server socket */
	private static ServerSocket serverSocket = null;
	/** Variable that show if the server is running */
	public static boolean running= false;
	/** The server static instance */
    private static Server server = null;
    /** The cache static instance */
    private static Cache cache = null; 
    
    /** The disk instance */
	public Disk<Long, Long> disk = null;
    
	/** Client socket */
	private Socket clientSocket = null;	
    /** Object input stream */
    private ObjectInputStream ois = null;
    /** Object output stream */
    private ObjectOutputStream oos = null;    
    /** Variable that store the gui instance */
	private ServerGUI serverGui = null;
	
	
	/**
	 * Create the server object
	 */
	public Server(ServerGUI serverGui){				
		super();
		if(disk == null){ // Server is launched for the first time and the disk must be instanced
			disk = new Disk<Long, Long>(false); 
			if(cache == null)
			cache = new Cache(disk);
		}		
		this.serverGui = serverGui;			
    }
    
	/**
	 * Thread that manage the connection with the client
	 */
	public void run() {
		
		try {			
				running = true;
				
				//Waiting for the client request
				clientSocket = serverSocket.accept();
				clientSocket.setTcpNoDelay(true);
				//Create a new thread
				Thread T = new Thread(server);
				T.setDaemon(true);
				T.start();
				//new Thread(server).start();
				
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				ois = new ObjectInputStream(clientSocket.getInputStream());
						
				while(clientSocket.isConnected()) {
				
					String message = (String)getObject();
					
					if(LOG)
						if(serverGui != null)
							serverGui.updateLogWindow(message + "\n");
					
					processRequest(message);
								
					oos.reset();
				
				}
				
				oos.close();
				ois.close();
				
								
		}catch (Exception e) {
//			serverGui.updateLogWindow("Open thread closed\n");
		}	
	}
	
    /** 
     *  Disconnect the socket.
     */
    public void finalize(){
        disconnect();
    }
	
    /**
     * Disconnect the server (close the socket).
     */
    public void disconnect(){
        try{
            serverSocket.close();
        }
        catch (IOException e){
        	
        }
    }
    
    /**
     * Returns true if the connection is working.
     * 
     * @return true if all is correctly working
     */
    private boolean isConnected(){
        return ((!serverSocket.isClosed()) && (serverSocket.isBound()));
    }
    
    /**
	 * Reads an object on the ObjectInputStream 
	 * 
	 * @return the readed object
	 * 
	 * throws Exception is the reading fail
	 */
    private Object getObject() throws Exception{
        
    	Object obj = null;
    	
    	try{
            //Checks that the connection is established  and ready      	
            if (isConnected() == false){
                throw new IOException();
            }
            
            obj = ois.readObject();
            
        }
        catch(Exception e){
        	
            throw e;
            
        }
                
        return obj;
    }
    
    /**
     * Send a message
     * 
     * @param m the message to send
     * 
     * @throws IOException if the communication channel has some problem
     */
    private void send(String m) throws IOException{
        try{
            if (isConnected() == false){
                throw new IOException();
            }
            
            oos.writeObject(m);
            
        }
        catch(IOException e){
            throw e;
        }
    }
    
    /**
	 * Method invoked to start the server
	 */
	public void start(){		
		
		if(!running)
		{
			if(serverGui != null) {
				serverGui.updateLogWindow("Starting server ...\n");
			} else {
				System.out.println("Starting server ...");
			}			
				
			try{
				serverSocket = new ServerSocket(ServerConfigFile.getInstance(SERVER_CONF_FILE).getSocketPort());
				if(server == null)
					server = this;
				//server = new Server(serverGui);
				Thread main = new Thread(server);
		        main.start();  
		        
		        if(serverGui != null) {
		        	serverGui.updateLogWindow("Server started\n");
		        } else {
					System.out.println("Server started");
				}
		        
			}catch(Exception e){
				
				if(serverGui != null) {
					serverGui.updateLogWindow("Error encourred during the server starting\n");
					serverGui.updateLogWindow(e.toString()+"\n");
				} else {
					e.printStackTrace();
				}
				
			}
			
        } else {
        	if(serverGui != null)
        		serverGui.updateLogWindow("Server is already running\n");
        }
		
	}
	
	/**
	 * Method invoked to stop the server.
	 */
	public void stop(){
		
		if(running)
		{
			if(serverGui != null)
				serverGui.updateLogWindow("Stopping server ...\n");
			else
				System.out.println("Stopping server ...");
			
			running = false;		
			try{
				
				if(serverSocket!=null) {
					serverSocket.close();
				}
				
			} catch (Exception e) {
				
				if(serverGui != null) {
					serverGui.updateLogWindow("Error encourred during the server stopping\n");
				} else {
					e.printStackTrace();
				}
				
				System.exit(1);
			}

			disk.close();
			if(serverGui != null)
				serverGui.updateLogWindow("Server stopped\n");
			else
				System.out.println("Server stopped");
				
		}
		else
		{
			if(serverGui != null)
				serverGui.updateLogWindow("Server is not running\n");
		}		
	}
    
	/**
	 * Analyzes the client request and call properly method
	 * 
	 * @param msg the client message
	 */
	public void processRequest(String msg) {
		//System.err.println(msg.substring(0,6));
		if(msg.startsWith(Protocol.GET_NODES_CODE)) {
			
			msg = msg.substring( Protocol.CODE_LENGTH + 1, msg.length()-1 );
			String[] nodePids = msg.split(" ");

			sendNodes(nodePids);

		} else if(msg.startsWith(Protocol.GET_SUPERBLOCK_CODE)) {
			
			sendSuperBlock();
			
		} else if(msg.startsWith(Protocol.STORE_NODES_CODE)) {
			
			storeNodes(Integer.parseInt(msg.split(" ")[1]));
			
		} else if(msg.startsWith(Protocol.CHANGE_CLIENT_SIDE_CACHE_ELEMENT_NUMBER)) {
			
			changeCacheElementNumber(Long.parseLong(msg.split(" ")[1]));
			
		} else if(msg.startsWith(Protocol.CHANGE_SERVER_SIDE_CACHE_ELEMENT_NUMBER)) {
			
			changeCacheElementNumberAndResetServerSideCache(Long.parseLong(msg.split(" ")[1]));
			
		} else if(msg.startsWith(Protocol.STORE_CACHE_NODES_CODE)) {
			
			storeCacheNodes(Integer.parseInt(msg.split(" ")[1]));
			
		} else if(msg.startsWith(Protocol.GET_CACHE_NODES_CODE)) {
			
			sendCacheNodes(Integer.parseInt(msg.split(" ")[1]));
			
		} else if(msg.startsWith(Protocol.GET_CACHE_STATUS)) {
			
			sendCacheStatus();
			
		} else if(msg.startsWith(Protocol.SAVE_CACHE_CODE)) {
			
			saveCacheAndConfirm();
			
		} else if(msg.startsWith(Protocol.DELETE_CACHE_CODE)) {
			
			deleteCacheAndConfirm();
			
		} else if (msg.startsWith(Protocol.SAVE_SB_CODE)) {
			storeSuperBlock(Long.parseLong(msg.split(" ")[1]), 
					        Long.parseLong(msg.split(" ")[2]), 
					        Long.parseLong(msg.split(" ")[3]), 
					        Long.parseLong(msg.split(" ")[4]), 
					        Long.parseLong(msg.split(" ")[5]),
					        Integer.parseInt(msg.split(" ")[6]));
		}
		
	}
	
	public void storeSuperBlock (long pd, long off0, long off1, long off2, long off3, int keynum) {
		try{			
			disk.updateSuperBlock(pd, off0, off1, off2, off3, keynum);

			if(LOG)
				if(serverGui!= null)
					serverGui.updateLogWindow("Height updated!\n" );				
		    send(Protocol.CONFIRM_SB_STORE);
			
		} catch(Exception e) { 
		
		}
	}
	
	
	/**
     * Changes the number of element per cache's level stored on the server
     * 
     * @param cacheElementNumber
     */
	private void changeCacheElementNumber(long cacheElementNumber) {
		
		try {
			
			DiskSuperBlock sp = disk.getDiskSuperBlock();
			sp.setNumLvlEle(cacheElementNumber);
			sp.save();
			
			send(Protocol.CONFIRM_CHANGE_CACHE_ELEMENT_NUMBER);
			
		} catch(Exception e) {				
			e.printStackTrace();				
		}
		
		
	}
	
	/**
     * Changes the number of element per cache's level stored on the server and reset the cache updating the new dimension
     * 
     * @param cacheElementNumber
     */
	private void changeCacheElementNumberAndResetServerSideCache(long cacheElementNumber) {
		
		try {
			
			cache.save();
			
			disk.getDiskSuperBlock().setNumLvlEle(cacheElementNumber);
			disk.getDiskSuperBlock().save();
			
			// Empty the cache in the cache block and saves it on the disk
			disk.getDiskCacheBlock().reset();
			//Loads the cache block from the disk
			disk.getDiskCacheBlock().load();
			
			cache = new Cache(disk);
			
			send(Protocol.CONFIRM_CHANGE_CACHE_ELEMENT_NUMBER);
			
		} catch(Exception e) {				
			e.printStackTrace();				
		}
		
		
	}
	
	/**
	 * Sends the nodes with the given pid to the client
	 * 
	 * @param nodePids
	 */
	private void sendNodes(String[] pids) {
		
		try {
			
			for(String p : pids) {
										
				oos.writeObject(Conversion.nodeBytesToString(disk.readNodeBytes(Long.parseLong(p))));
				oos.flush();				
			
			if(LOG)
				if(serverGui != null)
					serverGui.updateLogWindow("Node: " + p + " sent\n" );
			
			}
		
		} catch(Exception e) {				
			e.printStackTrace();				
		}
		
	}
	
	/**
	 * Sends the nodes at the given cache level to the client
	 * 
	 * @param level
	 */
	private void sendCacheNodes(int level) {
		
		try {
			
			for( int i = 0 ; i < cache.getLevel(level).getSize() ; i ++) {
										
				oos.writeObject(Conversion.nodeBytesToString(cache.getLevel(level).nodes.get(i)));
				oos.flush();	
				oos.writeObject(cache.getLevel(level).weights.get(i));
				oos.flush();	
			
			}
		
		} catch(Exception e) {				
			e.printStackTrace();				
		}	
		
	}
	
	/**
	 * Sends the superblock to the client
	 */
	private void sendSuperBlock() {
		
		try {		
			oos.writeObject(Conversion.superBlockToString(disk.getDiskSuperBlock()));
			oos.flush();	
		} catch(Exception e) {
			e.printStackTrace();
		}		
			
		if(LOG)
			if(serverGui != null)
				serverGui.updateLogWindow("Super block sent\n" );	
		
	}
	
	/**
	 * Receives the nodes sent by the client and stores them, at the end send an acknowledgment
	 * 
	 * @param num of nodes to store
	 */
	private void storeNodes(int num) {
		
		try{
			
			for(int i = 0 ; i < num ; i++ ) {			 
				
				String storeMsg = (String)ois.readObject(); 							
				long pid = Long.parseLong(storeMsg.split(" ")[0]);				
				String node = storeMsg.substring(storeMsg.indexOf(' ') + 1);	
				
				long offset = pid & 0x1FFFFFFFFFFFFFFFL;
		    	int diskNumber = (int)( (pid & 0x6000000000000000L) >> 61 );
				
		    	disk.registerWriteAccess(offset);
				disk.writeBytes(Conversion.stringToNodeBytes(node) , offset, diskNumber);
				
				
				if(LOG)
					if(serverGui!= null)
						serverGui.updateLogWindow("Node: " + pid + " stored\n" );
						
			}	
			
			send(Protocol.CONFIRM_NODES_STORE);
			
		} catch(Exception e) { 
			
			e.printStackTrace(); 
			System.err.println("Impossible read the store message"); 
			
		}
	}
	
	/**
	 * Receives the nodes sent by the client and stores them at the specified cache level, at the end send an acknowledgment
	 * 
	 * @param level the cache level
	 */
	private void storeCacheNodes(int level) {
		
		try{
			
			Long pid;
			byte[] node;
			byte[] weight; 
			
			if( level == 0) {
				
				pid = (Long)ois.readObject();
				node = Conversion.stringToNodeBytes((String)ois.readObject());
				weight = (byte[]) ois.readObject();
				
				cache.getLevel(level).setNode(0, pid, node, weight);
				
			} else {
				
				for( int i = 0 ; i < disk.getDiskSuperBlock().getNumLvlEle() ; i++) {
					
					pid = (Long)ois.readObject();
					node = Conversion.stringToNodeBytes((String)ois.readObject());
					weight = (byte[]) ois.readObject();
					
					cache.getLevel(level).setNode(i, pid, node, weight);
					
				}
					
			}
			
			send(Protocol.CONFIRM_NODES_STORE);
			
		} catch(Exception e) { 
			
			e.printStackTrace(); 
			System.err.println("Impossible read the store message"); 
			
		}
		
	}
	
	/**
	 * Sends the cache status to the client
	 */
	private void sendCacheStatus() {
		
		try{
			
			oos.writeObject(cache.isEmpty());
			
		} catch(Exception e) { 
	
			e.printStackTrace();
			
		}
	}
	
	/**
	 * Deletes the server's cache and sends a confirm to the client
	 */
	public void deleteCacheAndConfirm() {
		
		deleteCache();
		
		try{
			
			send(Protocol.CONFIRM_CACHE_DELETE);
			
		} catch(Exception e) { 
			
			e.printStackTrace(); 
			System.err.println("Impossible read the store message"); 
			
		}
		
	}
	
	public void deleteCache() {
		
		disk.getDiskCacheBlock().reset();
		cache = new Cache(disk);
		
		if(LOG)
			if(serverGui!= null)
				serverGui.updateLogWindow("Cache deleted on the disk\n" );
			else
				System.out.println("Cache deleted");
		
	}
	
	/**
	 * Saves the server's cache and sends a confirm to the client
	 *
	 */
	public void saveCacheAndConfirm() {
		
		saveCache();
		
		try{
			
			send(Protocol.CONFIRM_CACHE_SAVE);
			
		} catch(Exception e) { 
			
			e.printStackTrace(); 
			System.err.println("Impossible read the store message"); 
			
		}
		
	}
	
	/**
	 * Saves the cache
	 */
	public void saveCache() {
		
		cache.save();
		
		if(LOG)
			if(serverGui!= null)
				serverGui.updateLogWindow("Cache saved on the disk\n" );
			else
				System.out.println("Cache saved");
		
	}
	
	
}




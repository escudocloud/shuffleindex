package base.communication;

/**
 * The Protocol class that defines the messages format
 * @author Tommaso
 *
 */
public class Protocol {
	
	/** The format used by the client to request the nodes read */
	public final static String GET_NODES_CODE = "GETNDS";
	/** The format used by the client to request the cache nodes at the specified level */
	public final static String GET_CACHE_NODES_CODE = "GETCNS";
	/** The format used by the client to request the super block */
	public final static String GET_SUPERBLOCK_CODE = "GETSBK";
	/** The format used by the client to request the cache block status*/
	public final static String GET_CACHE_STATUS = "GETCST";
	
	/** The format used by the client to request the store of the nodes */
	public final static String STORE_NODES_CODE = "STRNDS";
	/** The format used by the client to request the store of the cache nodes at the given level */
	public final static String STORE_CACHE_NODES_CODE = "STRCNS";
	
	/** The format used by the client to request the save of the cache */
	public final static String SAVE_CACHE_CODE = "SAVCAC";
	
	/** The format used by the client to request the delete of the cache */
	public final static String DELETE_CACHE_CODE = "DELCAC";
	
	/** The format used by the server to send the confirm message after a successful store operation */
	public final static String CONFIRM_NODES_STORE = "NSTROK";	
	
	/** The format used by the server to send the confirm message after a successful cache saving operation */
	public final static String CONFIRM_CACHE_SAVE = "SACAOK";	
	/** The format used by the server to send the confirm message after a successful cache delete operation */
	public final static String CONFIRM_CACHE_DELETE = "DECAOK";	
	/** The format used by the server to send the confirm message after a successful change of the element number for cache's level */
	public final static String CONFIRM_CHANGE_CACHE_ELEMENT_NUMBER = "CHGEOK";	
	
	/** The format used by the client to request the change of the element number per cache's level */
	public final static String CHANGE_CLIENT_SIDE_CACHE_ELEMENT_NUMBER = "CHGCCN";
	/** The format used by the client to request the change of the element number per cache's level with the cache stored on the server*/
	public final static String CHANGE_SERVER_SIDE_CACHE_ELEMENT_NUMBER = "CHGSCN";
	
	/** The messages standard length */
	public final static int CODE_LENGTH = 6;
	
	/** The format used by the client to request the save of the superBlock */
	public final static String SAVE_SB_CODE = "SAVESB";
	/** The format used by the server to send the confirm message after a successful saving operation of the SuperBlock*/
	public final static String CONFIRM_SB_STORE = "SBLOK";
	
}

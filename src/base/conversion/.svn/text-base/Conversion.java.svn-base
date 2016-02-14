package base.conversion;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import base.bptree.INode;
import base.bptree.LNode;
import base.bptree.Node;
import base.disk.DiskSuperBlock;

/**
 * The conversion class
 * @author Tommaso
 *
 */
public class Conversion {

	/**
	 * Converts a String containing the node's information into a Node object
	 * 
	 * @param toConvert the string containing the node's information
	 * @param m the number of keys in a leaf node
	 * @param n the number of keys in a leaf node
	 * 
	 * @return the node object
	 */
	public static Node<Long,Long> stringToNode(String toConvert, int m, int n) {
		
		Node<Long,Long> node = null;
		
		boolean isLeaf = false;
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		bBuffer = ByteBuffer.wrap( new byte[ (toConvert.length()*2) ] );
		cBuffer = bBuffer.asCharBuffer();
		
		cBuffer.put(toConvert);
		
		byte[] withPadding = bBuffer.array();
		byte[] toIstance = new byte[ withPadding.length - 2 ];
		
		if( withPadding[0] == (byte)0  )
			isLeaf = true;
		
		System.arraycopy( withPadding, 1, toIstance, 0, withPadding.length - 2 );
		
		if( isLeaf ) {
			
			node =  new LNode<Long, Long>( null, m, n, toIstance );
			
		} else {
			
			node =  new INode<Long, Long>( null, n, toIstance );
			
		}
		
		return node;
	}
	
	/**
	 * Converts the informations stored in the string in a byte vector
	 * 
	 * @param toConvert the string containing the node's information
	 * 
	 * @return the byte vector containing the node's information
	 */
	public static byte[] stringToNodeBytes( String toConvert ) {
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		bBuffer = ByteBuffer.wrap( new byte[ (toConvert.length()*2) ] );
		cBuffer = bBuffer.asCharBuffer();
		
		cBuffer.put(toConvert);
		
		byte[] withPadding = bBuffer.array();
		byte[] result = new byte[ withPadding.length - 1 ];
		
		System.arraycopy( withPadding, 0, result, 0, withPadding.length - 1 );
		
		return result;
		
	}
	
	/**
	 * Converts a node object into a string containing its information
	 * 
	 * @param toConvert the node object
	 * 
	 * @return the string containing the node's information
	 */
	public static String nodeToString( Node<Long,Long> toConvert ) {
		
		return nodeBytesToString(toConvert.getBytes());
		
	}
	
	/**
	 * Converts a bytes vector containing the node information into a string
	 * 
	 * @param toConvert the bytes vector containing the node information
	 *  
	 * @return the string containing the node information
	 */
	public static String nodeBytesToString( byte[] toConvert ) {
	
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		byte[] toSend = new byte[ toConvert.length + 1 ];
		System.arraycopy( toConvert, 0, toSend, 0, toConvert.length );
		
		bBuffer = ByteBuffer.wrap(toSend);
		cBuffer = bBuffer.asCharBuffer();
		
		String result = cBuffer.toString();
		
		return result;
		
	}

	/**
	 * Converts a string containing the super block info into a DiskSuperBlock object
	 * 
	 * @param toConvert the string containing the super block info
	 * 
	 * @return the DiskSuperBlock object
	 */
	public static DiskSuperBlock stringToSuperBlock( String toConvert ) {
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		bBuffer = ByteBuffer.wrap( new byte[ (toConvert.length()*2) ] );
		cBuffer = bBuffer.asCharBuffer();
		
		cBuffer.put(toConvert);
		
		DiskSuperBlock result = new DiskSuperBlock(bBuffer.array());
		
		return result;
		
	}
	
	/**
	 * Converts the DiskSuperBlock object into a string containing its information
	 * 
	 * @param superBlock the DiskSuperBlock object
	 * 
	 * @return the string string containing the super block info
	 */
	public static String superBlockToString( DiskSuperBlock superBlock ) { 
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		bBuffer = ByteBuffer.wrap(superBlock.getBytes());
		cBuffer = bBuffer.asCharBuffer();
		
		String result = cBuffer.toString();
		
		return result;
		
	}
	
}

package server.debug;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import base.bptree.LNode;
import base.conversion.Conversion;
import base.disk.Disk;

public class TestNodeConversion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Disk<Long, Long> disk = new Disk<Long, Long>(true);
		
		LNode<Long, Long> node1 = new LNode<Long, Long>(disk);
		
		node1.num = 4;
		node1.nonce = 255;
			
		for(int i = 0 ; i < node1.M; i++) 
			node1.setKey(i, new Long(1));
			
		for(int i = 0 ; i < node1.M; i++) 
			node1.setValue(i, new Long(1));
					
		node1.printInfo();
		
		//TEST CONVERSIONE
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		byte[] node = node1.getBytes();
		byte[] toSend = new byte[ node.length + 1 ];
		System.arraycopy( node, 0, toSend, 0, node.length );
		
		bBuffer = ByteBuffer.wrap(toSend);
		cBuffer = bBuffer.asCharBuffer();	
		
		String nodeString = new String(cBuffer.toString());
		
		Conversion.stringToNode(nodeString, node1.M, node1.N);
		
	}

}

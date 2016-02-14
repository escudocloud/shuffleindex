package server.debug;

import base.bptree.INode;
import base.bptree.LNode;
import base.disk.Disk;

public class TestDisk {

	public static void main(String[] args) {
		
		Disk<Long, Long> disk = new Disk<Long, Long>(true);
		
		for(int a = 0 ; a < 3 ; a ++) {
			
			INode<Long, Long> node = new INode<Long, Long>(disk);
			
			node.num = 2;
			node.nonce = 63;
			
			for(int i = 0 ; i < node.num; i++) 
				node.setKey(i, new Long(a+1));
			
			for(int i = 0 ; i < node.num+1; i++) 
				node.children[i] = new Long(2);
					
			node.printInfo();
			
			disk.writeNode(node, true);
			
		}	
			
		LNode<Long, Long> node1 = new LNode<Long, Long>(disk);
			
		node1.num = 4;
		node1.nonce = 255;
			
		for(int i = 0 ; i < node1.M; i++) 
			node1.setKey(i, new Long(1));
			
		for(int i = 0 ; i < node1.M; i++) 
			node1.setValue(i, new Long(1));
					
		node1.printInfo();		
			
		disk.writeNode(node1, true);
		
		disk.readNode(264031).printInfo();
		
//		
//		long M = 4;
//		long N = 5;
//		
//		byte[] node;		
//		Long a = new Long(5);
//		
//		byte[] bArray = new byte[16];
//		ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
//		LongBuffer lBuffer = bBuffer.asLongBuffer();
//		lBuffer.put(M);
//		lBuffer.put(N);
//		
//		fs.writeBytes(bArray, 0);
		
//		Crypto.encryptNode(Crypto.createKey(), node);

	}

}

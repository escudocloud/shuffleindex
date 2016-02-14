package base.debug;

import base.bptree.INode;
import base.bptree.LNode;
import base.crypto.Crypto;
import base.disk.Disk;

public class TestCrypto {

	public static void main(String[] args) {
		
//		Crypto.createAndSaveKey();
		
		byte[] key = Crypto.loadKey();
		
		//Create node
		Disk<Long, Long> disk = new Disk<Long, Long>(false);
		LNode<Long, Long> node1 = new LNode<Long, Long>(disk);		
		node1.num = 4;		
		for(int i = 0 ; i < node1.M; i++) node1.setKey(i, new Long(1));			
		for(int i = 0 ; i < node1.M; i++) node1.setValue(i, new Long(1));
		
		System.out.println(node1.getBytes().length);
		System.out.println(Crypto.encryptBytes(key, node1.getBytes()).length);
		
		INode<Long, Long> node = new INode<Long, Long>(disk);
		node.num = 2;
		for(int i = 0 ; i < node.num; i++) node.setKey(i, new Long(1));
		for(int i = 0 ; i < node.num+1; i++) node.children[i] = new Long(1);
		
		node.printInfo();
		
		System.out.println(node.getBytes().length);
		
		byte[] encrypted = Crypto.encryptBytes(key, node.getBytes());	
		System.out.println(encrypted.length);
		
		byte[] decrypted = Crypto.decryptBytes(key, encrypted);
		System.out.println(decrypted.length);
		
		
		byte[] toInstance = new byte[decrypted.length];
		System.arraycopy(decrypted, 1, toInstance, 0, decrypted.length-1);
		INode<Long, Long> node3 = new INode<Long, Long>(disk, node.N, toInstance );
		node3.printInfo();
		
		System.out.println(node.getBytes().length + 16 - (node.getBytes().length % 16));
		
	}

}

package server.debug;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import base.disk.Disk;

public class TestSuperBlockConversion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Disk<Long, Long> disk = new Disk<Long, Long>(false);
		
//		System.out.println(disk.getDiskSuperBlock().getInfo());
//		System.out.println(disk.getDiskSuperBlock().getBytes().length);
		
		ByteBuffer bBuffer;
		CharBuffer cBuffer;
		
		bBuffer = ByteBuffer.wrap(disk.getDiskSuperBlock().getBytes());
		cBuffer = bBuffer.asCharBuffer();
		
		cBuffer.put('\n');
		
		System.out.println(cBuffer.toString());
		System.out.println("----------------");
		
//		for( byte b : bBuffer.array() ) {
//			System.out.println(b);
//		}
		
//		System.out.print((byte)'\n');
		
	}
	
}

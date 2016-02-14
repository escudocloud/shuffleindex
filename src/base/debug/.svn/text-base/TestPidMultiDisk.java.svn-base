package base.debug;

public class TestPidMultiDisk {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for( int i = 0 ; i < 4 ; i ++ ) {
			
			long pid = 264144;
			
			System.err.println(pid);
			
			pid = pid | ( (long)i << 61 ) ;
			System.err.println(pid);
			
			int index = (int)( (pid & 0x6000000000000000L) >> 61 );
			
			System.err.println(index);
			
			if( index == (byte)i )
				System.err.println("ok");
			
			System.err.println(pid & 0x1FFFFFFFFFFFFFFFL);
				
			System.err.println("--------------------------------------");
		}
		
	}

}

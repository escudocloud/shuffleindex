package client.debug;

import client.pir.Client;

public class TestPirCSC {

	public static void main(String[] args) {

		int num = 10;	
			
		Client client = new Client();
		
		long start, end, time;
		
//		client.openConnection();
//		
//		start = System.currentTimeMillis();
//				
//		for( int i = 0 ; i < num ; i++){
//			long key = (long)(client.getSuperBlock().getKeyNum()*Math.random()) + 1 ;			
//			client.pir( key);	
////			client.pir( num+1 );
////			System.out.println("Searched key:" + key + "   \tfound value:" + client.pir( key, num) + "   \tcover number:" + (i+1));			
//		}
//			
//		end = System.currentTimeMillis();	
//		time = end - start;		
//		System.err.println("Time with 2 fakes searches: \t\t\t" + time +"[ms] Search number: "+ num);
//			
//		client.closeConnection();
		
		client.openConnection();
		client.initClientSideCache(2);
		
		start = System.currentTimeMillis();
			
		for( int i = 0 ; i < num ; i++){
			long key = (long)(client.getSuperBlock().getKeyNum()*Math.random()) + 1 ;			
			try { 
				long value = client.pirCSC( key, 2); 
//				long value = client.find(i);
				if(value != key)
					System.err.println("Errore Key:" + key);
			} catch (Exception e) {
				client.saveClientSideCache();
				e.printStackTrace(); 
				System.err.println(i);
				System.exit(0);
			}
		}
			
		end = System.currentTimeMillis();	
		time = end - start;				
		System.err.println("Time with variable number of cover searches: \t" + time +"[ms] Search number: "+ num);
			
		client.saveClientSideCache();
		
		client.closeConnection();

	}

}

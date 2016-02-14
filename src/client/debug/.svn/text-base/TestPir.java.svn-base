package client.debug;

import client.pir.Client;

public class TestPir {

	static Client client;
	
	public static void main(String[] args) {
		
		client = new Client();
		client.openConnection();

		long start, end, time;
		start = System.currentTimeMillis();
		
		int num = 8191;
		
		for( long i = 0 ; i < num ; i++){
				
			client.pirCSC(i+1);
			
//			if(client.pir(i+1) != i+1) 
//				System.err.println("ERRORE");
			
			if( (i+1) %500 == 0)
				System.err.println(i+1);
			
		}
		
		end = System.currentTimeMillis();	
		time = end - start;	
		
		System.out.println("Time : " + time/1000 + "[s] "+ time +"[ms] Search number: "+ num);
		
		client.closeConnection();
		
	}

}

package client.debug.performance;

import client.pir.Client;

public class Search {

	public static void main(String[] args) {

		long start, end, time;
	
		Client client = new Client();
		
		client.openConnection();		
			System.err.println("1st search: " + client.pirCSC(1000L));
	
		start = System.currentTimeMillis();
			System.err.println("2nd search: " + client.pirCSC(500L));
			
		client.closeConnection();
		end = System.currentTimeMillis();	
		time = end - start;	
		System.err.println("Time in [ms] for the pir search algorithm: " +time);
		
		
		client.openConnection();
		start = System.currentTimeMillis();	
			System.err.println("3th search: " + client.find(1023L));
			client.closeConnection();
		end = System.currentTimeMillis();	
		time = end - start;	
		System.err.println("Time in [ms] for the traditional search algorithm: " +time);
		
		
	}

}

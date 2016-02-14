package client.debug;

import client.pir.Client;

public class TestClientSideCache {

	public static void main(String[] args) {

		Client client = new Client();
		client.openConnection();
		
//		System.err.println(client.getSuperBlockInfo());
		
		client.initClientSideCache(2);
		
		System.err.println(client.pirCSC(200000));
		
		client.saveClientSideCache();
		
		client.closeConnection();

	}

}

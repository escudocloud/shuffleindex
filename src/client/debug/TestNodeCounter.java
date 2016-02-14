package client.debug;

import client.pir.Client;

public class TestNodeCounter {

	public static void main(String[] args) {

		Client client = new Client();
		client.openConnection();
		System.err.println(client.getSuperBlock().getInnerNodeNum());
		System.err.println(client.getSuperBlock().getLeafNodeNum());
		client.closeConnection();
		
	}

}

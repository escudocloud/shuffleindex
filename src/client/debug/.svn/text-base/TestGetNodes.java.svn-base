package client.debug;

import java.util.LinkedList;

import base.bptree.INode;
import base.bptree.Node;
import client.pir.Client;

public class TestGetNodes {

	static Client client;
	
	public static void main(String[] args) {
		
		client = new Client();
		client.openConnection();
		
		LinkedList<Long> pids = new LinkedList<Long>();
		pids.add(client.getSuperBlock().getRootPid());
		
//		LinkedList<Node<Long,Long>> nodes = client.getNodes(pids);

//		printAll(nodes.get(0), "");
		
		client.closeConnection();
	
	}
	
	public static void printAll(Node<Long, Long> node, String tab) {
		
		if(node.num != 0) {
			System.out.print(tab);
			node.print();	
		}	
		
		if(node instanceof INode<?,?>) {		
			for(int i=0 ; i < node.num+1 ; i++) {	
				
				LinkedList<Long> pids = new LinkedList<Long>();
				pids.add(((INode<Long, Long>)node).children[i]);
//				LinkedList<Node<Long,Long>> nodes = client.getNodes(pids);
				
//				printAll(nodes.get(0), tab + "    ");
			}
		}
		
	}

}

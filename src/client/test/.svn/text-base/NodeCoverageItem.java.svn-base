package client.test;

import java.util.LinkedList;

public class NodeCoverageItem {
	
	private LinkedList<LinkedList<Long>> pids;
	private LinkedList<LinkedList<Long>> vids;
	
	private int length;
	
	public NodeCoverageItem( int height ) {
		
		length = height - 1;
		
		pids = new LinkedList<LinkedList<Long>>();
		vids = new LinkedList<LinkedList<Long>>();
		
		for( int i = 0 ; i < length ; i++ ) {
			pids.add(new LinkedList<Long>());
			vids.add(new LinkedList<Long>());
		}
			
	}
	
	public void addPid(Long pid, int level) {
		
		if(!pids.get(level-2).contains(pid)) {
			pids.get(level-2).add( pid );
		}
		
	}
	
	public void addVid(Long vid, int level) {
		
		if(!vids.get(level-2).contains(vid)) {
			vids.get(level-2).add( vid );
		}
		
	}
	
	public int getPidsNumber( int level ) {
		return pids.get(level-2).size();
	}
	
	public int getVidsNumber( int level ) {
		return vids.get(level-2).size();
	}
	
}

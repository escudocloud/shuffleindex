package client.test;

import java.io.File;
import java.util.LinkedList;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import base.log.Log;

public class NodeCoverage {

	private Logger logger;
	
	private LinkedList<NodeCoverageItem> nodeCoverage;
	
	public NodeCoverage() {
		nodeCoverage = new LinkedList<NodeCoverageItem>();
	}
	
	public void add(NodeCoverageItem nci) {
		nodeCoverage.add(nci);
	} 
	
	public void generateFile( long bptreeHeight, double profile, long innerNodeNumber, long leafNodeNumber, int infSearchNumber, int supSearchNumber, int stepLength, int cacheElementNumber, int coverSearchNumber) {
		
		//Pid coverage
		for( int a = 0 ; a < bptreeHeight - 1 ; a++) {			
			try{				
				logger = Logger.getLogger( "Pid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) );				
				logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + "Pid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) + ".log" ) );		
				logger.setLevel(Log.NODE_ID_LOG_LEVEL);				
			}catch(Exception e){ 				
				e.printStackTrace(); 				
			}
			
			for( int i = infSearchNumber, n = 0 ; i <= supSearchNumber  ; i = i + stepLength, n++ ) {			
				if(a == bptreeHeight - 2 )
					logger.info( i + "\t" + (double)( (double)nodeCoverage.get(n).getPidsNumber(a+2) / (double)leafNodeNumber ) + "\t" + nodeCoverage.get(n).getPidsNumber(a+2) + "\t" + leafNodeNumber);	
				else
					logger.info( i + "\t" + (double)( (double)nodeCoverage.get(n).getPidsNumber(a+2) / (double)(innerNodeNumber - 1 )) + "\t" + nodeCoverage.get(n).getPidsNumber(a+2) + "\t" +  (innerNodeNumber-1));	
			}
		}
		
		//Vid coverage
		for( int a = 0 ; a < bptreeHeight - 1 ; a++) {			
			try{				
				logger = Logger.getLogger( "Vid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) );				
				logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + "Vid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) + ".log" ) );		
				logger.setLevel(Log.NODE_ID_LOG_LEVEL);				
			}catch(Exception e){ 				
				e.printStackTrace(); 				
			}
			
			for( int i = infSearchNumber, n = 0 ; i <= supSearchNumber  ; i = i + stepLength, n++ ) {			
				if(a == bptreeHeight - 2 )
					logger.info( i + "\t" + (double)( (double)nodeCoverage.get(n).getVidsNumber(a+2) / (double)leafNodeNumber ) + "\t" + nodeCoverage.get(n).getVidsNumber(a+2) + "\t" +  leafNodeNumber);	
				else
					logger.info( i + "\t" + (double)( (double)nodeCoverage.get(n).getVidsNumber(a+2) / (double)(innerNodeNumber - 1 )) + "\t" + nodeCoverage.get(n).getVidsNumber(a+2) + "\t" +  (innerNodeNumber-1));	
			}
		}
		
	}
	
	public void generateFile( long bptreeHeight, double profile, int infSearchNumber, int supSearchNumber, int stepLength, int cacheElementNumber, int coverSearchNumber, int mfo) {
		
		//Pid coverage
		for( int a = 0 ; a < bptreeHeight - 1 ; a++) {			
			try{				
				logger = Logger.getLogger( "Pid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) );				
				logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + "Pid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) + ".log" ) );		
				logger.setLevel(Log.NODE_ID_LOG_LEVEL);				
			}catch(Exception e){ 				
				e.printStackTrace(); 				
			}
			
			for( int i = infSearchNumber, n = 0 ; i <= supSearchNumber  ; i = i + stepLength, n++ ) {			
				if(a == bptreeHeight - 2 )
					logger.info( i + "\t" + ( (double)nodeCoverage.get(n).getPidsNumber(a+2) / (double)(Math.pow((mfo+1),(bptreeHeight-1))) ) + "\t" + nodeCoverage.get(n).getPidsNumber(a+2) + "\t" + Math.pow((mfo+1),(bptreeHeight-1)) );	
				else
					logger.info( i + "\t" + ( (double)nodeCoverage.get(n).getPidsNumber(a+2) / (double)(Math.pow((mfo+1),(a+1))) ) + "\t" + nodeCoverage.get(n).getPidsNumber(a+2) + "\t" + Math.pow((mfo+1),(a+1)) );	
			}
		}
		
		//Vid coverage
		for( int a = 0 ; a < bptreeHeight - 1 ; a++) {			
			try{				
				logger = Logger.getLogger( "Vid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) );				
				logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + "Vid_Coverage_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (a+2) + ".log" ) );		
				logger.setLevel(Log.NODE_ID_LOG_LEVEL);				
			}catch(Exception e){ 				
				e.printStackTrace(); 				
			}
			
			for( int i = infSearchNumber, n = 0 ; i <= supSearchNumber  ; i = i + stepLength, n++ ) {			
				if(a == bptreeHeight - 2 )
					logger.info( i + "\t" + ( (double)nodeCoverage.get(n).getVidsNumber(a+2) / (double)(Math.pow((mfo+1),(bptreeHeight-1))) ) + "\t" + nodeCoverage.get(n).getVidsNumber(a+2) + "\t" +  Math.pow((mfo+1),(bptreeHeight-1)) );	
				else
					logger.info( i + "\t" + ( (double)nodeCoverage.get(n).getVidsNumber(a+2) / (double)(Math.pow((mfo+1),(a+1))) ) + "\t" + nodeCoverage.get(n).getVidsNumber(a+2) + "\t" + Math.pow((mfo+1),(a+1)) );	
			}
		}
		
	}
}

package client.test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import base.log.Log;
import client.pir.Client;

public class Test {
	
	/** Logger for this class */
	static final private Logger logger = Logger.getLogger(Test.class);
	
	/** Performance test parameters */
	static final private int 		P_SETUP_SEARCH_NUMBER 			= 1000;
	static final private int 		P_SEARCH_NUMBER_PER_TEST_CASE	= 30;
	static final private int 		P_INF_CACHE_ELEMENT_NUMBER 		= 1;
	static final private int 		P_SUP_CACHE_ELEMENT_NUMBER		= 10;
	static final private int 		P_INF_COVER_SEARCH_NUMBER		= 1;
	static final private int 		P_SUP_COVER_SEARCH_NUMBER 		= 10;
	static final private double[] 	P_PROFILES 						= {0.5};
	
	/** Node id log test parameters */
	static final private int 		NIL_SEARCH_NUMBER_PER_TEST_CASE = 1000;	
	static final private int 		NIL_INF_CACHE_ELEMENT_NUMBER 	= 4;
	static final private int 		NIL_SUP_CACHE_ELEMENT_NUMBER 	= 4;
	static final private int 		NIL_INF_COVER_SEARCH_NUMBER 	= 4;
	static final private int 		NIL_SUP_COVER_SEARCH_NUMBER 	= 4;
	static final private double[] 	NIL_PROFILES 					= {0.125, 0.25, 0.5};
	
	/** Node coverage test parameters */
	static final private int 		NC_INF_SEARCH_NUMBER 			= 1000;
	static final private int 		NC_SUP_SEARCH_NUMBER 			= 10000;
	static final private int 		NC_STEP_LENGTH 					= 500;
	static final private double[] 	NC_PROFILES 					= {0.125, 0.25, 0.5};
	static final private int 		NC_CACHE_ELEMENT_NUMBER 		= 4;
	static final private int 		NC_COVER_SEARCH_NUMBER 			= 4;
	
	/** 
	 * Cover and target nodes analysis, with target chosen 
	 * according to a given profile and covers randomly chosen 
	 */
	static final private int 		CRTP_NUMBER_OF_SEARCH 			= 1000;
	static final private int 		CRTP_DISTANCE_TO_CHECK 			= 100;
	static final private int 		CRTP_CACHE_ELEMENT_NUMBER 		= 1;
	static final private int 		CRTP_INF_COVER_SEARCH_NUMBER 	= 1;
	static final private int 		CRTP_SUP_COVER_SEARCH_NUMBER 	= 10;
	static final private double[] 	CRTP_TARGET_KEY_PROFILES  		= {0.125, 0.25, 0.5};
	
	/** 
	 * Cover and target nodes analysis, with target chosen 
	 * according to a given profile and covers chosen with the 
	 * probability distribution used for the key
	 */
	static final private int 		CPTP_NUMBER_OF_SEARCH 			= 1000;
	static final private int 		CPTP_DISTANCE_TO_CHECK 			= 100;
	static final private int 		CPTP_CACHE_ELEMENT_NUMBER 		= 1;
	static final private int 		CPTP_INF_COVER_SEARCH_NUMBER 	= 1;
	static final private int 		CPTP_SUP_COVER_SEARCH_NUMBER 	= 10;
	static final private double[] 	CPTP_TARGET_KEY_PROFILES 		= {0.125, 0.25, 0.5};
	static final private int 		CPTP_DOMAIN_WINDOW				= 10000;
	static final private int 		CPTP_INITIAL_NUMBER_OF_ACCESS	= 100000;
	static final private int  		CPTP_NUM_RESEARCHES				= 300;
	
	private Client client;
	
	public Test() {
		
	}
	
	public void runPerformanceTest() throws Exception {
		
		try{
			logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), Log.PERFORMANCE_LOG_FILE_PATH) );
		}catch(Exception e){ e.printStackTrace(); }

		logger.setLevel(Log.PERFORMANCE_LOG_LEVEL);
		
		client = new Client();
		client.openConnection();
		client.initClientSideCache(2);
		
		//Setup
		// setup(); 
		System.err.println("Setup finished");
		
		//Test
		for( int i = 0 ; i < P_PROFILES.length ; i++ ) {
			for( int a = P_INF_CACHE_ELEMENT_NUMBER ; a <= P_SUP_CACHE_ELEMENT_NUMBER ; a++ ) {
				for( int b = P_INF_COVER_SEARCH_NUMBER ; b <= P_SUP_COVER_SEARCH_NUMBER ; b++ ) {
					performanceTest(P_PROFILES[i], a, b);
					System.err.println(a + " " + b);
				}
			}
		}
		
		client.saveClientSideCache();
		client.closeConnection();
		
	}
	
	public void setup() throws Exception {
		for( int i = 0 ; i < P_SETUP_SEARCH_NUMBER ; i++ ) {
			
			long key = (long)(client.getSuperBlock().getKeyNum()*Math.random()) + 1 ;
			client.pirCSC(key, 2);
			
		}
	}
	
	public void insertTest(int cacheElementNumber, int coverSearchNumber, long TargetKey, long targetValue) throws Exception {
		try{
			logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), Log.PERFORMANCE_LOG_FILE_PATH) );
		}catch(Exception e){ e.printStackTrace(); }

		logger.setLevel(Log.PERFORMANCE_LOG_LEVEL);
		
		client = new Client();
		client.openConnection();
		client.initClientSideCache(2);
		
		//Setup
		// setup(); 
		System.err.println("Setup finished");
		
		
		client.initClientSideCache(cacheElementNumber);
		
		long value = client.pirCSC(TargetKey, coverSearchNumber, targetValue);
		System.err.println("Inserted value="+value+" in the Leaf with key="+TargetKey);
		
		
		client.saveClientSideCache();
		client.closeConnection();
	}
	
	
	
	public void performanceTest(double profile, int cacheElementNumber, int coverSearchNumber) throws Exception {
		
		long start, end;
		double time = 0.0, delta=0.0, mean = 0.0, mean_trad = 0.0, m2=0.0, m2_trad=0.0;
		double min_time = 1000.0;
		long key, value;
		
		client.initClientSideCache(cacheElementNumber);
		
			
		//The first search have a too high associated time 
		//key = generateKey(client.getSuperBlock().getKeyNum(), profile);	
		//client.pirCSC(key, coverSearchNumber);
		ArrayList<Double> listTemp = new ArrayList<Double>();
		
		for( int i = 1 ; i <= P_SEARCH_NUMBER_PER_TEST_CASE ; i++ ) {
			
			key = generateKey(client.getSuperBlock().getKeyNum(), profile);
			
			System.gc();
			start = System.nanoTime();
			value = client.pirCSC(key, coverSearchNumber);
			end = System.nanoTime();
			System.gc();
			time = ((end - start)/(double)1000);
			listTemp.add(time);
			delta = time - mean;
//			mean = mean + delta / ((double)i);
			m2=m2+delta*(time-mean);
			
			if(value != key)
				System.err.println("Errore Key:" + key);
		}
		
		Collections.sort(listTemp, new Comparator<Double>(){
			   @Override
			   public int compare(Double o1, Double o2){
			        if(o1.floatValue()  < o2.floatValue()){
			           return -1; 
			        }
			        if(o1.floatValue()  > o2.floatValue()){
			           return 1; 
			        }
			        return 0;
			   }
			}); 
//		mean = 0.0;
//		for (int i = 0; i< (listTemp.size()-P_SEARCH_NUMBER_PER_TEST_CASE/10); ++i) 
//			mean += listTemp.get(i);
//		mean /=(listTemp.size()-P_SEARCH_NUMBER_PER_TEST_CASE/10);
		mean = listTemp.get(listTemp.size()/2);
		m2 = Math.sqrt(m2/(P_SEARCH_NUMBER_PER_TEST_CASE-1));
		
		for( int i = 1 ; i <= P_SEARCH_NUMBER_PER_TEST_CASE ; i++ ) {
			
			key = generateKey(client.getSuperBlock().getKeyNum(), profile);
			
			start = System.nanoTime();//currentTimeMillis();
			value = client.find(key);
			end = System.nanoTime();//currentTimeMillis();
			time = ((end - start)/(double)1000);
			delta = time - mean_trad;
			mean_trad = mean_trad + delta / ((double)i);
			m2_trad=m2_trad+delta*(time-mean_trad);
			
			if(value != key)
				System.err.println("Errore Key:" + key);
			
		}
		m2_trad = Math.sqrt(m2_trad/(P_SEARCH_NUMBER_PER_TEST_CASE-1));
		//Log
		logger.info(cacheElementNumber + "\t" + coverSearchNumber + "\t" + 
		       String.format("%6.2f",mean/1000) + "\t" + String.format("%6.2f",mean_trad/1000) + "\t" + 
		       String.format("%6.2f",m2/1000) + "\t" + String.format("%6.2f",m2_trad/1000)
		);
		
	}
	
	public void runNodeIdLogTest() {
		
		client = new Client();
		client.openConnection();
		
		String outputFolder =	"MFO_" + client.getSuperBlock().getM() + "_HEIGHT_" + client.getSuperBlock().getHeight();
		
		//Test
		for( int i = 0 ; i < NIL_PROFILES.length ; i++ ) {
			for( int a = NIL_INF_CACHE_ELEMENT_NUMBER ; a <= NIL_SUP_CACHE_ELEMENT_NUMBER ; a++ ) {
				client.initClientSideCache(a);
				for( int b = NIL_INF_COVER_SEARCH_NUMBER ; b <= NIL_SUP_COVER_SEARCH_NUMBER ; b++ ) {
					
					new File( "log" + File.separator + outputFolder + File.separator + "cache_" + a + "_cover_" + b  ).mkdirs();
					new File( "log" + File.separator + outputFolder + File.separator + "cache_" + a + "_cover_" + b + File.separator + "data" ).mkdirs();
					new File( "log" + File.separator + outputFolder + File.separator + "cache_" + a + "_cover_" + b + File.separator + "ps" ).mkdirs();
					new File( "log" + File.separator + outputFolder + File.separator + "cache_" + a + "_cover_" + b + File.separator + "script" ).mkdirs();
					
					nodeIdLogTest(NIL_PROFILES[i], a, b, outputFolder + File.separator + "cache_" + a + "_cover_" + b );
					System.err.println("Test on profile "  + NIL_PROFILES[i] + " with "+ a + " cache element and  " + b + " cover serches done!");
				
				}
			}
		}
		
		//Generates script files 
		for( int a = NIL_INF_CACHE_ELEMENT_NUMBER ; a <= NIL_SUP_CACHE_ELEMENT_NUMBER ; a++ ) {
			for( int b = NIL_INF_COVER_SEARCH_NUMBER ; b <= NIL_SUP_COVER_SEARCH_NUMBER ; b++ ) {
				
				NodeIdLog.generateGnuplotScript(a, b, (int)client.getSuperBlock().getHeight(), outputFolder + File.separator + "cache_" + a + "_cover_" + b );
			
			}
		}
		
		client.saveClientSideCache();
		client.closeConnection();
	}
	
	public void nodeIdLogTest(double profile, int cacheElementNumber, int coverSearchNumber, String outputFolder) {
		
		NodeIdLog nil = new NodeIdLog((int)client.getSuperBlock().getHeight(), profile, cacheElementNumber, coverSearchNumber);	
		for( int i = 0 ; i < NIL_SEARCH_NUMBER_PER_TEST_CASE ; i++ ) {
			long key = generateKey(client.getSuperBlock().getKeyNum(), profile);
			client.pirCSCNodeIdLog(key, coverSearchNumber, nil);
		}	
		nil.generateLogFile(outputFolder);
		
	}
	
	public void runNodeCoverageTest() {
		
		client = new Client();
		client.openConnection();
				
		client.initClientSideCache(NC_CACHE_ELEMENT_NUMBER);
		
		//Test
		for( int i = 0 ; i < NC_PROFILES.length ; i++ ) { 
			System.err.println("Starting test on profile "  + NC_PROFILES[i]);
			nodeCoverageTest(NC_PROFILES[i]);
			System.err.println("Test on profile "  + NC_PROFILES[i] + " done!");
		}
		
		client.saveClientSideCache();
		client.closeConnection();
		
	}
	
	public void nodeCoverageTest( double profile ) {
		
		NodeCoverage nc = new NodeCoverage();
		
		for ( int i = NC_INF_SEARCH_NUMBER ; i <= NC_SUP_SEARCH_NUMBER ; i = i + NC_STEP_LENGTH ) {
			
			System.err.println(i);
			
			NodeCoverageItem nci = new NodeCoverageItem(( int)client.getSuperBlock().getHeight() );
			
			for( int s = 0 ; s < i ; s++ ) {
				long key = generateKey(client.getSuperBlock().getKeyNum(), profile);
				long value = client.pirCSCNodeCoverage(key, NC_COVER_SEARCH_NUMBER, nci);
				if(value != key)
					System.err.println("Errore Key:" + key);
			}
				
			nc.add(nci);
			
		}
		
		//Generate file
		nc.generateFile(client.getSuperBlock().getHeight(), profile, NC_INF_SEARCH_NUMBER, NC_SUP_SEARCH_NUMBER, NC_STEP_LENGTH, NC_CACHE_ELEMENT_NUMBER, NC_COVER_SEARCH_NUMBER, client.getSuperBlock().getM());
	
	}
	
	public void runCoverAndTargetCompareAnalysisWithRandomCoversTest(){
		
		client = new Client();
		client.openConnection();
				
		client.initClientSideCache(CRTP_CACHE_ELEMENT_NUMBER);
		
		//Test
		for( int i = 0 ; i < CRTP_TARGET_KEY_PROFILES.length ; i++ ){ 
			System.err.println("Starting test on profile "  + CRTP_TARGET_KEY_PROFILES[i]);		
			for( int b = CRTP_INF_COVER_SEARCH_NUMBER ; b <= CRTP_SUP_COVER_SEARCH_NUMBER ; b++ ){
				CoverAndTargetCompareAnalysisWithRandomCoversTest(CRTP_TARGET_KEY_PROFILES[i], b);
				System.err.println("Test on profile "  + CRTP_TARGET_KEY_PROFILES[i] + " with "+ CRTP_CACHE_ELEMENT_NUMBER + " cache element and  " + b + " cover serches done!");
			}
		}
		
		client.saveClientSideCache();
		client.closeConnection();
		
	}
	
	public void CoverAndTargetCompareAnalysisWithRandomCoversTest(double profile, int coverNum){
		
		LinkedList<HashMap<Long, String>> logWriteList = new LinkedList<HashMap<Long, String>>();
		LinkedList<HashMap<Long, String>> logReadList = new LinkedList<HashMap<Long, String>>();
		
		for( int i = 0 ; i < CRTP_NUMBER_OF_SEARCH ; i++ ){
			long key = generateKey(client.getSuperBlock().getKeyNum(), profile);
			HashMap<Long, String> wLog = new HashMap<Long, String>();
			HashMap<Long, String> rLog = new HashMap<Long, String>();
			client.pirCSCLeafsPidLog(key, coverNum, rLog, wLog);
			logWriteList.add(wLog);
			logReadList.add(rLog); 
		}
			
		analyzeLeafsPidLogAndGenerateLogFile("CoverAndTargetAnalysisWithRandomCovers", logReadList, logWriteList, CRTP_NUMBER_OF_SEARCH, CRTP_DISTANCE_TO_CHECK, profile, CRTP_CACHE_ELEMENT_NUMBER, coverNum);
		
	}
	
	public void runCoverAndTargetCompareAnalysis(){
		
		client = new Client();
		client.openConnection();
				
		client.initClientSideCache(CPTP_CACHE_ELEMENT_NUMBER);
		
		//Test
		for( int i = 0 ; i < CPTP_TARGET_KEY_PROFILES.length ; i++ ){ 
			System.err.println("Starting test on profile "  + CPTP_TARGET_KEY_PROFILES[i]);		
			for( int b = CPTP_INF_COVER_SEARCH_NUMBER ; b <= CPTP_SUP_COVER_SEARCH_NUMBER ; b++ ){
				CoverAndTargetCompareAnalysis(CPTP_TARGET_KEY_PROFILES[i], b);
				System.err.println("Test on profile "  + CPTP_TARGET_KEY_PROFILES[i] + " with "+ CPTP_CACHE_ELEMENT_NUMBER + " cache element and  " + b + " cover serches done!");
			}
		}
		
		client.saveClientSideCache();
		client.closeConnection();
		
	}
	
	public void CoverAndTargetCompareAnalysis(double profile, int coverNum){
		
		EsteemedAccessProfile eap = new EsteemedAccessProfile(	client.getSuperBlock().getKeyNum(), 
																CPTP_DOMAIN_WINDOW, 
																CPTP_INITIAL_NUMBER_OF_ACCESS, 
																CPTP_NUM_RESEARCHES, 
																profile );
		
		LinkedList<HashMap<Long, String>> logWriteList = new LinkedList<HashMap<Long, String>>();
		LinkedList<HashMap<Long, String>> logReadList = new LinkedList<HashMap<Long, String>>();
		
		for( int i = 0 ; i < CPTP_NUMBER_OF_SEARCH ; i++ ){
			
			long key = eap.genselfsimilar();
			eap.assessFdf(key);
			eap.computePdfCdf();
			
			HashMap<Long, String> wLog = new HashMap<Long, String>();
			HashMap<Long, String> rLog = new HashMap<Long, String>();
			client.pirCSCLeafsPidLogWithEsteemedAccessprofile(key, coverNum, eap, rLog, wLog);
			logWriteList.add(wLog);
			logReadList.add(rLog); 
		}
			
		analyzeLeafsPidLogAndGenerateLogFile("CoverAndTargetAnalysis", logReadList, logWriteList, CPTP_NUMBER_OF_SEARCH, CPTP_DISTANCE_TO_CHECK, profile, CPTP_CACHE_ELEMENT_NUMBER, coverNum);
		
	}
	
	public void analyzeLeafsPidLogAndGenerateLogFile(	String fileNamePrefix,
														LinkedList<HashMap<Long, String>> logReadList, 
														LinkedList<HashMap<Long, String>> logWriteList, 
														int numberOfSearch,
														int distanceToCheck, 
														double profile, 
														int cacheElementNumber, 
														int coverNum){
				
		String fileName = fileNamePrefix + "_searches_" + numberOfSearch + "_distance_" + distanceToCheck + "_profile_" + profile + "_cacheElementNumber_" + cacheElementNumber + "_coverNum_" + coverNum + ".log";
		Logger logger = Logger.getLogger(fileName);				
		try {
			logger.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + "test" + File.separator + fileName) );
		} catch (IOException e) {
			e.printStackTrace();
		}		
		logger.setLevel(Level.ALL);		
		
		Integer[] cover = new Integer[distanceToCheck];
		Integer[] target = new Integer[distanceToCheck];
		
		for( int i=0 ; i < distanceToCheck ; i++ ){
			cover[i] = 0;
			target[i] = 0;
		}
		
		for( int i = distanceToCheck ; i < logReadList.size() ; i++ ){
			
			Set<Long> readSet = logReadList.get(i).keySet();
			Iterator<Long> readIter = readSet.iterator();
			
			while(readIter.hasNext()){
				
				Long readPid = readIter.next();
			
				for( int a = (i-distanceToCheck) ; a < i ; a++){
						
					Set<Long> writeSet = logWriteList.get(a).keySet();
					Iterator<Long> writeIter = writeSet.iterator();
					
					while(writeIter.hasNext()){
						
						Long writePid = writeIter.next();
						
						if(writePid.equals(readPid)){
							
							if(logReadList.get(i).get(readPid).equals("cover")){
								cover[i-1-a] = cover[i-1-a] + 1 ;
							} else if(logReadList.get(i).get(readPid).equals("target")){
								target[i-1-a] = target[i-1-a] + 1 ;
							}
								
						}
					}
				}
			}
		}
		
		for( int i=0 ; i < distanceToCheck ; i++ ){
			logger.info(	(cover[i]/coverNum) + " " + 
							target[i] + " " + 
							((cover[i]/coverNum) - target[i]));
		}
		
	}
	
	public long generateKey( long supLimit, double profile) {
		
		return (long)(supLimit * Math.pow(Math.random(), Math.log(profile)/Math.log(1-profile)) + 1 );
		
	}
	
}


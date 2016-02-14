package client.test;


public class Main {

	public static void main(String[] args) {
		
		try {
			Test test = new Test();
		    
//			test.insertTest(1, 1, 70, 70);
//			test.insertTest(1, 1, 100, 100);
//			test.insertTest(1, 1, 140, 140);
//			test.insertTest(1, 1, 160, 160);
//			test.insertTest(1, 1, 80, 80);
//			test.insertTest(1, 1, 90, 90);
//			test.insertTest(1, 1, 66, 66);
//			test.insertTest(1, 1, 68, 68);
//			test.insertTest(1, 1, 65, 65);
			
			test.runPerformanceTest();

//			test.runNodeIdLogTest();
		
//			test.runNodeCoverageTest();
		
//			test.runCoverAndTargetCompareAnalysisWithRandomCoversTest();
		
//			test.runCoverAndTargetCompareAnalysis();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

}

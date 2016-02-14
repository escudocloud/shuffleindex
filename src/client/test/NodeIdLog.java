package client.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import base.log.Log;

public class NodeIdLog {
	
	private Logger loggerPids;
	private Logger loggerVids;
	
	private LinkedList<LinkedHashMap<Long, Long>> pids;
	private LinkedList<LinkedHashMap<Long, Long>> vids;
	
	private String pid_log_file_name_prefix;
	private String vid_log_file_name_prefix;
	
	private int length;
	
	public NodeIdLog( int height, double profile, int cacheElementNumber, int coverSearchNumber ) {
		
		pid_log_file_name_prefix = "Pid_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_";
		vid_log_file_name_prefix = "Vid_" + profile + "_" + cacheElementNumber + "_" + coverSearchNumber + "_";
		
		length = height - 1;
		
		pids = new LinkedList<LinkedHashMap<Long, Long>>();
		vids = new LinkedList<LinkedHashMap<Long, Long>>();
		
		for( int i = 0 ; i < length ; i++ ) {
			pids.add(new LinkedHashMap<Long, Long>());
			vids.add(new LinkedHashMap<Long, Long>());
		}
			
	}
	
	public void generateLogFile(String outputFolder) {
		
		for( int i = 0 ; i < length ; i++ ) {
			generateLogFile(i, outputFolder);
		}
			
	}
	
	private void generateLogFile( int l,  String outputFolder) {
		
		try{
			
			loggerPids = Logger.getLogger( pid_log_file_name_prefix + (l+2) );
			loggerVids = Logger.getLogger( vid_log_file_name_prefix + (l+2) );
			
			loggerPids.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + outputFolder + File.separator + "data" + File.separator + pid_log_file_name_prefix + (l+2) + ".log" ) );
			loggerVids.addAppender( new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN), "log" + File.separator + outputFolder + File.separator + "data" + File.separator + vid_log_file_name_prefix + (l+2) + ".log" ) );
		
			loggerPids.setLevel(Log.NODE_ID_LOG_LEVEL);
			loggerVids.setLevel(Log.NODE_ID_LOG_LEVEL);
			
		}catch(Exception e){ 
			
			e.printStackTrace(); 
			
		}
		
		LinkedHashMap<Long, Long> allPids = new LinkedHashMap<Long, Long>();
		LinkedHashMap<Long, Long> allVids = new LinkedHashMap<Long, Long>();
		
		allPids.putAll(pids.get(l));
		allVids.putAll(vids.get(l));
		
		//Sort pids	
		List<Long> mapKeys = new ArrayList<Long>(allPids.keySet());
		List<Long> mapValues = new ArrayList<Long>(allPids.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);
		
		Collections.reverse(mapValues);

		LinkedHashMap<Long, Long> sortedPids = new LinkedHashMap<Long, Long>();
		Iterator<Long> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Long val = valueIt.next();
			Iterator<Long> keyIt = mapKeys.iterator();
			while (keyIt.hasNext()) {
				Long key = keyIt.next();
				if (allPids.get(key) == val) {
					allPids.remove(key);
					mapKeys.remove(key);
					sortedPids.put(key, val);
					break;
				}
			}
		} 
		
		//Log pids on file
		mapKeys = new ArrayList<Long>(sortedPids.keySet());
		Iterator<Long> keyIt = mapKeys.iterator();		
		while (keyIt.hasNext()) {			  
			Long key = keyIt.next();		
			loggerPids.info(key + "\t" + sortedPids.get(key));				 
		} 
		
		//Sort vids	
		mapKeys = new ArrayList<Long>(allVids.keySet());
		mapValues = new ArrayList<Long>(allVids.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);
		
		Collections.reverse(mapValues);

		LinkedHashMap<Long, Long> sortedVids = new LinkedHashMap<Long, Long>();
		valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Long val = valueIt.next();
			keyIt = mapKeys.iterator();
			while (keyIt.hasNext()) {
				Long key = keyIt.next();
				if (allVids.get(key) == val) {
					allVids.remove(key);
					mapKeys.remove(key);
					sortedVids.put(key, val);
					break;
				}
			}
		} 
		
		//Log vids on file
		mapKeys = new ArrayList<Long>(sortedVids.keySet());
		keyIt = mapKeys.iterator();		
		while (keyIt.hasNext()) {			  
			Long key = keyIt.next();		
			loggerVids.info(key + "\t" + sortedVids.get(key));				 
		} 
		
		loggerPids.removeAllAppenders();
		
	}
	
	public void addPid(Long pid, int level) {
		
		if(pids.get(level-2).containsKey(pid)) {
			Long freq  = pids.get(level-2).get(pid);
			pids.get(level-2).remove(pid);
			pids.get(level-2).put( pid, freq + 1 );
		} else {
			pids.get(level-2).put(pid, 1L);
		}
		
	}
	
	public void addVid(Long vid, int level) {
		
		if(vids.get(level-2).containsKey(vid)) {
			Long freq  = vids.get(level-2).get(vid);
			vids.get(level-2).remove(vid);
			vids.get(level-2).put( vid, freq + 1 );
		} else {
			vids.get(level-2).put(vid, 1L);
		}
		
	}
	
	public static void generateGnuplotScript( int cacheElementNumber, int coverSearchNumber, int height, String outputFolder){
	
		String psFilesOutputDirectory = "ps";
		String logFilesOutputDirectory = "data";
		String scriptFilesOutputDirectory = "script";

		String gnuplot_script_file_name =	"log" + File.separator + 
											outputFolder + File.separator + 
											scriptFilesOutputDirectory + File.separator + 
											"script_cache_" + cacheElementNumber + "_cover_"+ coverSearchNumber + ".plt";	
		
		String titleSuffix = " (cache element = " + cacheElementNumber + ", cover search = " + coverSearchNumber + ").log";
		
		try {			
			
			FileWriter fw = new FileWriter(gnuplot_script_file_name);
			BufferedWriter bw = new BufferedWriter(fw);
		
			bw.write(	"set macro\n" +
						"set terminal postscript enhanced color\n" +
						"set logscale x\n" +
						"set logscale y\n" +
						"set yrange[0.1:100]" +
						"my_line_width = \"3\"\n" +
						"set style line 1 linecolor rgbcolor \"#0000AA\" linewidth @my_line_width\n" +
						"set style line 2 linecolor rgbcolor \"#990000\" linewidth @my_line_width\n" +
						"set style line 3 linecolor rgbcolor \"#52015b\" linewidth @my_line_width\n" +
						"set style line 4 linecolor rgbcolor \"#988f03\" linewidth @my_line_width\n" +
						"set style line 5 linecolor rgbcolor \"#be7400\" linewidth @my_line_width\n" +
						"set style line 6 linecolor rgbcolor \"#00AA00\" linewidth @my_line_width\n" +
						"set style line 7 linecolor rgbcolor \"#00b7be\" linewidth @my_line_width\n" +
						"set style line 8 linecolor rgbcolor \"#808080\" linewidth @my_line_width\n" +
						"set style line 9 linecolor rgbcolor \"#d26584\" linewidth @my_line_width\n" +
						"set ylabel \"Frequency\" textcolor lt \"#0000AA\"\n" +
						"#########################################################\n"
					);
			
			bw.write("set xlabel \"Pid\" textcolor lt \"#0000AA\"\n");
			
			for( int i = 0 ; i < height-1 ; i++ ) {
				
				bw.write("#FREQUENZE DEI PID A CONFRONTO NEI VARI PROFILI PER IL LIVELLO " + (i+2) + "\n");
				bw.write("set title \"PIDs frequency analysis (Level " + (i+2) + ")" + titleSuffix + "\"\n");
				
				bw.write("set output \"./" + psFilesOutputDirectory + File.separator + "Pid_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".ps\"\n");
				
				bw.write("plot \"./" + logFilesOutputDirectory + "/Pid_0.125_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.125\"," + 
						      " \"./" + logFilesOutputDirectory + "/Pid_0.25_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.25\"," + 
						       " \"./" + logFilesOutputDirectory + "/Pid_0.5_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.5\"\n");
		
			}
			
			bw.write("#########################################################\n");
			
			bw.write("set xlabel \"Vid\" textcolor lt \"#0000AA\"\n");
			
			for( int i = 0 ; i < height-1 ; i++ ) {
				
				bw.write("#FREQUENZE DEI VID A CONFRONTO NEI VARI PROFILI PER IL LIVELLO " + (i+2) + "\n");
				bw.write("set title \"VIDs frequency analysis (Level " + (i+2) + ")" + titleSuffix + "\"\n");
				
				bw.write("set output \"./" + psFilesOutputDirectory + File.separator + "Vid_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".ps\"\n");
				
				bw.write("plot \"./" + logFilesOutputDirectory + "/Vid_0.125_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.125\"," + 
						      " \"./" + logFilesOutputDirectory + "/Vid_0.25_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.25\"," + 
						       " \"./" + logFilesOutputDirectory + "/Vid_0.5_" + cacheElementNumber + "_" + coverSearchNumber + "_" + (i+2) + ".log\" using 2 with lines ti \"Profile 0.5\"\n");
		
			}
			
			bw.flush();
			bw.close();
			fw.close(); 
						
			String[] gnuplotCommand ={"bash","-c", "cd log/" + outputFolder + "/; gnuplot \"" + "script/script_cache_" + cacheElementNumber + "_cover_"+ coverSearchNumber + ".plt" +"\""};
			Runtime.getRuntime().exec(gnuplotCommand);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		
	}
	
}

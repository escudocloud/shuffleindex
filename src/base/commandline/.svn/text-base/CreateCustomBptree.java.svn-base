package base.commandline;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import base.bptree.BptreeCustom;
import base.disk.Disk;

/**
 * The CreateCustomBptree class used to create the bptree with the root node full. The main method recieve a commandline parameters 
 * that specify tthe number of disks to use during the bptree creation.
 * 
 * @author tommaso
 *
 */
public class CreateCustomBptree {

	/**
	 * The main method
	 * 
	 * @param args the number of disks to use during the bptree creation
	 */
	public static void main(String[] args) {
		

		InputStreamReader inputStreamReader = new InputStreamReader (System.in);
		BufferedReader bufferedReader = new BufferedReader (inputStreamReader);
		
		int diskNumber = 1;
		String answer = "";
		
		if(args.length == 0) {
			System.err.println("The disk number to be used not specified. Use the default disk number (1)? [y,n]");
			do {
				try { answer = bufferedReader.readLine(); } catch(Exception e) { e.printStackTrace(); }
			} while( !answer.equals("y") && !answer.equals("n"));
			if(answer.equals("n")) {
				return;
			}
		} else {
			if( Integer.parseInt(args[0]) > 0 && Integer.parseInt(args[0]) < 5) {
				diskNumber = Integer.parseInt(args[0]);
			} else {
				System.err.println("The disk number must be from 1 to 4");
				return;
			}
		}
		
		Disk<Long, Long> disk = new Disk<Long, Long>(true);	
		BptreeCustom bptree = new BptreeCustom(disk, diskNumber);
		
		System.err.println("Starting B+tree creation...");
		bptree.create();
		System.err.println("Creation completed");
		bptree.close();
		
		return;
		
	}

}

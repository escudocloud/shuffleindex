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


	static boolean TryParse(String value){
		try {
			Integer.parseInt(value);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}

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
		String a_disknum = "";

		System.err.println("Multi-Disk creation procedure - Use default disk number (1)? [y/n]");
		do {
			try { answer = bufferedReader.readLine(); } catch(Exception e) { e.printStackTrace(); }
		} while( !answer.equals("y") && !answer.equals("n"));
		if (answer.equals("n")){
			System.err.println("Number of disks to be used? [1-4]");
			do {
				try { a_disknum = bufferedReader.readLine(); } catch(Exception e) { e.printStackTrace(); }

			} while (!TryParse(a_disknum) || Integer.parseInt(a_disknum) < 0 || Integer.parseInt(a_disknum) > 5);
			diskNumber = Integer.parseInt(a_disknum);
		}


		/*if(args.length == 0) {
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
		}*/

		System.err.println(diskNumber + " disk(s) will be created...");
		Disk<Long, String> disk = new Disk<Long, String>(true);
		BptreeCustom bptree = new BptreeCustom(disk, diskNumber);
		
		System.err.println("Starting B+tree creation...");
		bptree.create();
		System.err.println("Creation completed.");
		bptree.close();
		return;
		
	}

}

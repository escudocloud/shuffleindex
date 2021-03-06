package base.bptree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import base.disk.Disk;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import sun.awt.image.ImageWatched;

/**
 * The BptreeCustom class
 * @author tommaso
 *
 * LEAF_NODE_SIZE		= 29 + 16 * LEAF_NODE_FAN_OUT [byte]
 * INNER_NODE_SIZE		= 37 + 16 * INNER_NODE_FAN_OUT [byte]
 *
 * MAX_LEAF_NODE_NUMBER	= (M+1)^(LEVEL_NUMBER-1)
 * MAX_BPTREE_SIZE		= MAX_LEAF_NODE_NUMBER * LEAF_NODE_SIZE
 *
 */
public class BptreeCustom{

	private int diskNumber;

	private int levelNumber;
	private int mfo;
	private long leafNodeNumber;
	private long nodeNumber;

	/** The disk object used to write and read nodes from disk */
	private Disk<Long, String> disk;

	private List<String> db;
	private long s_point = 0;



	public BptreeCustom(Disk<Long, String> disk, int diskNumber) {

		this.disk 		= disk;
		this.diskNumber = diskNumber;

		levelNumber 	= (int) disk.getDiskSuperBlock().getHeight();
		mfo 			= disk.getDiskSuperBlock().getM(); //M==N
		leafNodeNumber 	= (int)Math.pow((mfo+1), (levelNumber-1));

		nodeNumber= 0;
		for( int i = 0 ; i < levelNumber-1 ; i++) {
			nodeNumber += (long)Math.pow((double)(mfo+1), i);
		}
		nodeNumber += leafNodeNumber;

		File csv = new File("pa.csv");
		CSVParser parser = null;
		try {
			parser = CSVParser.parse(csv, Charset.forName("UTF-16"), CSVFormat.DEFAULT.withDelimiter(';'));
		} catch (IOException e) {
			e.printStackTrace();
		}

		db = new ArrayList<String>();
		for (CSVRecord record : parser) {
			db.add(record.get(0));
		}

	}

	public void create() {

		long currentNodeNumber = 0;
		int currentDiskNumber = 0;

		INode<Long , String> root = new INode<>(mfo);
		root.num = mfo;
		for( int i = 0 ; i < mfo ; i ++ ){
			root.setKey(i, (long)((i+1)*( Math.pow((mfo+1), (levelNumber-2))) +1 ));
		}

		/** WRITE NEW NODE */
		currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
		disk.writeNode(root, true, currentDiskNumber);
		currentNodeNumber++;

		disk.getDiskSuperBlock().setRootPid(root.pid);

		LinkedList<INode<Long, String>> innerNodeList =  new LinkedList<INode<Long, String>>();
		for( int lev = 0 ; lev < levelNumber - 2 ; lev++) {
			innerNodeList.add(new INode<Long, String>(mfo));
		}


		for( long lnn = 0 ; lnn < leafNodeNumber ; lnn++) {

			for( int lev = 0 ; lev < levelNumber-1 ; lev++) {

				if( lnn % Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) ) == 0) {
					if(lev != levelNumber-2) {

						if(lnn != 0) {
							//Saves node on the disk
							disk.writeNode(innerNodeList.get(lev), false);
						}

						INode<Long, String> in = new INode<Long, String>(mfo);
						in.num = 0;

						/** WRITE NEW NODE */
						currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
						disk.writeNode(in, true, currentDiskNumber);
						currentNodeNumber++;

						innerNodeList.set(lev, in);

						if(lev < levelNumber-2){

							in.num = mfo;
							//System.out.println(currentDiskNumber + " " + in.num);
							for( int i = 0 ; i < mfo ; i ++) {
								in.setKey	(i, (long)

										(
												(
														(lnn / Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) ))
																*
																Math.pow( (mfo+1),((levelNumber-1)-(lev+1)) )
												)
														+
														1
														+
														(
																(i+1) * Math.pow( (mfo+1),((levelNumber-2)-(lev+1)))
														)
										)
								);
							}
						}

						//Update parent
						if(lev != 0) {
							innerNodeList.get(lev-1).children[innerNodeList.get(lev-1).getLoc(lnn+1)] = in.pid;
						} else {
							root.children[root.getLoc(lnn+1)] = in.pid;
						}

					}
				}

				if(lev == levelNumber-2) {

					for(int a = 0 ; a < mfo+1 ; a++) {
						LNode<Long, String> ln = new LNode<Long, String>(mfo, mfo);
						for (int j = 0; j < mfo; j++)
							if (s_point + j >= db.size()) {
								String str = String.format("%-100s", "");
								byte[] convertByte = str.getBytes();
								ln.setValue(j,new String(convertByte) );
							}
							else {
								ln.setKey(j, s_point + j);
								String str = String.format("%-100s", db.get((int) (s_point + j)));
								byte[] convertByte = str.getBytes();
								ln.setValue(j, new String(convertByte));
							}
						s_point = s_point+mfo;
//						ln.setValue(0, lnn+1);
						ln.num = mfo;
						//ln.print();


						/** WRITE NEW NODE */
						currentDiskNumber = (int)(currentNodeNumber / ((nodeNumber/diskNumber)+1));
						disk.writeNode(ln, true, currentDiskNumber);
						currentNodeNumber++;
						//System.out.println("@disk" + currentDiskNumber+ ": LeafNodeNumber: " + lnn + " Writing node PID: "+ ln.pid + " Writing node VID: " + ln.vid);

						//Update parent
						if(lev != 0) {
							innerNodeList.get(lev-1).children[innerNodeList.get(lev-1).getLoc(lnn+1)] = ln.pid;
							if( a < mfo ) {
								innerNodeList.get(lev-1).setKey(a, lnn+2);
							}
						} else {
							root.children[root.getLoc(lnn+1)] = ln.pid;
						}

						if( a < mfo ) {
							lnn++;
						}

					}

				}

			}

		}

		for( int lev = 0 ; lev < levelNumber - 2 ; lev++) {
			disk.writeNode(innerNodeList.get(lev), false);
		}

		disk.writeNode(root, false);

	}

	/**
	 * Closes the disk associated with the b+ tree
	 */
	public void close(){

		disk.close();

	}

}

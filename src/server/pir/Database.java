package server.pir;

import base.bptree.Bptree;
import base.disk.Disk;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;

import com.sun.deploy.util.StringUtils;
import org.apache.commons.csv.*;

/**
 * The class Database
 * 
 * @author Tommaso
 *
 */
public class Database {

	/**
	 * Creates the b+tree and saves it on the disk, all the informations used
	 * during the creation are stored in the file disk_conf.xml
	 */
	public static void create() throws FileNotFoundException, IOException {

		Disk<Long, String> disk = new Disk<Long, String>(true);
		Bptree<Long, String> bptree = new Bptree<Long, String>(disk);

		File csv = new File("pa.csv");
		CSVParser parser = CSVParser.parse(csv, Charset.forName("UTF-16"),
				CSVFormat.DEFAULT.withDelimiter(';'));

		// CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(';');
		// CSVParser parser = new CSVParser(new FileReader("wifi.csv"), format);
		List<String> wifi = new ArrayList<String>();

		for (CSVRecord record : parser) {
			wifi.add(record.get(0));
		}
		// System.out.println("CSV importato correttamente");
		byte[] convertByte = null;
		String str;
		for (long i = 0; i < disk.getDiskSuperBlock().getKeyNum() && i < wifi.size(); i++) {


			str = String.format("%-100s", wifi.get((int) i)).replace(" ", "_");
			convertByte = str.getBytes();

			//System.out.println(String.format("%-100s", wifi.get((int) i)).getBytes().length);

			//convertByte = String.format("%-100s", wifi.get((int) i)).getBytes();
            bptree.insert(i + 1, new String(convertByte));
		}

		parser.close();
		bptree.close();

	}

}
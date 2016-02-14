package server.pir;
 
import base.bptree.Bptree;
import base.disk.Disk;
 
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
 
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.csv.*;
 
/**
 * The class Database
 * @author Tommaso
 *
 */
public class Database {
 
    /**
     * Creates the b+tree and saves it on the disk, all the informations used during the creation are stored in the file disk_conf.xml 
     */
    public static void create() throws FileNotFoundException, IOException {
         
 
        Disk<Long, String> disk = new Disk<Long, String>(true); 
        Bptree<Long, String> bptree = new Bptree<Long, String>(disk);
 
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(';');
        CSVParser parser = new CSVParser(new FileReader("wifi.csv"), format);
        List<String> wifi = new ArrayList<String>();
 
        for (CSVRecord record : parser){
            wifi.add(record.get(1));
        }
        System.out.println("CSV importato correttamente");
        for(long i = 0; i < disk.getDiskSuperBlock().getKeyNum() && i < wifi.size() ; i++)
        {
            System.out.println((i+1) + " " + wifi.get((int) i ));
            bptree.insert(i+1, wifi.get((int) i));
            }
         
         
        parser.close();
        bptree.close();
         
    }
 
}
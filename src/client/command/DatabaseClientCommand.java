package client.command;

import server.pir.Server;
import server.pir.Database;

import base.disk.Disk;

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by upara on 18/05/2016.
 */

public class DatabaseClientCommand {

    private Server server = null;

    public DatabaseClientCommand(Server server ){
        this.server = server;
    }

    public void execute() {

                server = null;
                System.err.println("Creating Database...");
                long start, end, time;
                start = System.currentTimeMillis();

                try {
                    Database.create();
                } catch (FileNotFoundException e) {
                    System.err.println("File non trovato");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("Generic IO exception");
                }
                end = System.currentTimeMillis();
                time = end - start;
                System.err.println("Created in: " + time + "[ms]");


                //server.disk.close();
                //server.disk = new Disk<Long, String>(false);

        }

}

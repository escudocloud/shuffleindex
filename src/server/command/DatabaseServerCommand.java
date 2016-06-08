package server.command;

import server.pir.Server;
import server.pir.Database;

import base.disk.Disk;

import java.io.IOException;


/**
 * Created by upara on 18/05/2016.
 */
public class DatabaseServerCommand {

    private Server server;

    public DatabaseServerCommand(Server server ){
        this.server = server;
    }

    public void execute() {
        try {
            Database.create();
            server.disk.close();
            server.disk=new Disk<Long,String>(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

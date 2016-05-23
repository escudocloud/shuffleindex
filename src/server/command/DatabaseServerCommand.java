package server.command;

import server.pir.Server;
import server.pir.Database;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

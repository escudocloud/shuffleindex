package client.command;

import base.disk.Disk;
import client.main.FTPFileUpload;
import org.apache.commons.net.ftp.FTPFile;
import server.pir.Database;
import server.pir.Server;

import java.io.IOException;

/**
 * Created by upara on 15/06/2016.
 */

public class DatabaseClientCommand {

    private Server server;

    public DatabaseClientCommand(Server server) {
        this.server = server;
    }

    public void execute() {
        try {

            Database.create();
            server.disk.close();
            server.disk = new Disk<Long, String>(false);
            FTPFileUpload.sendDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }










}


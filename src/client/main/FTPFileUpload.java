package client.main;

import client.pir.Client;

import java.io.*;
import java.net.Socket;

/**
 * Created by upara on 15/06/2016.
 */
public class FTPFileUpload {

    static Client client;

    public static void sendDisk() {
        long start = System.currentTimeMillis();
        client = new Client();
        client.openConnection();
        client.sendFTPFile();
        client.closeConnection();
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end-start) + "ms");
    }
}

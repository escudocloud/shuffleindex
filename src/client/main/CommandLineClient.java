package client.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.command.ExecuteClientCommand;
import client.command.DatabaseClientCommand;
import server.pir.Server;

public class CommandLineClient{

    private static Server server;
    private static ExecuteClientCommand executeClientCommand;
    private static DatabaseClientCommand databaseClientCommand;

    public static void main(String[] args) {

        InputStreamReader inputStreamReader = new InputStreamReader (System.in);
        BufferedReader bufferedReader = new BufferedReader (inputStreamReader);
        server = new Server(null);

        databaseClientCommand = new DatabaseClientCommand(server);

        executeClientCommand = new ExecuteClientCommand(databaseClientCommand);

        String command = "";

        do {

            try { command = bufferedReader.readLine(); } catch(Exception e) { e.printStackTrace(); }
            if(command.equals("create")) {
                executeClientCommand.create();
            }

        } while(true);

    }
}
package server.main;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

import server.command.CloseServerCommand;
import server.command.ExecuteServerCommand;
import server.command.StartServerCommand;
import server.command.StopServerCommand;
import server.command.DatabaseServerCommand;
import server.pir.Server;

public class CommandLineServer{

	private static Server server; 
	private static StartServerCommand startServerCommand;
	private static StopServerCommand stopServerCommand;
	private static CloseServerCommand closeServerCommand;
	private static ExecuteServerCommand executeServerCommand;
	private static DatabaseServerCommand databaseServerCommand;
	
	public static void main(String[] args) {
		
		InputStreamReader inputStreamReader = new InputStreamReader (System.in);
		BufferedReader bufferedReader = new BufferedReader (inputStreamReader);
		server = new Server(null);

		startServerCommand = new StartServerCommand(server);
		stopServerCommand = new StopServerCommand(server);
		closeServerCommand = new CloseServerCommand(server);
		databaseServerCommand = new DatabaseServerCommand(server);
		     
        executeServerCommand = new ExecuteServerCommand(startServerCommand, stopServerCommand, closeServerCommand, databaseServerCommand);
		
        String command = "";
        
        do {
        
        	try { command = bufferedReader.readLine(); } catch(Exception e) { e.printStackTrace(); }
        	
        	if(command.equals("start")) { 
        		executeServerCommand.start();
        	} else if(command.equals("stop")) {
        		executeServerCommand.stop();
        	} else if(command.equals("close")) {
        		executeServerCommand.stop();
                executeServerCommand.close();
        	} else if(command.equals("create")) {
				if (server.running)
					executeServerCommand.stop();
				executeServerCommand.create();
			}
        	
        } while(true);

	}
}
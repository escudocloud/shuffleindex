package server.main;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import server.command.CloseServerCommand;
import server.command.ExecuteServerCommand;
import server.command.StartServerCommand;
import server.command.StopServerCommand;
import server.pir.Server;

public class DaemonServer implements Daemon{

	private static Server server; 
	private static StartServerCommand startServerCommand;
	private static StopServerCommand stopServerCommand;
	private static CloseServerCommand closeServerCommand;
	private static ExecuteServerCommand executeServerCommand;
	
	public void init(DaemonContext daemonContext) {
		server = new Server(null); 
		startServerCommand = new StartServerCommand(server);
		stopServerCommand = new StopServerCommand(server);
		closeServerCommand = new CloseServerCommand(server);	     
        executeServerCommand = new ExecuteServerCommand(startServerCommand, stopServerCommand, closeServerCommand);	
	}
	
	public void start() {
		executeServerCommand.start();
	}
	
	public void stop() {
		executeServerCommand.stop();
	}
	
	public void destroy() {
		executeServerCommand.stop();
		executeServerCommand.close();
	}

}

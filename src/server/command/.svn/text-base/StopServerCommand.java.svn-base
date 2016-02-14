package server.command;

import server.pir.Server;

public class StopServerCommand implements Command {

	private Server server;
		
	public StopServerCommand( Server server ){
		this.server = server;
	} 
		
	public void execute() {
			
		if( server == null)
    		server = new Server(null) ;
		
		server.stop();
		server.saveCache();
	        
	}

}

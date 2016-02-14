package server.command;

import server.pir.Server;

public class StartServerCommand implements Command {

	private Server server;
	
	public StartServerCommand( Server server ){
		this.server = server;
	} 
	
	public void execute() {
		
		if( server == null)
    		server = new Server(null) ;
		
		server.start();
		Thread main = new Thread(server);
        main.start();
        
	}

}

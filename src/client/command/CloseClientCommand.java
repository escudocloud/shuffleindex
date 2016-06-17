package client.command;

import server.command.Command;
import server.pir.Server;

public class CloseClientCommand implements Command {
    private Server ser;

	public CloseClientCommand(){
	} 
	
	public void execute() {
		System.out.println("===== CLIENT CLOSED =====");
		System.exit(0);
        
	}
	
}

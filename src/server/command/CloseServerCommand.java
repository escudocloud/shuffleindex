package server.command;
import server.pir.*;

public class CloseServerCommand implements Command  {
    private Server ser;
    
	public CloseServerCommand(Server s){
		this.ser = s;
	} 
	
	public void execute() {
		System.err.println("PORCO PANDA !!!!");
		ser.stop();
		System.exit(0);
        
	}
	
}

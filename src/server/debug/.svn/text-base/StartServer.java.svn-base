package server.debug;

import server.pir.Server;

public class StartServer {
	
	public static void main(String args[]) {
		
		Server server = new Server(null); 
		server.start();
		
		Thread main = new Thread(server);
        main.start();
		
		
    }
}

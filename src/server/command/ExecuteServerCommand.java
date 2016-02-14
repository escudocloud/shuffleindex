package server.command;


public class ExecuteServerCommand {

	private StartServerCommand startServerCommand;
	private StopServerCommand stopServerCommand ;
	private CloseServerCommand closeServerCommand;
	
	public ExecuteServerCommand( StartServerCommand startServerCommand, StopServerCommand stopServerCommand, CloseServerCommand closeServerCommand  ) {
		this.startServerCommand = startServerCommand;
		this.stopServerCommand = stopServerCommand;
		this.closeServerCommand = closeServerCommand;
	}
	
	public void start() {
		startServerCommand.execute();
	}
	
	public void stop() {
		stopServerCommand.execute();
	}
	
	public void close() {
		closeServerCommand.execute();
	}
	
}

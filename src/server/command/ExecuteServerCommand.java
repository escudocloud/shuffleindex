package server.command;


public class ExecuteServerCommand {

	private StartServerCommand startServerCommand;
	private StopServerCommand stopServerCommand ;
	private CloseServerCommand closeServerCommand;
	private DatabaseServerCommand databaseServerCommand;
	
	public ExecuteServerCommand( StartServerCommand startServerCommand, StopServerCommand stopServerCommand, CloseServerCommand closeServerCommand, DatabaseServerCommand databaseServerCommand  ) {
		this.startServerCommand = startServerCommand;
		this.stopServerCommand = stopServerCommand;
		this.closeServerCommand = closeServerCommand;
		this.databaseServerCommand = databaseServerCommand;
	}
	
	public void start() {
		startServerCommand.execute();
	}
	
	public void stop() {
		stopServerCommand.execute();
	}
	
	public void close() { closeServerCommand.execute();}

	public void create() {databaseServerCommand.execute();}


}

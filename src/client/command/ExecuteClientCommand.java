package client.command;

/**
 * Created by upara on 15/06/2016.
 */
public class ExecuteClientCommand {

    private DatabaseClientCommand databaseClientCommand;

    public ExecuteClientCommand( DatabaseClientCommand databaseClientCommand  ) {
        this.databaseClientCommand = databaseClientCommand;
    }

    public void create() {databaseClientCommand.execute();}
}

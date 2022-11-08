package server.server_commands;


import server.service.Server;
import java.nio.channels.SelectionKey;


public class AdminDrainCommand extends Command
{
    private final Server server;

    public AdminDrainCommand(SelectionKey clientSelectionKey, boolean isFileTransfer, Server server)
    {
        super(clientSelectionKey, isFileTransfer);
        this.server = server;
    }


    @Override public void run()
    {
        writeToAllClients("SERVER: The server will stop responding in 1 minute. \n" +
                                          "All uploads and downloads of files are restricted from now til shutdown!", true);
        server.drainServer();
    }
}

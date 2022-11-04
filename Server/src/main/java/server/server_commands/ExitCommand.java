package server.server_commands;



import server.service.SocketProcessor;

import java.nio.channels.SelectionKey;


public class ExitCommand extends Command
{
    public ExitCommand(SelectionKey clientSelectionKey, boolean isFileTransfer)
    {
        super(clientSelectionKey, isFileTransfer);
    }


    @Override public void run()
    {
        SocketProcessor.closeClientConnection(clientSelectionKey);
    }
}

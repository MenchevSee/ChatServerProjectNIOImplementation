package server.server_commands;



import server.service.SocketProcessor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class ExitCommand extends Command
{
    public ExitCommand(SelectionKey clientSelectionKey)
    {
        super(clientSelectionKey);
    }


    @Override public void run()
    {
        SocketProcessor.closeClientConnection(clientSelectionKey);
    }
}

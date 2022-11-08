package server.server_commands;


import server.service.SocketProcessor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class AdminKillCommand extends Command
{
    private final String clientUserName;


    public AdminKillCommand(SelectionKey clientSelectionKey, boolean isFileTransfer, String clientUserName)
    {
        super(clientSelectionKey,isFileTransfer);
        this.clientUserName = clientUserName;
    }


    @Override public void run()
    {
        for (SelectionKey selectionKey : selectionKeys)
        {
            if (selectionKey.attachment().equals(clientUserName))
            {
                writeToClient("You have been kicked by the admin! ", (SocketChannel) selectionKey.channel());
                SocketProcessor.closeClientConnection(selectionKey);
                writeToAllClients(selectionKey.attachment() + " has been kicked by the admin!", true);
            }
        }
    }
}

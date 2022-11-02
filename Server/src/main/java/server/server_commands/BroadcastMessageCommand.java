package server.server_commands;


import server.service.SocketAcceptor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;


public class BroadcastMessageCommand extends Command
{
    private final String clientMessage;
    private final boolean includeThisClient;


    public BroadcastMessageCommand(String clientMessage, SelectionKey clientSelectionKey, boolean includeThisClient)
    {
        super(clientSelectionKey);
        this.clientMessage = clientMessage;
        this.includeThisClient = includeThisClient;
    }


    @Override public void run()
    {
        SocketChannel clientSocketChannel = (SocketChannel) clientSelectionKey.channel();
        Set<SelectionKey> selectionKeys = SocketAcceptor.messageChannelsSelector.keys();
        if (!includeThisClient)
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                SocketChannel clientMessageSocketChannel = (SocketChannel)selectionKey.channel();
                if (!clientMessageSocketChannel.equals(clientSocketChannel))
                {
                    writeToClient(clientMessage, clientMessageSocketChannel);
                }
            }
        }
        else
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                writeToClient(clientMessage, (SocketChannel)selectionKey.channel());
            }
        }

        if (clientMessage.contains("has entered the chat!"))
        {
            writeToClient("welcome", clientSocketChannel);
        }
    }
}

package server.server_commands;


import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class BroadcastMessageCommand extends Command
{
    private final String clientMessage;
    private String type;


    public BroadcastMessageCommand(String clientMessage, SelectionKey clientSelectionKey, boolean threadPoolFlag, String type)
    {
        super(clientSelectionKey, threadPoolFlag);
        this.clientMessage = clientMessage;
        this.type = type;
    }


    @Override public void run()
    {
        if (clientMessage.contains("has entered the chat!"))
        {
            writeToClient("welcome", clientSocketChannel);
        }
        if (type.equals("ALL"))
        {
            writeToAllClients(clientMessage, true);
        }
        else if (type.equals("excludeThisClient"))
        {
            writeToAllClients(clientMessage, false);
        }
        else if (type.equals("onlyThisClient"))
        {
            writeToClient(clientMessage, clientSocketChannel);
        }
    }

    private void writeToAllClients(String clientMessage, boolean includeThisClient)
    {
        if (includeThisClient)
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                writeToClient(clientMessage, (SocketChannel) selectionKey.channel());
            }
        }
        else
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                SocketChannel clientMessageSocketChannel = (SocketChannel)selectionKey.channel();
                if (!clientMessageSocketChannel.equals(clientSocketChannel))
                {
                    writeToClient(clientMessage, (SocketChannel) selectionKey.channel());
                }
            }
        }
    }
}

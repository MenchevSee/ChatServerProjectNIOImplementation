package server.server_commands;



import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class BroadcastMessageCommand extends Command
{
    private final String clientMessage;
    private final boolean includeThisClient;


    public BroadcastMessageCommand(String clientMessage, SelectionKey clientSelectionKey, boolean threadPoolFlag, boolean includeThisClient)
    {
        super(clientSelectionKey, threadPoolFlag);
        this.clientMessage = clientMessage;
        this.includeThisClient = includeThisClient;
    }


    @Override public void run()
    {
        SocketChannel clientSocketChannel = (SocketChannel)clientSelectionKey.channel();
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

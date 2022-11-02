package server.server_commands;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class Command implements Runnable
{

    protected SelectionKey clientSelectionKey;

    protected Command(SelectionKey clientSelectionKey)
    {
        this.clientSelectionKey = clientSelectionKey;
    }


    protected void writeToClient(String clientMessage, SocketChannel clientMessageSocketChannel)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(clientMessage.getBytes());
        try
        {
            clientMessageSocketChannel.write(byteBuffer);
            while (byteBuffer.hasRemaining())
            {
                clientMessageSocketChannel.write(byteBuffer);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

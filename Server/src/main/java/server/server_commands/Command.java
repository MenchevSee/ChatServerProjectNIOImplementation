package server.server_commands;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Command implements Runnable
{

    protected SocketChannel clientSocketChannel;


    protected Command(SocketChannel clientSocketChannel)
    {
        this.clientSocketChannel = clientSocketChannel;
    }


    protected void writeToClient(String clientMessage, SocketChannel clientMessageSocketChannel)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(clientMessage.getBytes());
        byteBuffer.rewind();
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

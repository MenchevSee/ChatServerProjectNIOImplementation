package server.client;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;


public class Client
{
    private String userName;
    private SocketChannel messagesChannel;
    private SocketChannel filesChannel;

    public static Map<String, Client> clients = new HashMap<>();


    public Client(String userName)
    {
        this.userName = userName;
        clients.put(userName, this);
    }


    public void closeClientConnection()
    {
        try
        {
            if (messagesChannel != null)
            {
                messagesChannel.close();
            }
            if (filesChannel != null)
            {
                filesChannel.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(messagesChannel, filesChannel);
        }
    }


    public SocketChannel getMessagesChannel()
    {
        return messagesChannel;
    }


    public void setMessagesChannel(SocketChannel messagesChannel)
    {
        this.messagesChannel = messagesChannel;
    }


    public SocketChannel getFilesChannel()
    {
        return filesChannel;
    }


    public void setFilesChannel(SocketChannel filesChannel)
    {
        this.filesChannel = filesChannel;
    }
}

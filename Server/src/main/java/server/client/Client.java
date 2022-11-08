package server.client;


import org.apache.commons.io.IOUtils;
import server.server_commands.Command;
import server.service.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


public class Client implements Runnable
{
    private String userName;
    private SocketChannel messagesChannel;
    private SocketChannel filesChannel;
    public static Map<String, Client> clients = new ConcurrentHashMap<>();
    private Long timeStampLastActive;
    private static Long maximumIdleTimeAllowed = Long.parseLong(Server.properties.getProperty("timeToLive", "60")) * 1000L;
    private Future idleCheckerIOU;


    public Client(String userName)
    {
        this.userName = userName;
        clients.put(userName, this);
        this.timeStampLastActive = System.currentTimeMillis();
    }


    @Override public void run()
    {
        if ((System.currentTimeMillis() - timeStampLastActive) > maximumIdleTimeAllowed)
        {
            writeToClient("You have been kicked due to inactivity!");
            closeClientConnection();
        }
    }

    private void writeToClient(String clientMessage)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(clientMessage.getBytes());
        try
        {
            messagesChannel.write(byteBuffer);
            while (byteBuffer.hasRemaining())
            {
                messagesChannel.write(byteBuffer);
            }
        }
        catch (IOException e)
        {
            Server.logger.error(e);
        }
    }

    public void closeClientConnection()
    {
        Client.clients.remove(userName);
        idleCheckerIOU.cancel(true);
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
            Server.logger.error(e);
        }
        finally
        {
            IOUtils.closeQuietly(messagesChannel, filesChannel);
        }
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


    public void setTimeStampLastActive(Long timeStampLastActive)
    {
        this.timeStampLastActive = timeStampLastActive;
    }


    public void setIdleCheckerIOU(Future idleCheckerIOU)
    {
        this.idleCheckerIOU = idleCheckerIOU;
    }
}

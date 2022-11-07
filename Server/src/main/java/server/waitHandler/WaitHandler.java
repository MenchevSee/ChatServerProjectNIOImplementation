package server.waitHandler;


import org.apache.commons.io.IOUtils;
import server.client.Client;
import server.license.LicenseManagement;
import server.license.LicenseManager;
import server.server_commands.Command;
import server.server_commands.CommandFactory;
import server.service.Server;
import server.service.SocketAcceptor;
import server.service.SocketProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;


public class WaitHandler implements Runnable
{
    private SocketProcessor socketProcessor;
    private SocketChannel clientMessageSocket;
    private String clientUserName;
    public static final List<WaitHandler> waitingClientHandlerList = Collections.synchronizedList(new ArrayList<>());
    public static LicenseManagement licenseManager;
    private Future future;
    private int timeCounter;
    private CommandFactory commandFactory;


    public WaitHandler(SocketProcessor socketProcessor, SocketChannel messageSocket)
    {
        this.socketProcessor = socketProcessor;
        this.commandFactory = socketProcessor.getCommandFactory();
        this.clientMessageSocket = messageSocket;
    }


    @Override public void run()
    {
        if (this.clientUserName == null)
        {
            assignUserName();
        }

        if (timeCounter == 10)
        {
            writeToClient("You were rejected from the server!");
            closeEverything();
        }

        timeCounter++;
        if (!clientMessageSocket.isOpen())
        {
            int rankInQueue = WaitHandler.waitingClientHandlerList.indexOf(this);
            if (licenseManager.verify() && rankInQueue == 0)
            {
                future.cancel(true);
                waitingClientHandlerList.remove(this);
                Client client = new Client(clientUserName);
                client.setMessagesChannel(clientMessageSocket);
                try
                {
                    SelectionKey clientSelectionKey = clientMessageSocket.register(SocketAcceptor.messageChannelsSelector, SelectionKey.OP_READ);
                    clientSelectionKey.attach(clientUserName);
                    Command command = commandFactory.getInstance(
                                    "SERVER: " + clientUserName + " has entered the chat!",
                                    clientSelectionKey, "excludeThisClient");
                    SocketProcessor.commandExecutor.execute(command);
                }
                catch (ClosedChannelException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                writeToClient("You are number " + (rankInQueue + 1) + " in the waiting queue!");
            }
        }
    }


    private void closeEverything()
    {
        if (future != null)
        {
            future.cancel(true);
        }
        try
        {
            if (clientMessageSocket != null)
            {
                clientMessageSocket.close();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(clientMessageSocket);
        }

    }


    private void assignUserName()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try
        {
            while (clientMessageSocket.read(byteBuffer) > 0)
            {
                clientMessageSocket.read(byteBuffer);
            }
            byteBuffer.flip();
            this.clientUserName = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void writeToClient(String clientMessage)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(clientMessage.getBytes());
        try
        {
            clientMessageSocket.write(byteBuffer);
            while (byteBuffer.hasRemaining())
            {
                clientMessageSocket.write(byteBuffer);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void setFuture(Future future)
    {
        this.future = future;
    }
}

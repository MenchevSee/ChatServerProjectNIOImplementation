package server.waitHandler;


import org.apache.commons.io.IOUtils;
import server.client.Client;
import server.license.LicenseManagement;
import server.server_commands.Command;
import server.server_commands.CommandFactory;
import server.service.Server;
import server.service.SocketAcceptor;
import server.service.SocketProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;


public class WaitHandler implements Runnable
{
    private final SocketChannel clientMessageSocket;
    private String clientUserName;
    public static final List<WaitHandler> waitingClientHandlerList = Collections.synchronizedList(new ArrayList<>());
    public static LicenseManagement licenseManager;
    private Future future;
    private int timeCounter;
    private final CommandFactory commandFactory;


    public WaitHandler(SocketProcessor socketProcessor, SocketChannel messageSocket)
    {
        this.commandFactory = socketProcessor.getCommandFactory();
        this.clientMessageSocket = messageSocket;
        this.timeCounter = 1;
    }


    @Override public void run()
    {
        try
        {
            while (!clientMessageSocket.finishConnect())
            {
                Thread.sleep(1000);
            }
        }
        catch (IOException | InterruptedException e)
        {
            Server.logger.error(e);
        }

        if (this.clientUserName == null)
        {
            assignUserName();
        }

        if (timeCounter == 10)
        {
            writeToClient("You were rejected from the server!");
            closeEverything();
        }

        if (clientMessageSocket.isOpen())
        {
            int rankInQueue = WaitHandler.waitingClientHandlerList.indexOf(this);
            if (licenseManager.verify() && rankInQueue == 0)
            {
                future.cancel(true);
                waitingClientHandlerList.remove(this);
                Client client = new Client(clientUserName);
                Client.clients.put(clientUserName, client);
                client.setMessagesChannel(clientMessageSocket);
                try
                {
                    clientMessageSocket.configureBlocking(false);
                    SelectionKey clientSelectionKey = clientMessageSocket.register(SocketAcceptor.messageChannelsSelector,
                                                                                   SelectionKey.OP_READ);
                    clientSelectionKey.attach(clientUserName);
                    Command command = commandFactory.getInstance("SERVER: " + clientUserName + " has entered the chat!", clientSelectionKey,
                                                                 "excludeThisClient");
                    SocketProcessor.commandExecutor.execute(command);
                }
                catch (IOException e)
                {
                    Server.logger.error(e);
                }
            }
            else
            {
                writeToClient("You are number " + (rankInQueue + 1) + " in the waiting queue!");
                timeCounter++;
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
            Server.logger.error(e);
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
            clientMessageSocket.read(byteBuffer);
            byteBuffer.flip();
            this.clientUserName = StandardCharsets.UTF_8.decode(byteBuffer).toString().split(" ")[1];
        }
        catch (IOException e)
        {
            Server.logger.error(e);
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
            Server.logger.error(e);
        }
    }


    public void setFuture(Future future)
    {
        this.future = future;
    }
}

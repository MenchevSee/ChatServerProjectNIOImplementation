package server.service;


import org.apache.commons.io.IOUtils;
import server.client.Client;
import server.license.LicenseManagement;
import server.license.LicenseManager;
import server.waitHandler.WaitHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SocketAcceptor implements Runnable
{
    private Server server;
    private final LicenseManagement licenseManagement;
    private final int tcpMessagesPort;
    private final int tcpFilesPort;
    public static Selector messageChannelsSelector;
    public static Selector fileChannelsSelector;
    private Selector acceptSocketChannelsSelector;
    private ServerSocketChannel messagesChannel;
    private ServerSocketChannel filesChannel;
    private ScheduledExecutorService waitingPool;

    static
    {
        try
        {
            messageChannelsSelector = Selector.open();
            fileChannelsSelector = Selector.open();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public SocketAcceptor(Server server, int tcpMessagesPort, int tcpFilesPort, int licenseCount)
    {
        this.server = server;
        this.licenseManagement = new LicenseManager(licenseCount);
        this.tcpMessagesPort = tcpMessagesPort;
        this.tcpFilesPort = tcpFilesPort;
        this.waitingPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() / 3);
    }


    @Override public void run()
    {
        try
        {
            messagesChannel = ServerSocketChannel.open();
            messagesChannel.socket().bind(new InetSocketAddress("localhost", tcpMessagesPort));
            messagesChannel.configureBlocking(false);
            filesChannel = ServerSocketChannel.open();
            filesChannel.socket().bind(new InetSocketAddress("localhost", tcpFilesPort));
            filesChannel.configureBlocking(false);
            acceptSocketChannelsSelector = Selector.open();
            messagesChannel.register(acceptSocketChannelsSelector, SelectionKey.OP_ACCEPT);
            filesChannel.register(acceptSocketChannelsSelector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e)
        {
            Server.logger.error(e);
        }
        WaitHandler.licenseManager = licenseManagement;
        listenForIncomingConnections();
    }


    private void listenForIncomingConnections()
    {
        while (acceptSocketChannelsSelector.isOpen())
        {
            try
            {
                acceptSocketChannelsSelector.select();
                if (!acceptSocketChannelsSelector.isOpen())
                {
                    break;
                }
            }
            catch (IOException e)
            {
                Server.logger.error(e);
            }
            Set<SelectionKey> selectedKeys = acceptSocketChannelsSelector.selectedKeys();

            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext())
            {
                SelectionKey key = keyIterator.next();
                ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                if (channel.equals(messagesChannel))
                {
                    if (licenseManagement.verify())
                    {
                        try
                        {
                            SocketChannel clientMessageChannel = channel.accept();
                            clientMessageChannel.configureBlocking(false);
                            clientMessageChannel.register(messageChannelsSelector, SelectionKey.OP_READ);
                        }
                        catch (IOException e)
                        {
                            Server.logger.error(e);
                            break;
                        }
                    }
                    else
                    {
                        try
                        {
                            SocketChannel clientMessageChannel = channel.accept();
                            WaitHandler waitHandler = new WaitHandler(server.getSocketProcessor(), clientMessageChannel);
                            WaitHandler.waitingClientHandlerList.add(waitHandler);
                            waitHandler.setFuture(waitingPool.scheduleAtFixedRate(waitHandler, 0, 30, TimeUnit.SECONDS));
                        }
                        catch (IOException e)
                        {
                            Server.logger.error(e);
                        }
                    }
                }
                else if (channel.equals(filesChannel))
                {
                    try
                    {
                        SocketChannel clientFileChannel = channel.accept();
                        clientFileChannel.configureBlocking(false);
                        clientFileChannel.register(fileChannelsSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                    catch (IOException e)
                    {
                        Server.logger.error(e);
                        break;
                    }
                }
                keyIterator.remove();
            }
        }
    }


    public void stopAcceptingNewConnections()
    {
        SocketProcessor.fileTransferPool.shutdown();
        try
        {
            acceptSocketChannelsSelector.close();
            fileChannelsSelector.close();
            messagesChannel.close();
            filesChannel.close();
        }
        catch (IOException e)
        {
            Server.logger.error(e);
        }
        finally
        {
            IOUtils.closeQuietly(messagesChannel, filesChannel, fileChannelsSelector, acceptSocketChannelsSelector);
        }
        closeFileChannelSockets();
    }


    private void closeFileChannelSockets()
    {
        while (!SocketProcessor.fileTransferPool.isShutdown())
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Server.logger.error(e);
            }
        }

        Map<String, Client> clientMap = Client.clients;
        for (Map.Entry<String, Client> client : clientMap.entrySet())
        {
            try
            {
                SocketChannel fileSocketConnection = client.getValue().getFilesChannel();
                if (fileSocketConnection != null)
                {
                    fileSocketConnection.close();
                }
            }
            catch (IOException e)
            {
                Server.logger.error(e);
            }
            finally
            {
                IOUtils.closeQuietly(client.getValue().getFilesChannel());
            }
        }
    }
}

package server.service;


import org.apache.commons.io.IOUtils;
import server.license.LicenseManagement;
import server.license.LicenseManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;


public class SocketAcceptor implements Runnable
{
    private final LicenseManagement licenseManagement;
    private final int tcpMessagesPort;
    private final int tcpFilesPort;
    public static Selector messageChannelsSelector;
    public static Selector fileChannelsSelector;
    private Selector acceptSocketChannelsSelector;
    private ServerSocketChannel messagesChannel;
    private ServerSocketChannel filesChannel;
    private Server server;

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

    public SocketAcceptor(int tcpMessagesPort, int tcpFilesPort, int licenseCount, Server server)
    {
        this.licenseManagement = new LicenseManager(licenseCount);
        this.tcpMessagesPort = tcpMessagesPort;
        this.tcpFilesPort = tcpFilesPort;
        this.server = server;
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
            e.printStackTrace();
        }
        listenForIncomingConnections();
    }


    private void listenForIncomingConnections()
    {
        while (this.messagesChannel.isOpen() && this.filesChannel.isOpen())
        {
            try
            {
                acceptSocketChannelsSelector.select();
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
                            e.printStackTrace();
                            break;
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
                        e.printStackTrace();
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
            messagesChannel.close();
            filesChannel.close();
            fileChannelsSelector.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(messagesChannel,filesChannel, fileChannelsSelector);
        }

        closeFileChannelSockets();
    }


    private void closeFileChannelSockets()
    {
        Set<SelectionKey> fileSelectionKeys = fileChannelsSelector.keys();
        Iterator<SelectionKey> keyIterator = fileSelectionKeys.iterator();
        while (keyIterator.hasNext())
        {
            SelectionKey selectionKey = keyIterator.next();
            SocketChannel fileSocketChannel = (SocketChannel)selectionKey.channel();
            try
            {
                fileSocketChannel.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                IOUtils.closeQuietly(fileSocketChannel);
            }
        }
    }

    public Server getServer()
    {
        return server;
    }
}

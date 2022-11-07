package server.service;


import org.apache.commons.io.IOUtils;
import server.properties.PropertiesCache;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class Server
{
    private SocketAcceptor socketAcceptor;
    private SocketProcessor socketProcessor;
    private int tcpMessagesPort;
    private int tcpFilesPort;
    private int licenseCount;
    public static final Properties properties = PropertiesCache.getPropertiesCache();
    public static AtomicBoolean drainOfServerInitiated = new AtomicBoolean(false);
    private AtomicBoolean closeAllClientConnections = new AtomicBoolean(false);


    public Server(int tcpMessagesPort, int tcpFilesPort, int licenseCount)
    {
        this.tcpMessagesPort = tcpMessagesPort;
        this.tcpFilesPort = tcpFilesPort;
        this.licenseCount = licenseCount;
    }


    public void start()
    {
        socketAcceptor = new SocketAcceptor(this ,tcpMessagesPort, tcpFilesPort, licenseCount);
        socketProcessor = new SocketProcessor(this);

        Thread acceptorThread = new Thread(socketAcceptor);
        Thread processorThread = new Thread(socketProcessor);

        acceptorThread.start();
        processorThread.start();

        closeAllMessageConnections();
    }


    private void closeAllMessageConnections()
    {
        try
        {
            while (!closeAllClientConnections.get())
            {
                synchronized (this)
                {
                    this.wait();
                }
            }
            Thread.sleep(60000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        SocketProcessor.commandExecutor.shutdownNow();
        Set<SelectionKey> messagesSelectionKeys = SocketAcceptor.messageChannelsSelector.keys();
        Iterator<SelectionKey> keyIterator = messagesSelectionKeys.iterator();
        while (keyIterator.hasNext())
        {
            SelectionKey selectionKey = keyIterator.next();
            SocketChannel messageSocketChannel = (SocketChannel)selectionKey.channel();
            try
            {
                messageSocketChannel.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                IOUtils.closeQuietly(messageSocketChannel);
            }
        }
        try
        {
            if (SocketAcceptor.messageChannelsSelector != null)
            {
                SocketAcceptor.messageChannelsSelector.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(SocketAcceptor.messageChannelsSelector);
        }
    }


    public void drainServer()
    {
        socketAcceptor.stopAcceptingNewConnections();
        closeAllClientConnections.set(true);
        synchronized (this)
        {
            this.notify();
        }
    }


    public SocketAcceptor getSocketAcceptor()
    {
        return socketAcceptor;
    }


    public SocketProcessor getSocketProcessor()
    {
        return socketProcessor;
    }
}

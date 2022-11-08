package server.service;


import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public static Properties properties = PropertiesCache.getPropertiesCache();
    public static AtomicBoolean drainOfServerInitiated = new AtomicBoolean(false);
    private AtomicBoolean closeAllClientConnections = new AtomicBoolean(false);
    public static Logger logger = LogManager.getLogger();


    public Server()
    {
        this.tcpMessagesPort = Integer.parseInt(properties.getProperty("messagesServerPort"));
        this.tcpFilesPort = Integer.parseInt(properties.getProperty("transferServerPort"));
        this.licenseCount = Integer.parseInt(properties.getProperty("licencesCount", "3"));
    }


    public void start()
    {
        socketAcceptor = new SocketAcceptor(this, tcpMessagesPort, tcpFilesPort, licenseCount);
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
            logger.error(e);
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
                logger.error(e);
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
            logger.error(e);
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


    public SocketProcessor getSocketProcessor()
    {
        return socketProcessor;
    }
}

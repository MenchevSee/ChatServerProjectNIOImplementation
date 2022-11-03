package server.service;


import server.properties.PropertiesCache;

import java.util.Properties;


public class Server
{
    private SocketAcceptor socketAcceptor;
    private SocketProcessor socketProcessor;
    private int tcpMessagesPort;
    private int tcpFilesPort;
    private int licenseCount;
    public static final Properties properties = PropertiesCache.getPropertiesCache();

    public Server(int tcpMessagesPort, int tcpFilesPort, int licenseCount)
    {
        this.tcpMessagesPort = tcpMessagesPort;
        this.tcpFilesPort = tcpFilesPort;
        this.licenseCount = licenseCount;
    }


    public void start()
    {
        socketAcceptor = new SocketAcceptor(tcpMessagesPort, tcpFilesPort, licenseCount);
        socketProcessor = new SocketProcessor();

        Thread acceptorThread = new Thread(socketAcceptor);
        Thread processorThread = new Thread(socketProcessor);

        acceptorThread.start();
        processorThread.start();
    }
}

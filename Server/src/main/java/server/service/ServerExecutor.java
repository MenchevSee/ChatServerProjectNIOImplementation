package server.service;


import org.apache.logging.log4j.LogManager;


public class ServerExecutor
{
    public static void main(String[] args)
    {
        System.setProperty("log4j2.configurationFile", "log4j2.xml");
        Server server = new Server();
        server.start();
    }
}

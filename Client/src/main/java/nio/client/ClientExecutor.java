package nio.client;


public class ClientExecutor
{
    public static void main(String[] args)
    {
        System.setProperty("log4j2.configurationFile", "log4j2.xml");
        Thread client = new Thread(new Client());
        client.start();
    }
}

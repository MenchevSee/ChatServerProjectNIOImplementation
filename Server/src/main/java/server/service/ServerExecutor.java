package server.service;




public class ServerExecutor
{
    public static void main(String[] args)
    {
        Server server = new Server(9999,9998,10);
        server.start();
    }
}

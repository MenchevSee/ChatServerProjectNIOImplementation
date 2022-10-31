package nio.client;


public class ClientExecutor
{
    public static void main(String[] args)
    {
        Thread client = new Thread(new Client());
    }
}

package nio.client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;


public class Client implements Runnable
{
    private SocketChannel socketChannel;
    private String userName;
    private Scanner scanner;
    private ByteBuffer writeByteBuffer;
    private ByteBuffer readByteBuffer;


    public Client()
    {
        try
        {
            this.socketChannel = SocketChannel.open();
            this.socketChannel.connect(new InetSocketAddress("localhost",9999));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        this.userName = scanner.nextLine();
        writeToServer(userName);
        this.readByteBuffer = ByteBuffer.allocate(1024);
        readFromServer();
    }


    private void sendMessage()
    {
        new Thread(new Runnable()
        {
            @Override public void run()
            {
                while (socketChannel.isOpen())
                {
                    try
                    {
                        while (System.in.available() != 0)
                        {
                            writeToServer(scanner.nextLine());
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private void writeToServer(String clientMessage)
    {
        String amendedClientMessage = userName + ": " + clientMessage;
        writeByteBuffer = ByteBuffer.wrap(amendedClientMessage.getBytes());
        writeByteBuffer.rewind();
        try
        {
            socketChannel.write(writeByteBuffer);
            while (writeByteBuffer.hasRemaining())
            {
                socketChannel.write(writeByteBuffer);
            }
            writeByteBuffer.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void readFromServer()
    {
        while (socketChannel.isOpen())
        {
            String serverMessage;
            try
            {
                while (socketChannel.read(readByteBuffer) >= 0 || readByteBuffer.position() > 0)
                {
                    socketChannel.read(readByteBuffer);
                }
                serverMessage = readByteBuffer.toString();
                if (!serverMessage.trim().isEmpty())
                {
                    messageHandling(serverMessage);
                    readByteBuffer.clear();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void messageHandling(String serverMessage)
    {
        String[] splitServerMessage = serverMessage.split(" ");
        switch (splitServerMessage[0])
        {
            case "welcome":
                sendMessage();
                break;
            default:
                System.out.println(serverMessage);
                break;
        }
    }


    @Override public void run()
    {

    }
}

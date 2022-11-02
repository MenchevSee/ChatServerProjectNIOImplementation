package nio.client;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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
            this.socketChannel.connect(new InetSocketAddress("localhost", 9999));
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
    }


    @Override public void run()
    {
        readFromServer();
    }


    private void sendMessage()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override public void run()
            {
                while (socketChannel.isOpen())
                {
                    try
                    {
                        while (System.in.available() != 0)
                        {
                            clientMessageHandling(scanner.nextLine());
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    private void clientMessageHandling(String clientMessage)
    {
        String[] splitClientMessage = clientMessage.split(" ");
        String command = splitClientMessage[0];
        switch (command)
        {
            case "EXIT":
                writeToServer(clientMessage);
                closeEverything();
                break;
            default:
                writeToServer(clientMessage);
                break;
        }
    }


    private void writeToServer(String clientMessage)
    {
        String amendedClientMessage = userName + ": " + clientMessage;
        writeByteBuffer = ByteBuffer.wrap(amendedClientMessage.getBytes());
        try
        {
            socketChannel.write(writeByteBuffer);
            while (writeByteBuffer.hasRemaining())
            {
                socketChannel.write(writeByteBuffer);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void readFromServer()
    {
        String serverMessage;
        while (socketChannel.isOpen())
        {
            try
            {
                socketChannel.read(readByteBuffer);
                readByteBuffer.flip();
                serverMessage = StandardCharsets.UTF_8.decode(readByteBuffer).toString();
                readByteBuffer.clear();
                if (!serverMessage.trim().isEmpty())
                {
                    serverMessageHandling(serverMessage);
                }
            }
            catch (AsynchronousCloseException e){
//                connection cut from serve side!!!
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void serverMessageHandling(String serverMessage)
    {
        String[] splitServerMessage = serverMessage.split(" ");
        switch (splitServerMessage[0])
        {
            case "welcome":
                System.out.println("You have entered the chat!");
                sendMessage();
                break;
            default:
                System.out.println(serverMessage);
                break;
        }
    }


    private void closeEverything()
    {
        try
        {
            if (socketChannel != null)
            {
                socketChannel.close();
            }
            if (scanner != null)
            {
                scanner.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(socketChannel, scanner);
        }
    }
}

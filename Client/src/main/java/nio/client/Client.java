package nio.client;


import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import properties.PropertiesCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client implements Runnable
{
    private SocketChannel messagesSocketChannel;
    private SocketChannel filesSocketChannel;
    private final String userName;
    private final Scanner scanner;
    private final ExecutorService filesTransferPool;
    private final PropertiesCache properties;
    private final File clientFilesDir;
    private static final Logger logger = LogManager.getLogger();


    public Client()
    {
        this.properties = PropertiesCache.getPropertiesCache();
        try
        {
            this.messagesSocketChannel = SocketChannel.open();
            this.messagesSocketChannel.connect(new InetSocketAddress(properties.getProperty("serverIp"),
                                                                     Integer.parseInt(properties.getProperty("messageServerPort"))));
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        this.scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        this.userName = scanner.nextLine();
        writeToServer(userName);
        this.filesTransferPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 3);
        this.clientFilesDir = new File(properties.getProperty("storeFiles"));
    }


    @Override public void run()
    {
        readFromServer();
    }


    private void sendMessage()
    {
        Thread thread = new Thread(() ->
                                   {
                                       while (messagesSocketChannel.isOpen())
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
                                               logger.error(e);
                                               closeEverything();
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
            case "SEND":
                openFileSocketChannelConnection();
                File file = new File(splitClientMessage[1]);
                writeToServer("SEND " + file.getName() + " " + file.length());
                sendFileToServer(file);
                break;
            case "FILE-GET":
                openFileSocketChannelConnection();
                writeToServer(clientMessage);
                break;
            default:
                writeToServer(clientMessage);
                break;
        }
    }


    private void openFileSocketChannelConnection()
    {
        if (filesSocketChannel == null)
        {
            try
            {
                this.filesSocketChannel = SocketChannel.open();
                this.filesSocketChannel.connect(new InetSocketAddress(properties.getProperty("serverIp"),
                                                                      Integer.parseInt(properties.getProperty("transferServerPort"))));
                this.filesSocketChannel.configureBlocking(false);
                while (!filesSocketChannel.finishConnect())
                {
                    Thread.sleep(1000);
                }
                filesSocketChannel.write(ByteBuffer.wrap(userName.getBytes()));
            }
            catch (IOException | InterruptedException e)
            {
                logger.error(e);
                closeEverything();
            }
        }
    }


    private void sendFileToServer(File file)
    {
        filesTransferPool.execute(() ->
                                  {
                                      ByteBuffer readFileByteBuffer = ByteBuffer.allocate(4096);
                                      try (
                                                      FileInputStream fin = new FileInputStream(file);
                                                      FileChannel fileChannel = fin.getChannel();)
                                      {
                                          while (fileChannel.read(readFileByteBuffer) != -1)
                                          {
                                              readFileByteBuffer.flip();
                                              filesSocketChannel.write(readFileByteBuffer);
                                              readFileByteBuffer.clear();
                                          }
                                      }
                                      catch (IOException e)
                                      {
                                          logger.error(e);
                                          closeEverything();
                                      }
                                  });
    }


    private void writeToServer(String clientMessage)
    {
        String amendedClientMessage = userName + ": " + clientMessage;
        ByteBuffer writeByteBuffer = ByteBuffer.wrap(amendedClientMessage.getBytes());
        try
        {
            messagesSocketChannel.write(writeByteBuffer);
            while (writeByteBuffer.hasRemaining())
            {
                messagesSocketChannel.write(writeByteBuffer);
            }
        }
        catch (IOException e)
        {
            logger.error(e);
            closeEverything();
        }
    }


    private void readFromServer()
    {
        ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
        String serverMessage;
        while (messagesSocketChannel.isOpen())
        {
            try
            {
                messagesSocketChannel.read(readByteBuffer);
                readByteBuffer.flip();
                serverMessage = StandardCharsets.UTF_8.decode(readByteBuffer).toString();
                readByteBuffer.clear();
                if (serverMessage.isEmpty())
                {
                    logger.info("The connection was cut from the server side!!!");
                    closeEverything();
                }
                serverMessageHandling(serverMessage);
            }
            catch (IOException e)
            {
                logger.error(e);
                closeEverything();
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
            case "SENDING":
                receiveFileFromServer(splitServerMessage[1], splitServerMessage[2]);
                break;
            default:
                System.out.println(serverMessage);
                break;
        }
    }


    private void receiveFileFromServer(String fileName, String fileLength)
    {
        filesTransferPool.execute(() ->
                                  {
                                      if (!clientFilesDir.isDirectory())
                                      {
                                          clientFilesDir.mkdir();
                                      }
                                      int fileLengthInt = Integer.parseInt(fileLength);
                                      File file = new File(clientFilesDir, fileName);
                                      try (
                                                      FileOutputStream out = new FileOutputStream(file);
                                                      FileChannel fileChannel = out.getChannel())
                                      {
                                          ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                                          int bytesRead = 0;
                                          while (bytesRead < fileLengthInt)
                                          {
                                              bytesRead += filesSocketChannel.read(byteBuffer);
                                              byteBuffer.flip();
                                              while (byteBuffer.hasRemaining())
                                              {
                                                  fileChannel.write(byteBuffer);
                                              }
                                              byteBuffer.clear();
                                          }
                                      }
                                      catch (IOException e)
                                      {
                                          logger.error(e);
                                          closeEverything();
                                      }
                                      System.out.println("File " + fileName + " was successfully downloaded!");
                                  });
    }


    private void closeEverything()
    {
        try
        {
            if (messagesSocketChannel != null)
            {
                messagesSocketChannel.close();
            }
            if (filesSocketChannel != null)
            {
                filesSocketChannel.close();
            }
            if (scanner != null)
            {
                scanner.close();
            }
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        finally
        {
            IOUtils.closeQuietly(messagesSocketChannel, filesSocketChannel, scanner);
            if (filesTransferPool != null)
            {
                filesTransferPool.shutdownNow();
            }
        }
    }
}

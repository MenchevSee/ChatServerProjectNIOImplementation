package client;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import properties.PropertiesCache;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client extends Thread
{
    private Socket messageSocket;
    private Socket fileTransferSocket;
    private BufferedReader messageBufferedReader;
    private BufferedWriter messageBufferedWriter;
    private BufferedInputStream fileBufferedInputStream;
    private BufferedOutputStream fileBufferedOutputStream;
    private final String userName;
    private File clientFilesDir;
    private final Scanner scanner;
    private final ExecutorService fileTransferPool;
    public static final Logger logger = LogManager.getLogger();
    private final PropertiesCache properties;


    public Client()
    {
        this.properties = PropertiesCache.getPropertiesCache();
        scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        userName = scanner.nextLine();
        try
        {
            this.messageSocket = new Socket(properties.getProperty("serverIp"), Integer.parseInt(properties.getProperty("messageServerPort")));
            this.fileTransferSocket = new Socket(properties.getProperty("serverIp"), Integer.parseInt(properties.getProperty("transferServerPort")));
            this.messageBufferedReader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
            this.messageBufferedWriter = new BufferedWriter(new OutputStreamWriter(messageSocket.getOutputStream()));
            this.fileBufferedInputStream = new BufferedInputStream(fileTransferSocket.getInputStream());
            this.fileBufferedOutputStream = new BufferedOutputStream(fileTransferSocket.getOutputStream());
            logger.info("You are connected to the serve!");
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
        this.fileTransferPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 3);
        writeToServer(userName);
        this.clientFilesDir = new File(properties.getProperty("storeFiles"));
    }


    @Override public void run()
    {
        listenForMessage();
        scanner.close();
    }


    /**
     * Send message to server.
     */
    public void sendMessage()
    {
        Thread thread = new Thread(() ->
                                   {
                                       try
                                       {
                                           while (!messageSocket.isClosed())
                                           {
                                               while (System.in.available() != 0)
                                               {
                                                   messageHandling(scanner.nextLine());
                                               }
                                           }
                                       }
                                       catch (SocketException e)
                                       {
                                           // when a user uses ADMIN:DRAIN, the server is still not shutdown the user wants to EXIT!!!
                                           logger.info("You left the group chat!");
                                       }
                                       catch (IOException e)
                                       {
                                           logger.error(e.getMessage(), e);
                                       }
                                   });
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Listen for message from server.
     */
    public void listenForMessage()
    {
        String serverMessage;
        String[] splitServerMessage;
        while (!messageSocket.isClosed())
        {
            try
            {
                serverMessage = messageBufferedReader.readLine();
                //                server shutdown
                if (serverMessage == null)
                {
                    closeEverything();
                    break;
                }

                splitServerMessage = serverMessage.split(" ");
                switch (splitServerMessage[0])
                {
                    case "rejected":
                        closeEverything();
                        logger.info("You were rejected from the server!");
                        System.out.println("You were rejected from the server!");
                        break;
                    case "welcome":
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
            catch (SocketException e)
            {
                //                when a user uses ADMIN:DRAIN, the server is still not shutdown the user wants to EXIT!!!
                logger.info("You left the group chat!");
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
                closeEverything();
                break;
            }
        }
    }


    /**
     * Receiving a file from the server
     *
     * @param fileName   the file name received
     * @param fileLength the length of the file to be received
     */
    private void receiveFileFromServer(String fileName, String fileLength)
    {
        fileTransferPool.execute(new Runnable()
        {
            @Override public void run()
            {
                if(!clientFilesDir.isDirectory()){
                    clientFilesDir.mkdir();
                }
                int fileLengthInt = Integer.parseInt(fileLength);
                File file = new File(clientFilesDir, fileName);
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
                {
                    for (int i = 0; i < fileLengthInt; i++)
                    {
                        out.write(fileBufferedInputStream.read());
                    }
                    out.flush();
                    System.out.println("File " + fileName + " was successfully downloaded!");
                    logger.info("File " + fileName + " was successfully downloaded!");
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }


    /**
     * Send a message to server.
     *
     * @param message message to send to the server
     */
    private void writeToServer(String message)
    {
        try
        {
            messageBufferedWriter.write(userName + ": " + message);
            messageBufferedWriter.newLine();
            messageBufferedWriter.flush();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * Handling the client's message and execution any necessary operations on it.
     *
     * @param clientMessage the message from the client
     */
    public void messageHandling(String clientMessage)
    {
        String[] splitClientMessage = clientMessage.split(" ");
        switch (splitClientMessage[0])
        {
            case "EXIT":
                writeToServer("EXIT");
                closeEverything();
                System.out.println("You left the chat server!");
                break;
            case "SEND":
                File file = new File(splitClientMessage[1]);
                writeToServer("SEND " + file.getName() + " " + file.length());
                sendFileToServer(file);
                break;
            default:
                writeToServer(clientMessage);
                break;
        }
    }


    /**
     * Send file to server.
     *
     * @param file the file to be sent to the server
     */
    private void sendFileToServer(File file)
    {
        fileTransferPool.execute(() ->
                                 {
                                     System.out.println("Sending ...");
                                     try (InputStream in = new BufferedInputStream(new FileInputStream(file)))
                                     {
                                         int data = in.read();
                                         while (data != -1)
                                         {
                                             fileBufferedOutputStream.write(data);
                                             data = in.read();
                                         }
                                         fileBufferedOutputStream.flush();
                                         System.out.println("File " + file.getName() + " has been sent to the server!");
                                     }
                                     catch (IOException e)
                                     {
                                         logger.error(e.getMessage(), e);
                                     }
                                 });
    }



    /**
     * Closes the socket and the streams attached to the socket itself.
     * Furthermore, executes any necessary operations to close the connection (reallocating memory).
     */
    public void closeEverything()
    {
        try
        {
            if (!messageSocket.isClosed())
            {
                messageSocket.close();
            }
            if (!fileTransferSocket.isClosed())
            {
                fileTransferSocket.close();
            }
            if (messageBufferedReader != null)
            {
                messageBufferedReader.close();
            }
            if (messageBufferedWriter != null)
            {
                messageBufferedWriter.close();
            }
            if (fileBufferedInputStream != null)
            {
                fileBufferedInputStream.close();
            }
            if (fileBufferedOutputStream != null)
            {
                fileBufferedOutputStream.close();
            }
            fileTransferPool.shutdown();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}

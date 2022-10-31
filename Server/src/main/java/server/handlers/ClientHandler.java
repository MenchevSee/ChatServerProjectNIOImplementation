//package server.handlers;
//
//
//import server.file_cache.FilesCacheRepo;
//import server.server_commands.Command;
//import server.server_commands.CommandFactory;
//import server.service.Server;
//
//import java.io.*;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//
//public class ClientHandler extends Handler
//{
//    public static final List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>());
//    protected static final FilesCacheRepo filesCache = FilesCacheRepo.getFilesCache();
//    private Long timeStampLastActive;
//    private static Long maximumInactiveTimeAllowed;
//    private static final ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
//    private CommandFactory commandFactory;
//
//
//    public ClientHandler(Socket messageSocket, Socket fileSocket, Server server)
//    {
//        super(messageSocket, fileSocket, server);
//        try
//        {
//            this.clientUserName = messageBufferedReader.readLine().split(" ")[1];
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//            closeEverything();
//        }
//        instantiateInfoForClientHandler();
//    }
//
//
//    //    used by WaitingHandler to pass the socket of the next client to be let in the group chat
//    protected ClientHandler(Socket messageSocket, Socket fileSocket, String clientUserName, Server server)
//    {
//        super(messageSocket, fileSocket, server);
//        instantiateInfoForClientHandler();
//        this.clientUserName = clientUserName;
//    }
//
//
//    /**
//     * Common instantiating operations.
//     */
//    private void instantiateInfoForClientHandler()
//    {
//        this.timeStampLastActive = System.currentTimeMillis();
//        clientHandlerList.add(this);
//        writeToClient("welcome");
//        writeToClient("You joined the group chat!");
//        commandFactory = new CommandFactory();
////        Command command = commandFactory.getInstance("SERVER: " + clientUserName + " joined the chat!");
////        commandExecutor.execute(command);
//    }
//
//
//    @Override public void run()
//    {
//        while (!messageSocket.isClosed())
//        {
//            try
//            {
//                if (System.currentTimeMillis() - timeStampLastActive >= maximumInactiveTimeAllowed)
//                {
//                    writeToClient("You have been kicked due to inactivity!");
//                    closeEverything();
//                }
//                if (messageBufferedReader.ready())
//                {
//                    timeStampLastActive = System.currentTimeMillis();
////                    Command command = commandFactory.getInstance(messageBufferedReader.readLine());
////                    commandExecutor.execute(command);
//                }
//            }
//            catch (SocketException e)
//            {
//                //when command ADMIN:KILL is used and THIS client is removed by another one's thread, it triggers socketException
//                Server.LOGGER.info("Client " + clientUserName + " has been removed by another client!", e);
//            }
//            catch (IOException e)
//            {
//                Server.LOGGER.error(e.getMessage(), e);
//                closeEverything();
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void removeHandler()
//    {
//        clientHandlerList.remove(this);
////        Command command = commandFactory.getInstance("SERVER: " + clientUserName + " has left the chat!");
////        commandExecutor.execute(command);
//    }
//
//
//    @Override
//    public void closeEverything()
//    {
//        if (!Server.drainOfServerInitiated.get())
//        {
//            future.cancel(true);
//            removeHandler();
//            Server.LOGGER.info(clientUserName + " left the group chat!");
//        }
//        try
//        {
//            if (!messageSocket.isClosed())
//            {
//                messageSocket.close();
//            }
//            if (!fileSocket.isClosed())
//            {
//                fileSocket.close();
//            }
//            if (messageBufferedReader != null)
//            {
//                messageBufferedReader.close();
//            }
//            if (messageBufferedWriter != null)
//            {
//                messageBufferedWriter.close();
//            }
//            if (fileBufferedInputStream != null)
//            {
//                fileBufferedInputStream.close();
//            }
//            if (fileBufferedOutputStream != null)
//            {
//                fileBufferedOutputStream.close();
//            }
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.warn(e.getMessage(), e);
//        }
//    }
//
//
//    public static void setMaximumInactiveTimeAllowed(Long maximumInactiveTimeAllowed)
//    {
//        ClientHandler.maximumInactiveTimeAllowed = maximumInactiveTimeAllowed;
//    }
//
//
//    public static ExecutorService getCommandExecutor()
//    {
//        return commandExecutor;
//    }
//}

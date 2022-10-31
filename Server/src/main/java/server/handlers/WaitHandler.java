//package server.handlers;
//
//
//import server.license.LicenseManagement;
//import server.service.Server;
//
//import java.io.*;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//
//
//public class WaitHandler extends Handler
//{
//    protected static final List<WaitHandler> waitingClientHandlerList = Collections.synchronizedList(new ArrayList<>());
//    private int timeCounter;
//    private final LicenseManagement licenseManager;
//    private final ExecutorService chatPool;
//
//
//    public WaitHandler(Socket messageSocket, Socket fileSocket, Server server)
//    {
//        super(messageSocket, fileSocket, server);
//        this.licenseManager = Server.licenseManager;
//        this.chatPool = Server.chatPool;
//        try
//        {
//            clientUserName = messageBufferedReader.readLine();
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//            closeEverything();
//        }
//        waitingClientHandlerList.add(this);
//    }
//
//
//    @Override public void run()
//    {
//        if (timeCounter == 10)
//        {
//            writeToClient("rejected");
//            closeEverything();
//        }
//
//        timeCounter++;
//
//        if (!messageSocket.isClosed())
//        {
//            if (licenseManager.verify())
//            {
//                ClientHandler clientHandler = new ClientHandler(this.messageSocket, fileSocket, clientUserName, server);
//                clientHandler.setFuture(chatPool.submit(clientHandler));
//                future.cancel(true);
//                removeHandler();
//            }
//            else
//            {
//                int rankInQueue = WaitHandler.waitingClientHandlerList.indexOf(this) + 1;
//                writeToClient("You are number " + rankInQueue + " in the waiting queue!");
//            }
//        }
//    }
//
//
//    @Override
//    public void closeEverything()
//    {
//        removeHandler();
//        future.cancel(true);
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
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//        }
//    }
//
//
//    @Override
//    protected void removeHandler()
//    {
//        waitingClientHandlerList.remove(this);
//    }
//}

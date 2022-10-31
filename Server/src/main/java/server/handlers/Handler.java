//package server.handlers;
//
//
//import server.service.Server;
//
//import java.io.*;
//import java.net.Socket;
//import java.util.concurrent.Future;
//
//
//public abstract class Handler implements Runnable
//
//{
//    protected Socket messageSocket;
//    protected Socket fileSocket;
//    protected Server server;
//    protected String clientUserName;
//    protected BufferedReader messageBufferedReader;
//    protected BufferedWriter messageBufferedWriter;
//    protected BufferedInputStream fileBufferedInputStream;
//    protected BufferedOutputStream fileBufferedOutputStream;
//    protected Future future;
//
//
//    protected Handler(Socket messageSocket, Socket fileSocket, Server server)
//    {
//        this.messageSocket = messageSocket;
//        this.fileSocket = fileSocket;
//        this.server = server;
//        try
//        {
//            this.messageBufferedReader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
//            this.messageBufferedWriter = new BufferedWriter(new OutputStreamWriter(messageSocket.getOutputStream()));
//            this.fileBufferedInputStream = new BufferedInputStream(fileSocket.getInputStream());
//            this.fileBufferedOutputStream = new BufferedOutputStream(fileSocket.getOutputStream());
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//            closeEverything();
//        }
//    }
//
//
//    /**
//     * Write a message to the client.
//     *
//     * @param message the message that will be sent to the client
//     */
//    public void writeToClient(String message)
//    {
//        try
//        {
//            messageBufferedWriter.write(message);
//            messageBufferedWriter.newLine();
//            messageBufferedWriter.flush();
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//            closeEverything();
//        }
//    }
//
//
//    public void setFuture(Future future)
//    {
//        this.future = future;
//    }
//
//
//    protected abstract void removeHandler();
//
//    /**
//     * Closes the socket and the streams attached to the socket itself.
//     * Furthermore, executes any necessary operations to close the connection (reallocating memory).
//     */
//    public abstract void closeEverything();
//
//
//    public String getClientUserName()
//    {
//        return clientUserName;
//    }
//
//
//    public BufferedInputStream getFileBufferedInputStream()
//    {
//        return fileBufferedInputStream;
//    }
//
//
//    public BufferedOutputStream getFileBufferedOutputStream()
//    {
//        return fileBufferedOutputStream;
//    }
//
//
//    public BufferedReader getMessageBufferedReader()
//    {
//        return messageBufferedReader;
//    }
//
//
//    public BufferedWriter getMessageBufferedWriter()
//    {
//        return messageBufferedWriter;
//    }
//
//
//    public Server getServer()
//    {
//        return server;
//    }
//}

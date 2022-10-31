//package server.server_commands;
//
//
//import server.handlers.ClientHandler;
//import server.service.Server;
//
//import java.io.*;
//
//
//public class FileGetCommand extends Command
//{
//    private final String fileName;
//    public FileGetCommand(ClientHandler clientHandler, String fileName)
//    {
//        super(clientHandler);
//        this.fileName = fileName;
//    }
//
//    @Override public void run()
//    {
//        if (!Server.drainOfServerInitiated.get())
//        {
//            Server.fileTransferPool.execute(new Runnable()
//            {
//                @Override public void run()
//                {
//                    File downloadFile = filesCache.getFile(fileName);
//                    if (downloadFile == null)
//                    {
//                        clientHandler.writeToClient("SERVER: the requested file is not present in the repository!");
//                    }
//                    else
//                    {
//                        clientHandler.writeToClient("SENDING " + downloadFile.getName() + " " + downloadFile.length());
//                        try (InputStream in = new BufferedInputStream(new FileInputStream(downloadFile)))
//                        {
//                            int data = in.read();
//                            while (data != -1)
//                            {
//                                fileBufferedOutputStream.write(data);
//                                data = in.read();
//                            }
//                            fileBufferedOutputStream.flush();
//                            Server.LOGGER.info("File " + downloadFile.getName() + " was downloaded successfully by client " + clientUserName);
//                        }
//                        catch (IOException e)
//                        {
//                            Server.LOGGER.error(e.getMessage(), e);
//                        }
//                    }
//                }
//            });
//        }
//        else
//        {
//            clientHandler.writeToClient("Server drain has been initiated. No more files can be downloaded from the server!");
//        }
//    }
//}

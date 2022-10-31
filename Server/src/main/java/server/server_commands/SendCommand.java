//package server.server_commands;
//
//
//import server.handlers.ClientHandler;
//import server.service.Server;
//
//
//public class SendCommand extends Command
//{
//    private final String fileName;
//    private final String fileLength;
//
//
//    public SendCommand(ClientHandler clientHandler, String fileName, String fileLength)
//    {
//        super(clientHandler);
//        this.fileName = fileName;
//        this.fileLength = fileLength;
//    }
//
//
//    @Override public void run()
//    {
//        if (!Server.drainOfServerInitiated.get())
//        {
//            Server.fileTransferPool.execute(new Runnable()
//            {
//                @Override public void run()
//                {
//                    Server.LOGGER.info("Receiving file " + fileName);
//                    if (filesCache.addFile(fileName, fileBufferedInputStream, fileLength))
//                    {
//                        clientHandler.writeToClient("File uploaded successfully!");
//                        Command command = commandFactory.getInstance(
//                                        "SERVER: " + clientUserName + " uploaded " + fileName + " to the server!");
//                        commandExecutor.execute(command);
//                        Server.LOGGER.info(clientHandler.getClientUserName() + " uploaded " + fileName + " to the server!");
//                    }
//                    else
//                    {
//                        clientHandler.writeToClient("The server was unable to upload the file successfully!");
//                    }
//                }
//            });
//        }
//        else
//        {
//            clientHandler.writeToClient("Server drain has been initiated. No more files can be send to the server!");
//        }
//    }
//}

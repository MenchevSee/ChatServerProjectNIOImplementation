//package server.server_commands;
//
//
//import server.handlers.ClientHandler;
//import server.service.Server;
//
//
//public class AdminDrainCommand extends Command
//{
//    private final Server server;
//
//
//    public AdminDrainCommand(ClientHandler clientHandler)
//    {
//        super(clientHandler);
//        this.server = clientHandler.getServer();
//    }
//
//
//    @Override public void run()
//    {
//        Command command = commandFactory.getInstance("SERVER: The server will stop responding in 1 minute. \n" +
//                                                                     "All uploads and downloads of files is restricted from now on till shutdown!");
//        commandExecutor.execute(command);
//        server.closeServer();
//        Server.drainOfServerInitiated.set(true);
//        try
//        {
//            Thread.sleep(60000);
//            for (ClientHandler clientHandler : ClientHandler.clientHandlerList)
//            {
//                clientHandler.closeEverything();
//            }
//            commandExecutor.shutdown();
//        }
//        catch (InterruptedException e)
//        {
//            Server.LOGGER.info("The thread closing all client socket has been interrupted!");
//        }
//    }
//}

//package server.server_commands;
//
//
//import server.handlers.ClientHandler;
//
//
//public class AdminKillCommand extends Command
//{
//    private final String clientUserName;
//    public AdminKillCommand(ClientHandler clientHandler, String clientUserName)
//    {
//        super(clientHandler);
//        this.clientUserName = clientUserName;
//    }
//
//
//    @Override public void run()
//    {
//        for (ClientHandler clientHandler : ClientHandler.clientHandlerList)
//        {
//            if (clientHandler.getClientUserName().equals(clientUserName))
//            {
//                clientHandler.writeToClient("You have been removed by the admin!");
//                clientHandler.closeEverything();
//            }
//        }
//    }
//}

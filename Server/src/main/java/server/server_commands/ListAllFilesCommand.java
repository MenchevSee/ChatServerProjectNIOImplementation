//package server.server_commands;
//
//
//import server.handlers.ClientHandler;
//import server.service.Server;
//
//import java.io.File;
//import java.nio.channels.SocketChannel;
//
//
//public class ListAllFilesCommand extends Command
//{
//    public ListAllFilesCommand(ClientHandler clientHandler)
//    {
//        super();
//    }
//
//
//    @Override public void run()
//    {
//        File file = new File(Server.properties.getProperty("storedFilesDir"));
//        String[] fileList = file.list();
//        for (String fileName : fileList)
//        {
//            clientHandler.writeToClient(fileName);
//        }
//    }
//}

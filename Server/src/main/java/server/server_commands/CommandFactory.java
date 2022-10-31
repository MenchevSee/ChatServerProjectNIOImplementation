package server.server_commands;


import java.nio.channels.SocketChannel;


public class CommandFactory
{
//    TODO: restructure the class
    public Command getInstance(String clientMessage, SocketChannel clientSocketChannel)
    {
        Command command = null;
        String[] splitClientMessage = clientMessage.split(" ");
        String commandName = splitClientMessage[1];
        switch (commandName)
        {
//            case "EXIT":
//                command = new ExitCommand(clientHandler);
//                break;
//            case "TIME":
//                command = new TimeCommand(clientHandler);
//                break;
//            case "SEND":
//                command = new SendCommand(clientHandler, splitClientMessage[2], splitClientMessage[3]);
//                break;
//            case "FILE-GET":
//                command = new FileGetCommand(clientHandler, splitClientMessage[2]);
//                break;
//            case "ADMIN:DRAIN":
//                command = new AdminDrainCommand(clientHandler);
//                break;
//            case "ADMIN:KILL":
//                command = new AdminKillCommand(clientHandler, splitClientMessage[2]);
//                break;
//            case "LIST-FILES":
//                command = new ListAllFilesCommand(clientHandler);
//                break;
            default:
                command = new BroadcastMessageCommand(clientMessage, clientSocketChannel, false);
                break;
        }
        return null;
    }
}

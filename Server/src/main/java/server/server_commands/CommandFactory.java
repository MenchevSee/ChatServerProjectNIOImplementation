package server.server_commands;


import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class CommandFactory
{
//    TODO: restructure the class
    public Command getInstance(String clientMessage, SelectionKey clientSelectionKey)
    {
        Command command = null;
        String[] splitClientMessage = clientMessage.split(" ");
        String commandName = splitClientMessage[1];
        switch (commandName)
        {
            case "EXIT":
                command = new ExitCommand(clientSelectionKey, false);
                break;
            case "TIME":
                command = new TimeCommand(clientSelectionKey, false);
                break;
            case "SEND":
                command = new SendCommand(clientSelectionKey, true, splitClientMessage[2], splitClientMessage[3]);
                break;
            case "FILE-GET":
                command = new FileGetCommand(clientSelectionKey, true, splitClientMessage[2]);
                break;
            case "ADMIN:DRAIN":
                command = new AdminDrainCommand(clientSelectionKey, false);
                break;
            case "ADMIN:KILL":
                command = new AdminKillCommand(clientSelectionKey, false, splitClientMessage[2]);
                break;
            case "LIST-FILES":
                command = new ListAllFilesCommand(clientSelectionKey, false);
                break;
            default:
                command = new BroadcastMessageCommand(clientMessage, clientSelectionKey, false, false);
                break;
        }
        return command;
    }
}

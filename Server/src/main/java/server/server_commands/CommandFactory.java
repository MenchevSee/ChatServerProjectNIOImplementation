package server.server_commands;


import server.service.Server;

import java.nio.channels.SelectionKey;


public class CommandFactory
{
    private final Server server;


    public CommandFactory(Server server)
    {
        this.server = server;
    }


    public Command getInstance(String clientMessage, SelectionKey clientSelectionKey, String type)
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
                command = new AdminDrainCommand(clientSelectionKey, false, server);
                break;
            case "ADMIN:KILL":
                command = new AdminKillCommand(clientSelectionKey, false, splitClientMessage[2]);
                break;
            case "LIST-FILES":
                command = new ListAllFilesCommand(clientSelectionKey, false);
                break;
            default:
                command = new BroadcastMessageCommand(clientMessage, clientSelectionKey, false, type);
                break;
        }
        return command;
    }
}

package server.server_commands;


import server.handlers.ClientHandler;

import java.nio.channels.SelectionKey;


public class AdminKillCommand extends Command
{
    public AdminKillCommand(SelectionKey clientSelectionKey)
    {
        super(clientSelectionKey);
    }

    @Override public void run()
    {
        for (ClientHandler clientHandler : ClientHandler.clientHandlerList)
        {
            if (clientHandler.getClientUserName().equals(clientUserName))
            {
                clientHandler.writeToClient("You have been removed by the admin!");
                clientHandler.closeEverything();
            }
        }
    }
}

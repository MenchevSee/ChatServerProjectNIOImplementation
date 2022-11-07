package server.server_commands;


import server.service.Server;

import java.io.File;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class ListAllFilesCommand extends Command
{
    public ListAllFilesCommand(SelectionKey clientSelectionKey, boolean isFileTransfer)
    {
        super(clientSelectionKey, isFileTransfer);
    }


    @Override public void run()
    {
        File file = new File(Server.properties.getProperty("storedFilesDir"));
        String[] fileList = file.list();
        for (String fileName : fileList)
        {
            writeToClient(fileName, clientSocketChannel);
        }
    }
}

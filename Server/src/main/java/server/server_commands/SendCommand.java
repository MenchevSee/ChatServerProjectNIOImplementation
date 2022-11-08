package server.server_commands;


import server.client.Client;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class SendCommand extends Command
{
    private final String fileName;
    private final int fileLength;


    public SendCommand(SelectionKey clientSelectionKey, boolean isFileTransfer, String fileName, String fileLength)
    {
        super(clientSelectionKey, isFileTransfer);
        this.fileName = fileName;
        this.fileLength = Integer.parseInt(fileLength);
    }


    @Override public void run()
    {
        SocketChannel fileSocketChannel = Client.clients.get(clientSelectionKey.attachment()).getFilesChannel();
        if (filesCache.addFile(fileName, fileSocketChannel, fileLength))
        {
            writeToClient("File uploaded successfully!", clientSocketChannel);
        }
        else
        {
            writeToClient("The server was unable to upload the file successfully!", clientSocketChannel);
        }
    }
}

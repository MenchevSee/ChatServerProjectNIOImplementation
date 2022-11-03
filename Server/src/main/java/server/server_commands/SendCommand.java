package server.server_commands;


import server.client.Client;
import server.service.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class SendCommand extends Command
{
    private final String fileName;
    private final int fileLength;
    private SocketChannel fileSocketChannel;


    public SendCommand(SelectionKey clientSelectionKey, String fileName, String fileLength)
    {
        super(clientSelectionKey);
        this.fileName = fileName;
        this.fileLength = Integer.parseInt(fileLength);
    }


    @Override public void run()
    {
        fileSocketChannel = Client.clients.get(clientSelectionKey.attachment()).getFilesChannel();
        if (filesCache.addFile(fileName, fileSocketChannel, fileLength))
        {
            writeToClient("File uploaded successfully!", (SocketChannel)clientSelectionKey.channel());
        }
        else
        {
            writeToClient("The server was unable to upload the file successfully!", (SocketChannel)clientSelectionKey.channel());
        }
    }
}

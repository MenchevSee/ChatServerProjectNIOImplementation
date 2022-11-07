package server.server_commands;


import server.client.Client;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class FileGetCommand extends Command
{
    private final String fileName;


    public FileGetCommand(SelectionKey clientSelectionKey, boolean isFileTransfer, String fileName)
    {
        super(clientSelectionKey, isFileTransfer);
        this.fileName = fileName;
    }


    @Override public void run()
    {
        SocketChannel fileSocketChannel = Client.clients.get(clientSelectionKey.attachment()).getFilesChannel();
        File file = filesCache.getFile(fileName);
        if (file == null)
        {
            writeToClient("SERVER: the requested file is not present in the repository!", clientSocketChannel);
        }
        else
        {
            writeToClient("SENDING " + file.getName() + " " + file.length(), clientSocketChannel);
            try (
                            FileInputStream fin = new FileInputStream(file);
                            FileChannel fileChannel = fin.getChannel();
            )
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                while (fileChannel.read(byteBuffer) != -1)
                {
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining())
                    {
                        fileSocketChannel.write(byteBuffer);
                    }
                    byteBuffer.clear();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

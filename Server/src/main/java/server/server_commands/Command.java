package server.server_commands;


import server.file_cache.FilesCacheRepo;
import server.service.SocketAcceptor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;


public abstract class Command implements Runnable
{

    protected SelectionKey clientSelectionKey;
    protected Set<SelectionKey> selectionKeys;
    protected FilesCacheRepo filesCache;
    private boolean isFileTransfer;

    protected Command(SelectionKey clientSelectionKey, boolean threadPoolFlag)
    {
        this.clientSelectionKey = clientSelectionKey;
        this.isFileTransfer = threadPoolFlag;
        this.selectionKeys = SocketAcceptor.messageChannelsSelector.keys();
        this.filesCache = FilesCacheRepo.getFilesCache();
    }


    protected void writeToClient(String clientMessage, SocketChannel clientMessageSocketChannel)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(clientMessage.getBytes());
        try
        {
            clientMessageSocketChannel.write(byteBuffer);
            while (byteBuffer.hasRemaining())
            {
                clientMessageSocketChannel.write(byteBuffer);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public boolean getIsFileTransfer()
    {
        return isFileTransfer;
    }
}

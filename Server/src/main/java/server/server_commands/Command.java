package server.server_commands;


import server.file_cache.FilesCacheRepo;
import server.service.Server;
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
    protected SocketChannel clientSocketChannel;


    protected Command(SelectionKey clientSelectionKey, boolean threadPoolFlag)
    {
        this.clientSelectionKey = clientSelectionKey;
        this.clientSocketChannel = (SocketChannel)clientSelectionKey.channel();
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
            Server.logger.error(e);
        }
    }

    protected void writeToAllClients(String clientMessage, boolean includeThisClient)
    {
        if (includeThisClient)
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                writeToClient(clientMessage, (SocketChannel) selectionKey.channel());
            }
        }
        else
        {
            for (SelectionKey selectionKey : selectionKeys)
            {
                SocketChannel clientMessageSocketChannel = (SocketChannel)selectionKey.channel();
                if (!clientMessageSocketChannel.equals(clientSocketChannel))
                {
                    writeToClient(clientMessage, (SocketChannel) selectionKey.channel());
                }
            }
        }
    }
    public boolean getIsFileTransfer()
    {
        return isFileTransfer;
    }
}

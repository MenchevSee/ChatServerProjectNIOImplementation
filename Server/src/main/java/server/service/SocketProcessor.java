package server.service;


import org.apache.commons.io.IOUtils;
import server.server_commands.Command;
import server.server_commands.CommandFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketProcessor implements Runnable
{
    public static ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    private CommandFactory commandFactory;


    public SocketProcessor()
    {
        this.commandFactory = new CommandFactory();
    }


    @Override public void run()
    {
        while (SocketAcceptor.messageChannelsSelector.isOpen())
        {
            try
            {
                //                int sth = SocketAcceptor.messageChannelsSelector.select();
                int selectionKeyCount = SocketAcceptor.messageChannelsSelector.selectNow();
                if (selectionKeyCount == 0)
                {
                    continue;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Set<SelectionKey> selectedKeys = SocketAcceptor.messageChannelsSelector.selectedKeys();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext())
            {
                //            TODO: pass the code below to another thread???
                SelectionKey selectionKey = keyIterator.next();
                SocketChannel messageSocketChannel = (SocketChannel)selectionKey.channel();
                try
                {
                    int bytesRead = messageSocketChannel.read(byteBuffer);
                    while (bytesRead > 0)
                    {
                        bytesRead = messageSocketChannel.read(byteBuffer);
                    }
                    byteBuffer.flip();
                    String clientMessage = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    byteBuffer.clear();
                    System.out.println(clientMessage);

                    //                in case this is the first input from the client - a.k.a. its username
                    if (selectionKey.attachment() == null)
                    {
                        selectionKey.attach(clientMessage.split(" ")[1]);
                        selectionKey.cancel();
                        Command command = commandFactory.getInstance(
                                        "SERVER: " + clientMessage.split(" ")[1] + " has entered the chat!",
                                        selectionKey);
                        commandExecutor.execute(command);
                    }
                    else
                    {
                        Command command = commandFactory.getInstance(clientMessage, selectionKey);
                        commandExecutor.execute(command);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                keyIterator.remove();
            }
        }
    }


    public static void closeClientConnection(SelectionKey clientSelectionKey)
    {
        clientSelectionKey.cancel();
        try
        {
            System.out.println("closing client connection!");
            clientSelectionKey.channel().close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

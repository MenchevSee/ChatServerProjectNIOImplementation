package server.service;


import server.client.Client;
import server.server_commands.Command;
import server.server_commands.CommandFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketProcessor implements Runnable
{
    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    private CommandFactory commandFactory;


    public SocketProcessor()
    {
        this.commandFactory = new CommandFactory();
    }


    @Override public void run()
    {
        handleTransferOfFiles();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (SocketAcceptor.messageChannelsSelector.isOpen())
        {
            try
            {
                //                int sth = SocketAcceptor.messageChannelsSelector.select();
                if (SocketAcceptor.messageChannelsSelector.selectNow() == 0)
                {
                    continue;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Set<SelectionKey> selectedKeys = SocketAcceptor.messageChannelsSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext())
            {
                //            TODO: pass the code below to another thread???
                SelectionKey selectionKey = keyIterator.next();
                SocketChannel messageSocketChannel = (SocketChannel)selectionKey.channel();
                try
                {
                    while (messageSocketChannel.read(byteBuffer) > 0)
                    {
                        messageSocketChannel.read(byteBuffer);
                    }
                    byteBuffer.flip();
                    String clientMessage = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    byteBuffer.clear();
                    System.out.println(clientMessage);

                    //                in case this is the first input from the client - a.k.a. its username
                    if (selectionKey.attachment() == null)
                    {
                        String userName = clientMessage.split(" ")[1];
                        if (!Client.clients.containsKey(userName))
                        {
                            selectionKey.attach(userName);
                            Client client = new Client(userName);
                            client.setMessagesChannel((SocketChannel)selectionKey.channel());
                            Command command = commandFactory.getInstance(
                                            "SERVER: " + userName + " has entered the chat!",
                                            selectionKey);
                            commandExecutor.execute(command);
                        }
                        else
                        {
                            //                        TODO: in  case the client name already exists
                            closeClientConnection(selectionKey);
                        }
                    }
                    else
                    {
                        Command command = commandFactory.getInstance(clientMessage, selectionKey);
                        commandExecutor.execute(command);
                    }
                }
                catch (IOException e)
                {
                    if (e.getMessage().contains("An existing connection was forcibly closed by the remote host"))
                    {
                        SocketProcessor.closeClientConnection(selectionKey);
                        continue;
                    }
                    e.printStackTrace();
                }
                keyIterator.remove();
            }
        }
    }


    private void handleTransferOfFiles()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override public void run()
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                while (SocketAcceptor.fileChannelsSelector.isOpen())
                {
                    try
                    {
                        if (SocketAcceptor.fileChannelsSelector.selectNow() == 0)
                        {
                            continue;
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    Set<SelectionKey> selectionKeys = SocketAcceptor.fileChannelsSelector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                    while (keyIterator.hasNext())
                    {
                        SelectionKey selectionKey = keyIterator.next();
                        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                        if (selectionKey.isReadable())
                        {
                            if (selectionKey.attachment() == null)
                            {
                                try
                                {
                                    while (socketChannel.read(byteBuffer) > 0)
                                    {
                                        socketChannel.read(byteBuffer);
                                    }
                                    byteBuffer.flip();
                                    String clientName = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                                    byteBuffer.clear();
                                    Client client = Client.clients.get(clientName);
                                    client.setFilesChannel((SocketChannel)selectionKey.channel());
                                    selectionKey.cancel();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    public static void closeClientConnection(SelectionKey clientSelectionKey)
    {
        Client.clients.get(clientSelectionKey.attachment()).closeClientConnection();
        clientSelectionKey.cancel();
        System.out.println("closing client connection!");
    }
}

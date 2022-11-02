package server.server_commands;



import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;


public class TimeCommand extends Command
{
    public TimeCommand(SelectionKey clientSelectionKey)
    {
        super(clientSelectionKey);
    }


    @Override public void run()
    {
        writeToClient(new Date().toString(), (SocketChannel) clientSelectionKey.channel());
    }
}

package server.file_cache;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import server.service.Server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;


public final class FilesCacheRepo
{
    private static FilesCacheRepo filesCache;
    private final LoadingCache<String, File> cachedFiles;
    private final File serverRepo;


    private FilesCacheRepo()
    {
        cachedFiles = CacheBuilder.newBuilder()
                                  .maximumSize(10) //maximum of 10 files can be kept in cache
                                  .expireAfterAccess(2, TimeUnit.MINUTES) //file cached for 2 min
                                  .build(new CacheLoader<>()
                                  {
                                      @Override public File load(String fileName)
                                                      throws FileNotFoundException
                                      {
                                          File file = new File(Server.properties.getProperty("storedFilesDir") + File.separator + fileName);
                                          if (file.exists())
                                          {
                                              return file;
                                          }
                                          throw new FileNotFoundException();
                                      }
                                  });
        serverRepo = new File(Server.properties.getProperty("storedFilesDir"));
    }


    /**
     * @return reference to the filesCache.
     */
    public static synchronized FilesCacheRepo getFilesCache()
    {
        if (filesCache == null)
        {
            return new FilesCacheRepo();
        }
        else
        {
            return filesCache;
        }
    }


    /**
     * Adding a file to repository.
     *
     * @param fileName            the file name to be added
     * @param fileLength          the length of the file to be added
     */
    public boolean addFile(String fileName, SocketChannel fileChannelSocket, int fileLength)
    {
        if (!serverRepo.isDirectory())
        {
            serverRepo.mkdir();
        }
        File file = new File(serverRepo, fileName);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        try (
                        FileOutputStream out = new FileOutputStream(file);
                        FileChannel fileChannel = out.getChannel();)
        {
            int bytesRead = 0;
            while (bytesRead < fileLength)
            {
                bytesRead += fileChannelSocket.read(byteBuffer);
                byteBuffer.flip();
                while (byteBuffer.hasRemaining())
                {
                    fileChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            return true;
        }
        catch (IOException e)
        {
            Server.logger.error(e);
            return false;
        }
    }


    /**
     * Returns the file if it is present in the repo.
     *
     * @param fileName the name of the file to be returned
     * @return the file if found in the repo
     */
    public File getFile(String fileName)
    {
        File file = null;
        try
        {
            file = cachedFiles.get(fileName);
        }
        catch (ExecutionException e)
        {
            Server.logger.error(e);
        }
        return file;
    }
}

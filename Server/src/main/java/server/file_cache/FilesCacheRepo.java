//package server.file_cache;
//
//
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import server.properties.PropertiesCache;
//import server.service.Server;
//
//import java.io.*;
//import java.util.concurrent.*;
//
//
//public final class FilesCacheRepo
//{
//    private static FilesCacheRepo filesCache;
//    private final LoadingCache<String, File> cachedFiles;
//    private PropertiesCache properties;
//    private File serverRepo;
//
//
//    private FilesCacheRepo()
//    {
//        cachedFiles = CacheBuilder.newBuilder()
//                                  .maximumSize(10) //maximum of 10 files can be kept in cache
//                                  .expireAfterAccess(2, TimeUnit.MINUTES) //file cached for 2 min
//                                  .build(new CacheLoader<>()
//                                  {
//                                      @Override public File load(String fileName)
//                                                      throws FileNotFoundException
//                                      {
//                                          File file = new File(Server.properties.getProperty("storedFilesDir") + "/" + fileName);
//                                          if (file.exists())
//                                          {
//                                              return file;
//                                          }
//                                          throw new FileNotFoundException();
//                                      }
//                                  });
//        serverRepo = new File(Server.properties.getProperty("storedFilesDir"));
//    }
//
//
//    /**
//     * @return reference to the filesCache.
//     */
//    public static synchronized FilesCacheRepo getFilesCache()
//    {
//        if (filesCache == null)
//        {
//            return new FilesCacheRepo();
//        }
//        else
//        {
//            return filesCache;
//        }
//    }
//
//
//    /**
//     * Adding a file to repository.
//     *
//     * @param fileName            the file name to be added
//     * @param bufferedInputStream the buffered stream from the client's socket
//     * @param fileLength          the length of the file to be added
//     */
//    public boolean addFile(String fileName, BufferedInputStream bufferedInputStream, String fileLength)
//    {
//        if (!serverRepo.isDirectory())
//        {
//            serverRepo.mkdir();
//        }
//        File file = new File(serverRepo, fileName);
//        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
//        {
//            int fileLengthInt = Integer.parseInt(fileLength);
//            for (int i = 0; i < fileLengthInt; i++)
//            {
//                out.write(bufferedInputStream.read());
//            }
//            out.flush();
//            return true;
//        }
//        catch (IOException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//            return false;
//        }
//    }
//
//
//    /**
//     * Returns the file if it is present in the repo.
//     *
//     * @param fileName the name of the file to be returned
//     * @return the file if found in the repo
//     */
//    public File getFile(String fileName)
//    {
//        File file = null;
//        try
//        {
//            file = cachedFiles.get(fileName);
//        }
//        catch (ExecutionException e)
//        {
//            Server.LOGGER.error(e.getMessage(), e);
//        }
//        return file;
//    }
//}

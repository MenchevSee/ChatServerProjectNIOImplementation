package server.properties;


import server.service.Server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public final class PropertiesCache extends Properties
{
    private static PropertiesCache propertiesCache;

    private PropertiesCache()
    {
        super();
    }


    public static PropertiesCache getPropertiesCache()
    {
        if (propertiesCache == null)
        {
            propertiesCache = new PropertiesCache();
            try
            {
                propertiesCache.load(new BufferedInputStream(
                                new FileInputStream("C:\\DEV\\workspace\\ChatServerProjectNIOImplementation\\Server\\src\\main\\resources\\serverConfig.properties")));
            }
            catch (IOException e)
            {
//                Server.LOGGER.error(e.getMessage(), e);
            }
        }
        return propertiesCache;
    }
}

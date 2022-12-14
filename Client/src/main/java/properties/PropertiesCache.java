package properties;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class PropertiesCache extends Properties
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
                                new FileInputStream("C:\\DEV\\workspace\\ChatServerProjectNIOImplementation\\Client\\src\\main\\resources\\clientConfig.properties")));
            }
            catch (IOException e)
            {
//                Client.logger.error(e.getMessage(), e);
            }
        }
        return propertiesCache;
    }
}

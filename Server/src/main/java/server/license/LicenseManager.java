package server.license;


import server.service.SocketAcceptor;


public class LicenseManager implements LicenseManagement
{
    private final int licenseQuantity;


    public LicenseManager(int licenseQuantity)
    {
        this.licenseQuantity = licenseQuantity;
    }


    @Override public boolean verify()
    {
        return SocketAcceptor.messageChannelsSelector.keys().size() < licenseQuantity;
    }
}

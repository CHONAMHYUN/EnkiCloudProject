package utility;

import java.net.InetAddress;

public class CommonUtility {
    public static boolean isReachable(String ip)
    {
        InetAddress id = null;
        try {
            InetAddress[] addresses = InetAddress.getAllByName(ip);
            for (InetAddress address : addresses) {
                if (address.isReachable(5000)) return true;
                else return false;
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }
}

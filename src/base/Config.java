package base;

import base.ImageInfo;
import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

    public static boolean isMonitor = false;
    public static final int IMAGE_PORT = 55557;
    public static SimpleWhiteboardPanel panel;

    public static void init() {
        ipList.add(getLocalIp());
        imageInfo = new ImageInfo(0);
    }

    private static List<String> ipList = new ArrayList<String>();

    public static void setIpList(List<String> value) {
        ipList = value;
        listUpdateTime = new Date();
    }

    public static List<String> getIpList() {
        return ipList;
    }

    public static void addIp(String ip) {
        if (ipList.contains(ip)) {
            return;
        }
        ipList.add(ip);
        listUpdateTime = new Date();
    }

    public static void removeIp(String ip) {
        ipList.remove(ip);
        listUpdateTime = new Date();
    }

    private static Date listUpdateTime = null;

    public static Date getIpListUpdateTime() {
        return listUpdateTime;
    }

    public static void freshIpListUpdateTime() {
        listUpdateTime = new Date();
    }

    private static boolean isConnnected = false;

    public static boolean getIsConnected() {
        return isConnnected;
    }

    public static void setIsConnected(boolean value) {
        isConnnected = value;
    }

    private static String localIp = null;

    public static String getLocalIp() {
        if (localIp == null) {
            try {
                localIp = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
        }
        return localIp;
    }

    private static byte[] onlineImageBytes;
    private static Date onlineImageUpdatedTime = new Date();

    static void setOnlineImageBytes(byte[] bytes) {
        onlineImageBytes = bytes;
        onlineImageUpdatedTime = new Date();
    }

    public static ImageInfo imageInfo;

    static void setOnlineWhiteBoardImageInfo(ImageInfo info) {
        imageInfo = info;
    }

    private static String appName = "Whiteboard";

    public static void setAppName(String name) {
        appName = name;
    }

    public static String getAppName() {
        return appName;
    }
}

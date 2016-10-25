package base;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.LogObservable;
import log.LogTarget;

public class MulticastingDealer extends Thread {

    private String message;

    public MulticastingDealer(byte[] buffer, int length) {
        this.message = new String(buffer, 0, length);;
    }

    @Override
    public void run() {
        try {
            String[] arr = message.split(":");
            LogObservable.setMessage("Dealing with UDP message:" + message,LogTarget.CONSOLE, LogTarget.FILE);
            String type = arr[0];
            String content = arr[1];
            switch (type) {
                case "Join":
                    Config.addIp(content);
                    if (content.equalsIgnoreCase(Config.getLocalIp())) {
                        return;
                    }
                    MulticastingUtil.send("Welcome:" + Config.getLocalIp());
                    break;
                case "Welcome":
                    Config.addIp(content);
                    //You do not reply a welcome message.
                    break;
                case "Leave":
                    Config.removeIp(content);
                    break;
                case "ImageUpdated":
                    //If the image is older than local image, do nothing.
                    Long time = Long.parseLong(arr[2]);
                    if(time<Config.imageInfo.getCreateTime())
                    {
                        LogObservable.setMessage("Old Image, stop processing.",LogTarget.CONSOLE, LogTarget.FILE);
                        LogObservable.setMessage("Remote:"+Long.toString(time),LogTarget.CONSOLE, LogTarget.FILE);
                        LogObservable.setMessage("Local :"+Long.toString(Config.imageInfo.getCreateTime()),LogTarget.CONSOLE, LogTarget.FILE);
                        LogObservable.setMessage("Compar:"+Long.toString(time-Config.imageInfo.getCreateTime()),LogTarget.CONSOLE, LogTarget.FILE);
                        return;
                    }
                    
                    if (Config.getIsConnected()) {
                        //Do not update the panel if the image is from the same ip.
                        if (content.equals(Config.getLocalIp()) && !Config.isMonitor) {
                            return;
                        }
                        synchronized (Config.imageInfo) {
                            ImageRequester.Request(content, false);
                            SimpleWhiteboard.getInstance().Update(Config.imageInfo);
                        }
                    }
                    break;
                case "ImageRequest":

                    break;
                case "IPList":
                    String[] ips = content.split(",");
                    List<String> ipList = new ArrayList<String>();
                    for (int i = 0; i < ips.length; i++) {
                        ipList.add(ips[i]);
                    }
                    Config.setIpList(ipList);
                    Config.freshIpListUpdateTime();
                    break;
            };
        } catch (Exception ex) {
            Logger.getLogger(MulticastingDealer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

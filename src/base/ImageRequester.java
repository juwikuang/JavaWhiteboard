/*
 Requests images via TCP connection.
 */
package base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.LogObservable;
import log.LogTarget;

public class ImageRequester {

    public static void Request(String ip, boolean isRetry) {
        if (ip.equals(Config.getLocalIp()) && !Config.isMonitor) {
            return;
        }
        Socket p2pSocket = null;
        PrintWriter textOutput = null;
        try {
            System.out.println("createTime:" +Long.toString(Config.imageInfo.getCreateTime()) );
            InetAddress address = InetAddress.getByName(ip);
            p2pSocket = new Socket();
            p2pSocket.connect(new InetSocketAddress(address, Config.IMAGE_PORT), 5000);
            //ask for the image
            textOutput = new PrintWriter(p2pSocket.getOutputStream());
            textOutput.println("ImageRequest");
            textOutput.flush();
            //get the image
            ObjectInputStream imageInput = new ObjectInputStream(p2pSocket.getInputStream());
            ImageInfo imageOnline = (ImageInfo) imageInput.readObject();
            if (Config.imageInfo.getCreateTime() > imageOnline.getCreateTime()) {
                LogObservable.setMessage("Retrived image from " + ip + ", but the image is too old and will not be processed.", LogTarget.GUI, LogTarget.CONSOLE);
                LogObservable.setMessage("Local Image Time:" + Long.toString(Config.imageInfo.getCreateTime()), LogTarget.CONSOLE);
                LogObservable.setMessage("Remot Image Time:" + Long.toString(imageOnline.getCreateTime()), LogTarget.CONSOLE);
                return;
            }
            textOutput.close();
            imageInput.close();
            p2pSocket.close();
            Config.imageInfo.update(imageOnline);
            //clone the list, otherwise concurrent exception will be thrown.
            ArrayList<String> ips = new ArrayList();
            for (String retryIP : Config.imageInfo.getRetryIPs()) {
                ips.add(retryIP);
            }
            for (String retryIP : ips) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ImageRequester.class.getName()).log(Level.SEVERE, null, ex);
                }
                Request(retryIP, true);
            }
            LogObservable.setMessage("Retrived image from " + ip, LogTarget.GUI, LogTarget.CONSOLE);
            //Check the image
        } catch (java.net.ConnectException ex) {
            LogObservable.setMessage("Failed to retrive image from " + ip + "\r\nPlease check your network.", LogTarget.GUI, LogTarget.CONSOLE);
        } catch (java.io.InvalidClassException ex) {
            if (isRetry) {
                String error = "Image Message from " + ip + " is not recognised.\r\nThat might be caused by whiteboard verson imcompatible \r\nor by malfunctioning terminals.";
                LogObservable.setMessage(error, LogTarget.GUI, LogTarget.CONSOLE);
                Logger.getLogger(ImageRequester.class.getName()).log(Level.SEVERE, error);
            } else {
                //retry
                Request(ip, true);
            }
        } catch (IOException e) {
            LogObservable.setMessage("Failed to retrieve the image from " + ip, LogTarget.GUI, LogTarget.CONSOLE, LogTarget.FILE);
            //This message makes no sense to the end user.
            LogObservable.setMessage("Socket read Error", LogTarget.CONSOLE, LogTarget.FILE);
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImageRequester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (p2pSocket != null && !p2pSocket.isClosed()) {
                    p2pSocket.close();
                }
                //This message makes no sense to the end user.
                LogObservable.setMessage("Connection Closed", LogTarget.CONSOLE, LogTarget.FILE);
            } catch (IOException ex) {
                Logger.getLogger(ImageRequester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        String ip = Config.getLocalIp();
        Config.isMonitor = true;
        if (args.length >= 1) {
            ip = args[0];
        }
        System.out.println("Requester Started");
        Request(ip, false);
    }
}

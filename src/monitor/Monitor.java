/*
 This is simply an alternative of SimpleWhiteboardDemo class.
 I removed the TCP server to aoivd conflict.

 */
package monitor;

import base.Config;
import base.ImageRequester;
import base.MulticastingListener;
import base.MulticastingUtil;
import base.SimpleWhiteboard;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import log.ConsoleLogObserver;
import log.GuiLogObserver;
import log.LogObservable;

public class Monitor implements Runnable {

    private SimpleWhiteboard theWhiteboard;
    private String nodename;

    public Monitor(String nodename) {
        try {
            this.nodename = nodename;
            this.theWhiteboard = SimpleWhiteboard.getInstance();

        } catch (Exception ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        this.theWhiteboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.theWhiteboard.setPreferredSize(new Dimension(800, 600));
        this.theWhiteboard.pack();
        this.theWhiteboard.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        Config.init();
        Config.isMonitor = true;
        Config.setIsConnected(true);
        Config.setAppName("Monitor");
        JFrame.setDefaultLookAndFeelDecorated(true);
        String nodename = "defaultnode";
        if (args.length > 0) {
            nodename = args[0];
        }

        LogObservable.getInstance().addObserver(new GuiLogObserver());
        LogObservable.getInstance().addObserver(new ConsoleLogObserver());
        LogObservable.setMessage("Connected");

        Monitor simpleWhiteboardDemo = new Monitor(
                nodename);
        javax.swing.SwingUtilities.invokeLater(simpleWhiteboardDemo);

        MulticastingListener listener = new MulticastingListener();
        listener.start();
        MulticastingUtil.send("Join:" + Config.getLocalIp());

        //SimpleWhiteboard.getInstance().UpdateMessage("Connected");
        Thread.sleep(200);
        String serverIp = null;
        for (String ip : Config.getIpList()) {
            serverIp = ip;
            break;
        }

        if (serverIp != null) {
            ImageRequester.Request(serverIp, false);
            SimpleWhiteboard.getInstance().Update(Config.imageInfo);
        }
    }
}

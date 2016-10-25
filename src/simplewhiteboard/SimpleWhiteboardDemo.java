package simplewhiteboard;

import base.Config;
import base.ImageDispatcher;
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
import log.LogTarget;

public class SimpleWhiteboardDemo implements Runnable {

    private SimpleWhiteboard simpleWhiteboard;
    private String nodename;

    public SimpleWhiteboardDemo(String nodename) {
        try {
            this.nodename = nodename;
            //Singleton
            this.simpleWhiteboard = SimpleWhiteboard.getInstance();

        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        this.simpleWhiteboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.simpleWhiteboard.setPreferredSize(new Dimension(800, 600));
        this.simpleWhiteboard.pack();
        this.simpleWhiteboard.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        Config.init();
        Config.setIsConnected(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        String nodename = "defaultnode";
        if (args.length > 0) {
            nodename = args[0];
        }
        SimpleWhiteboardDemo simpleWhiteboardDemo = new SimpleWhiteboardDemo(
                nodename);
        javax.swing.SwingUtilities.invokeLater(simpleWhiteboardDemo);

        MulticastingListener updMonitor = new MulticastingListener();
        updMonitor.start();
        ImageDispatcher dispatcher = new ImageDispatcher(Config.imageInfo);
        dispatcher.start();
        //This is important.
        //must wait for the monitor to start before sending upd messages
        //otherwise, replies will not be received.
        Thread.sleep(200);
        MulticastingUtil.send("Join:" + Config.getLocalIp());
        LogObservable.getInstance().addObserver(new GuiLogObserver());
        LogObservable.getInstance().addObserver(new ConsoleLogObserver());
        LogObservable.setMessage("Connected", LogTarget.GUI);
        Thread.sleep(200);
        String serverIp = null;
        for (String ip : Config.getIpList()) {
            if (ip.equalsIgnoreCase(Config.getLocalIp())) {
                continue;
            }

            serverIp = ip;
            break;
        }

        if (serverIp != null) {
            ImageRequester.Request(serverIp, false);
            SimpleWhiteboard.getInstance().Update(Config.imageInfo);
        }
    }
}

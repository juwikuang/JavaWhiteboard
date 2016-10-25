package robot;

import base.Config;
import base.ImageDispatcher;
import base.ImageInfo;
import base.ImageItemInfo;
import base.MulticastingUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.ConsoleLogObserver;
import log.LogObservable;
import log.LogTarget;
import thirdPart.ClockSync;

public class Robot {

    public static ImageInfo image = new ImageInfo(ClockSync.getAdjustedTime());

    //Type a paragraph or sentence.
    //the letters are shown one by one
    //like someone typing.
    public static void type(String s, int interval) {
        while (true) {
            for (int i = 0; i < s.length(); i++) {
                int x = 50 + (i % 50) * 12;
                int y = 50 + (i / 50) * 15;
                ImageItemInfo si = new ImageItemInfo();
                si.type = ImageItemInfo.ImageItemType.TEXT;
                si.text = s.substring(i, i + 1);
                si.color = Color.BLACK;
                si.fontname = "Monospaced";
                si.fontStyle = Font.PLAIN;
                si.fontsize = 20;
                si.point = new Point(x, y);
                image.addItem(si);
                try {
                    MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(image.getCreateTime()));
                } catch (Exception ex) {
                    Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
            }
            image.update(new ImageInfo(ClockSync.getAdjustedTime()));
        }
    }

    public static void move(String s, int interval) {
        while (true) {
            for (int x = 0; x < 400; x = x + 20) {
                image.clone(new ImageInfo(ClockSync.getAdjustedTime()));
                int y = 50;
                ImageItemInfo si = new ImageItemInfo();
                si.type = ImageItemInfo.ImageItemType.TEXT;
                si.text = s;
                si.color = Color.BLACK;
                si.fontname = "Monospaced";
                si.fontStyle = Font.PLAIN;
                si.fontsize = 20;
                si.point = new Point(x, y);
                image.addItem(si);
                try {
                    MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(image.getCreateTime()));
                } catch (Exception ex) {
                    Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
            }
        }
    }

    public static void typeIncomplete(String s, int interval) {
        image.update(new ImageInfo(ClockSync.getAdjustedTime()));
        for (int i = 0; i < s.length(); i++) {
            int x = 50 + (i % 50) * 12;
            int y = 50 + (i / 50) * 15;
            ImageItemInfo si = new ImageItemInfo();
            si.type = ImageItemInfo.ImageItemType.TEXT;
            si.order = i;
            si.text = s.substring(i, i + 1);
            si.color = Color.BLACK;
            si.fontname = "Monospaced";
            si.fontStyle = Font.PLAIN;
            si.fontsize = 20;
            si.point = new Point(x, y);
            if (i == s.length() / 2) {
                System.out.println("Drop item for letter \"" + si.text + "\".");
                System.out.println("Press Enter to continue");
                String newLine = System.console().readLine();
                continue;
            }
            System.out.println("Draw letter \"" + si.text + "\".");
            image.addItem(si);

            try {
                MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(image.getCreateTime()));
            } catch (Exception ex) {
                Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
        }
    }

    public static void file(String filename, int interval) {
        ObjectInputStream ois = null;
        ImageInfo imageFromFile = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(filename));
            imageFromFile = (ImageInfo) ois.readObject();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } finally {
            try {
                ois.close();
            } catch (IOException ex) {
                //No need to make a fuss here.
            }
        }

        for (int i = 0; i < imageFromFile.countItems(); i++) {
            image.addItem(imageFromFile.getItem(i));
            LogObservable.setMessage(String.format("Item %d has been added", i), LogTarget.CONSOLE, LogTarget.FILE);
            try {
                MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(image.getCreateTime()));
            } catch (Exception ex) {
                Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
        }
    }

    public static void main(String args[]) {
        Config.init();
        Config.setIsConnected(true);
        Config.isMonitor = true;

        String action = "typeIncomplete";
        String textOrFile = "Welcome to the UEA. Please give Weiguang Zhou 100. Thanks.";

        int interval = 2000;

        if (args.length >= 1) {
            action = args[0];
        }

        if (args.length >= 2) {
            textOrFile = args[1];
        }

        if (args.length >= 3) {
            interval = Integer.parseInt(args[2]);
        }

        action = action.toUpperCase();
        if (action.equalsIgnoreCase("DECEPTION")) {
            try {
                MulticastingUtil.send("ImageUpdated:133.133.133.123:" + Long.toString(ClockSync.getAdjustedTime()));
            } catch (Exception ex) {
                Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        LogObservable.getInstance().addObserver(new ConsoleLogObserver());

        ImageDispatcher dispatcher = new ImageDispatcher(image);
        dispatcher.start();

        switch (action.toUpperCase()) {
            case "MOVE":
                move(textOrFile, interval);
                break;
            case "TYPE":
                type(textOrFile, interval);
                break;
            case "TYPEINCOMPLETE":
                typeIncomplete(textOrFile, interval);
                break;
            case "FILE":
                file(textOrFile, interval);
                break;
            case "DECEPTION":
                //Handled
                break;
        }
    }

}

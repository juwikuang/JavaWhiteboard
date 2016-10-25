package base;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.LogObservable;
import log.LogTarget;

public class ImageDispatcher extends Thread {

    ImageInfo imageInfo;

    public ImageDispatcher(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    @Override
    public void run() {
        Socket socket = null;
        ServerSocket server = null;

        try {
            server = new ServerSocket(Config.IMAGE_PORT);
        } catch (java.net.BindException ex) {
            Logger.getLogger(ImageDispatcher.class.getName()).log(Level.SEVERE,
                    null, ex);
            LogObservable.setMessage("TCP channel is occupied. Images will not be updated to others. Please check the TCP channel.", LogTarget.CONSOLE, LogTarget.FILE);
        } catch (IOException ex) {
            Logger.getLogger(ImageDispatcher.class.getName()).log(Level.SEVERE,
                    null, ex);
        }

        LogObservable.setMessage("TCP server starts.", LogTarget.CONSOLE);

        while (true) {
            if (!Config.getIsConnected()) {
                continue;
            }
            try {
                socket = server.accept();
                if (!Config.getIsConnected()) {
                    socket.close();
                    continue;
                }
                P2PDispatcher ap2pDispatcher = new P2PDispatcher(socket);
                ap2pDispatcher.start();
            } catch (IOException ex) {

                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex1) {
                    Logger.getLogger(ImageDispatcher.class.getName()).log(
                            Level.SEVERE, null, ex1);
                }

                Logger.getLogger(ImageDispatcher.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }

    class P2PDispatcher extends Thread {

        Socket p2pSocket = null;

        private P2PDispatcher(Socket p2pSocket) {
            this.p2pSocket = p2pSocket;
        }

        @Override
        public void run() {
            try {
                //text input
                BufferedReader textInput = new BufferedReader(
                        new InputStreamReader(p2pSocket.getInputStream()));
                String message = textInput.readLine().toUpperCase();
                switch (message) {
                    case "IMAGEREQUEST":
                        outputImage();
                        break;
                }
                textInput.close();
            } catch (IOException ex) {
                Logger.getLogger(ImageDispatcher.class.getName()).log(
                        Level.SEVERE, null, ex);
            } finally {
                try {
                    p2pSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImageDispatcher.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }

        private void outputImage() {
            ObjectOutputStream dataOut = null;
            try {
                dataOut = new ObjectOutputStream(p2pSocket.
                        getOutputStream());
                dataOut.writeObject(imageInfo);
                dataOut.flush();
                dataOut.close();
                LogObservable.setMessage("Image given to the requester." + p2pSocket.
                        getInetAddress(), LogTarget.CONSOLE);
            } catch (IOException ex) {
                Logger.getLogger(ImageDispatcher.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    dataOut.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImageDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static void main(String args[]) {
        LogObservable.setMessage("ImageDispatcher Started", LogTarget.CONSOLE, LogTarget.FILE);
        Config.setIsConnected(true);
        ImageDispatcher p2p = new ImageDispatcher(generateDate());
        p2p.start();
    }

    public static ImageInfo generateDate() {
        ImageInfo ii = new ImageInfo(0);
        ImageItemInfo si = new ImageItemInfo();
        si.type = ImageItemInfo.ImageItemType.TEXT;
        si.text = (new Date()).toString();
        si.fontname = "Monospaced";
        si.fontsize = 20;
        si.color = Color.BLACK;
        ii.addItem(si);
        return ii;
    }
}

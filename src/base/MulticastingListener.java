package base;

import static base.MulticastingUtil.MULTICASTING_IP;
import static base.MulticastingUtil.RECEIVER_PORT;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MulticastingListener extends Thread {

    @Override
    public void run() {
        InetAddress address;
        MulticastSocket mcs = null;
        try {
            address = InetAddress.getByName(MULTICASTING_IP);
            mcs = new MulticastSocket(RECEIVER_PORT);
            mcs.joinGroup(address);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MulticastingListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MulticastingListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] buffer = new byte[MulticastingUtil.LENGTH];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                mcs.receive(packet);
                if (Config.getIsConnected()) {
                    Thread dealer = new MulticastingDealer(buffer, packet.getLength());
                    dealer.run();
                }

            } catch (Exception ex) {
                Logger.getLogger(MulticastingListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

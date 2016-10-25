package base;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastingUtil {

    public static final String MULTICASTING_IP = "224.0.249.154";
    public static final int SENDER_PORT = 55556;
    public static final int RECEIVER_PORT = 55555;
    public static final int LENGTH = 100;

    public static void send(String message) throws Exception {
        if(message.length()>LENGTH){
            throw new IllegalArgumentException("Message should not be more than 100 characters.");
        }
        
        InetAddress remoteIP = InetAddress.getByName(MULTICASTING_IP);;
        DatagramSocket socket = new DatagramSocket(SENDER_PORT);
        byte[] sendMes = message.getBytes();
        DatagramPacket packet = new DatagramPacket(sendMes, sendMes.length, remoteIP,
                RECEIVER_PORT);
        socket.send(packet);
        socket.close();
    }

    public static String receive() throws Exception {
        InetAddress ia = InetAddress.getByName(MULTICASTING_IP);
        MulticastSocket mcs = new MulticastSocket(RECEIVER_PORT);
        mcs.joinGroup(ia);
        byte[] buffer = new byte[LENGTH];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        mcs.receive(packet);
        String message = new String(buffer);
        System.out.println(message);
        return message;
    }
}

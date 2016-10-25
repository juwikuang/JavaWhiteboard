package thirdPart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;

/**
 * @author Benjamin Hill
 */
public class ClockSync {

    /**
     * The reason for this to exist. Use instead of System.currentTimeMillis()
     */
    public static long getAdjustedTime() {
        return System.currentTimeMillis() + timerOffset;
    }

    /**
     * Sends out a timestamped multicast "ping"
     */
    final class BroadcastTask extends TimerTask {

        @Override
        public void run() {
            ClockSync.this.bout.putShort(PACKET_CLASS);
            ClockSync.this.bout.putInt(ClockSync.this.myId);
            ClockSync.this.bout.putLong(ClockSync.getAdjustedTime());
            final DatagramPacket dp = new DatagramPacket(ClockSync.this.bout.array(), ClockSync.this.bout.position(), ClockSync.this.group, UPNP_MULTI_PORT);
            try {
                ClockSync.this.ms.send(dp);
                ClockSync.this.bout.clear();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            logger.finer("Sent ClockSync ping");
        }
    }

    /**
     * Blocking thread for getting timer packets
     */
    final class ReceiveThread implements Runnable {

        boolean running = false;

        @Override
        public void run() {
            this.running = true;
            while (this.running) {

                try {
                    ClockSync.this.bin.clear();
                    final DatagramPacket dp = new DatagramPacket(ClockSync.this.bin.array(), ClockSync.this.bin.capacity());
                    logger.finest("Waiting for next packet");
                    ClockSync.this.ms.receive(dp);  // BLOCK HERE
                    ClockSync.this.bin.rewind();
                    final long now = ClockSync.getAdjustedTime();
                    final short pclass = ClockSync.this.bin.getShort();
                    if (PACKET_CLASS != pclass) {
                        logger.finest("Random packet, skipping");
                        continue;
                    }
                    final int id = ClockSync.this.bin.getInt();
                    if (id == ClockSync.this.myId) {
                        logger.finest("My own packet, skipping");
                        continue;
                    }
                    final long ts = ClockSync.this.bin.getLong();
                    if (now >= ts) {
                        logger.finest("Other peer is behind me, skipping");
                        continue;
                    }
                    logger.log(Level.FINER, "Got Relevent Packet:{0} {1} {2}", new Object[]{pclass, id, ts});
                    final long ahead = ts - now;
                    timerOffset += (ahead >> 1);
                    logger.log(Level.INFO, "Other peer is {0} ahead of me, catching up halfway with a new offset of {1}", new Object[]{ahead, timerOffset});
                } catch (IOException ex) {
                    Logger.getLogger(ClockSync.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
    protected static volatile long timerOffset = 0;
    static final Logger logger = Logger.getLogger(ClockSync.class.getName());
    final static short PACKET_CLASS = 2700; // random
    static final Random r = new Random();
    static final String UPNP_ADDRESS = "239.255.255.250";
    static final int UPNP_MULTI_PORT = 1900;
    final ByteBuffer bin, bout;
    final int BUFF_SIZE = Short.SIZE + Integer.SIZE + Long.SIZE; // Packet class + machine ID + Time
    final InetAddress group;
    final MulticastSocket ms;
    final int myId;
    final ReceiveThread rc = new ReceiveThread();
    final Thread recThread = new Thread(rc);
    final Timer broadTask = new Timer();

    public ClockSync() throws IOException {
        this.ms = new MulticastSocket(null);
        this.group = InetAddress.getByName(UPNP_ADDRESS);
        this.ms.setTimeToLive(4);
        this.ms.setSoTimeout(0);
        this.ms.setLoopbackMode(true);
        this.ms.setReuseAddress(true);
        if (!this.ms.getReuseAddress()) {
            logger.warning("MS Socket can't reuse address");
        }
        this.ms.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), UPNP_MULTI_PORT));
        this.ms.joinGroup(this.group);
        this.bin = ByteBuffer.allocate(this.BUFF_SIZE);
        this.bout = ByteBuffer.allocate(this.BUFF_SIZE);
        this.myId = r.nextInt();
    }

    /**
     * Don't forget to run this!
     */
    public void startSync() {
        this.recThread.start();
        this.broadTask.schedule(new BroadcastTask(), 0, 5000);
    }

    /**
     * Don't bother stopping it if you are ok with the overhead, in case new
     * clients join
     */
    public void stopSync() {
        this.rc.running = false;
        this.broadTask.cancel();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!this.ms.isClosed()) {
            if (this.ms.isBound() && !this.ms.isClosed()) {
                this.ms.leaveGroup(this.group);
                this.ms.close();
            }
        }
    }

    public static void main(String args[]) {
        System.out.println("1234567890123");
        for (int i = 0; i < 100; i++) {
            System.out.println(getAdjustedTime());
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClockSync.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

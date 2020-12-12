import java.io.IOException;
import java.net.*;

public class ServerUDP {
    private DatagramSocket udpSocket;

    public ServerUDP(int port) throws IOException {
        this.udpSocket = new DatagramSocket(port);
    }

    private void run() throws Exception {
        System.out.println("-- Running Server at " + InetAddress.getLocalHost() + " on port " + udpSocket.getLocalPort()  + " --");
        String msg;

        while (true) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            // Receive the message from the client
            udpSocket.receive(packet);
            msg = new String(packet.getData()).trim();

            System.out.println("Message from " + packet.getAddress().getHostAddress() + ": " + msg);

            // Send the message back to the client
            udpSocket.send(packet);
        }
    }

    public static void main(String[] args) throws Exception {
        ServerUDP server = new ServerUDP(5555);
        server.run();
    }
}

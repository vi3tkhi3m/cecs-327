import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

class ClientUDP {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa");

    public static void main(String[] args) throws NumberFormatException {
        final int MIN_PORT_NUMBER = 1;
        final int MAX_PORT_NUMBER = 65535;

        boolean connected = false;
        boolean ipValid = false;
        boolean portValid = false;
        boolean running = true;

        Client client = null;
        Scanner in = new Scanner(System.in);
        String ipAddress = "";
        int port = 0;

        // The program will keep asking the user for an valid IP address and port until it can make an connection with the server
        while(!connected) {
            // Keep checking the IP until it's valid
            while(!ipValid) {
                System.out.println(printCurrentTimestamp() + " Please enter the IP address of the server:");
                ipAddress = in.nextLine();

                // You can also type in localhost. It will simply translate this to 127.0.0.1
                if(ipAddress.equals("localhost")) {
                    ipAddress = "127.0.0.1";
                }

                if(ValidateIPv4.isValidInet4Address(ipAddress)) {
                    ipValid = true;
                } else {
                    System.out.println(printCurrentTimestamp() + " Please enter a valid IP address!");
                }
            }

            // Keep checking the port until it's valid
            while(!portValid) {
                System.out.println(printCurrentTimestamp() + " Please enter the port of the server:");
                port = Integer.parseInt(in.nextLine());

                if(port >= MIN_PORT_NUMBER && port <= MAX_PORT_NUMBER) {
                    portValid = true;
                } else {
                    System.out.println(printCurrentTimestamp() + " Please enter a valid port number ranging from " + MIN_PORT_NUMBER + " to " + MAX_PORT_NUMBER);
                }
            }

            // Try to connect with the server
            try {
                client = new Client(ipAddress, port);
                System.out.println(printCurrentTimestamp() + " Attempting to connect to " + ipAddress + ":" + port);
                if(client.connect()) {
                    connected = true;
                    System.out.println(printCurrentTimestamp() + " Successfully connected to the server!");
                } else {
                    ipValid = false;
                    portValid = false;
                }
            } catch (UnknownHostException | SocketException e) {
                System.out.println(printCurrentTimestamp() + " Please enter a valid IP address and/or port!");
                System.out.println(printCurrentTimestamp() + " Error message: " + e.getMessage());
            }
        }

        // This will make the program keep sending messages until the user types :q
        while(running) {
            System.out.println(printCurrentTimestamp() + " Please enter a message or type :q to quit.");
            String msg = in.nextLine();

            if(msg.equals(":q")) {
                running = false;
            } else {
                try {
                    client.send(msg);
                } catch (SocketTimeoutException e) {
                    System.out.println(printCurrentTimestamp() + " Can't connect to the server! Shutting down client.");
                    exit(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(printCurrentTimestamp() + " Closing connection with the server.");
        client.close();
        System.out.println(printCurrentTimestamp() + " Successfully closed the connection with the server.");
    }

    static String printCurrentTimestamp() {
        return "[" + sdf.format(new Date()) + "]";
    }
}

class Client {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public Client(String ipAddress, int port) throws UnknownHostException, SocketException {
        address = InetAddress.getByName(ipAddress);
        socket = new DatagramSocket();
        this.port = port;
    }

    // This function will send a "ping" packet to the server. If the client receives an answer from the server, the client is then "connected"
    public boolean connect() {
        byte[] buf = "Ping".getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, port);

        try {
            socket.setSoTimeout(5000);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return true;
        } catch (SocketTimeoutException e) {
            System.out.println(ClientUDP.printCurrentTimestamp() + " Can't connect to the server! Please try again!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // This function will send messages to the server and receive the response of the server and prints it.
    public void send(String msg) throws IOException {
        byte[] buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(
                packet.getData(), 0, packet.getLength());
        System.out.println(ClientUDP.printCurrentTimestamp() + " Response from the server: " + received);
    }

    // Close the socket
    public void close() {
        socket.close();
    }
}

class ValidateIPv4
{
    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidInet4Address(String ip) {
        if (ip == null) {
            return false;
        }

        Matcher matcher = IPv4_PATTERN.matcher(ip);

        return matcher.matches();
    }
}

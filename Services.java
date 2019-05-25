
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author Raju
 * @Uses: This is the main class and responsible for: 
 * 1. Initialization of input variables. 
 * 2. Running multiple threads
 */
public class Services {

    public static void main(String args[]) {

        MulticastSocket multicastSocket = null;
        try {
            final int MULTICAST_PORT = 9998;
            final int TCP_PORT = 9999;
            final String GROUP_IP = "228.5.6.7";
            //Initialize the multicast group
            InetAddress group = InetAddress.getByName(GROUP_IP);
            String peerId = new String(args[0]);
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            //Join the peer in the multicast group
            multicastSocket.joinGroup(group);
            //Execute Receive message thread
            new ReceiveMessageService(multicastSocket, TCP_PORT, peerId);
            //Execute send message thread
            new SendMessageService(multicastSocket, group, MULTICAST_PORT, TCP_PORT, peerId);
        } catch (IOException | NumberFormatException e) {
        }
    }

}


import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Raju
 * @Uses: This class also runs in a thread to perform: 
 * 1. Receive message from multicast socket and print the message 
 * 2. Check if the file is available in the peers resource 3. Open the TCP socket if the file is available 4. Send file into the socket.
 */
public class ReceiveMessageService extends Thread {

    private final String FILE_EXTENSION = "txt";
    MulticastSocket multicastSocket = null;
    String peerId = null;
    int tcpPort;

    public ReceiveMessageService(MulticastSocket multicastSocket, int tcpPort, String peerId) {

        this.multicastSocket = multicastSocket;
        this.peerId = peerId;
        this.tcpPort = tcpPort;
        this.start();
    }

    public void run() {

        while (true) {

            try {
                //Define the buffer size to receive the message
                byte[] buffer = new byte[100];
                //Prepare datagram packet to receive the message
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                //Receive message from multicast socket. 
                multicastSocket.receive(messageIn);
                String msg = new String(messageIn.getData(), 0, messageIn.getLength());
                String[] temp;
                //Parse the message to get Requesting peer id and file name
                String delimiter = "-";
                temp = msg.split(delimiter);
                String sender = temp[0];
                //Check the requesting peer, skip this step if the message is initiated by same peer
                if (!sender.toLowerCase().trim().equals(peerId.toLowerCase().trim())) {
                    String filename = temp[1];
                    //Print user friendly message
                    System.out.println(sender + " says: Do you have file: " + filename + "?");
                    //Define the resource path of peer
                    String filePath = peerId + "\\resources\\" + filename + "." + FILE_EXTENSION;
                    File file = new File(filePath);
                    if (file.exists()) {
                        //Open the server socket if the file is available in local resource
                        ServerSocket serverSocket = new ServerSocket(tcpPort);
                        while (true) {
                            try (Socket socket = serverSocket.accept()) {
                                //Prepare outstream
                                OutputStream outputStream = socket.getOutputStream();
                                //Defile path variable
                                Path sourcePath = Paths.get(filePath);
                                //Send file from source path to output stream. Following code block works with latest java version (7 and above) only. 
                                Files.copy(sourcePath, outputStream);
                            }
                            //Close the server socket. 
                            serverSocket.close();
                        }

                    }
                }

            } catch (SocketException e) {

            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }

        }

    }

}

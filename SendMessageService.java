
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author Raju
 * @uses: This class is responsible for: 
 * 1. Broadcasting message to each peer 
 * 2. Check the client socket if the input stream available.
 */
public class SendMessageService extends Thread {

    private final String FILE_EXTENSION = "txt";
    MulticastSocket ms = null;
    InetAddress group = null;
    int multicastPort;
    int tcpPort;
    String peerId = "";

    //This method runs in thread to continuously broadcast message via multicast socket
    public SendMessageService(MulticastSocket multicastSocket, InetAddress group, int multicastPort, int tcpPort, String peerId) {

        //Initialize input variables
        this.ms = multicastSocket;
        this.tcpPort = tcpPort;
        this.group = group;
        this.multicastPort = multicastPort;
        this.peerId = peerId;
        this.start();
    }

    @Override
    public void run() {

        String msg = null;

        while (true) {
            try {

                //Reading message from Peer. it should contain the file name required
                System.out.println("Enter the file Name:");
                Scanner scanner = new Scanner(System.in);
                msg = scanner.next();

                //leave the group if the peer has entered "End' word. 
                if (msg.toLowerCase().compareTo(new String("end")) == 0) {
                    ms.close();
                    System.exit(0);
                } else {
                    //Concatinate the peer id and file name for transmission
                    String transmitMessage = peerId + "-" + msg;
                    //Convert the message to byte array
                    byte[] transmitMessageBytes = transmitMessage.getBytes();
                    //Prepare output message
                    DatagramPacket messageOut = new DatagramPacket(transmitMessageBytes, transmitMessageBytes.length, group, multicastPort);
                    //Send message to multicast group
                    ms.send(messageOut);
                    //Start the file download process 
                    DownloadFile(msg);

                }

            } catch (SocketException e) {
                System.out.println("File Not Found");
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }
        }
    }

    //To download the file from client socket
    public void DownloadFile(String msg) throws IOException {
        String fileName = peerId + "\\downloads\\" + msg + "-download." + FILE_EXTENSION;
        Socket socket = new Socket("localhost", tcpPort);
        //Get input stream
        InputStream inputStream = socket.getInputStream();
        if (inputStream != null) {
            //Get downloadable path
            Path downloadPath = Paths.get(fileName);
            //Save the inputstream into download path
            Files.copy(inputStream, downloadPath);
            //Print user friendly message to notify the file download process 
            System.out.println("File of name: " + msg + " was found and saved as " + msg + "-download." + FILE_EXTENSION + " in download directory");
            //Close the socket
            socket.close();
        } else {
            //Notify if the inputstream is null 
            System.out.println("No file received ");
            //Close the socket
            socket.close();
        }

    }

}

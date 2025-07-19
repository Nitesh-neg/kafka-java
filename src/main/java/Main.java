import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running
        // tests.
        System.err.println("Logs from program will appear here!");

        ServerSocket serverSocket = null;
      //  Socket clientSocket = null;
        int port = 9092;
        try {

            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            while(true) {

                // Wait for connection from client.
               Socket clientSocket = serverSocket.accept();
  
                new Thread(() -> {
                    ClientManager clientManager = new ClientManager(clientSocket);
                    clientManager.handleClient();
                }).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
  }
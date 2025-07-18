import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 9092;
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);
     
      clientSocket = serverSocket.accept();// wait for a client to connect
      InputStream inputStream = clientSocket.getInputStream();
      byte[] messageSize = inputStream.readNBytes(4);
      byte[] requestApiKey = inputStream.readNBytes(2);
      byte[] requestApiVersion = inputStream.readNBytes(2);
      byte[] requestCorrelationId = inputStream.readNBytes(4);
      byte[] response = ByteBuffer.allocate(4).put(requestCorrelationId).array();
      clientSocket.getOutputStream().write(messageSize);
      clientSocket.getOutputStream().write(response);
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }
}

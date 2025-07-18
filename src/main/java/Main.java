import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
  public static void main(String[] args){
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
      short  requestApiVersion_integer = ByteBuffer.wrap(requestApiVersion).getShort();
      byte[] requestCorrelationId = inputStream.readNBytes(4);
      byte[] response = ByteBuffer.allocate(4).put(requestCorrelationId).array();
      clientSocket.getOutputStream().write(messageSize);
      clientSocket.getOutputStream().write(response);
      clientSocket.getOutputStream().write(new byte[]{0,35});
      
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

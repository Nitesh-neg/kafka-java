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
        Socket clientSocket = null;
        int port = 9092;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            // Wait for connection from client.
            clientSocket = serverSocket.accept();

            // Read input
            // ByteBuffer.wrap(): creates a ByteBuffer from the byte array
            // getInt(): reads the bytes as a 32-bit signed integer

            while(true){
                  BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
                  byte[] requestMessageSize = in.readNBytes(4);
                  byte[] apiKey = in.readNBytes(2);
                  byte[] apiVersion = in.readNBytes(2);
                  byte[] correlationIdRaw = in.readNBytes(4);
                  int correlationId = ByteBuffer.wrap(correlationIdRaw).getInt();

                  // Debug prints
                  System.err.println("Correlation ID Raw: " + bytesToHex(correlationIdRaw));
                  System.err.println("Correlation ID decimal: " + correlationId);

                  // Write to output
                  // ByteBuffer.allocate(): creates a new ByteBuffer with x bytes of capacity
                  // putInt(): writes integer into ByteBuffer
                  // array(): Extracts the underlying byte array from the ByteBuffer
                  OutputStream out = clientSocket.getOutputStream();
                  byte[] messageSizeBytes = ByteBuffer.allocate(4).putInt(19).array();
                  out.write(messageSizeBytes);
                  byte[] responseCorrelationIdBytes =
                          ByteBuffer.allocate(4).putInt(correlationId).array();
                  out.write(responseCorrelationIdBytes);
                  // Check the API version, it must be less than or equal to 4
                  // If it is greater than 4, enter error code 35
                  // Else, error code is 0
                  out.write(
                          apiVersion[0] != 0 || apiVersion[1] > 4
                                  ? new byte[] {0, 35}
                                  : new byte[] {0, 0});

                  //
                  // This part writes the API version array
                  // num_of_api_keys => 1 byte
                  // (the the num of API + 1. In this case, it is 2 because we only have 1 api)   , use compact array
                  // api_key => 2 bytes
                  // (in this case, it is 0x0012)
                  // min_version => 2 bytes
                  // (0x0000)
                  // max_version => 2 bytes
                  // (0x0004)
                  // buffer => 1 byte
                  // Note: if there are many API keys, then this pattern will repeat
                  // throttle_time_ms => 4 bytes
                  // buffer => 1 byte
                  out.write(new byte[] {2, 00, 0x12, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0});

            }

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

    // helper method to debug
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b & 0xFF));
        }
        return hex.toString();
    }
}
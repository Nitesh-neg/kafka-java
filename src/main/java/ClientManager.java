import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientManager {
    private final Socket clientSocket;

    public ClientManager(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public void handleClient() {
        try {

            // This is the main loop that handles the client requests
           
              while(true){
                  BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
                  byte[] requestMessageSize = in.readNBytes(4);
                  byte[] apiKey = in.readNBytes(2);
                  byte[] apiVersion = in.readNBytes(2);
                  byte[] correlationIdRaw = in.readNBytes(4);
                  int correlationId = ByteBuffer.wrap(correlationIdRaw).getInt();

                  // Write to output
                  // ByteBuffer.allocate(): creates a new ByteBuffer with x bytes of capacity
                  // putInt(): writes integer into ByteBuffer
                  // array(): Extracts the underlying byte array from the ByteBuffer
                  OutputStream out = clientSocket.getOutputStream();
                  byte[] messageSizeBytes = ByteBuffer.allocate(4).putInt(26).array();
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
                out.write(new byte[] {
                            // === ApiVersions array ===
                            3,                  // compact array length = 2 + 1 = 3

                            // Entry 1: ApiKey 18 (ApiVersions)
                            0x00, 0x12,         // ApiKey = 18
                            0x00, 0x00,         // MinVersion = 0
                            0x00, 0x04,         // MaxVersion = 4
                            0x00,               // Tagged fields = 0

                            // Entry 2: ApiKey 75 (DescribeTopicPartitions)
                            0x00, 0x4B,         // ApiKey = 75
                            0x00, 0x00,         // MinVersion = 0
                            0x00, 0x00,         // MaxVersion = 0
                            0x00,               // Tagged fields = 0

                            // === ThrottleTimeMs ===
                            0x00, 0x00, 0x00, 0x00,  // throttle_time_ms = 0

                            // === Final Tagged Fields ===
                            0x00                // Tagged fields = 0
                        });
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}

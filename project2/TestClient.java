import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Random;

public class TestClient {
  private static final String HOST = "localhost";
  private static Random rand = new Random();
  private static DatagramSocket udpSocket;
  private static Socket tcpSocket;
  private static InetAddress ipAddress;

  public static void main(String[] args) {
    if (!testStageA())
      System.out.println("STAGE A FAILED");
    else
      System.out.println("STAGE A PASSED");
    if (!testStageB())
      System.out.println("STAGE B FAILED");
    else
      System.out.println("STAGE B PASSED");
    if (!testStageC())
      System.out.println("STAGE C FAILED");
    else
      System.out.println("STAGE C PASSED");
    if (!testStageD())
      System.out.println("STAGE D FAILED");
    else
      System.out.println("STAGE D PASSED");
  }

  public static boolean testStageA() {
    boolean passed = true;
    String message = "hello world";
    ByteBuffer payload = ByteBuffer.allocate(12);
    payload.put(message.getBytes());
    passed = checkHeaders(payload, message.length() + 1, 0, (short) 1,
                          (short) 219, 12235, true);
    return passed;
  }

  public static boolean testStageB() {
    return false;
  }

  public static boolean testStageC() {
    return false;
  }

  public static boolean testStageD() {
    return false;
  }

  private static void initializeUDP(int port) {
    try {
      ipAddress = InetAddress.getByName(HOST);
      udpSocket = new DatagramSocket();
    } catch (Exception e) { e.printStackTrace(); }
    udpSocket.connect(ipAddress, port);
  }

  private static void initializeTCP(int port) {
    try {
      tcpSocket = new Socket(HOST, port);
    } catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * If a bad header is sent the connection should be closed immediately, so we can
   * just use one method to check all of this on every step.
   */
  private static boolean checkHeaders(ByteBuffer payload, int payloadlen, int psecret,
                                      short step, short studentID, int port, boolean isUdp) {
    boolean passed = true;
    if (isUdp)
      initializeUDP(port);
    else
      initializeTCP(port);
    ByteBuffer badPayloadlenHeader = malformedPayloadlen(psecret, step, studentID);
    passed = checkHeadersHelper(badPayloadlenHeader, payload, port, isUdp,
                                new String("  FAILED MALFORMED PAYLOAD LENGTH"));

    if (isUdp)
      initializeUDP(port);
    else
      initializeTCP(port);
    ByteBuffer badPsecretHeader = malformedPsecret(payloadlen, step, studentID);
    passed = checkHeadersHelper(badPsecretHeader, payload, port, isUdp,
                                new String("  FAILED MALFORMED SECRET"));

    if (isUdp)
      initializeUDP(port);
    else
      initializeTCP(port);
    ByteBuffer badStepHeader = malformedStep(payloadlen, psecret, studentID);
    passed = checkHeadersHelper(badStepHeader, payload, port, isUdp,
                                new String("  FAILED MALFORMED STEP"));

    return passed;
  }

  private static boolean checkHeadersHelper(ByteBuffer badHeader, ByteBuffer payload,
                                            int port, boolean isUdp, String errorMessage) {
    boolean passed = true;
    ByteBuffer badPacket = ByteBuffer.allocate(payload.capacity() + 12);
    badPacket.put(badHeader.array());
    badPacket.put(payload.array());
    System.out.println("Malformed Packet: ");
    bytesToHex(badPacket.array());
    if (isUdp)
      if (!sendUDP(badPacket.array(), port)) {
        System.out.println(errorMessage);
        passed = false;
      }
    else
      if (!sendTCP(badPacket.array(), port)) {
        System.out.println(errorMessage);
        passed = false;
      }

    return passed;
  }

  /*
   * sendUDP returns true if the connection was closed, which is what should happen
   * since we're sending malformed headers
   */
  private static boolean sendUDP(byte[] sendData, int port) {
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
    try {
      udpSocket.send(sendPacket);
    } catch (Exception e) { e.printStackTrace(); }

    // wait for server to check everything and disconnect
    try {
      Thread.sleep(3000);
    } catch (Exception e) { e.printStackTrace(); }

    // connection should close
    System.out.println(udpSocket.isConnected());
    if (!udpSocket.isConnected())
      return true;
    else
      return false;
  }

  private static boolean sendTCP(byte[] sendData, int port) {
    return false;
  }

  // MALFORMED HEADERS
  private static ByteBuffer malformedPayloadlen(int psecret, short step, short studentID) {
    ByteBuffer header = ByteBuffer.allocate(12);
    // hopefully this isn't the actual payload length
    int payloadlen = rand.nextInt();
    header.putInt(payloadlen);
    header.putInt(psecret);
    header.putShort(step);
    header.putShort(studentID);
    return header;
  }

  private static ByteBuffer malformedPsecret(int payloadlen, short step, short studentID) {
    ByteBuffer header = ByteBuffer.allocate(12);
    int psecret = rand.nextInt();
    header.putInt(payloadlen);
    header.putInt(psecret);
    header.putShort(step);
    header.putShort(studentID);
    return header;
  }

  private static ByteBuffer malformedStep(int payloadlen, int psecret, short studentID) {
    ByteBuffer header = ByteBuffer.allocate(12);
    // no rand.nextShort() ...
    short step = (short) rand.nextInt(Short.MAX_VALUE + 1);
    header.putInt(payloadlen);
    header.putInt(psecret);
    header.putShort(step);
    header.putShort(studentID);
    return header;
  }

  private static void bytesToHex(byte[] bytes) {
    char[] hexArray = "0123456789ABCDEF".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for ( int j = 0; j < bytes.length; j++ ) {
        v = bytes[j] & 0xFF;
        hexChars[j * 2] = hexArray[v >>> 4];
        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }

    String str = new String(hexChars);
    for(int i = 0; i < str.length(); i+=2) {
        System.out.print(str.charAt(i));
        if(i + 1 < str.length()) {
          System.out.print(str.charAt(i + 1));
        }
        System.out.print(" ");
    }
    System.out.println();
  }
}

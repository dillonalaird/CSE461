import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Random;

public class TestClient {
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
    return false;
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

  /*
   * If a bad header is sent the connection should be closed immediately, so we can
   * just use one method to check all of this on every step.
   */
  private static boolean checkHeaders(ByteBuffer payload, int payloadlen, int psecret,
                                      short step, short studentID, int port, boolean isUdp) {
    boolean passed = true;
    ByteBuffer badPayload = malformedPayloadlen(psecret, step, studentID);
    badPayload.put(payload);
    if (isUdp)
      if (!sendUDP(badPayload.array(), port)) {
        System.out.println("FAILED MALFORMED PAYLOAD LENGTH");
        passed = false;
      }
    else
      if (!sendTCP(badPayload.array(), port)) {
        System.out.println("FAILED MALFORED PAYLOAD LENGTH");
        passed = false;
      }

    ByteBuffer badPsecret = malformedPsecret(payloadlen, step, studentID);
    badPsecret.put(payload);
    if (isUdp)
      if (!sendUDP(badPsecret.array(), port)) {
        System.out.println("FAILED MALFORMED SECRET");
        passed = false;
      }
    else
      if (!sendTCP(badPsecret.array(), port)) {
        System.out.println("FAILED MALFORMED SECRET");
        passed = false;
      }

    ByteBuffer badStep = malformedStep(payloadlen, psecret, studentID);
    badStep.put(payload);
    if (isUdp)
      if (!sendUDP(badStep.array(), port)) {
        System.out.println("FAILED MALFORMED STEP");
        passed = false;
      }
    else
      if (!sendTCP(badPsecret.array(), port)) {
        System.out.println("FAILED MALFORMED STEP");
        passed = false;
      }

    return passed;
  }

  private static boolean sendUDP(byte[] sendData, int port) {
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
    try {
      udpSocket.send(sendPacket);
    } catch (Exception e) { e.printStackTrace(); }

    // connection should close
    if (udpSocket.isConnected())
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
}

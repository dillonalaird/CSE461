import java.io.*;
import java.net.*;
import java.nio.*;
import java.lang.*;
import java.util.*;

public class Server {
  private static Random randy = new Random();
  private static final int TIMEOUT = 3000;
  private static final int HEADER_LENGTH = 12;
  private static final int START_PORT = 12235;

  public static void main(String[] args) throws Exception {
    Server s = new Server();
    s.runServer();
  }

  public void runServer() {
    try {
      DatagramSocket serverSock = new DatagramSocket(START_PORT);

      for(;;) {
        byte[] buf = new byte[100];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        Thread thread = new Thread(new ServerConnection(serverSock, packet));
        thread.start();
      }
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public class ServerConnection implements Runnable {
    private InetAddress ipAddress;
    private DatagramSocket dSocket;
    private DatagramPacket dPacket;
    private short sid;
    private int port, len, num, secretA, secretB, secretC, secretD, pSecret = 0;

    ServerConnection(DatagramSocket s, DatagramPacket p) {
      dSocket = s;
      dPacket = p;
    }

    @Override
    public void run () {
      if (!stageA())
        return;
      pSecret = secretA;
      if (!stageB())
        return;
    }

    private boolean stageA() {
      ipAddress = dPacket.getAddress();
      Packet461 p = new Packet461(ByteBuffer.wrap(dPacket.getData()));
      System.out.println(new String(p.payload));
      if ("hello world".equals(new String(p.payload))
            && pSecret == p.secret)
      {
        sid = p.sid;
        port = randy.nextInt();
        len = randy.nextInt();
        num = randy.nextInt();
        int secretA = randy.nextInt();

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(num);
        buf.putInt(len);
        buf.putInt(port);
        buf.putInt(secretA);

        sendUDP(generatePacket((short) 1, buf.array()), START_PORT);
        return true;
      }
      return false;
    }

    private boolean stageB() {
      return false;
    }

    private byte[] generatePacket(short step, byte[] payload) {
      int len = fourByteAlign(payload.length);
      ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH + len);
      buf.putInt(len);
      buf.putInt(pSecret);
      buf.putShort(step);
      buf.putShort(sid);
      buf.put(payload);
      return buf.array();
    }

    public void sendUDP(byte[] sendData, int port) {
      try {
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
        dSocket.send(sendPacket);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private class Packet461 {
      public int length;
      public int secret;
      public short step;
      public short sid;
      public byte[] payload;

      Packet461(ByteBuffer b) {
        length = b.getInt();
        secret = b.getInt();
        step = b.getShort();
        sid = b.getShort();
        payload = new byte[length];
        b.get(payload, 0, length);
      }
    }
  }

  public static int fourByteAlign(int num) {
    return ((num + 3) / 4) * 4;
  }

}
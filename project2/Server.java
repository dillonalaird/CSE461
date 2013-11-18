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
        serverSock.receive(packet);
        Thread thread = new Thread(new ServerConnection(serverSock, packet));
        thread.start();
      }
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public class ServerConnection implements Runnable {
    private InetAddress ipAddress;
    private DatagramSocket dSocket;
    private DatagramPacket dPacket;
    private ServerSocket serverSock;
    private Socket tcpSock;
    private short sid;
    private byte c;
    private int tcpPort, port, len, num, secretA, secretB, secretC, secretD, pSecret = 0;

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
      pSecret = secretB;
      if (!stageC())
        return;
      pSecret = secretC;
      if (!stageD())
        return;
    }

    private boolean stageA() {
      ipAddress = dPacket.getAddress();
      Packet461 p = new Packet461(ByteBuffer.wrap(dPacket.getData()));
      if ("hello world".equals(new String(p.payload).substring(0,11))
            && pSecret == p.secret)
      {
        sid = p.sid;
        port = randy.nextInt(49151);
        len = randy.nextInt(80);
        num = randy.nextInt(10) + 1;
        secretA = randy.nextInt();

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(num);
        buf.putInt(len);
        buf.putInt(port);
        buf.putInt(secretA);

        sendUDP(generatePacket((short) 1, buf.array()), dPacket.getPort());
        return true;
      }
      return false;
    }

    private boolean stageB() {
      try {
        dSocket = new DatagramSocket(port);
        //don't ack the first packet.
        dSocket.receive(dPacket);
        dSocket.receive(dPacket);
      } catch (Exception e) {e.printStackTrace();}

      
      ByteBuffer ackbuf = ByteBuffer.allocate(4);
      int packetId = 0;
      while (packetId < num) {
        try { dSocket.receive(dPacket); } catch (Exception e) {e.printStackTrace();}
        Packet461 pack = new Packet461(ByteBuffer.wrap(dPacket.getData()));
        if (verifyPacket(pack, packetId, len + 4, (byte)0)) {
          if (randy.nextInt(10) > 1) {
            ackbuf.putInt(0, packetId++);
            sendUDP(generatePacket((short) 1, ackbuf.array()), dPacket.getPort());
          }
        }
        else {
          return false;
        }
      }

      ByteBuffer buf = ByteBuffer.allocate(8);
      tcpPort = randy.nextInt(49151);
      secretB = randy.nextInt();

      buf.putInt(tcpPort);
      buf.putInt(secretB);
      sendUDP(generatePacket((short) 1, buf.array()), dPacket.getPort());
      return true;
    }

    private boolean verifyPacket(Packet461 pack, int packetId, int expectedLength, byte c) {
      if (pack.length != expectedLength)
        return false;
      if (pack.secret != pSecret)
        return false;
      // if (pack.step !=  ??)
      //   return false
      ByteBuffer payload = ByteBuffer.wrap(pack.payload);
      if (packetId > 0) {
        int packId = payload.getInt();
        if (packId != packetId)
          return false;
      }
      for (int i = 0; i < len; i++)
        if (payload.get() != c)
          return false;
      return true;
    }

    private boolean stageC() {
      try {
        serverSock = new ServerSocket(tcpPort);
        serverSock.setSoTimeout(TIMEOUT);
        tcpSock = serverSock.accept();
      } catch (Exception e) {e.printStackTrace();}

      ByteBuffer buf = ByteBuffer.allocate(16);
      len = randy.nextInt(80);
      num = randy.nextInt(10) + 1;
      secretC = randy.nextInt();
      c = (byte) randy.nextInt(256);

      buf.putInt(num);
      buf.putInt(len);
      buf.putInt(secretC);
      buf.put(c);
      

      try {
        sendBytes(generatePacket((short) 1, buf.array()));
      } catch (Exception e) {e.printStackTrace();}
      return true;
    }

    private boolean stageD() {
      Packet461 pack = null;
      try {
        for (int i = 0; i < num; i++) {
          byte[] res = readBytes(fourByteAlign(HEADER_LENGTH + len));
          pack = new Packet461(ByteBuffer.wrap(res));
          pack.print();
          if (!verifyPacket(pack, -1, len, c))
            i = i;
            //close connection
        }
      } catch (Exception e) {e.printStackTrace();}

      secretD = randy.nextInt();
      ByteBuffer buf = ByteBuffer.allocate(4);

      buf.putInt(secretD);
      sendBytes(generatePacket((short) 1, buf.array()));
      return true;
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
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
      bytesToHex(sendData);
      System.out.println("port = " +  port);
      System.out.println("sendPacket addr. = " + sendPacket.getAddress());
      try {
        dSocket.send(sendPacket);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void sendBytes(byte[] bytes) {
      try {
      System.out.println("sending : ");
      bytesToHex(bytes);
      OutputStream out = tcpSock.getOutputStream();
      DataOutputStream dos = new DataOutputStream(out);
      dos.write(bytes, 0, bytes.length);
      dos.flush();
      } catch (Exception e) {e.printStackTrace();}
    }

    public byte[] readBytes(int len) throws IOException {
      System.out.println("in readBytes");
      // Again, probably better to store these objects references in the support class
      InputStream in = tcpSock.getInputStream();
      DataInputStream dis = new DataInputStream(in);
      byte[] data = new byte[len];
      System.out.println("is connected = " + tcpSock.isConnected());
      try {
          dis.readFully(data);
      } catch(Exception e) {
          System.out.println(e.toString());
          e.printStackTrace();
      }
      return data;
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

      public void print() {
        System.out.println(
          "length: " + length 
        + "\nsecret: " + secret
        + "\n step: " + step
        + "\n sid: " + sid);
        bytesToHex(payload);
      }
    }
  }

  public static int fourByteAlign(int num) {
    return ((num + 3) / 4) * 4;
  }

  public static void bytesToHex(byte[] bytes) {
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

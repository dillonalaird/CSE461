import java.io.*;
import java.net.*;
import java.nio.*;

public class JavaSockets {
    public static final String HOST = "bicycle.cs.washington.edu";
    public static DatagramSocket clientSocket;
    public static InetAddress IPAddress;

    public static Socket tcpSock;

    public static void main(String[] args) throws Exception {
      byte[] stageAres = stageA();
      //bytesToHex(stageAres);
      byte[] bRes = stageB(stageAres);
      clientSocket.close();
      byte[] cRes = stageC(bRes);
      stageD(cRes);
    }

    public static void stageD(byte[] input) throws Exception {
      ByteBuffer results = ByteBuffer.allocate(100);
      results.put(input);
      int num2 = results.getInt(12);
      int len2 = results.getInt(16);
      int secretc = results.getInt(20);
      byte character = results.get(24);
      System.out.println("Stage c num2: " + num2);
      System.out.println("Stage c Secret: " + secretc);
      System.out.println("Stage c Length: " + len2);
      ByteBuffer sendData = ByteBuffer.allocate(12 + fourByteAlign(len2));
      sendData.putInt(len2);         // payload_len
      sendData.putInt(secretc);      // psecret
      sendData.putShort((short) 1);  // step
      sendData.putShort((short) 219);// student number
      for(int i = 0; i < len2; i++) {
          sendData.put(i + 12, character);
      }
      for(int i = 0; i < num2; i++) {
          sendBytes(sendData.array());
      }
      //System.out.println("about to read bytes");
      byte[] cres = readBytes(16);
      System.out.println("Stage D results:");
      bytesToHex(cres);
      results = ByteBuffer.allocate(100);
      results.put(cres);
      int dSecret = results.getInt(12);
      System.out.println("Stage d secret: " + dSecret);
    }

    public static byte[] stageC(byte[] input) throws Exception {
	ByteBuffer results = ByteBuffer.allocate(100);
	results.put(input);
	int port = results.getInt(12);
	int secret = results.getInt(16);
	System.out.println("Stage b Port result: " + port);
	System.out.println("Stage b Secret: " + secret);
	ByteBuffer sendData = ByteBuffer.allocate(12);
	sendData.putInt(0); // payload_len
	sendData.putInt(secret);            // psecret
	sendData.putShort((short) 1);  // step
	sendData.putShort((short) 219);// student number
	initializeTcpSock(port);
	byte[] cRes = readBytes(28);
	bytesToHex(cRes);
	return cRes;
    }

    public static byte[] receiveUDP(int port) throws Exception {
	byte[] receiveData = new byte[100];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	while(true) {
	    try{
		clientSocket.receive(receivePacket);
		return receiveData;
	    } catch(Exception e) {
		System.out.println(e.toString());
	    }
	}
    }

    public static void initializeTcpSock(int port) throws Exception {
	tcpSock = new Socket(HOST, port);
    }

    public static void initializeSocket(int port) throws Exception {
	IPAddress = InetAddress.getByName(HOST);
	clientSocket = new DatagramSocket();
	clientSocket.connect(IPAddress, port);
	clientSocket.setSoTimeout(1000);
    }

    public static byte[] sendUDP(ByteBuffer sendData, int port) throws Exception {
	DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.array().length, IPAddress, port);
	while(true) {
	    try {
		clientSocket.send(sendPacket);
		byte[] receiveData = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		return receiveData;
	    } catch(Exception e) {
		System.out.println(e.toString());
	    }
	}
    }

    public static byte[] stageB(byte[] input) throws Exception {
	ByteBuffer results = ByteBuffer.allocate(100);
	results.put(input);
	int num = results.getInt(12);
	int len = results.getInt(16);
	int port = results.getInt(20);
	int psecret = results.getInt(24);
	initializeSocket(port);
	System.out.println("Num: " + num);
	System.out.println("Len: " + len);
	System.out.println("Port: " + port);
	System.out.println("Secret: " + psecret);
	for(int i = 0; i < num; i++) {
	    int alloc = fourByteAlign(len + 4);
	    ByteBuffer sendData = ByteBuffer.allocate(alloc + 12);
	    sendData.putInt(len + 4); // payload_len
	    sendData.putInt(psecret);            // psecret
	    sendData.putShort((short) 1);  // step
	    sendData.putShort((short) 219);// student number
	    sendData.putInt(i);
	    // bytesToHex(sendData.array());
	    System.out.println("Packet " + i + " response: ");
	    byte[] res = sendUDP(sendData, port);
	    bytesToHex(res);
	}
	System.out.println("Stage B results: ");
	byte[] stageRes = receiveUDP(port);
	bytesToHex(stageRes);
	return stageRes;
    }

    public static int fourByteAlign(int num) {
	return ((num + 3) / 4) * 4;
    }

    public static byte[] stageA() throws Exception {
	String message = "hello world";
	ByteBuffer sendData = ByteBuffer.allocate(24);
	sendData.putInt(message.length() + 1); // payload_len
	sendData.putInt(0);            // psecret
	sendData.putShort((short) 1);  // step
	sendData.putShort((short) 219);// student number
	sendData.put(message.getBytes());
	initializeSocket(12235);
	return sendUDP(sendData, 12235);
    }

    public static void sendBytes(byte[] bytes) throws IOException {
      OutputStream out = tcpSock.getOutputStream();
      DataOutputStream dos = new DataOutputStream(out);
      //bytesToHex(bytes);
      //System.out.println("len = " + bytes.length);
      dos.write(bytes, 0, bytes.length);
      dos.flush();
    }

    public static byte[] readBytes(int len) throws IOException {
      //System.out.println("in readBytes");
      // Again, probably better to store these objects references in the support class
      InputStream in = tcpSock.getInputStream();
      DataInputStream dis = new DataInputStream(in);
      byte[] data = new byte[len];
      //System.out.println("is connected = " + tcpSock.isConnected());
      try {
          dis.readFully(data);
      } catch(Exception e) {
          System.out.println(e.toString());
          e.printStackTrace();
      }
      return data;
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

import java.io.*;
import java.net.*;
import java.nio.*;

public class JavaSockets {
    public static final String HOST = "bicycle.cs.washington.edu";

    public static void main(String[] args) throws Exception {
	byte[] stageAres = stageA();
	//	bytesToHex(stageAres);
	stageB(stageAres);
    }

    public static byte[] receiveUDP(int port) throws Exception {
	DatagramSocket clientSocket = new DatagramSocket();
	InetAddress IPAddress = InetAddress.getByName(HOST);
	byte[] receiveData = new byte[100];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, IPAddress, port);
	while(true) {
	    try{
		clientSocket.receive(receivePacket);
		return receiveData;
	    } catch(Exception e) {
		System.out.println(e.toString());
	    }
	}
    }

    public static initializeSocket(int port) {
	InetAddress IPAddress = InetAddress.getByName(HOST);
	clientSocket = new DatagramSocket();
	clientSocket.connect(IPAddress, port);
	clientSocket.setSoTimeout(1000);
    }

    public static byte[] sendUDP(ByteBuffer sendData, int port) throws Exception {
	InetAddress IPAddress = InetAddress.getByName(HOST);
	DatagramSocket clientSocket = new DatagramSocket();
	clientSocket.setSoTimeout(1000);
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

    public static void stageB(byte[] input) throws Exception {
	ByteBuffer results = ByteBuffer.allocate(100);
	results.put(input);
	int num = results.getInt(12);
	int len = results.getInt(16);
	int port = results.getInt(20);
	int psecret = results.getInt(24);
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
	receiveUDP(port);
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
	return sendUDP(sendData, 12235);
    }


    
    public static void bytesToHex(byte[] bytes) throws Exception {
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

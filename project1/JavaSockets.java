import java.io.*;
import java.net.*;
import java.nio.*;

public class JavaSockets {
    public static final String HOST = "bicycle.cs.washington.edu";


    public static void main(String[] args) throws Exception {
	stageA("hello world");
    }

    public static void sendUDP(ByteBuffer sendData) throws Exception {
	DatagramSocket clientSocket = new DatagramSocket();
	InetAddress IPAddress = InetAddress.getByName(HOST);
	DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.array().length, IPAddress, 12235);
	clientSocket.send(sendPacket);
	byte[] receiveData = new byte[100];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	clientSocket.receive(receivePacket);
	bytesToHex(receiveData);
    }

    public static void stageA(String message) throws Exception {
	ByteBuffer sendData = ByteBuffer.allocate(24);
	sendData.putInt(message.length() + 1); // payload_len
	sendData.putInt(0);            // psecret
	sendData.putShort((short) 1);  // step
	sendData.putShort((short) 219);// student number
	sendData.put(message.getBytes());
	sendUDP(sendData);
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

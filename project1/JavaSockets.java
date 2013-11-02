import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;

public class JavaSockets {

  public static void main(String[] args) {
  }

  public static void stageA() throws Exception {
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress IPAddress = InetAddress.getByName("");
    String msg = "hello world";
    CharsetEncoder ce = null;
    
    ByteBuffer sendData = ByteBuffer.allocate(24);
    sendData.putInt(12);           // payload_len
    sendData.putInt(0);            // psecret
    sendData.putShort((short) 1);  // step
    sendData.putShort((short) 219);// student number
    sendData.put(ce.encode(CharBuffer.wrap("hello world")));

    //DatagramPacket sendPacket = new DatagramPacket(sendData, 
    //		sendData.length, IPAddress, 12235);
  }
}

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.out.println("Usage: java Client <addr> <port> <op> <id> [<val>]");
            return;
        }
        
        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        String op = args[2];
        int id = Integer.parseInt(args[3]);
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // 5 seconds timeout
            
            InetAddress serverAddr = InetAddress.getByName(addr);
            
            if (op.equals("get")) {
                String message = op + " " + id;
                byte[] sendData = message.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, serverAddr, port);
                socket.send(sendPacket);
                
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                try {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String[] parts = response.split(" ");
                    
                    if (parts.length == 2) {
                        System.out.println("Sensor " + parts[0] + " average: " + parts[1]);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Request timed out");
                }
            } else if (op.equals("put") && args.length == 5) {
                float val = Float.parseFloat(args[4]);
                String message = op + " " + id + " " + val;
                byte[] sendData = message.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, serverAddr, port);
                
                while (true) {
                    socket.send(sendPacket);
                    System.out.println("Sent reading " + val + " from sensor " + id);
                    Thread.sleep(1000); // 1 second interval
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
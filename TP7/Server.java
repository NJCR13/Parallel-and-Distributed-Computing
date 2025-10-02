import java.io.*;
import java.net.*;
import java.util.*;

class Sensor {
    private int id;
    private List<Float> readings = new ArrayList<>();
    
    public Sensor(int id) {
        this.id = id;
    }
    
    public void addReading(float value) {
        readings.add(value);
    }
    
    public float getAverage() {
        if (readings.isEmpty()) return 0;
        float sum = 0;
        for (float val : readings) {
            sum += val;
        }
        return sum / readings.size();
    }
}

public class Server {
    private static Sensor[] sensors;
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Server <port> <no_sensors>");
            return;
        }
        
        int port = Integer.parseInt(args[0]);
        int noSensors = Integer.parseInt(args[1]);
        
        sensors = new Sensor[noSensors];
        for (int i = 0; i < noSensors; i++) {
            sensors[i] = new Sensor(i);
        }
        
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server started on port " + port);
            
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                String[] parts = message.split(" ");
                
                if (parts.length < 2) continue;
                
                String op = parts[0];
                int id = Integer.parseInt(parts[1]);
                
                if (id < 0 || id >= noSensors) continue;
                
                if (op.equals("put") && parts.length == 3) {
                    float val = Float.parseFloat(parts[2]);
                    sensors[id].addReading(val);
                    System.out.println("Added reading " + val + " to sensor " + id);
                } else if (op.equals("get")) {
                    float avg = sensors[id].getAverage();
                    String response = id + " " + avg;
                    
                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length,
                        packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
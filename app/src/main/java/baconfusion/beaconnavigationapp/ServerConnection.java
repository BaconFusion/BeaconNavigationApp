package baconfusion.beaconnavigationapp;

import android.os.StrictMode;
import android.util.FloatMath;

import org.altbeacon.beacon.Beacon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Stefan on 15-Sep-16.
 */
public class ServerConnection {

    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;
    private static boolean connected = false;

    public static void connect(String ip, int port) throws IOException {

        // :(
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), 1500);

        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

        connected = true;
    }


    public static void sendBeacons(ArrayList<Beacon> beaconList) {
        try {
            dos.writeByte(0);
            dos.writeShort((short) beaconList.size());
            for (Beacon beacon : beaconList) {
                dos.write(beacon.getId1().toByteArray(), 0, 16);
                dos.write(beacon.getId2().toByteArray(), 0, 2);
                dos.write(beacon.getId3().toByteArray(), 0, 2);

                dos.writeFloat(DistanceCalculator.calculateDistance(beacon.getRssi()));
            }
            dos.writeLong(System.currentTimeMillis());

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void sendCalibrationData(ArrayList<Float> keys, ArrayList<Float> values){
        try {
            dos.writeByte(1);
            dos.writeByte((byte) keys.size());

            for(int i=0; i<keys.size();i++) {
                dos.writeFloat(keys.get(i));
                dos.writeFloat(values.get(i));
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        receiveCalibrationResult();
    }

    public static void sendSensorData(byte dataType, float[] data){
        try {
            dos.writeByte(2);
            dos.writeByte(dataType);
            dos.writeByte((byte) data.length);

            for(int i=0; i<data.length; i++){
                dos.writeFloat(data[i]);
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }



    public static void receiveCalibrationResult(){
        try {
            assert(dis.readByte() == 1);

            float a = dis.readFloat();
            float b = dis.readFloat();
            float c = dis.readFloat();

            DistanceCalculator.update(a, b, c);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static float[] receivePosition(){
        try {
            assert(dis.readByte() == 0);
            float[] position = {dis.readFloat(), dis.readFloat()};
            return position;

        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isConnected(){
        return connected;
    }

}

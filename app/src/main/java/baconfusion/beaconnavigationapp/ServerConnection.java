package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Stefan on 15-Sep-16.
 */
public class ServerConnection implements Runnable {

	public static final byte MODUS_BEACON_BROADCAST = 0;
	public static final byte MODUS_BEACON_CALIBRATE = 1;
	public static final byte MODUS_SMARTPHONE_SENSORS = 2;
	
	
    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;

    private static Activity activityReference;

    private static Thread thread;
    private static PositionNotifier positionNotifier = new PositionNotifier() {
        @Override
        public void onDataArrived(float x, float y, float[] b_x, float[] b_y, int[] b_i) {
            // empty
        }
    };


    public static synchronized void connect(String ip, int port) throws IOException {
        // :(
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), 1500);

        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());


        thread = new Thread(new ServerConnection());
        thread.start();

    }

    public static synchronized void disconnect(){
        try {
            socket.close();
            thread.join();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static synchronized void sendBeacons(ArrayList<Beacon> beaconList) {
        try {
            dos.writeByte(MODUS_BEACON_BROADCAST);
            dos.writeShort((short) beaconList.size());
            dos.writeLong(System.currentTimeMillis());
            for (Beacon beacon : beaconList) {
                dos.write(beacon.getId1().toByteArray(), 0, 16);
                dos.write(beacon.getId2().toByteArray(), 0, 2);
                dos.write(beacon.getId3().toByteArray(), 0, 2);
                //DistanceCalculator.calculateDistance(beacon.getRssi());
                dos.writeFloat((float) beacon.getDistance());
                //dos.writeFloat((float)beacon.getDistance());
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static synchronized void sendCalibrationData(ArrayList<Float> distance, ArrayList<Float> averageRSSI){
        try {
            dos.writeByte(MODUS_BEACON_CALIBRATE);
            dos.writeByte((byte) distance.size());

            for(int i=0; i<distance.size();i++) {
                dos.writeFloat(distance.get(i));
                dos.writeFloat(averageRSSI.get(i));
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static synchronized void sendSensorData(byte dataType, float[] data){
        try {
            dos.writeByte(MODUS_SMARTPHONE_SENSORS);
            dos.writeByte(dataType);
            dos.writeByte((byte) data.length);

            for(int i=0; i<data.length; i++){
                dos.writeFloat(data[i]);
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //Position from Mobile Device
    public static void receivePosition() {
        try {

            float x = dis.readFloat();
            float y = dis.readFloat();

            byte len = dis.readByte();
            float[] b_x = new float[len];
            float[] b_y = new float[len];
            int[] b_i = new int[len];
            for (int i = 0; i < len; i++) {
                b_x[i] = dis.readFloat();
                b_y[i] = dis.readFloat();
                b_i[i] = dis.readInt();
            }
            positionNotifier.onDataArrived(x, y, b_x, b_y, b_i);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveCalibrationResult() {
        try {
            float a = dis.readFloat();
            float b = dis.readFloat();
            float c = dis.readFloat();
            float d = dis.readFloat();

            DistanceCalculator.update(a, b, c, d);
            calibrationFinishedPopup(a, b, c);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void calibrationFinishedPopup(final float a, final float b, final float c) {
        activityReference.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activityReference, "a=" + a + "\nb=" + b + "\nc=" + c, Toast.LENGTH_LONG).show();
            }
        });
        activityReference.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activityReference);
                    builder.setTitle("Finished calibration successfully.");
                    builder.setMessage("a=" + a + "\nb=" + b + "\nc=" + c);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
            }
        );
    }

    public static void setPositionNotifier(PositionNotifier positionNotifier){
        ServerConnection.positionNotifier = positionNotifier;

    }

    public static void setActivityReference(Activity activity){
        activityReference = activity;
    }

    public static boolean isConnected(){
        return (socket != null && socket.isConnected());
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                byte x=-1;
                switch (x=dis.readByte()) {
                    case MODUS_BEACON_BROADCAST:
                        receivePosition();
                        break;
                    case MODUS_BEACON_CALIBRATE:
                        receiveCalibrationResult();
                        break;
                    default:
                        throw new RuntimeException("Mode not defined.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

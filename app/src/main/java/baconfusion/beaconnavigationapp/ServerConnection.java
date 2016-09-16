package baconfusion.beaconnavigationapp;

import android.os.StrictMode;

import org.altbeacon.beacon.Beacon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Stefan on 15-Sep-16.
 */
public class ServerConnection {

    private Socket socket;
    private DataOutputStream dos;

    public ServerConnection(String ip, int port) throws IOException {

        // :(
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        socket = new Socket(ip, port);
        dos = new DataOutputStream(socket.getOutputStream());
    }


    public void sendBeacons(ArrayList<Beacon> beaconList) {
        try {
            dos.writeShort((short) beaconList.size());
            for (Beacon beacon : beaconList) {
                dos.write(beacon.getId1().toByteArray(), 0, 16);
                dos.write(beacon.getId2().toByteArray(), 0, 2);
                dos.write(beacon.getId3().toByteArray(), 0, 2);
                dos.write((byte) beacon.getRssi());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

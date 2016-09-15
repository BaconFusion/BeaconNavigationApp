package baconfusion.beaconnavigationapp.beaconreference;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.EditText;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

import baconfusion.beaconnavigationapp.R;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private static Socket clientSocket;
    private static DataOutputStream dos;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    public static void startTCPConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
        } catch (Exception e) {
            //logToDisplay("Error while registering Socket.");
        }
        try {
            dos = new DataOutputStream(clientSocket.getOutputStream());
        } catch (Exception e) {
            //logToDisplay(e.getStackTrace().toString());
            return;
        }
    }

    public static void stopTCPConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);

        // adding iBeacon Format to Library:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        stopTCPConnection();    //
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                logToDisplay("found " + beacons.size() + "beacons");
                try {
                    dos.writeShort((short) beacons.size());
                    for (Beacon beacon : beacons) {
                        dos.write(beacon.getId1().toByteArray(), 0, 16);
                        dos.write(beacon.getId2().toByteArray(), 0, 2);
                        dos.write(beacon.getId3().toByteArray(), 0, 2);
                        dos.write((byte) beacon.getRssi());
                    }
                } catch (IOException e) {
                    logToDisplay("Error writing data");
                    return;
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) RangingActivity.this.findViewById(R.id.rangingText);
                editText.append(line + "\n");
            }
        });
    }
}

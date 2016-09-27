package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class ShowPositionActivity extends Activity implements BeaconConsumer {

    private PaintView pv;

    private BeaconManager beaconManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_position);
        pv = (PaintView) findViewById(R.id.view);


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(MainActivity.LAYOUT_IBEACON));
        beaconManager.setForegroundBetweenScanPeriod(MainActivity.BEACON_SCAN_INTERVALL);
        beaconManager.setForegroundScanPeriod(MainActivity.BEACON_SCAN_INTERVALL);
        beaconManager.bind(this);

       /* float x = 20.0f;
        float y = -20.0f;
        float[] b_x = {0, 1.1f};
        float[] b_y = {4.0f,-5.0f};
        int[] b_i = {638, 223};

       pv.onDataArrived(x, y, b_x, b_y, b_i);*/
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                if(beacons.size() == 0)
                    return;
                if(ServerConnection.isConnected()){
                    ArrayList<Beacon> list = new ArrayList<>();
                    list.addAll(beacons);
                    ServerConnection.sendBeacons(list);
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("...", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace();  }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);

    }
}

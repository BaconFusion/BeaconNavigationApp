package org.altbeacon.beaconreference;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.os.StrictMode;
import android.view.View;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by fabiola on 15.09.16.
 */
public class CallibratingActivity extends Activity {
    protected static final String TAG = "CallibratingActivity";
    private MonitoringActivity monitoringActivity = null;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    String ip = "";
    String port = "";
    private int sum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_callibrating);
        Bundle values = getIntent().getExtras();

        if (values != null){
            ip = values.getString("EXTRA_IP");
            port = values.getString("EXTRA_PORT");
        }

        TextView text1 = (TextView)findViewById(R.id.ip_text);
        TextView text2 = (TextView)findViewById(R.id.port_text);
        text1.setText("Used IP-Adress: "+ip);
        text2.setText("Connect to Port: "+port);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BeaconReferenceApplication) this.getApplicationContext()).setCallibratingActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setCallibratingActivity(null);
    }


    public void callibrateOne(View view){
        monitoringActivity = new MonitoringActivity();

        Intent myIntent = new Intent(this, RangingActivity.class);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        int rssi_onemeter = startLog();
        logToDisplay(Integer.toString(rssi_onemeter));
        this.startActivity(myIntent);
    }


    public int startLog() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                logToDisplay("found " + beacons.size() + "beacons");

                for (Beacon beacon : beacons) {
                    if (beacon.getId3().toInt() == 981){
                        sum += beacon.getRssi();
                        logToDisplay(Integer.toString(beacon.getRssi()));
                    }

                }

            }
        });

        return sum;
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) CallibratingActivity.this.findViewById(R.id.calli_events);
                editText.append(line + "\n");
            }
        });
    }

}

package baconfusion.beaconnavigationapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import baconfusion.beaconnavigationapp.filters.StupidFilter;


/**
 * Created by Stefan on 15-Sep-16.
 */
public class MainActivity extends Activity implements BeaconConsumer {
	public static final String LAYOUT_IBEACON = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final int BEACON_SCAN_INTERVALL = 100;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MainActivity";
    public BeaconListAdapter beaconListAdapter;
    ArrayList<Beacon> beaconList = new ArrayList<>();
    public RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
            if(beacons.size() == 0)
                return;
            beaconList.clear();
            beaconList.addAll(beacons);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    beaconListAdapter.notifyDataSetChanged();
                }
            });
            if(ServerConnection.isConnected()){
                ServerConnection.sendBeacons(beaconList);
            }
        }
    };
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        verifyBluetooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
        //BeaconManager.setRssiFilterImplClass(org.altbeacon.beacon.service.ArmaRssiFilter.class);
        //BeaconManager.setRssiFilterImplClass(SimpleKalman.class);
        BeaconManager.setRssiFilterImplClass(StupidFilter.class);

        // adding iBeacon layout to Library:
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(LAYOUT_IBEACON));
        beaconManager.setForegroundBetweenScanPeriod(BEACON_SCAN_INTERVALL);
        beaconManager.setForegroundScanPeriod(BEACON_SCAN_INTERVALL);
        beaconManager.bind(this);


        //init list + adapter
        ListView beaconListView = (ListView) findViewById(R.id.listView);
        beaconListAdapter = new BeaconListAdapter(this, R.layout.list_item, beaconList);
        beaconListView.setAdapter(beaconListAdapter);

        loadPreferences();
        DistanceCalculator.init(this);
        ServerConnection.setActivityReference(this);
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

        ServerConnection.disconnect();
        savePreferences();
    }


    private void loadPreferences(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getString(R.string.save_ip), "0.0.0.0");
        String port = sharedPref.getString(getString(R.string.save_port), "55666");
        ((EditText)findViewById(R.id.editText_ip)).setText(ip);
        ((EditText)findViewById(R.id.editText_port)).setText(port);
    }
    private void savePreferences(){
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(getString(R.string.save_ip), ((EditText)findViewById(R.id.editText_ip)).getText().toString());
        editor.putString(getString(R.string.save_port), ((EditText) findViewById(R.id.editText_port)).getText().toString());
        editor.apply();
    }


    public void onConnectClicked(View view){

        Button button = (Button) findViewById(R.id.button_connect);

        if(ServerConnection.isConnected()) {
            ServerConnection.disconnect();
            button.setText("connect");
            Toast.makeText(this, "Disconnected from server.",
                    Toast.LENGTH_SHORT).show();

        } else{
            String ip = ((EditText) findViewById(R.id.editText_ip)).getText().toString();
            String port_s = ((EditText) findViewById(R.id.editText_port)).getText().toString();
            int port = (port_s.equals("") ? 0 : Integer.parseInt(port_s));
            try {
                ServerConnection.connect(ip, port);
                ServerConnection.setPositionNotifier(new PositionNotifier() {
                    @Override
                    public void onDataArrived(float x, float y, float[] b_x, float[] b_y, int[] b_i) {
                        TextView tv = (TextView) findViewById(R.id.text_position);
                        tv.setText("x: " + x + " ,y: " + y);
                    }
                });

                button.setText("disconnect");
                Toast.makeText(this, "Established connection to server.",
                        Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Couldn't establish connection.",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onCalibrationClicked(View view){
        if(!ServerConnection.isConnected())
            onConnectClicked(new View(this));

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Beacon for Calibration");
        builder.setMessage("Please select a beacon by holding it close to your device, removing others from the immediate vicinity and clicking \"OK\" afterwards.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCalibration(beaconList.get(0));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void startCalibration(Beacon beacon){
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra(getString(R.string.intent_extra_uuid), beacon.getId1().toString());
        intent.putExtra(getString(R.string.intent_extra_major), beacon.getId2().toString());
        intent.putExtra(getString(R.string.intent_extra_minor), beacon.getId3().toString());
        startActivity(intent);
    }

    public void onShowPositionClicked(View view){
        startActivity(new Intent(this, ShowPositionActivity.class));
    }

    public void onStartSensorsClicked(View view){
        startActivity(new Intent(this, SensorListActivity.class));
    }



    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }
            });
            builder.show();
        }
    }




    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(rangeNotifier);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("...", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
            }
        }
    }
}

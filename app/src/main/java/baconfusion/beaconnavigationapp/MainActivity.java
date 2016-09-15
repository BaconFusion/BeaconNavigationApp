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
import android.widget.EditText;
import android.widget.ListView;

import android.view.View;
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


/**
 * Created by Stefan on 15-Sep-16.
 */
public class MainActivity extends Activity implements BeaconConsumer {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MainActivity";

    private BeaconManager beaconManager;
    ArrayList<Beacon> beaconList = new ArrayList<>();
    public BeaconListAdapter beaconListAdapter;

    ServerConnection serverConnection = null;

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
            if(serverConnection != null){
                serverConnection.sendBeacons(beaconList);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        verifyBluetooth();
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

        // adding iBeacon Format to Library:
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);


        //init list + adapter
        ListView beaconListView = (ListView) findViewById(R.id.listView);
        beaconListAdapter = new BeaconListAdapter(this, R.layout.list_item, beaconList);
        beaconListView.setAdapter(beaconListAdapter);

        loadPreferences();
        DistanceCalculator.init(this);
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

        savePreferences();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);

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
        String ip = ((EditText)findViewById(R.id.editText_ip)).getText().toString();
        String port_s = ((EditText)findViewById(R.id.editText_port)).getText().toString();
        int port = (port_s.equals("")? 0 : Integer.parseInt(port_s));
        try {
            serverConnection = new ServerConnection(ip, port);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't establish connection.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onCalibrationClicked(View view){
//        Intent intent = new Intent(this,  calibrationclass   .class);
//        startActivity(intent);
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
            beaconManager.startRangingBeaconsInRegion(new Region("f7826da6-4fa2-4e98-8024-bc5b71e0893e", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

package org.altbeacon.beaconreference;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.os.StrictMode;
import android.view.View;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import org.altbeacon.beacon.BeaconConsumer;
import android.os.RemoteException;
import android.util.Log;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.FileReader;
import android.os.Environment;
import java.util.Calendar;

/**
 * Created by fabiola on 15.09.16.
 */
public class CallibratingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "CallibratingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private int sum = 0;
    private boolean readLines = false;
    private int counter = 1;
    public static final String PREFS_NAME = "MyPrefsFile";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callibrating);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
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

                if (beacons.size() > 0) {
                    logToDisplay("The first beacon's rssi: "+beacons.iterator().next().getRssi());
                    EditText distance = (EditText) findViewById(R.id.distance);
                    appendLog(beacons.iterator().next().getRssi(),distance.getText().toString());
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace();  }
    }

    public void startCallibration(View view){
        // adding iBeacon Format to Library:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }


    public void stopCallibration(View view) {
        // read from log file and get average
        //onPause();
        File sdcard = Environment.getExternalStorageDirectory();

        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR);

        EditText dist = (EditText) findViewById(R.id.distance);
        String distance = dist.getText().toString();

        //Get the text file
        String filename = date+hour+distance+"meter.txt";
        File file = new File(sdcard,filename);
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {

                sum += Integer.parseInt(line);
                counter ++;
            }
            br.close();
        }
        catch (IOException e) {
            logToDisplay("Failed to calculate RSSI");
        }
        //Save value to SharedPreferences
        SharedPreferences constants = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = constants.edit();
        String key = "rssi_"+distance+"meter";
        editor.putInt(key, sum/counter);
        editor.commit();

        //Lets see the value, just for checking
        int consta = constants.getInt(key,0);
        EditText editText = (EditText) CallibratingActivity.this.findViewById(R.id.onemeter);
        editText.setText("RSSI in "+distance+" meter: "+Integer.toString(consta));

        file.delete();
    }



    public void appendLog(int value,String distance)
    {
        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR);

        //Create the text file
        String filename = date+hour+distance+"meter.txt";
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,filename);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Write to file
        FileWriter writer;
        try {
            writer = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(writer);
            out.write(Integer.toString(value));
            out.newLine();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateConstants(View view){

        //Calculate Ratio: RSSI/RSSI@1meter
        Integer[] ratio = new Integer[10];

        for (int d=1;d<=10;d++){
            SharedPreferences constants = getSharedPreferences(PREFS_NAME,0);
            String key = "rssi_"+d+"meter";
            int rssi = constants.getInt(key,0);
            ratio[d-1]=rssi/constants.getInt( "rssi_1meter",0);
        }
        //Save Ratio in file
        String filename = "ratios.txt";
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,filename);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Write to file
        FileWriter writer;
        for(int i=1;i<=10;i++){
            try {
                writer = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(writer);
                out.write(Integer.toString(ratio[i]));
                out.newLine();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) CallibratingActivity.this.findViewById(R.id.calli_events);
                editText.setText(line);
            }
        });
    }

}
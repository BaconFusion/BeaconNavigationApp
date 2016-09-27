package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class CalibrationActivity extends Activity implements BeaconConsumer{

    public static final int DATA_POINTS_PER_DISTANCE = 10;
    public static final String TAG = "CalibrationActivity";
    public static final Object syncObject = new Object();

    private static BeaconManager beaconManager;

    private static int counter, sum;
    private static ArrayList<Float> distance = new ArrayList<>();
    private static ArrayList<Float> averageRSSI = new ArrayList<>();

    private static String calibrationBeaconUUID, calibrationBeaconMajor, calibrationBeaconMinor;
    private static TextView countdownTextView;
    private static TextView currentRSSITextView;

    private static Activity selfReference;

    private static final RangeNotifier defaultRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
            ArrayList<Beacon> list = new ArrayList<>();
            list.addAll(collection);
            for (final Beacon beacon : list) {
                if (beacon.getId1().toString().equals(calibrationBeaconUUID)
                        && beacon.getId2().toString().equals(calibrationBeaconMajor)
                        && beacon.getId3().toString().equals(calibrationBeaconMinor)) {

                        selfReference.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentRSSITextView.setText(Integer.toString(beacon.getRssi()));
                            }
                        });
                    break;
                }
            }
        }
    };

    public static void promptMeasurementAt(final float meters){
        selfReference.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(selfReference);
                                            builder.setTitle(meters + " Meter Measurement");
                                            builder.setMessage("Hold your device at a distance of " + meters + " meter(s) and hit \"OK\".");
                                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startMeasurementsAt(meters);
                                                }
                                            });
//                                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                                @Override
//                                                public void onDismiss(DialogInterface dialog) {
//                                                    selfReference.finish();
//                                                }
//                                            });
                                            builder.show();
                                        }
                                    }
        );
    }

    public static void startMeasurementsAt(final float meters){
        countdownTextView.setText(Integer.toString(DATA_POINTS_PER_DISTANCE));
        counter = 0;
        sum = 0;

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

                ArrayList<Beacon> list = new ArrayList<>();
                list.addAll(collection);

                for (final Beacon beacon : list) {

                    if (!(beacon.getId1().toString().equals(calibrationBeaconUUID)
                            && beacon.getId2().toString().equals(calibrationBeaconMajor)
                            && beacon.getId3().toString().equals(calibrationBeaconMinor))) {
                        continue;
                    }
                    final float rssi = beacon.getRssi() / beacon.getTxPower();

                    counter++;
                    sum += rssi;

                    selfReference.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentRSSITextView.setText(Float.toString(rssi));
                            countdownTextView.setText(Integer.toString(DATA_POINTS_PER_DISTANCE - counter));
                        }
                    });


                    if (counter == DATA_POINTS_PER_DISTANCE) {
                        float average = sum / ((float) counter);
                        distance.add(meters);
                        averageRSSI.add(average);

                        finishedMeasurementsAtDistanceToast(meters);
                        beaconManager.setRangeNotifier(defaultRangeNotifier);

                        synchronized (syncObject) {
                            syncObject.notify();
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("...", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace();  }
    }

    private static void finishedMeasurementsAtDistanceToast(final float meters) {
        selfReference.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(selfReference, "Finished measurements at " + meters + " meter(s).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void finishCalibration() {

        //update oneMeterReferenceRSSI - one meter first, mandatory
        DistanceCalculator.setOneMeterReferenceRSSI(averageRSSI.get(0));

        if (ServerConnection.isConnected()) {
            ServerConnection.sendCalibrationData(distance, averageRSSI);
            selfReference.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(selfReference, "Finished calibration successfully.", Toast.LENGTH_SHORT).show();
                }
            });
            selfReference.finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        selfReference = this;

        Bundle extras = getIntent().getExtras();

        calibrationBeaconUUID = extras.getString(getString(R.string.intent_extra_uuid));
        calibrationBeaconMajor = extras.getString(getString(R.string.intent_extra_major));
        calibrationBeaconMinor = extras.getString(getString(R.string.intent_extra_minor));

        ((TextView) findViewById(R.id.calibration_textView_beacon)).setText("Calibrating beacon:\n" +
                "UUID: " + calibrationBeaconUUID + "\n" +
                "Major: " + calibrationBeaconMajor + "\n" +
                "Minor: " + calibrationBeaconMinor);

        countdownTextView = (TextView) findViewById(R.id.calibration_textView_countdown);
        countdownTextView.setText(Integer.toString(DATA_POINTS_PER_DISTANCE));

        currentRSSITextView = (TextView) findViewById(R.id.calibration_textView_rssi);
        currentRSSITextView.setText("");


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(MainActivity.LAYOUT_IBEACON));
        beaconManager.setForegroundBetweenScanPeriod(MainActivity.BEACON_SCAN_INTERVALL);
        beaconManager.setForegroundScanPeriod(MainActivity.BEACON_SCAN_INTERVALL);
        beaconManager.bind(this);
    }

    public void startCalibration(View view) {

        String distances = ((EditText) findViewById(R.id.calibration_editText_distance)).getText().toString().replaceAll(" ", "");
        Scanner scanner = new Scanner(distances);
        scanner.useDelimiter(",");
        ArrayList<Float> values = new ArrayList<>();
        while (scanner.hasNextFloat()) {
            values.add(scanner.nextFloat());
        }

        new CalibrationTask().execute(values);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(defaultRangeNotifier);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("...", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace();  }
    }

}

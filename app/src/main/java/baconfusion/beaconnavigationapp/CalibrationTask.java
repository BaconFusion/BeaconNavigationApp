package baconfusion.beaconnavigationapp;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Stefan on 21-Sep-16.
 */
public class CalibrationTask extends AsyncTask<ArrayList<Float>, Integer, Long> {

    @Override
    protected Long doInBackground(ArrayList<Float>... params) {


        synchronized (CalibrationActivity.syncObject){
            CalibrationActivity.promptMeasurementAt(1.0f);

            try {
                CalibrationActivity.syncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i=0; i<params[0].size(); i++){

            synchronized (CalibrationActivity.syncObject){
                CalibrationActivity.promptMeasurementAt(params[0].get(i));

                try {
                    CalibrationActivity.syncObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        CalibrationActivity.finishCalibration();

        return null;
    }

}

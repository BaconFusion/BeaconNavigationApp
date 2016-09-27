package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Stefan on 19-Sep-16.
 */
public class SensorListener implements SensorEventListener{

    private static SensorManager sensorManager;
    private Activity referenceActivity;

    private Sensor accelerometer;



    public SensorListener(Activity activity){
        referenceActivity = activity;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        register();
    }


    public void register(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister(){
        sensorManager.unregisterListener(this, accelerometer);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:

                ((TextView) referenceActivity.findViewById(R.id.sensorList_textView_acc_0)).setText(Float.toString(event.values[0]));
                ((TextView) referenceActivity.findViewById(R.id.sensorList_textView_acc_1)).setText(Float.toString(event.values[1]));
                ((TextView) referenceActivity.findViewById(R.id.sensorList_textView_acc_2)).setText(Float.toString(event.values[2]));

                //ServerConnection.sendSensorData((byte) 0, event.values);

                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
            case Sensor.TYPE_MAGNETIC_FIELD:
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}

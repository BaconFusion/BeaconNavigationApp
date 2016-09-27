package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Stefan on 27-Sep-16.
 */
public class SensorListActivity extends Activity{

    SensorListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);

        sensorListener = new SensorListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorListener = new SensorListener(this);
    }

}

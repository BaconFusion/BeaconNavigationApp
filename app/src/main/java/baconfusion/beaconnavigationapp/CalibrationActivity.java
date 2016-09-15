package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by fabiola on 15.09.16.
 */
public class CalibrationActivity extends Activity {
    protected static final String TAG = "CalibrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calibrating);

        //Intent intent = getIntent();
        logToDisplay("Calibrating just launched");
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //TextView textView = new TextView(this);
        //textView.setTextSize(40);
        //textView.setText("Calibrating");

        //ViewGroup layout = (ViewGroup) findViewById(R.id.activity_calibrating);
        //layout.addView(textView);
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((BeaconReferenceApplication) this.getApplicationContext()).setCallibratingActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        ((BeaconReferenceApplication) this.getApplicationContext()).setCallibratingActivity(null);
    }

    public void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)CalibrationActivity.this
                        .findViewById(R.id.calibratingText);
                editText.append(line+"\n");
            }
        });
    }

}

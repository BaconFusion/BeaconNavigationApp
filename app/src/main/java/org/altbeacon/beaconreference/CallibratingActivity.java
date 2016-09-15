package org.altbeacon.beaconreference;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by fabiola on 15.09.16.
 */
public class CallibratingActivity extends Activity {
    protected static final String TAG = "CallibratingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_callibrating);

        //Intent intent = getIntent();
        logToDisplay("Callibrating just launched");
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //TextView textView = new TextView(this);
        //textView.setTextSize(40);
        //textView.setText("Callibrating");

        //ViewGroup layout = (ViewGroup) findViewById(R.id.activity_callibrating);
        //layout.addView(textView);
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

    public void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)CallibratingActivity.this
                        .findViewById(R.id.callibratingText);
                editText.append(line+"\n");
            }
        });
    }

}

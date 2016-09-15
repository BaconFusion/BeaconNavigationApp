package org.altbeacon.beaconreference;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by fabiola on 15.09.16.
 */
public class CallibratingActivity extends Activity {
    protected static final String TAG = "CallibratingActivity";
    private MonitoringActivity monitoringActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_callibrating);
        Bundle values = getIntent().getExtras();

        String ip = "";
        String port = "";

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

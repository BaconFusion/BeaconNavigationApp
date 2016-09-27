package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.os.Bundle;

public class ShowPositionActivity extends Activity {

    private PaintView pv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_position);
        pv = (PaintView) findViewById(R.id.view);
       /* float x = 20.0f;
        float y = -20.0f;
        float[] b_x = {0, 1.1f};
        float[] b_y = {4.0f,-5.0f};
        int[] b_i = {638, 223};

       pv.onDataArrived(x, y, b_x, b_y, b_i);*/
    }
}

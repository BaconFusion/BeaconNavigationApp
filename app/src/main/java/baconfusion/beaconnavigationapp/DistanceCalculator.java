package baconfusion.beaconnavigationapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.altbeacon.beacon.Beacon;

/**
 * Created by Stefan on 16-Sep-16.
 */
public class DistanceCalculator {

    private static boolean initialized = false;
    private static Activity activityReference;
	
    public static void init(Activity activity){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        float a = sharedPref.getFloat(activity.getString(R.string.save_var_a), 0);
        float b = sharedPref.getFloat(activity.getString(R.string.save_var_b), 0);
        float c = sharedPref.getFloat(activity.getString(R.string.save_var_c), 0);

        org.altbeacon.beacon.distance.DistanceCalculator dc = new org.altbeacon.beacon.distance.CurveFittedDistanceCalculator(a, b, c);
        Beacon.setDistanceCalculator(dc);

        activityReference = activity;
        initialized = true;
    }

    public static void update(float a, float b, float c) {

        org.altbeacon.beacon.distance.DistanceCalculator dc = new org.altbeacon.beacon.distance.CurveFittedDistanceCalculator(a, b, c);
        Beacon.setDistanceCalculator(dc);

        SharedPreferences.Editor editor = activityReference.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putFloat(activityReference.getString(R.string.save_var_a), a);
        editor.putFloat(activityReference.getString(R.string.save_var_b), b);
        editor.putFloat(activityReference.getString(R.string.save_var_c), c);
        editor.apply();
    }
}

package baconfusion.beaconnavigationapp;

import android.content.Context;
import android.content.SharedPreferences;

import android.app.Activity;
/**
 * Created by Stefan on 16-Sep-16.
 */
public class DistanceCalculator {

    private static int a, b, c;
    private static boolean initialized = false;

    public static void init(Activity activity){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        a = sharedPref.getInt(activity.getString(R.string.save_var_a), 1);
        b = sharedPref.getInt(activity.getString(R.string.save_var_b), 1);
        c = sharedPref.getInt(activity.getString(R.string.save_var_c), 1);
        initialized = true;
    }

    public static double calculateDistance(byte rssi){
        assert(initialized);
        return a * Math.pow( 1.0 / 1.0, b) + c;
    }
}

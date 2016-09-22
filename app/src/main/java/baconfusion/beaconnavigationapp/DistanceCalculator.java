package baconfusion.beaconnavigationapp;

import android.content.Context;
import android.content.SharedPreferences;

import android.app.Activity;

/**
 * Created by Stefan on 16-Sep-16.
 */
public class DistanceCalculator {

    private static float a, b, c, d, oneMeterReferenceRSSI;
    private static boolean initialized = false;
    private static Activity activityReference;
	
    public static void init(Activity activity){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        a = sharedPref.getFloat(activity.getString(R.string.save_var_a), 0);
        b = sharedPref.getFloat(activity.getString(R.string.save_var_b), 0);
        c = sharedPref.getFloat(activity.getString(R.string.save_var_c), 0);
        d = sharedPref.getFloat(activity.getString(R.string.save_var_d), 0);
        oneMeterReferenceRSSI = sharedPref.getFloat(activity.getString(R.string.save_var_oneMeterReferenceRSSI), 1);
        activityReference = activity;
        initialized = true;
    }

    public static void setOneMeterReferenceRSSI(float referenceRSSI){
        oneMeterReferenceRSSI = referenceRSSI;
        SharedPreferences.Editor editor = activityReference.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putFloat(activityReference.getString(R.string.save_var_oneMeterReferenceRSSI), referenceRSSI);
        editor.apply();
    }

    public static void update(float a, float b, float c, float d){
        DistanceCalculator.a = a;
        DistanceCalculator.b = b;
        DistanceCalculator.c = c;
        DistanceCalculator.d = d;
        SharedPreferences.Editor editor = activityReference.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putFloat(activityReference.getString(R.string.save_var_a), a);
        editor.putFloat(activityReference.getString(R.string.save_var_b), b);
        editor.putFloat(activityReference.getString(R.string.save_var_c), c);
        editor.putFloat(activityReference.getString(R.string.save_var_d), d);
        editor.apply();
    }


    public static float calculateDistance(int rssi){
        assert(initialized);
        return (float) (a*Math.pow(rssi, 3)+ b*Math.pow(rssi,2) + c*Math.pow(rssi,1) + d);
        //(a * Math.pow( rssi / oneMeterReferenceRSSI, b) + c);
    }
}

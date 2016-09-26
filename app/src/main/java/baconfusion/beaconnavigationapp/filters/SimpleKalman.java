package baconfusion.beaconnavigationapp.filters;

import org.altbeacon.beacon.service.RssiFilter;

/**
 * Created by glor on 9/24/16.
 */
public class SimpleKalman implements RssiFilter {
    public static double q; //process noise covariance
    public static double r; //measurement noise covariance
    public static double p; //estimation error covariance
    double x; //value
    double k; //kalman gain

    private boolean noMeasurements = true;

    public SimpleKalman() {
    }

    public void addMeasurement(Integer measurement) {
        addMeasurement((double) measurement);
    }

    void addMeasurement(double measurement) {
        if (noMeasurementsAvailable()) {
            x = measurement;
            noMeasurements = false;
            return;
        }
        //prediction update
        p = p + q;
        //measurement update
        k = p / (p + r);
        x = x + k * (measurement - x);
        p = (1 - k) * p;
    }

    public boolean noMeasurementsAvailable() {
        return noMeasurements;
    }

    public double calculateRssi() {
        return x;
    }
}

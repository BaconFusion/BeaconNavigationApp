package baconfusion.beaconnavigationapp.filters;

import org.altbeacon.beacon.service.RssiFilter;

/**
 * Created by glor on 9/24/16.
 */
public class StupidFilter implements RssiFilter {
    boolean initialized = false;
    double value;

    @Override
    public void addMeasurement(Integer integer) {
        if (!initialized)
            initialized = true;
        value = (double) integer;
    }

    @Override
    public boolean noMeasurementsAvailable() {
        return !initialized;
    }

    @Override
    public double calculateRssi() {
        return value;
    }
}

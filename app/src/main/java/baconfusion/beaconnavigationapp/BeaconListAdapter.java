package baconfusion.beaconnavigationapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Stefan on 15-Sep-16.
 */
public class BeaconListAdapter
        extends ArrayAdapter<Beacon> {
    ArrayList<Beacon> beaconList;

    public BeaconListAdapter(Context context, int resource, ArrayList<Beacon> beaconList) {
        super(context, resource, beaconList);
        this.beaconList = beaconList;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        TextView beaconUUID = (TextView)view.findViewById(R.id.list_item_uuid);
        TextView beaconMajor = (TextView)view.findViewById(R.id.list_item_major);
        TextView beaconMinor = (TextView)view.findViewById(R.id.list_item_minor);
        TextView beaconRSSI = (TextView)view.findViewById(R.id.list_item_rssi);
        Beacon beacon = this.beaconList.get(position);
        beaconUUID.setText("uuid: " + beacon.getId1());
        beaconMajor.setText("major: " + beacon.getId2());
        beaconMinor.setText("minor: " + beacon.getId3());
        beaconRSSI.setText("rssi: " + beacon.getRssi());

        return view;
    }
}

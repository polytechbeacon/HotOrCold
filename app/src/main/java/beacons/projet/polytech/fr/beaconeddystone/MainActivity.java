package beacons.projet.polytech.fr.beaconeddystone;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class MainActivity extends Activity implements BeaconConsumer, RangeNotifier {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    private TextView text;

    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));

        try {
            // set the duration of the scan to be 1.11 seconds
            beaconManager.setForegroundScanPeriod(110l);
            beaconManager.setForegroundBetweenScanPeriod(2000l);

            // set the duration of the scan to be 1.1 seconds
            beaconManager.setBackgroundScanPeriod(1100l);
            // set the time between each scan to be 2 seconds
            beaconManager.setBackgroundBetweenScanPeriod(2000l);

            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.bind(this);
        beaconManager.bind(this);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.e(TAG, "beacons size = " + beacons.size());
        for (Beacon beacon: beacons) {
            // This is a Eddystone-UID frame
            Identifier namespaceId = beacon.getId1();
            Identifier instanceId = beacon.getId2();
            Log.e(TAG, "I see a beacon transmitting namespace id: "+namespaceId+
                    " and instance id: "+instanceId+
                    " approximately "+beacon.getDistance()+" meters away.");

            // Do we have telemetry data?
            if (beacon.getExtraDataFields().size() > 0) {
                long telemetryVersion = beacon.getExtraDataFields().get(0);
                long batteryMilliVolts = beacon.getExtraDataFields().get(1);
                long pduCount = beacon.getExtraDataFields().get(3);
                long uptime = beacon.getExtraDataFields().get(4);

                Log.e(TAG, "The above beacon is sending telemetry version "+telemetryVersion+
                        ", has been up for : "+uptime+" seconds"+
                        ", has a battery level of "+batteryMilliVolts+" mV"+
                        ", and has transmitted "+pduCount+" advertisements.");
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }
}

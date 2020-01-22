package com.orange.altbeacontransmitter;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private UUID uuid;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createUUID();
    }

    private void createUUID(){
        uuid = UUID.randomUUID();
        textView = findViewById(R.id.textView);
        textView.setText(uuid.toString());
        createBeacon(uuid);
    }

    private void createBeacon(UUID uuid) {
        Beacon beacon = new Beacon.Builder()
            .setId1(uuid.toString())
            .setId2("1")
            .setId3("2")
            .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
            .setTxPower(-59)
            .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
            .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("TAG", "Advertisement start failed with code: "+errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i("TAG", "Advertisement start succeeded.");
            }
        });

    }
}

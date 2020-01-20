package com.hummbletech.beacon_get;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String sharedPreference = "com.hummbletech.beacon_get.sharedPref";
    private String  current_uuid = null;
    private int BLUETOOTH_MAIN_REQUEST = 27;
    private int BLUETOOTH_MAIN_REQUEST_ADMIN = 28;
    AdvertiseCallback callback;
    BluetoothLeAdvertiser advertiser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getButton = findViewById(R.id.button);
        TextView textView = findViewById(R.id.text_1);

        if(!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "Sorry! your device doesn't support it! try another device!", Toast.LENGTH_LONG).show();

            getButton.setEnabled(false);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreference, MODE_PRIVATE);

        if(sharedPreferences.getString("uuid_dekh_lo", null) == null){
            UUID uuid = UUID.randomUUID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uuid_dekh_lo", uuid.toString());
            editor.apply();
            Toast.makeText(this, "UUID Created! Restart the App", Toast.LENGTH_LONG).show();
            finish();
        } else {
            current_uuid = sharedPreferences.getString("uuid_dekh_lo", null);
            if(current_uuid == null){
                //Abort
                textView.setText("FATAL! NO UUID FOUND! THUS THE APP WON'T WORK! Try again by restarting the App!");
                getButton.setEnabled(false);

            }

            textView.setText(current_uuid);


            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED ) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH)){
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            BLUETOOTH_MAIN_REQUEST

                    );

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                            BLUETOOTH_MAIN_REQUEST_ADMIN

                    );

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {


                advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

                final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                        .setConnectable(true)
                        .build();

                callback = new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        Toast.makeText(getBaseContext(), "Done! you are being advertised!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        Toast.makeText(getBaseContext(), "Sorry! Not working! " + errorCode, Toast.LENGTH_SHORT).show();
                    }
                };
                getButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getBaseContext(), "Working! wait!", Toast.LENGTH_SHORT).show();
                        ParcelUuid pUid = new ParcelUuid(UUID.fromString(current_uuid));
                        AdvertiseData data = new AdvertiseData.Builder()
                                .setIncludeDeviceName(false)
                                .addServiceUuid(pUid)
                                .build();

                        advertiser.startAdvertising(settings, data,callback);

                    }
                });

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(advertiser != null) {
            advertiser.stopAdvertising(callback);
        }
        else{
            Toast.makeText(getBaseContext(), "Advertising not happening!", Toast.LENGTH_SHORT).show();
        }


    }
}

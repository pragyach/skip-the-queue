package com.sdsmdg.skipthequeue;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.sdsmdg.skipthequeue.BeaconFinder.BeaconAdapter;
import com.sdsmdg.skipthequeue.BeaconFinder.BeaconFinderService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_LOCATION = 2;
    private final static int REQUEST_ENABLE_FINE_LOCATION = 3;
    ListView beaconsListView;
    BroadcastReceiver broadcastReceiver;
    ArrayList<IEddystoneDevice> beaconsArray;
    BeaconAdapter beaconAdapter;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconsArray = new ArrayList<>();
        beaconsListView = (ListView) findViewById(R.id.beacons_list_view);
        beaconAdapter = new BeaconAdapter(this,beaconsArray);
        beaconsListView.setAdapter(beaconAdapter);

        makeReceiver();
        checkPermissions();
        Log.i("HEy","hi");

    }

    private void makeReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                beaconsArray = intent.getParcelableArrayListExtra(BeaconFinderService.beacons_array);
                Toast.makeText(getApplicationContext() , String.valueOf(beaconsArray.size()),Toast.LENGTH_SHORT).show();
                beaconAdapter = new BeaconAdapter(getApplicationContext(),beaconsArray);
                beaconsListView.setAdapter(beaconAdapter);

            }
        };
    }

    private void startService() {
        Intent i = new Intent(this, BeaconFinderService.class);
        startService(i);
    }

    @Override
    protected void onStart() {

        //Register the receiver
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(BeaconFinderService.intent_filter)
        );

        super.onStart();
    }

    @Override
    protected void onStop() {

        //Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();

    }


    private void checkPermissions()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,REQUEST_ENABLE_BT);
        }


        if(!isLocationEnabled(getApplicationContext()))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Settings").setMessage("Please Turn on Location Settings to proceed further.").setPositiveButton("Ok",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Intent ii = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(ii,REQUEST_ENABLE_LOCATION);

                }
            }).show();
        }

        if(Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ENABLE_FINE_LOCATION);
        }

        else
        {
            startService();
        }

    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {

        if(requestCode == REQUEST_ENABLE_FINE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startService();
        }

        else
        {
            Toast.makeText(this,"Please accept the Runtime Permission", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){

            case REQUEST_ENABLE_BT:{

                if(resultCode != RESULT_OK)
                    Toast.makeText(this,"Bluetooth is Required.",Toast.LENGTH_SHORT).show();
                    break;


            }

            case REQUEST_ENABLE_LOCATION:{

                if(!isLocationEnabled(getApplicationContext()))
                    Toast.makeText(this,"Location is required.",Toast.LENGTH_SHORT).show();
                    break;
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

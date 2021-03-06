package com.sdsmdg.skipthequeue.BeaconFinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sdsmdg.skipthequeue.MapsActivity;

public class DisconnectBeaconListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"Beacon Disconnected.", Toast.LENGTH_SHORT).show();
        Intent i  = new Intent(context, MapsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(i);
    }
}

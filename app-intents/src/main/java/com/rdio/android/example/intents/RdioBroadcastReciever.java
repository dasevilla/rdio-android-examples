package com.rdio.android.example.intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RdioBroadcastReciever extends BroadcastReceiver {
    private static String TAG = "RdioBroadcastReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast: " + intent.getAction());
    }

}

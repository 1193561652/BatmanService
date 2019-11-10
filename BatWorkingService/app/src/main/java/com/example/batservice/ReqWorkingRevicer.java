package com.example.batservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReqWorkingRevicer extends BroadcastReceiver {

    WorkingServiceConnection wsConnection = new WorkingServiceConnection();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("ReqWorkingRevicer", "onReceive");

        Intent startIntent = new Intent();
        ComponentName componentName = new ComponentName("com.example.batservice", "com.example.batservice.WorkingService");
        startIntent.setComponent(componentName);
        context.startForegroundService(intent);
        //context.unbindService(wsConnection);
    }
}


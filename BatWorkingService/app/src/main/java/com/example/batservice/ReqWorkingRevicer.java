package com.example.batservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.batservice.WorkingServiceConnection;

import static android.content.Context.BIND_AUTO_CREATE;

public class ReqWorkingRevicer extends BroadcastReceiver {

    com.example.batservice.WorkingServiceConnection wsConnection = new WorkingServiceConnection();

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


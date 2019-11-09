package com.example.batservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;

public class PermissionHelper {
    @SuppressLint("NewApi")
    public static void requestReadExternalPermission(Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Log.d(TAG, "READ permission IS NOT granted...");
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        } else {
            //Log.d(TAG, "READ permission is granted...");
        }
    }

    public static void requestWriteExternalPermission(Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Log.d(TAG, "READ permission IS NOT granted...");
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        } else {
            //Log.d(TAG, "READ permission is granted...");
        }
    }
}

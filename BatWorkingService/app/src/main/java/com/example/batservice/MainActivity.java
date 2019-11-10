package com.example.batservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.batservice.R;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WorkingServiceConnection wsConnection = new WorkingServiceConnection();

    Button btn = null;
    Button btn2 = null;
    Button btn3 = null;
    TextView textView = null;
    TextView textView2 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        PermissionHelper.requestReadExternalPermission(this);
        PermissionHelper.requestWriteExternalPermission(this);

        Intent startIntent = new Intent();
        ComponentName componentName = new ComponentName("com.example.batservice", "com.example.batservice.WorkingService");
        startIntent.setComponent(componentName);
        getApplication().startForegroundService(startIntent);
        boolean bSec = getApplication().bindService(startIntent, wsConnection, BIND_AUTO_CREATE);

        btn = (Button)findViewById(R.id.button);
        btn2 = (Button)findViewById(R.id.button2);
        btn3 = (Button)findViewById(R.id.button3);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = wsConnection.mixInfo();
                textView.setText(str);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wsConnection.tiggerReq();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String err = wsConnection.getError();
                textView2.setText(err);
            }
        });

//        DatabaseAdaper db = new DatabaseAdaper(this.getApplicationContext());
//        BatWorkingInfo batWorkingInfo = new BatWorkingInfo();
//        for(int i=0 ;i < 10; i++) {
//            batWorkingInfo.issue = "" + i;
//            db.insertAndUpdate(batWorkingInfo);
//            List<BatWorkingInfo> result = db.findAll();
//            Log.v("a", "" + result.size());
//        }

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public void requestNeedWorking() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LuaState lua = LuaStateFactory.newLuaState();
                //if(lua == null)
                lua.openLibs();
                int ret = lua.LdoString(readAssetsTxt(MainActivity.this, "test.lua"));
                lua.getGlobal("getUrl");
                ret = lua.pcall(0, 1, 0);
                String url = lua.toString(-1);
                lua.pop(1);

                String strReq = HttpURLConnectionUtil.get(url, new HashMap<String, Object>() );


                lua.getGlobal("needWorking");
                lua.pushString(strReq);
                ret = lua.pcall(1, 1, 0);
                String needWorking = lua.toString(-1);
                lua.pop(1);
                lua.close();


            }
        }).start();
    }

    public static class HttpURLConnectionUtil {
        public static String get(String url, Map<String, Object> param) {
            StringBuilder builder = new StringBuilder();
            try {
                StringBuilder params = new StringBuilder();
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    params.append(entry.getKey());
                    params.append("=");
                    params.append(entry.getValue().toString());
                    params.append("&");
                }
                if (params.length() > 0) {
                    params.deleteCharAt(params.lastIndexOf("&"));
                }
                URL restServiceURL = new URL(url + (params.length() > 0 ? "?" + params.toString() : ""));
                HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept", "application/json");
                if (httpConnection.getResponseCode() != 200) {
                    throw new RuntimeException("HTTP GET Request Failed with Error code : " + httpConnection.getResponseCode());
                }
                InputStream inStrm = httpConnection.getInputStream();
                byte[] b = new byte[1024];
                int length = -1;
                while ((length = inStrm.read(b)) != -1) {
                    builder.append(new String(b, 0, length));
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }
    }


    public static String readAssetsTxt(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "err";
    }
}

package com.example.batservice;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.example.batservice.db.BatWorkingInfo;
import com.example.batservice.db.DatabaseAdaper;
import com.example.batservice.http.HttpURLConnectionUtil;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.example.batservice.R;

import static android.app.PendingIntent.getActivity;

public class WorkingService extends Service {
    RequsetWorking mBind = new RequsetWorking();
    ReqThread mThread = null;
    private PowerManager.WakeLock wakeLock = null;
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("RequsetWorking", "onBind");
        return mBind;
    }

    @Override
    public void onCreate() {
        Log.v("RequsetWorking", "onCreate");
        super.onCreate();


        this.setForeground();

        String fileFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WorkingService/";
        String fileName = fileFolder + "test.lua";
        boolean dirOK = false;
        File dir = new File(fileFolder);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                //return true;
                dirOK = true;
            } else {
                //return false;
                dirOK = false;
            }
        } else {
            dirOK = true;
        }

        if(dirOK) {
            File luaTest = new File(fileName);
            if(!luaTest.exists()) {
                try {
                    if(luaTest.createNewFile()) {
                        OutputStream os = new FileOutputStream(luaTest);
                        InputStream is = this.getApplication().getAssets().open("test.lua");
                        if(os != null && is != null) {
                            byte[] buffer = new byte[1024];
                            int byteCount = 0;
                            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                                // buffer字节
                                os.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                            }
                            os.flush();// 刷新缓冲区
                            is.close();
                            os.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        DatabaseAdaper db = new DatabaseAdaper(this.getApplicationContext());

        mThread = new ReqThread(mBind, mBuilder, (NotificationManager)getSystemService(NOTIFICATION_SERVICE), db);
        mThread.start();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WorkingService.class.getName());
        wakeLock.acquire();

        Intent intent = new Intent(WorkingService.this, ReqWorkingRevicer.class);
        PendingIntent sender = PendingIntent.getBroadcast(WorkingService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, sender);
    }



    private final String ID="channel_1";
    private final String NAME="前台服务";
    private Notification.Builder mBuilder = null;
    private void setForeground(){
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel (ID, NAME, NotificationManager.IMPORTANCE_MIN);
        channel.enableLights(false);
        channel.setSound(null, null);
        manager.createNotificationChannel (channel);
        mBuilder = new Notification.Builder(this, ID)
                .setContentTitle ("notify")
                .setContentText ("---")
                .setSmallIcon (R.mipmap.ic_launcher)
                .setLargeIcon (BitmapFactory.decodeResource (getResources (),R.mipmap.ic_launcher));
        Notification notification = mBuilder.build();

        startForeground (1, notification);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.v("RequsetWorking", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.v("RequsetWorking", "onDestroy");
        super.onDestroy();
        mThread.myStop();
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel(ID);
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public static class ReqThread extends Thread {
        public static final int MSG_EXT = 1;
        public static final int MSG_REQ = 2;
        public static final int MSG_TIMER = 3;
        public static final int MSE_CREATE = 4;

        private Notification.Builder mBuilder = null;
        private RequsetWorking mBind = null;
        private Handler mHandler = null;
        private long lastReqTime = 0;
        private NotificationManager manager = null;
        private DatabaseAdaper mDb = null;
        public ReqThread(RequsetWorking bind, Notification.Builder builder, NotificationManager manager, DatabaseAdaper db) {
            this.mBind = bind;
            this.mBuilder = builder;
            this.manager = manager;
            this.mDb = db;
        }

        @Override
        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //super.handleMessage(msg);
                    switch (msg.what) {
                        case ReqThread.MSE_CREATE:
                            Log.v("ReqThread", "ReqThread.MSE_CREATE");

                            BatWorkingInfo batWorkingInfo = mDb.findById(0);
                            if(batWorkingInfo != null) {
                                lastReqTime = batWorkingInfo.reqTime;
                                if(mBind != null) {
                                    mBind.setResult(batWorkingInfo.working, "A", batWorkingInfo.issue, batWorkingInfo.name, batWorkingInfo.reqTime);
                                }
                                lastInfo = batWorkingInfo.working + "-" + "A" + "-" + batWorkingInfo.name + "-" + batWorkingInfo.issue;
                                updateNofify(System.currentTimeMillis());
                            }
                            Message msg1 = new Message();
                            msg1.what = ReqThread.MSG_TIMER;
                            mHandler.sendMessage(msg1);
                            break;
                        case ReqThread.MSG_EXT:
                            Looper.myLooper().quit();
                        break;
                        case ReqThread.MSG_REQ:
                            Log.v("ReqThread", "ReqThread.MSG_REQ");
                            ReqWorking();
                            ReqThread.this.updateNofify(System.currentTimeMillis());
                            lastReqTime = System.currentTimeMillis();
                            mHandler.removeMessages(ReqThread.MSG_TIMER);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = ReqThread.MSG_TIMER;
                                    mHandler.sendMessage(msg);
                                }
                            }, 60 * 1000);
                        break;
                        case ReqThread.MSG_TIMER:
                            long currTime = System.currentTimeMillis();

                            if(currTime - lastReqTime >= 30 * 60 * 1000) {
                                lastReqTime = currTime;
                                ReqWorking();
                            }

                            ReqThread.this.updateNofify(currTime);


                            Log.v("ReqThread", "ReqThread.MSG_TIMER");

                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = ReqThread.MSG_TIMER;
                                    mHandler.sendMessage(msg);
                                }
                            }, 60 * 1000);
                            break;
                        default:
                            break;
                    }
                }
            };

            Message msg = new Message();
            msg.what = ReqThread.MSE_CREATE;
            mHandler.sendMessage(msg);
            mBind.setThreadHandler(mHandler);
            Looper.loop();
        }

        protected void updateNofify(long currTime) {
            if(mBuilder != null) {
                Date date1 = new Date(lastReqTime);
                String last = new SimpleDateFormat("HH:mm:ss").format(date1);

                Date date2 = new Date(currTime);
                String curr = new SimpleDateFormat("HH:mm:ss").format(date2);

                mBuilder.setContentTitle(lastInfo);
                mBuilder.setContentText("lastTime:" + last + " currTime:" + curr);
                manager.notify(1, mBuilder.build());
            }
        }

        public void myStop() {
            mHandler.sendEmptyMessage(ReqThread.MSG_EXT);
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected String lastInfo = "";
        protected boolean ReqWorking() {
            LuaState lua = LuaStateFactory.newLuaState();
            if(lua == null) {
                if(mBind != null)
                    mBind.setError("newLuaState", "", System.currentTimeMillis());
                lastInfo = "error";
                return false;
            }
            lua.openLibs();
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WorkingService/test.lua";
            int ret = lua.LdoString(readFileTxt(fileName));
            if(ret != 0) {
                if(mBind != null)
                    mBind.setError("LdoString", "" + ret, System.currentTimeMillis());
                lastInfo = "error";
                return false;
            }
            lua.getGlobal("getUrl");
            ret = lua.pcall(0, 1, 0);
            if(ret != 0) {
                if(mBind != null)
                    mBind.setError("pcall, getUrl", "" + ret, System.currentTimeMillis());
                lastInfo = "error";
                return false;
            }

            String url = lua.toString(-1);
            lua.pop(1);

            String[] result = new String[1];
            String[] error = new String[1];
            boolean bReq = HttpURLConnectionUtil.get(url, new HashMap<String, Object>(), result, error);
            if(bReq == false) {
                if(mBind != null)
                    mBind.setError("HttpURLConnectionUtil.get", error[0]==null?"":error[0], System.currentTimeMillis());
                lastInfo = "error";
                return false;
            }
            String strReq = result[0];
            lua.getGlobal("needWorking");
            lua.pushString(strReq);
            ret = lua.pcall(1, 4, 0);
            if(ret != 0) {
                if(mBind != null)
                    mBind.setError("pcall,needWorking", "" + ret, System.currentTimeMillis());
                lastInfo = "error";
                return false;
            }

            String time = lua.toString(-1);
            String name = lua.toString(-2);
            String type = lua.toString(-3);
            String needWorking = lua.toString(-4);
            lua.pop(4);
            lua.close();

            if(mBind != null)
                mBind.setResult(needWorking, type, time, name, System.currentTimeMillis());
            lastInfo = needWorking + "-" + type + "-" + name + "-" + time;
            BatWorkingInfo batWorkingInfo = new BatWorkingInfo();

            batWorkingInfo.issue = time;
            batWorkingInfo.reqTime = System.currentTimeMillis();
            batWorkingInfo.working = needWorking;
            batWorkingInfo.name = name;
            batWorkingInfo.id = 0;
            mDb.insertAndUpdate(batWorkingInfo);

            return true;
        }

        public String readFileTxt(String fileName) {
            try {
                InputStream is = new FileInputStream(fileName);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String text = new String(buffer, "utf-8");
                return text;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "error";
        }

    }

    public class RequsetWorking extends IReqWorkingAidlInterface.Stub {
        public class WorkingData{
            public long updateTime = 0;
            public String needWorking = "non";
            public String workingType = "non";
            public String tagName = "non";
            public String issueTime = "non";

            @Override
            public String toString() {
                Date date1 = new Date(updateTime);
                String strUpdateTime = new SimpleDateFormat("HH:mm:ss").format(date1);
                return needWorking + "-" + workingType + "-" + tagName + "-" + issueTime + "-" + strUpdateTime;
            }
        }

        public class ReqError {
            public String tag = "non";
            public String info = "non";
            public long updateTime = 0;

            @Override
            public String toString() {
                Date date1 = new Date(updateTime);
                String strUpdateTime = new SimpleDateFormat("HH:mm:ss").format(date1);
                return "tag:" + tag + " info:" + info + " time:" + strUpdateTime;
            }
        }

        WorkingData mWorkingData = new WorkingData();
        ReqError mReqError = new ReqError();

        RemoteCallbackList<IRspWorkingAidlInterface> mListenerList = new RemoteCallbackList<>();
        Handler mHandler = null;

        public void setThreadHandler(Handler handler) {
            mHandler = handler;
        }

        public String request() {
            Log.v("RequsetWorking", "request");
            String result = "non";
            synchronized (mWorkingData) {
                result = mWorkingData.needWorking;
            }
            return result;
        }

        public String mixInfo() {
            String result = "non";
            synchronized (mWorkingData) {
                result = mWorkingData.toString();
            }
            return result;
        }

        public void tiggerReq() {
            if(mHandler != null) {
                Message msg = new Message();
                msg.what = ReqThread.MSG_REQ;
                mHandler.sendMessage(msg);
            }
        }

        public String getError() {
            synchronized (mReqError) {
                return mReqError.toString();
            }
        }

        public void setWorking(String needWorking) {
            this.setResult(needWorking, "type", "issueTime", "tagName", System.currentTimeMillis());
        }

        public void regsetRspCallback(IRspWorkingAidlInterface callback) {
            if(callback != null)
                mListenerList.register(callback);
        }

        public void unregsetRspCallback(IRspWorkingAidlInterface callback) {
            if(callback != null) {
                mListenerList.unregister(callback);
            }
        }

        public void setResult(String needWorking, String workingType, String issueTime, String tagName, long currTime) {
            synchronized (mWorkingData) {
                mWorkingData.needWorking = needWorking;
                mWorkingData.workingType = workingType;
                mWorkingData.issueTime = issueTime;
                mWorkingData.updateTime = currTime;
                mWorkingData.tagName = tagName;
            }

            int n = mListenerList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IRspWorkingAidlInterface listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onRspWorking(needWorking);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mListenerList.finishBroadcast();
        }

        public void setError(String strTag, String strInfo, long currTime) {
            synchronized (mReqError) {
                mReqError.tag = strTag;
                mReqError.info = strInfo;
                mReqError.updateTime = currTime;
            }

            int n = mListenerList.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IRspWorkingAidlInterface listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onRspWorking("error2");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mListenerList.finishBroadcast();
        }

        public boolean isAlive() {
            return true;
        }
    }

}

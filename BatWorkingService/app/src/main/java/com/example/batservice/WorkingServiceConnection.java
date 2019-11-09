package com.example.batservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.batservice.IReqWorkingAidlInterface;
import com.example.batservice.IRspWorkingAidlInterface;

public class WorkingServiceConnection implements ServiceConnection {
    class RspWorkingCallback extends IRspWorkingAidlInterface.Stub {
        public void onRspWorking(String needWorking) {
            Log.v("RspWorkingCallback", needWorking);
        }
    }

    IRspWorkingAidlInterface rspCallback = new RspWorkingCallback();
    public IReqWorkingAidlInterface reqInterface = null;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // 这里和本地服务不同，本地服务是强制类型转换，远程服务直接使用代理完成
        //iMusician = IMusician.Stub.asInterface(service);
        reqInterface = IReqWorkingAidlInterface.Stub.asInterface(service);
        try {
            reqInterface.regsetRspCallback(rspCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if(reqInterface != null) {
            try {
                reqInterface.unregsetRspCallback(rspCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String needWorking() {
        if(reqInterface != null) {
            try {
                return reqInterface.request();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }

    public String mixInfo() {
        if(reqInterface != null) {
            try {
                return reqInterface.mixInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }

    public void tiggerReq() {
        if(reqInterface != null) {
            try {
                reqInterface.tiggerReq();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public String getError() {
        if(reqInterface != null) {
            try {
                return reqInterface.getError();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }

    public void setWorking(String needWorking) {
        if(reqInterface != null) {
            try {
                reqInterface.setWorking(needWorking);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
